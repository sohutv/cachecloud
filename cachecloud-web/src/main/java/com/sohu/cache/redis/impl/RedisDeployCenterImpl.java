package com.sohu.cache.redis.impl;

import com.google.common.collect.Lists;
import com.sohu.cache.constant.AppDescEnum;
import com.sohu.cache.constant.ClusterOperateResult;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.protocol.RedisProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.RedisClusterNode;
import com.sohu.cache.redis.RedisConfigTemplateService;
import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.redis.enums.DirEnum;
import com.sohu.cache.redis.enums.RedisConfigEnum;
import com.sohu.cache.redis.util.AuthUtil;
import com.sohu.cache.redis.util.JedisUtil;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.ssh.SSHTemplate;
import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.IdempotentConfirmer;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.RedisOperateEnum;
import com.sohu.cache.web.enums.UseTypeEnum;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.ResourceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import redis.clients.jedis.*;
import redis.clients.jedis.args.ClusterFailoverOption;
import redis.clients.jedis.exceptions.JedisDataException;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by yijunzhang on 14-8-25.
 */
@Service("redisDeployCenter")
public class RedisDeployCenterImpl implements RedisDeployCenter {
    // 重试次数
    private static int RETRY_TIMES = 10;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private MachineDao machineDao;
    @Autowired
    @Lazy
    private MachineCenter machineCenter;
    @Autowired
    @Lazy
    private RedisCenter redisCenter;
    @Autowired
    private AppDao appDao;
    @Autowired
    private RedisConfigTemplateService redisConfigTemplateService;
    @Autowired
    private InstanceDeployCenter instanceDeployCenter;
    @Resource(name = "appService")
    private AppService appService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private SSHService sshService;

    @Override
    public boolean deployClusterInstance(long appId, List<RedisClusterNode> clusterNodes, int maxMemory) {
        if (!isExist(appId)) {
            return false;
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        String host = null;
        Integer port = null;
        Map<Jedis, Jedis> clusterMap = new LinkedHashMap<Jedis, Jedis>();
        for (RedisClusterNode node : clusterNodes) {
            String masterHost = node.getMasterHost();
            String slaveHost = node.getSlaveHost();
            Integer masterPort = machineCenter.getAvailablePort(masterHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
            if (masterPort == null) {
                logger.error("masterHost={} getAvailablePort is null", masterHost);
                return false;
            }

            if (host == null || port == null) {
                host = masterHost;
                port = masterPort;
            }
            boolean isMasterRun = runInstance(appDesc, masterHost, masterPort, maxMemory, true);
            if (!isMasterRun) {
                return false;
            }
            if (StringUtils.isNotBlank(slaveHost)) {
                Integer slavePort = machineCenter.getAvailablePort(slaveHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
                if (slavePort == null) {
                    logger.error("slaveHost={} getAvailablePort is null", slaveHost);
                    return false;
                }
                boolean isSlaveRun = runInstance(appDesc, slaveHost, slavePort, maxMemory, true);
                if (!isSlaveRun) {
                    return false;
                }
                clusterMap.put(redisCenter.getJedis(appId, masterHost, masterPort),
                        redisCenter.getJedis(appId, slaveHost, slavePort));
            } else {
                clusterMap.put(redisCenter.getJedis(appId, masterHost, masterPort), null);
            }
        }

        boolean isCluster;
        try {
            isCluster = startCluster(appId, clusterMap);
            if (!isCluster) {
                logger.error("startCluster create error!");
                return false;
            }
            for (Map.Entry<Jedis, Jedis> entry : clusterMap.entrySet()) {
                Jedis master = entry.getKey();
                Jedis slave = entry.getValue();
                //保存实例信息 & 触发收集
                saveInstance(appId, master.getClient().getHost(),
                        master.getClient().getPort(), maxMemory, ConstUtils.CACHE_TYPE_REDIS_CLUSTER, "");
                if (slave != null) {
                    saveInstance(appId, slave.getClient().getHost(), slave.getClient().getPort(),
                            maxMemory, ConstUtils.CACHE_TYPE_REDIS_CLUSTER, "");
                }
            }
        } finally {
            //关闭jedis连接
            for (Map.Entry<Jedis, Jedis> entry : clusterMap.entrySet()) {
                entry.getKey().close();
                if (entry.getValue() != null) {
                    entry.getValue().close();
                }
            }
        }

        return true;
    }

    private boolean clusterMeet(Jedis jedis, long appId, String host, int port) {
        boolean isSingleNode = redisCenter.isSingleClusterNode(appId, host, port);
        if (!isSingleNode) {
            logger.error("{}:{} isNotSingleNode", host, port);
            return false;
        } else {
            logger.warn("{}:{} isSingleNode", host, port);
        }

        String response = jedis.clusterMeet(host, port);
        boolean isMeet = response != null && response.equalsIgnoreCase("OK");
        if (!isMeet) {
            logger.error("{}:{} meet error", host, port);
            return false;
        }
        return true;
    }

    public boolean startCluster(final long appId, Map<Jedis, Jedis> clusterMap) {
        final Jedis jedis = new ArrayList<Jedis>(clusterMap.keySet()).get(0);
        //meet集群节点
        for (final Map.Entry<Jedis, Jedis> entry : clusterMap.entrySet()) {
            final Jedis master = entry.getKey();
            boolean isMeet = new IdempotentConfirmer() {

                @Override
                public boolean execute() {
                    boolean isMeet = clusterMeet(jedis, appId, master.getClient().getHost(),
                            master.getClient().getPort());
                    if (!isMeet) {
                        return false;
                    }
                    return true;
                }
            }.run();
            if (!isMeet) {
                return false;
            }
            final Jedis slave = entry.getValue();
            if (slave != null) {
                isMeet = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        boolean isMeet = clusterMeet(jedis, appId, slave.getClient().getHost(),
                                slave.getClient().getPort());
                        if (!isMeet) {
                            return false;
                        }
                        return true;
                    }
                }.run();
                if (!isMeet) {
                    return false;
                }
            }
        }
        int masterSize = clusterMap.size();
        int perSize = (int) Math.ceil(16384 * 1.0D / masterSize);
        int index = 0;
        int masterIndex = 0;
        final ArrayList<Integer> slots = new ArrayList<Integer>();
        List<Jedis> masters = new ArrayList<Jedis>(clusterMap.keySet());
        //分配slot
        for (int slot = 0; slot <= 16383; slot++) {
            slots.add(slot);
            if (index++ >= perSize || slot == 16383) {
                final int[] slotArr = new int[slots.size()];
                for (int i = 0; i < slotArr.length; i++) {
                    slotArr[i] = slots.get(i);
                }
                final Jedis masterJedis = masters.get(masterIndex++);
                boolean isSlot = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        String response = masterJedis.clusterAddSlots(slotArr);
                        boolean isSlot = response != null && response.equalsIgnoreCase("OK");
                        if (!isSlot) {
                            return false;
                        }
                        return true;
                    }
                }.run();
                if (!isSlot) {
                    logger.error("{}:{} set slots:{}", masterJedis.getClient().getHost(),
                            masterJedis.getClient().getPort(), slots);
                    return false;
                }
                slots.clear();
                index = 0;
            }
        }
        //设置从节点
        for (Map.Entry<Jedis, Jedis> entry : clusterMap.entrySet()) {
            final Jedis masterJedis = entry.getKey();
            final Jedis slaveJedis = entry.getValue();
            if (slaveJedis == null) {
                continue;
            }
            final String nodeId = getClusterNodeId(masterJedis);
            boolean isReplicate = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    try {
                        //等待广播节点
                        TimeUnit.SECONDS.sleep(2);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    String response = null;
                    try {
                        response = slaveJedis.clusterReplicate(nodeId);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    boolean isReplicate = response != null && response.equalsIgnoreCase("OK");
                    if (!isReplicate) {
                        try {
                            //等待广播节点
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        return false;
                    }
                    return true;
                }
            }.run();

            if (!isReplicate) {
                logger.error("{}:{} set replicate:{}", slaveJedis.getClient().getHost(),
                        slaveJedis.getClient().getPort(), isReplicate);
                return false;
            }
        }

        return true;
    }

    public boolean startClusterMaster(final long appId, Map<Jedis, Jedis> clusterMap) {
        final Jedis jedis = new ArrayList<Jedis>(clusterMap.keySet()).get(0);
        //meet集群节点
        for (final Map.Entry<Jedis, Jedis> entry : clusterMap.entrySet()) {
            final Jedis master = entry.getKey();
            boolean isMeet = new IdempotentConfirmer() {

                @Override
                public boolean execute() {
                    boolean isMeet = clusterMeet(jedis, appId, master.getClient().getHost(),
                            master.getClient().getPort());
                    if (!isMeet) {
                        return false;
                    }
                    return true;
                }
            }.run();
            if (!isMeet) {
                return false;
            }
        }
        int masterSize = clusterMap.size();
        int perSize = (int) Math.ceil(16384 * 1.0D / masterSize);
        int index = 0;
        int masterIndex = 0;
        final ArrayList<Integer> slots = new ArrayList<Integer>();
        List<Jedis> masters = new ArrayList<Jedis>(clusterMap.keySet());
        //分配slot
        for (int slot = 0; slot <= 16383; slot++) {
            slots.add(slot);
            if (index++ >= perSize || slot == 16383) {
                final int[] slotArr = new int[slots.size()];
                for (int i = 0; i < slotArr.length; i++) {
                    slotArr[i] = slots.get(i);
                }
                final Jedis masterJedis = masters.get(masterIndex++);
                boolean isSlot = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        String response = masterJedis.clusterAddSlots(slotArr);
                        boolean isSlot = response != null && response.equalsIgnoreCase("OK");
                        if (!isSlot) {
                            return false;
                        }
                        return true;
                    }
                }.run();
                if (!isSlot) {
                    logger.error("{}:{} set slots:{}", masterJedis.getClient().getHost(),
                            masterJedis.getClient().getPort(), slots);
                    return false;
                }
                slots.clear();
                index = 0;
            }
        }
        return true;
    }

    public boolean startClusterSlave(final long appId, Map<Jedis, Jedis> clusterMap) {
        final Jedis jedis = new ArrayList<Jedis>(clusterMap.keySet()).get(0);
        //meet集群节点
        for (final Map.Entry<Jedis, Jedis> entry : clusterMap.entrySet()) {
            final Jedis slave = entry.getValue();
            if (slave != null) {
                boolean isMeet = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        boolean isMeet = clusterMeet(jedis, appId, slave.getClient().getHost(),
                                slave.getClient().getPort());
                        if (!isMeet) {
                            return false;
                        }
                        return true;
                    }
                }.run();
                if (!isMeet) {
                    return false;
                }
            }
        }
        //设置从节点
        for (Map.Entry<Jedis, Jedis> entry : clusterMap.entrySet()) {
            final Jedis masterJedis = entry.getKey();
            final Jedis slaveJedis = entry.getValue();
            if (slaveJedis == null) {
                continue;
            }
            final String nodeId = getClusterNodeId(masterJedis);
            boolean isReplicate = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    try {
                        //等待广播节点
                        TimeUnit.SECONDS.sleep(2);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    String response = null;
                    try {
                        response = slaveJedis.clusterReplicate(nodeId);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    boolean isReplicate = response != null && response.equalsIgnoreCase("OK");
                    if (!isReplicate) {
                        try {
                            //等待广播节点
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        return false;
                    }
                    return true;
                }
            }.run();

            if (!isReplicate) {
                logger.error("{}:{} set replicate:{}", slaveJedis.getClient().getHost(),
                        slaveJedis.getClient().getPort(), isReplicate);
                return false;
            }
        }
        return true;
    }

    private String getClusterNodeId(Jedis jedis) {
        try {
            String infoOutput = jedis.clusterNodes();
            for (String infoLine : infoOutput.split("\n")) {
                if (infoLine.contains("myself")) {
                    return infoLine.split(" ")[0];
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean deploySentinelInstance(long appId, String masterHost, String slaveHost, int maxMemory,
                                          List<String> sentinelList) {
        if (!isExist(appId)) {
            return false;
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        //获取端口
        Integer masterPort = machineCenter.getAvailablePort(masterHost, ConstUtils.CACHE_REDIS_STANDALONE);
        if (masterPort == null) {
            logger.error("masterHost={} getAvailablePort is null", masterHost);
            return false;
        }
        Integer slavePort = machineCenter.getAvailablePort(slaveHost, ConstUtils.CACHE_REDIS_STANDALONE);
        if (slavePort == null) {
            logger.error("slaveHost={} getAvailablePort is null", slaveHost);
            return false;
        }
        //运行实例
        boolean isMasterRun = runInstance(appDesc, masterHost, masterPort, maxMemory, false);
        if (!isMasterRun) {
            return false;
        }
        boolean isSlaveRun = runInstance(appDesc, slaveHost, slavePort, maxMemory, false);
        if (!isSlaveRun) {
            return false;
        }
        //添加slaveof配置
        boolean isSlave = slaveOf(appDesc.getAppId(), masterHost, masterPort, slaveHost, slavePort);
        if (!isSlave) {
            return false;
        }

        //运行sentinel实例组
        boolean isRunSentinel = runSentinelGroup(appDesc, sentinelList, masterHost, masterPort, appId,
                appDesc.getAppPassword());
        if (!isRunSentinel) {
            return false;
        }

        //写入instanceInfo 信息
        saveInstance(appId, masterHost, masterPort, maxMemory,
                ConstUtils.CACHE_REDIS_STANDALONE, "");
        saveInstance(appId, slaveHost, slavePort, maxMemory, ConstUtils.CACHE_REDIS_STANDALONE, "");

        return true;
    }

    @Override
    public boolean deployStandaloneInstance(long appId, String host, int maxMemory) {
        if (!isExist(appId)) {
            return false;
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        //获取端口
        Integer port = machineCenter.getAvailablePort(host, ConstUtils.CACHE_REDIS_STANDALONE);
        if (port == null) {
            logger.error("masterHost={} getAvailablePort is null", host);
            return false;
        }

        //运行实例
        boolean isMasterRun = runInstance(appDesc, host, port, maxMemory, false);
        if (!isMasterRun) {
            return false;
        }

        //写入instanceInfo 信息
        saveInstance(appId, host, port, maxMemory, ConstUtils.CACHE_REDIS_STANDALONE,
                "");
        return true;
    }

    private InstanceInfo saveInstance(long appId, String host, int port, int maxMemory, int type,
                                      String cmd) {
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setAppId(appId);
        MachineInfo machineInfo = machineDao.getMachineInfoByIp(host);
        instanceInfo.setHostId(machineInfo.getId());
        instanceInfo.setConn(0);
        instanceInfo.setMem(maxMemory);
        instanceInfo.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
        instanceInfo.setPort(port);
        instanceInfo.setType(type);
        instanceInfo.setCmd(cmd);
        instanceInfo.setIp(host);
        instanceDao.saveInstance(instanceInfo);
        return instanceInfo;
    }

    private boolean runSentinelGroup(AppDesc appDesc, List<String> sentinelList, String masterHost, int masterPort,
                                     long appId, String password) {
        for (String sentinelHost : sentinelList) {
            boolean isRun = runSentinel(appDesc, sentinelHost, getMasterName(masterHost, masterPort), masterHost,
                    masterPort);
            if (!isRun) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean createRunNode(AppDesc appDesc, String host, Integer port, int maxMemory, boolean isCluster) {
        return runInstance(appDesc, host, port, maxMemory, isCluster);
    }

    @Override
    public String getRedisRunShell(boolean isCluster, String host, int port, String redisDir) {
        String runShell = machineCenter.isK8sMachine(host) == true
                ? RedisProtocol.getK8sRunShellByVersion(host, port, isCluster, redisDir)
                : RedisProtocol.getRunShellByVersion(port, isCluster, redisDir);
        return runShell;
    }

    @Override
    public String getSentinelRunShell(String host, int port, String redisDir) {
        String runShell = machineCenter.isK8sMachine(host) == true
                ? RedisProtocol.getK8sSentinelShellByVersion(host, port, redisDir)
                : RedisProtocol.getSentinelShellByVersion(port, redisDir);
        return runShell;
    }

    private boolean runInstance(AppDesc appDesc, String host, Integer port, int maxMemory, boolean isCluster) {
        return this.runInstanceWithDefaultConfig(appDesc, host, port, maxMemory, isCluster, null);
    }

    private boolean runInstanceWithDefaultConfig(AppDesc appDesc, String host, Integer port, int maxMemory, boolean isCluster, List<String> defautlConfigs) {
        long appId = appDesc.getAppId();
        String password = appDesc.getAppPassword();
        // 获取redis路径
        SystemResource redisResource = resourceService.getResourceById(appDesc.getVersionId());
        String redisDir = redisResource == null ? ConstUtils.REDIS_DEFAULT_DIR : ConstUtils.getRedisDir(redisResource.getName());
        // Redis资源校验&推包
        Boolean installStatus = redisConfigTemplateService.checkAndInstallRedisResource(host, redisResource);
        if (!installStatus) {
            throw new RuntimeException(String.format("machine: %s version :%s is installed %s", host, redisResource, installStatus));
        }
        // 生成配置
        List<String> configs = handleCommonConfig(host, port, maxMemory, appDesc.getMaxmemoryPolicy(), appDesc.getVersionId());
        if (isCluster) {
            configs.addAll(handleClusterConfig(port, appDesc.getVersionId()));
        }
        if (StringUtils.isNotBlank(password)) {
            //加两个选项
            configs.add(RedisConfigEnum.REQUIREPASS.getKey() + ConstUtils.SPACE + password);
            configs.add(RedisConfigEnum.MASTERAUTH.getKey() + ConstUtils.SPACE + password);
        }

        printConfig(configs);
        String fileName;
        String runShell;
        if (isCluster) {
            runShell = getRedisRunShell(true, host, port, redisDir);
            fileName = RedisProtocol.getConfig(port, true);
        } else {
            runShell = getRedisRunShell(false, host, port, redisDir);
            fileName = RedisProtocol.getConfig(port, false);
        }

        if(CollectionUtils.isNotEmpty(defautlConfigs)){
            configs.addAll(defautlConfigs);
        }

        String pathFile = machineCenter.createRemoteFile(host, fileName, configs);
        if (StringUtils.isBlank(pathFile)) {
            logger.error("createFile={} error", pathFile);
            return false;
        }
        if (isCluster) {
            //删除cluster节点配置
            String deleteNodeShell = String.format("rm -rf %s/nodes-%s.conf", machineCenter.getMachineRelativeDir(host, DirEnum.DATA_DIR.getValue()), port);
            String deleteNodeResult = machineCenter.executeShell(host, deleteNodeShell);
            if (!ConstUtils.INNER_ERROR.equals(deleteNodeResult)) {
                logger.warn("runDeleteNodeShell={} at host {}", deleteNodeShell, host);
            }
        }
        //启动实例
        logger.info("masterShell:host={};shell={}", host, runShell);
        boolean isMasterShell = machineCenter.startProcessAtPort(host, port, runShell);
        if (!isMasterShell) {
            logger.error("runShell={} error,{}:{}", runShell, host, port);
            return false;
        }
        //验证实例
        if (!redisCenter.isRun(appId, host, port)) {
            logger.error("host:{};port:{} not run", host, port);
            return false;
        } else {
            logger.warn("runInstance-fallback : redis-cli -h {} -p {} shutdown", host, port);
        }
        return true;
    }

    public boolean bornConfigAndRunNode(AppDesc appDesc, InstanceInfo instanceInfo, String host, Integer port, int maxMemory, boolean isCluster, boolean isInstallModule, String moduleCommand) {

        long appId = appDesc.getAppId();
        String password = appDesc.getAppPassword();
        // 获取redis路径
        SystemResource redisResource = resourceService.getResourceById(appDesc.getVersionId());
        String redisDir = redisResource == null ? ConstUtils.REDIS_DEFAULT_DIR : ConstUtils.getRedisDir(redisResource.getName());
        // 生成配置
        List<String> configs = handleCommonConfig(host, port, maxMemory, appDesc.getMaxmemoryPolicy(), appDesc.getVersionId());
        if (isCluster) {
            configs.addAll(handleClusterConfig(port, appDesc.getVersionId()));
        }
        if (StringUtils.isNotBlank(password)) {
            //加两个选项
            configs.add(RedisConfigEnum.REQUIREPASS.getKey() + ConstUtils.SPACE + password);
            configs.add(RedisConfigEnum.MASTERAUTH.getKey() + ConstUtils.SPACE + password);
        }
        printConfig(configs);
        String fileName;
        String runShell;
        if (isCluster) {
            runShell = getRedisRunShell(true, host, port, redisDir);
            fileName = RedisProtocol.getConfig(port, true);
        } else {
            runShell = getRedisRunShell(false, host, port, redisDir);
            fileName = RedisProtocol.getConfig(port, false);
        }

        // 删除redis配置
        String deleteConfShell = String.format("rm -rf %sredis-cluster-%s.conf", MachineProtocol.CONF_DIR, port);
        String deleteConfResult = machineCenter.executeShell(host, deleteConfShell);
        if (!ConstUtils.INNER_ERROR.equals(deleteConfResult)) {
            logger.warn("runDeleteConfShell={} at host {}", deleteConfResult, host);
        }
        // 写入新的配置文件
        String pathFile = machineCenter.createRemoteFile(host, fileName, configs);
        if (StringUtils.isBlank(pathFile)) {
            logger.error("createFile={} error", pathFile);
            return false;
        }
        // 是否装载模块
        if (isInstallModule) {
            // 刷新配置
            String cmd = String.format("echo \"%s\" >> %s", moduleCommand, pathFile);
            try {
                SSHTemplate.Result result = sshService.executeWithResult(host, cmd);
                logger.info("refresh config {}:{} load module:{} refresh config result:{}", host, port, cmd, result);
            } catch (SSHException e) {
                logger.error("refresh config error host:{},command :{} ,error:{}", host, cmd, e.getMessage(), e);
            }

        }
        //启动实例
        logger.info("masterShell:host={};shell={}", host, runShell);
        boolean isMasterShell = machineCenter.startProcessAtPort(host, port, runShell);
        if (!isMasterShell) {
            logger.error("runShell={} error,{}:{}", runShell, host, port);
            return false;
        }
        //验证实例
        if (!redisCenter.isRun(appId, host, port)) {
            logger.error("host:{};port:{} not run", host, port);
            return false;
        } else {
            logger.warn("runInstance-restart success : redis-cli -h {} -p {} ", host, port);
            instanceInfo.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
            instanceDao.update(instanceInfo);
        }
        return true;
    }

    private boolean runSentinel(AppDesc appDesc, String sentinelHost, String masterName, String masterHost,
                                Integer masterPort) {
        //应用信息
        long appId = appDesc.getAppId();
        String password = appDesc.getAppPassword();
        // 获取redis路径
//        RedisVersion redisVersion = redisConfigTemplateService.getRedisVersionById(appDesc.getVersionId());
//        String redisDir = redisVersion == null ? ConstUtils.REDIS_DEFAULT_DIR : redisVersion.getDir();
        SystemResource redisResource = resourceService.getResourceById(appDesc.getVersionId());
        String redisDir = redisResource == null ? ConstUtils.REDIS_DEFAULT_DIR : ConstUtils.getRedisDir(redisResource.getName());
        //启动sentinel实例
        Integer sentinelPort = machineCenter.getAvailablePort(sentinelHost, ConstUtils.CACHE_REDIS_SENTINEL);
        if (sentinelPort == null) {
            logger.error("host={} getAvailablePort is null", sentinelHost);
            return false;
        }
        List<String> masterSentinelConfigs = handleSentinelConfig(masterName, masterHost, masterPort, sentinelHost, sentinelPort, appDesc.getVersionId());
        if (StringUtils.isNotBlank(password)) {
            masterSentinelConfigs.add("sentinel " + RedisConfigEnum.AUTH_PASS.getKey() + ConstUtils.SPACE + masterName
                    + ConstUtils.SPACE + password);
        }

        printConfig(masterSentinelConfigs);
        String masterSentinelFileName = RedisProtocol.getConfig(sentinelPort, false);
        String sentinelPathFile = machineCenter
                .createRemoteFile(sentinelHost, masterSentinelFileName, masterSentinelConfigs);
        if (StringUtils.isBlank(sentinelPathFile)) {
            return false;
        }
        String sentinelShell = getSentinelRunShell(sentinelHost, sentinelPort, redisDir);
        logger.info("sentinelMasterShell:{}", sentinelShell);
        boolean isSentinelMasterShell = machineCenter.startProcessAtPort(sentinelHost, sentinelPort, sentinelShell);
        if (!isSentinelMasterShell) {
            logger.error("sentinelMasterShell={} error", sentinelShell);
            return false;
        }
        //验证实例
        if (!redisCenter.isRun(sentinelHost, sentinelPort)) {
            logger.error("host:{};port:{} not run", sentinelHost, sentinelPort);
            return false;
        } else {
            logger.warn("runSentinel-fallback : redis-cli -h {} -p {} shutdown", sentinelHost, sentinelPort);
        }
        //save sentinel
        /**
         *  getMasterName(masterHost, masterPort) 存在问题:(instance_info表cmd 集群名称不一致问题)
         *  1).如果已经存在一个sentinel集群,masterName:master-node1(主节点node1,从节点node2),
         *  2).当主从节点failover(主节点为node2),再添加新的sentinel节点：master-node2
         */
        String sentinelMasterName = StringUtils.isEmpty(masterName) ? getMasterName(masterHost, masterPort) : masterName;
        saveInstance(appId, sentinelHost, sentinelPort, 0, ConstUtils.CACHE_REDIS_SENTINEL, sentinelMasterName);
        return true;
    }

    /**
     * 获取redis 基础配置
     *
     * @param port
     * @param maxMemory
     * @return
     */
    public List<String> handleCommonConfig(String host, int port, int maxMemory, Integer maxMemoryPolicyType, int versionId) {
        List<String> configs = null;
        try {
            String maxMemoryPolicy = null;
            if(maxMemoryPolicyType != null){
                AppDescEnum.MaxmemoryPolicyType policyType = AppDescEnum.MaxmemoryPolicyType.getByType(maxMemoryPolicyType);
                if(policyType != null){
                    maxMemoryPolicy = policyType.getName();
                }
            }
            configs = redisConfigTemplateService.handleCommonConfig(host, port, maxMemory, maxMemoryPolicy, versionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (CollectionUtils.isEmpty(configs)) {
            configs = redisConfigTemplateService.handleCommonDefaultConfig(port, maxMemory);
        }
        return configs;
    }

    private List<String> handleSentinelConfig(String masterName, String host, int port, String sentinelHost, int sentinelPort, int versionId) {
        List<String> configs = null;
        try {
            configs = redisConfigTemplateService.handleSentinelConfig(masterName, host, port, sentinelHost, sentinelPort, versionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (CollectionUtils.isEmpty(configs)) {
            configs = redisConfigTemplateService.handleSentinelDefaultConfig(masterName, host, port, sentinelPort);
        }
        return configs;
    }

    private List<String> handleClusterConfig(int port, int versionId) {
        List<String> configs = null;
        try {
            configs = redisConfigTemplateService.handleClusterConfig(port, versionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (CollectionUtils.isEmpty(configs)) {
            configs = redisConfigTemplateService.handleClusterDefaultConfig(port);
        }
        return configs;
    }

    private String getMasterName(String host, int port) {
        String masterSentinelName = String.format("sentinel-%s-%s", host, port);
        return masterSentinelName;
    }

    private void printConfig(List<String> masterConfigs) {
        logger.info("==================redis-{}-config==================", masterConfigs);
        for (String line : masterConfigs) {
            logger.info(line);
        }
    }

    private boolean isExist(long appId) {
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        if (instanceInfos != null && instanceInfos.size() > 0) {
            logger.error("appId={} instances is exist , instanceInfos={}", appId, instanceInfos);
            return false;
        }
        return true;
    }

    @Override
    public boolean modifyAppConfig(long appId, String parameter, String value) {
        List<InstanceInfo> list = instanceDao.getInstListByAppId(appId);
        if (list == null || list.isEmpty()) {
            logger.error(String.format("appId=%s no instances", appId));
            return false;
        }
        for (InstanceInfo instance : list) {
            int type = instance.getType();
            if (!TypeUtil.isRedisType(type)) {
                logger.error("appId={};type={};is not redisType", appId, type);
                return false;
            }
            //忽略sentinel
            if (TypeUtil.isRedisSentinel(type)) {
                continue;
            }
            //忽略下线
            if (instance.isOffline()) {
                continue;
            }
            String host = instance.getIp();
            int port = instance.getPort();
            if (!modifyInstanceConfig(appId, host, port, parameter, value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean modifyInstanceConfig(final long appId, final String host, final int port, final String parameter,
                                        final String value) {
        final Jedis jedis = redisCenter.getJedis(appId, host, port, 5000, 5000);
        try {
            boolean isConfig = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    boolean isRun = redisCenter.isRun(appId, host, port);
                    if (!isRun) {
                        logger.warn("modifyInstanceConfig{}:{} is shutdown", host, port);
                        return true;
                    }
                    String result = jedis.configSet(parameter, value);
                    boolean isConfig = result != null && result.equalsIgnoreCase("OK");
                    if (!isConfig) {
                        logger.error(String.format("modifyConfigError:ip=%s,port=%s,result=%s", host, port, result));
                        return false;
                    }
                    return isConfig;
                }
            }.run();
            boolean isRewrite = redisCenter.configRewrite(appId, host, port);
            if (!isRewrite) {
                logger.error("configRewrite={}:{} failed", host, port);
            }
            return isConfig;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public boolean addSentinel(long appId, String sentinelHost) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        JedisSentinelPool jedisSentinelPool = redisCenter.getJedisSentinelPool(appDesc);
        if (jedisSentinelPool == null) {
            return false;
        }
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        String masterName = null;
        for (Iterator<InstanceInfo> i = instanceInfos.iterator(); i.hasNext(); ) {
            InstanceInfo instanceInfo = i.next();
            if (instanceInfo.getType() != ConstUtils.CACHE_REDIS_SENTINEL) {
                i.remove();
                continue;
            }
            if (masterName == null && StringUtils.isNotBlank(instanceInfo.getCmd())) {
                masterName = instanceInfo.getCmd();
            }
        }
        Jedis jedis = null;
        String masterHost = null;
        Integer masterPort = null;
        try {
            jedis = jedisSentinelPool.getResource();
            masterHost = jedis.getClient().getHost();
            masterPort = jedis.getClient().getPort();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
                jedisSentinelPool.destroy();
            }
        }
        boolean isRun = runSentinel(appDesc, sentinelHost, masterName, masterHost, masterPort);
        if (!isRun) {
            return false;
        }
        return true;
    }

    @Override
    public RedisOperateEnum addSlotsFailMaster(final long appId, int lossSlotsInstanceId, final String newMasterHost)
            throws Exception {
        // 1.参数、应用、实例信息确认
        Assert.isTrue(appId > 0);
        Assert.isTrue(lossSlotsInstanceId > 0);
        Assert.isTrue(StringUtils.isNotBlank(newMasterHost));
        AppDesc appDesc = appDao.getAppDescById(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisCluster(type)) {
            logger.error("{} is not redis cluster type", appDesc);
            return RedisOperateEnum.FAIL;
        }
        //获取失联slots的实例信息
        InstanceInfo lossSlotsInstanceInfo = instanceDao.getInstanceInfoById(lossSlotsInstanceId);
        Assert.isTrue(lossSlotsInstanceInfo != null);

        // 2.获取集群中一个健康的master作为clusterInfo Nodes的数据源
        List<InstanceInfo> allInstanceInfo = redisCenter.getAllHealthyInstanceInfo(appId);
        //InstanceInfo sourceMasterInstance = redisCenter.getHealthyInstanceInfo(appId);
        if (allInstanceInfo == null || allInstanceInfo.size() == 0) {
            logger.warn("appId {} get all instance is zero", appId);
            return RedisOperateEnum.FAIL;
        }
        //默认获取第一个master节点
        InstanceInfo sourceMasterInstance = allInstanceInfo.get(0);
        // 并未找到一个合适的实例可以
        if (sourceMasterInstance == null) {
            logger.warn("appId {} does not have right instance", appId);
            return RedisOperateEnum.FAIL;
        }

        // 3. 找到丢失的slots，如果没找到就说明集群正常，直接返回
        String healthyMasterHost = sourceMasterInstance.getIp();
        int healthyMasterPort = sourceMasterInstance.getPort();
        int healthyMasterMem = sourceMasterInstance.getMem();
        // 3.1 查看整个集群中是否有丢失的slots
        List<Integer> allLossSlots = redisCenter.getClusterLossSlots(appId, healthyMasterHost, healthyMasterPort);
        if (CollectionUtils.isEmpty(allLossSlots)) {
            logger.warn("appId {} all slots is regular and assigned", appId);
            return RedisOperateEnum.ALREADY_SUCCESS;
        }
        // 3.2 查看目标实例丢失slots
        final List<Integer> clusterLossSlots = redisCenter
                .getInstanceSlots(appId, healthyMasterHost, healthyMasterPort, lossSlotsInstanceInfo.getIp(),
                        lossSlotsInstanceInfo.getPort());
        // 4.开启新的节点
        // 4.1 从newMasterHost找到可用的端口newMasterPort
        final Integer newMasterPort = machineCenter
                .getAvailablePort(newMasterHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        if (newMasterPort == null) {
            logger.error("host={} getAvailablePort is null", newMasterHost);
            return RedisOperateEnum.FAIL;
        }
        // 4.2 按照sourceMasterInstance的内存启动
        boolean isRun = runInstance(appDesc, newMasterHost, newMasterPort, healthyMasterMem, true);
        if (!isRun) {
            logger.error("{}:{} is not run", newMasterHost, newMasterPort);
            return RedisOperateEnum.FAIL;
        }
        // 4.3 拷贝配置
        boolean isCopy = copyCommonConfig(appId, healthyMasterHost, healthyMasterPort, newMasterHost, newMasterPort);
        if (!isCopy) {
            logger.error("{}:{} copy config {}:{} is error", healthyMasterHost, healthyMasterPort, newMasterHost,
                    newMasterPort);
            return RedisOperateEnum.FAIL;
        }

        // 5. meet
        boolean isClusterMeet = false;
        Jedis sourceMasterJedis = null;
        try {
            sourceMasterJedis = redisCenter.getJedis(appId, healthyMasterHost, healthyMasterPort);
            isClusterMeet = clusterMeet(sourceMasterJedis, appId, newMasterHost, newMasterPort);
            if (!isClusterMeet) {
                logger.error("{}:{} cluster is failed", newMasterHost, newMasterPort);
                return RedisOperateEnum.FAIL;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (sourceMasterJedis != null) {
                sourceMasterJedis.close();
            }
        }
        if (!isClusterMeet) {
            logger.warn("{}:{} meet {}:{} is fail", healthyMasterHost, healthyMasterPort, newMasterHost, newMasterPort);
            return RedisOperateEnum.FAIL;
        }

        // 6. 分配slots
        //String addSlotsResult = "";
        Jedis newMasterJedis = null;
        //Jedis healthyMasterJedis = null;
        try {
            newMasterJedis = redisCenter.getJedis(appId, newMasterHost, newMasterPort, 5000, 5000);
            //获取新的补救节点的nodeid
            final String nodeId = getClusterNodeId(newMasterJedis);
            //healthyMasterJedis = redisCenter.getJedis(appId, healthyMasterHost, healthyMasterPort, 5000, 5000);
            // 新加节点也需要addsolts
            InstanceInfo addInstance = new InstanceInfo();
            addInstance.setIp(newMasterHost);
            addInstance.setPort(newMasterPort);
            allInstanceInfo.add(addInstance);

            for (InstanceInfo instance : allInstanceInfo) {
                final Jedis masterJedis = redisCenter.getJedis(appId, instance.getIp(), instance.getPort(), 5000, 5000);
                logger.warn("{}:{} set {}:{} slots start", instance.getIp(), instance.getPort(), newMasterHost,
                        newMasterPort);
                // 1. nodes meet 2. nodes set
                boolean setSlotStatus = true;
                try {
                    setSlotStatus = new IdempotentConfirmer() {
                        @Override
                        public boolean execute() {
                            String setSlotsResult = null;
                            try {
                                for (final Integer slot : clusterLossSlots) {
                                    setSlotsResult = masterJedis.clusterSetSlotNode(slot, nodeId);
                                    logger.warn("set slot {}, result is {}", slot, setSlotsResult);
                                }
                            } catch (JedisDataException exception) {
                                logger.warn(exception.getMessage());
                                // unkown jedis node
                                try {
                                    TimeUnit.SECONDS.sleep(2);
                                } catch (InterruptedException e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                            // result
                            boolean nodeSetStatus = setSlotsResult != null && setSlotsResult.equalsIgnoreCase("OK");
                            return nodeSetStatus;
                        }
                    }.run();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    //close jedis
                    if (masterJedis != null) {
                        masterJedis.close();
                    }
                }
                // set slots result
                if (setSlotStatus) {
                    logger.warn("{}:{} set {}:{} slots success", instance.getIp(), instance.getPort(), newMasterHost,
                            newMasterPort);
                } else {
                    logger.warn("{}:{} set {}:{} slots faily", instance.getIp(), instance.getPort(), newMasterHost,
                            newMasterPort);
                    return RedisOperateEnum.FAIL;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (newMasterJedis != null) {
                newMasterJedis.close();
            }
        }

        // 7.保存实例信息、并开启收集信息
        saveInstance(appId, newMasterHost, newMasterPort, healthyMasterMem, ConstUtils.CACHE_TYPE_REDIS_CLUSTER, "");

        // 休息一段时间，同步clusterNodes信息
        TimeUnit.SECONDS.sleep(2);

        // 8.最终打印出当前还没有补充的slots
        List<Integer> currentLossSlots = redisCenter.getClusterLossSlots(appId, newMasterHost, newMasterPort);
        logger.warn("appId {} failslots assigned unsuccessfully, lossslots is {}", appId, currentLossSlots);

        return RedisOperateEnum.OP_SUCCESS;
    }

    @Override
    public boolean addSlave(long appId, int instanceId, final String slaveHost) {
        Assert.isTrue(appId > 0);
        Assert.isTrue(instanceId > 0);
        Assert.isTrue(StringUtils.isNotBlank(slaveHost));
        AppDesc appDesc = appDao.getAppDescById(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisType(type)) {
            logger.error("{} is not redis type", appDesc);
            return false;
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        Assert.isTrue(instanceInfo != null);
        String masterHost = instanceInfo.getIp();
        int masterPort = instanceInfo.getPort();
        final Integer slavePort = machineCenter.getAvailablePort(slaveHost, ConstUtils.CACHE_REDIS_STANDALONE);
        if (slavePort == null) {
            logger.error("host={} getAvailablePort is null", slaveHost);
            return false;
        }

        //检查插件安装情况
        List<ModuleVersion> moduleList = appService.getAppToModuleList(appId);
        List<String> defaultConfigs = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(moduleList)) {
            redisCenter.checkAndDownloadModule(slaveHost, moduleList);
            defaultConfigs = redisCenter.getLoadModuleDefaultConfig(appId, moduleList);
        }

        boolean isRun;
        if (TypeUtil.isRedisCluster(type)) {
            isRun = runInstanceWithDefaultConfig(appDesc, slaveHost, slavePort, instanceInfo.getMem(), true, defaultConfigs);
        } else {
            isRun = runInstanceWithDefaultConfig(appDesc, slaveHost, slavePort, instanceInfo.getMem(), false, defaultConfigs);
        }

        if (!isRun) {
            logger.error("{}:{} is not run", slaveHost, slavePort);
            return false;
        }

        // 注意redis高低版本复制config的问题
        boolean isCopy = copyCommonConfig(appId, masterHost, masterPort, slaveHost, slavePort);
        if (!isCopy) {
            logger.error("{}:{} copy config {}:{} is error", masterHost, masterPort, slaveHost, slavePort);
            return false;
        }
        if (TypeUtil.isRedisCluster(type)) {
            final Jedis masterJedis = redisCenter
                    .getJedis(appId, masterHost, masterPort, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
            final Jedis slaveJedis = redisCenter
                    .getJedis(appId, slaveHost, slavePort, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
            try {

//                // 检查主节点是否有加载redis插件
//                redisCenter.checkAndLoadModule(appId, slaveHost, slavePort);

                boolean isClusterMeet = clusterMeet(masterJedis, appId, slaveHost, slavePort);
                if (!isClusterMeet) {
                    logger.error("{}:{} cluster is failed", slaveHost, slaveHost);
                    return isClusterMeet;
                }
                final String nodeId = redisCenter.getNodeId(appId, masterHost, masterPort);
                if (StringUtils.isBlank(nodeId)) {
                    logger.error("{}:{} getNodeId failed", masterHost, masterPort);
                    return false;
                }

                boolean isClusterReplicate = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        try {
                            //等待广播节点
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        String response = slaveJedis.clusterReplicate(nodeId);
                        logger.info("clusterReplicate-{}:{}={}", slaveHost, slavePort, response);
                        return response != null && response.equalsIgnoreCase("OK");
                    }
                }.run();
                if (!isClusterReplicate) {
                    logger.error("{}:{} clusterReplicate {} is failed ", slaveHost, slavePort, nodeId);
                    return false;
                }

                // 工具迁移添加slave节点时 master可能出现阻塞
                int times = 1; //最多重试10次
                boolean blockingFlag = true;//检测master节点是否阻塞  true:阻塞 false:不阻塞
                while (blockingFlag && times++ <= RETRY_TIMES) {
                    try {
                        String masterPong = masterJedis.ping();
                        String slavePong = slaveJedis.ping();
                        logger.info("master ping :{}", masterPong);
                        logger.info("slave ping :{}", slavePong);
                        blockingFlag = false;
                    } catch (Exception e) {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        logger.error(" waiting  master/slave blocking status ,waiting 2s .... exception:{}", e.getMessage());
                    }
                }
                //保存配置
                masterJedis.clusterSaveConfig();
                slaveJedis.clusterSaveConfig();
                redisCenter.configRewrite(appId, masterHost, masterPort);
                redisCenter.configRewrite(appId, slaveHost, slavePort);
            } finally {
                masterJedis.close();
                slaveJedis.close();
            }
        } else {
            boolean isSlave = slaveOf(appId, masterHost, masterPort, slaveHost, slavePort);
            if (!isSlave) {
                logger.error("{}:{} sync {}:{} is error", slaveHost, slavePort, masterHost, masterPort);
                return false;
            }
        }

        //写入instanceInfo 信息
        if (TypeUtil.isRedisCluster(type)) {
            saveInstance(appId, slaveHost, slavePort, instanceInfo.getMem(),
                    ConstUtils.CACHE_TYPE_REDIS_CLUSTER, "");
        } else {
            saveInstance(appId, slaveHost, slavePort, instanceInfo.getMem(),
                    ConstUtils.CACHE_REDIS_STANDALONE, "");
        }
        return true;
    }

    @Override
    public String genSlaveIp(long appId, int instanceId) throws Exception {
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        String ip = instanceInfo.getIp();
        MachineInfo machineInfo = machineCenter.getMachineInfoByIp(ip);
        //先从混合部署机器进行选择
        List<MachineMemStatInfo> machineMixCandiList = machineCenter.getAllValidMachineMem(Arrays.asList(ip), machineInfo.getRoom(), UseTypeEnum.Machine_mix.getValue());
        String slaveIp = this.getSlaveIp(machineMixCandiList, ip, machineInfo, instanceInfo);
        if(StringUtils.isEmpty(slaveIp)){
            //再从专用部署机器进行选择
            List<MachineMemStatInfo> machineSpecialCandiList = machineCenter.getAllValidMachineMem(Arrays.asList(ip), machineInfo.getRoom(), UseTypeEnum.Machine_special.getValue());
            slaveIp = this.getSlaveIp(machineSpecialCandiList, ip, machineInfo, instanceInfo);
        }
        if(StringUtils.isEmpty(slaveIp)){
            //再从测试部署机器进行选择
            List<MachineMemStatInfo> machineTestCandiList = machineCenter.getAllValidMachineMem(Arrays.asList(ip), machineInfo.getRoom(), UseTypeEnum.Machine_test.getValue());
            slaveIp = this.getSlaveIp(machineTestCandiList, ip, machineInfo, instanceInfo);
        }
        //若该应用下的机器没有满足条件的，选择其他机器
        return slaveIp;
    }

    private String getSlaveIp(List<MachineMemStatInfo> machineList, String ip, MachineInfo machineInfo, InstanceInfo instanceInfo){
        if (CollectionUtils.isNotEmpty(machineList)) {
            List<String> machineResList = getMachineCandi(machineList, ip, instanceInfo.getMem(), machineInfo.getRack());
            if (CollectionUtils.isNotEmpty(machineResList)) {
                int random = new Random().nextInt(machineResList.size());
                return machineResList.get(random);
            }
        }
        return "";
    }

    private boolean isSameRealMachine(String ip1, String ip2) {
        if (ip1.equals(ip2)) return true;
        MachineInfo machine1 = machineDao.getMachineInfoByIp(ip1);
        MachineInfo machine2 = machineDao.getMachineInfoByIp(ip2);
        String realIp1 = machine1.getVirtual() == 1 ? machine1.getRealIp() : machine1.getIp();
        String realIp2 = machine2.getVirtual() == 1 ? machine2.getRealIp() : machine2.getIp();
        return realIp1.equals(realIp2);
    }

    private List<String> getMachineCandi(List<MachineMemStatInfo> machineCandiList, String ip, int reqMem, String rack) {
        return machineCandiList.stream()
                .filter(machineCandi -> !isSameRealMachine(ip, machineCandi.getIp()))
                .filter(machineCandi -> StringUtils.isEmpty(rack) || StringUtils.isEmpty(machineCandi.getRack()) ||
                        !rack.equals(machineCandi.getRack()))
                .filter(machineCandi -> (machineCandi.getMem() * 1024 - machineCandi.getUsedMem() / 1024 / 1024) > reqMem)
                .map(MachineMemStatInfo::getIp)
                .collect(Collectors.toList());
    }

    @Override
    public boolean sentinelFailover(final long appId) throws Exception {
        Assert.isTrue(appId > 0);
        AppDesc appDesc = appDao.getAppDescById(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisSentinel(type)) {
            logger.warn("app={} is not sentinel", appDesc);
            return false;
        }
        final List<InstanceInfo> instanceList = instanceDao.getEffectiveInstListByAppId(appId);
        if (instanceList == null || instanceList.isEmpty()) {
            logger.warn("app={} instances is empty", appId);
            return false;
        }
        for (InstanceInfo instanceInfo : instanceList) {
            int instanceType = instanceInfo.getType();
            if (TypeUtil.isRedisSentinel(instanceType)) {
                final String host = instanceInfo.getIp();
                final int port = instanceInfo.getPort();
                final String masterName = instanceInfo.getCmd();
                if (StringUtils.isBlank(masterName)) {
                    logger.warn("{} cmd is null", instanceInfo);
                    continue;
                }
                boolean isRun = redisCenter.isRun(host, port);
                if (!isRun) {
                    logger.warn("{}:{} is not run", host, port);
                    continue;
                }
                boolean isSentinelFailOver = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        Jedis jedis = redisCenter.getJedis(host, port);
                        try {
                            String response = jedis.sentinelFailover(masterName);
                            return response != null && response.equalsIgnoreCase("OK");
                        } finally {
                            jedis.close();
                        }
                    }
                }.run();
                if (!isSentinelFailOver) {
                    logger.warn("{}:{} sentienl isSentinelFailOver error", host, port);
                    return false;
                } else {
                    logger.warn("SentinelFailOver done! ");
                    break;
                }
            }
        }
        return true;
    }

    public boolean sentinelReset(long app_id) throws Exception {

        AppDesc appDesc = appService.getByAppId(app_id);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisSentinel(type)) {
            logger.warn("app={} is not sentinel", appDesc);
            return false;
        }
        final List<InstanceInfo> instanceList = instanceDao.getInstancesByType(app_id, ConstUtils.CACHE_REDIS_SENTINEL);
        if (instanceList == null || instanceList.isEmpty()) {
            logger.warn("app={} instances is empty", app_id);
            return false;
        }
        for (InstanceInfo instanceInfo : instanceList) {
            int instanceType = instanceInfo.getType();
            if (TypeUtil.isRedisSentinel(instanceType)) {
                final String host = instanceInfo.getIp();
                final int port = instanceInfo.getPort();
                final String masterName = instanceInfo.getCmd();
                if (StringUtils.isBlank(masterName)) {
                    logger.warn("{} sentinel masterName is null", instanceInfo);
                    continue;
                }
                boolean isRun = redisCenter.isRun(host, port);
                if (!isRun) {
                    logger.warn("{}:{} is not run", host, port);
                    continue;
                }
                boolean isSentinelReset = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        Jedis jedis = redisCenter.getJedis(host, port);
                        try {
                            Long response = jedis.sentinelReset("*");
                            if (response == 1) {
                                return true;
                            }
                        } finally {
                            jedis.close();
                        }
                        return false;
                    }
                }.run();
                if (!isSentinelReset) {
                    logger.warn("{}:{} sentinel isSentinelReset error", host, port);
                    return false;
                } else {
                    logger.warn("{}:{} SentinelReset done! ", host, port);
                }
            }
        }

        return true;
    }

    public boolean clusterFailover(long appId, HostAndPort hostAndPort, String failoverParam) throws Exception {
        InstanceInfo inst = instanceDao.getInstByIpAndPort(hostAndPort.getHost(), hostAndPort.getPort());
        if (inst != null && inst.getId() > 0) {
            return clusterFailover(appId, inst.getId(), failoverParam);
        } else {
            logger.warn("appid:{} clusterFailover error : {} get instanceinfo is empty,inst:{} ", appId, hostAndPort, inst);
            return false;
        }
    }

    @Override
    public boolean clusterFailover(final long appId, int slaveInstanceId, final String failoverParam) throws Exception {
        Assert.isTrue(appId > 0);
        Assert.isTrue(slaveInstanceId > 0);
        AppDesc appDesc = appDao.getAppDescById(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisCluster(type)) {
            logger.error("{} is not redis cluster type", appDesc);
            return false;
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(slaveInstanceId);
        Assert.isTrue(instanceInfo != null);
        String slaveHost = instanceInfo.getIp();
        int slavePort = instanceInfo.getPort();
        final Jedis slaveJedis = redisCenter.getJedis(appId, slaveHost, slavePort);
        boolean isClusterFailOver = new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String response = null;
                if (StringUtils.isBlank(failoverParam)) {
                    response = slaveJedis.clusterFailover();
                } else if ("force".equals(failoverParam)) {
                    response = slaveJedis.clusterFailover(ClusterFailoverOption.FORCE);
                } else if ("takeover".equals(failoverParam)) {
                    response = slaveJedis.clusterFailover(ClusterFailoverOption.TAKEOVER);
                } else {
                    logger.error("appId {} failoverParam {} is wrong", appId, failoverParam);
                }
                return response != null && response.equalsIgnoreCase("OK");
            }
        }.run();
        if (!isClusterFailOver) {
            logger.error("{}:{} clusterFailover {} failed", slaveHost, slavePort, failoverParam);
            return false;
        } else {
            logger.warn("{}:{} clusterFailover {} Done! ", slaveHost, slavePort, failoverParam);
        }
        return true;
    }

    @Override
    public ClusterOperateResult delNode(final Long appId, int delNodeInstanceId) {
        final InstanceInfo forgetInstanceInfo = instanceDao.getInstanceInfoById(delNodeInstanceId);
        final String forgetNodeId = redisCenter.getNodeId(appId, forgetInstanceInfo.getIp(),
                forgetInstanceInfo.getPort());
        if (StringUtils.isBlank(forgetNodeId)) {
            logger.warn("{} nodeId is null", forgetInstanceInfo.getHostPort());
            return ClusterOperateResult.fail(String.format("%s nodeId is null", forgetInstanceInfo.getHostPort()));
        }
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        for (InstanceInfo instanceInfo : instanceInfos) {
            if (instanceInfo == null) {
                continue;
            }
            if (instanceInfo.isOffline()) {
                continue;
            }
            // 过滤当前节点
            if (forgetInstanceInfo.getHostPort().equals(instanceInfo.getHostPort())) {
                continue;
            }
            final String instanceHost = instanceInfo.getIp();
            final int instancePort = instanceInfo.getPort();
            boolean isForget = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    String response = null;
                    Jedis jedis = null;
                    try {
                        jedis = redisCenter.getJedis(appId, instanceHost, instancePort);
                        logger.warn("{}:{} is forgetting {}", instanceHost, instancePort, forgetNodeId);
                        response = jedis.clusterForget(forgetNodeId);
                        boolean success = response != null && response.equalsIgnoreCase("OK");
                        logger.warn("{}:{} is forgetting {} result is {}", instanceHost, instancePort, forgetNodeId,
                                success);
                        return success;
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    } finally {
                        if (jedis != null) {
                            jedis.close();
                        }
                    }
                    return response != null && response.equalsIgnoreCase("OK");
                }
            }.run();
            if (!isForget) {
                logger.warn("{}:{} forget {} failed", instanceHost, instancePort, forgetNodeId);
                return ClusterOperateResult
                        .fail(String.format("%s:%s forget %s failed", instanceHost, instancePort, forgetNodeId));
            }
        }

        // shutdown
        boolean isShutdown = instanceDeployCenter.shutdownExistInstance(appId, delNodeInstanceId);
        if (!isShutdown) {
            logger.warn("{} shutdown failed", forgetInstanceInfo.getHostPort());
            return ClusterOperateResult.fail(String.format("%s shutdown failed", forgetInstanceInfo.getHostPort()));
        }

        return ClusterOperateResult.success();
    }

    /**
     * 1. 被forget的节点必须在线(这个条件有待验证)
     * 2. 被forget的节点不能有从节点
     * 3. 被forget的节点不能有slots
     */
    @Override
    public ClusterOperateResult checkClusterForget(Long appId, int forgetInstanceId) {
        // 0.各种验证
        Assert.isTrue(appId > 0);
        Assert.isTrue(forgetInstanceId > 0);
        AppDesc appDesc = appDao.getAppDescById(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisCluster(type)) {
            logger.error("{} is not redis cluster type", appDesc);
            return ClusterOperateResult.fail(String.format("instanceId: %s must be cluster type", forgetInstanceId));
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(forgetInstanceId);
        Assert.isTrue(instanceInfo != null);
        String forgetHost = instanceInfo.getIp();
        int forgetPort = instanceInfo.getPort();
        // 1.是否在线
        boolean isRun = redisCenter.isRun(appId, forgetHost, forgetPort);
        if (!isRun) {
            logger.warn("{}:{} is not run", forgetHost, forgetPort);
            return ClusterOperateResult.fail(String.format("被forget的节点(%s:%s)必须在线", forgetHost, forgetPort));
        }
        // 2.被forget的节点不能有从节点
        BooleanEnum hasSlaves = redisCenter.hasSlaves(appId, forgetHost, forgetPort);
//        if (hasSlaves == null || hasSlaves) {
//            logger.warn("{}:{} has slave", forgetHost, forgetPort);
//            return ClusterOperateResult.fail(String.format("被forget的节点(%s:%s)不能有从节点", forgetHost, forgetPort));
//        }
        if (hasSlaves == BooleanEnum.OTHER || hasSlaves == BooleanEnum.TRUE) {
            logger.warn("{}:{} has slave", forgetHost, forgetPort);
            return ClusterOperateResult.fail(String.format("被forget的节点(%s:%s)不能有从节点", forgetHost, forgetPort));
        }

        // 3.被forget的节点不能有slots
        Map<String, InstanceSlotModel> clusterSlotsMap = redisCenter.getClusterSlotsMap(appId);
        InstanceSlotModel instanceSlotModel = clusterSlotsMap.get(instanceInfo.getHostPort());
        if (instanceSlotModel != null && instanceSlotModel.getSlotList() != null
                && instanceSlotModel.getSlotList().size() > 0) {
            logger.warn("{}:{} has slots", forgetHost, forgetPort);
            return ClusterOperateResult.fail(String.format("被forget的节点(%s:%s)不能持有slot", forgetHost, forgetPort));
        }

        return ClusterOperateResult.success();
    }

    /**
     * 拷贝redis配置
     *
     * @param sourceHost
     * @param sourcePort
     * @param targetHost
     * @param targetPort
     * @return
     */
    private boolean copyCommonConfig(long appId, String sourceHost, int sourcePort, String targetHost, int targetPort) {
        String[] compareConfigs = new String[]{"maxmemory-policy", "maxmemory", "cluster-node-timeout",
                "cluster-require-full-coverage", "repl-backlog-size", "appendonly", "hash-max-ziplist-entries",
                "hash-max-ziplist-value", "list-max-ziplist-entries", "list-max-ziplist-value",
                "set-max-intset-entries",
                "zset-max-ziplist-entries", "zset-max-ziplist-value", "timeout", "tcp-keepalive"};
        try {
            for (String config : compareConfigs) {
                String sourceValue = getConfigValue(appId, sourceHost, sourcePort, config);
                if (StringUtils.isBlank(sourceValue)) {
                    continue;
                }
                String targetValue = getConfigValue(appId, targetHost, targetPort, config);
                /**
                 * todo chenshi
                 * 上面参数配置是按照redis3.0版本配置，高版本redis 3.2的参数配置有变化(需要做映射或默认配置)
                 * 3.0.7 : list-max-ziplist-entries   list-max-ziplist-value
                 * 3.2.10: 无上面两个参数配置list-max-ziplist-size list-compress-depth
                 */
                if (StringUtils.isNotBlank(targetValue)) {
                    if (!targetValue.equals(sourceValue)) {
                        this.modifyInstanceConfig(appId, targetHost, targetPort, config, sourceValue);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    private String getConfigValue(long appId, String host, int port, String key) {
        Jedis jedis = redisCenter
                .getJedis(appId, host, port, Protocol.DEFAULT_TIMEOUT * 3, Protocol.DEFAULT_TIMEOUT * 3);
        try {
            List<String> values = jedis.configGet(key);
            if (values == null || values.size() < 1) {
                return null;
            }
            return values.get(1);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            jedis.close();
        }
    }

    @Override
    public boolean fixPassword(Long appId, String newPkey) {
        if (appId == null) {
            logger.warn("appId is null");
            return false;
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            logger.error("appId = {} not exist", appId);
            return false;
        }
        if (StringUtils.isBlank(newPkey)) {
            logger.warn("newPkey is null, fix empty password");
        }
        String newPasswordMD5 = "";
        if (StringUtils.isNotBlank(newPkey)) {
            newPasswordMD5 = AuthUtil.getAppIdMD5(newPkey);
        }

        String oldPasswordMD5 = appDesc.getAppPassword();

        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);

        boolean isSuccess = batchFixPassword(instanceInfos, oldPasswordMD5, newPasswordMD5);
        if (isSuccess) {
            appDesc.setPkey(newPkey);
            appDao.update(appDesc);
        }

        return isSuccess;
    }

    @Override
    public boolean fixPassword(Long appId, String password, Boolean customPwdFlag, boolean initRedisFlag) {
        if (appId == null) {
            logger.warn("appId is null");
            return false;
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            logger.error("appId = {} not exist", appId);
            return false;
        }
        if(customPwdFlag == null){
            if(initRedisFlag){
                customPwdFlag = appDesc.isSetCustomPassword();
            }else{
                customPwdFlag = false;
            }
        }

        String newPassword = null;
        String newPkey = null;
        if(customPwdFlag){
            if(StringUtils.isBlank(password)){
                if(initRedisFlag){
                    newPassword = appDesc.getCustomPassword();
                }else{
                    newPassword = password;
                }
            }else{
                newPassword = password;
            }
        }else{
            if(StringUtils.isBlank(password)){
                if(initRedisFlag){
                    newPkey = String.valueOf(appId);
                    newPassword = AuthUtil.getAppIdMD5(newPkey);
                }else{
                    newPkey = null;
                    newPassword = null;
                }
            }else{
                newPkey = password;
                newPassword = AuthUtil.getAppIdMD5(password);
            }
        }
        if(newPassword == null){
            newPassword = "";
        }

        String oldPassword = appDesc.getAppPassword();
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        boolean isSuccess = batchFixPassword(instanceInfos, oldPassword, newPassword);
        if(!customPwdFlag){
            if (isSuccess) {
                appDesc.setPkey(newPkey);
                appDesc.setCustomPassword(null);
                appDao.updateWithCustomPwd(appDesc);
            }
        }else{
            if (isSuccess) {
                appDesc.setCustomPassword(newPassword);
                appDao.updateWithCustomPwd(appDesc);
            }
        }
        return isSuccess;
    }

    @Override
    public boolean checkAuths(Long appId) {
        if (appId == null) {
            logger.warn("appId is null");
            return false;
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            logger.error("appId = {} not exist", appId);
            return false;
        }

        String password = appDesc.getAppPassword();
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        List<Jedis> nodeList = Lists.newArrayList();
        for (InstanceInfo instanceInfo : instanceInfos) {
            String host = instanceInfo.getIp();
            try {
                int port = instanceInfo.getPort();
                int type = instanceInfo.getType();
                if (instanceInfo.isOffline()) {
                    logger.info("instanceInfo {}:{} is offline", host, port);
                    continue;
                }
                if (TypeUtil.isRedisCluster(type) || TypeUtil.isRedisStandalone(type)) {
                    Jedis jedis = redisCenter.getJedis(host, port, password);
                    nodeList.add(jedis);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return checkAuthNodes(nodeList, password);
    }

    private boolean batchFixPassword(List<InstanceInfo> instanceInfos, String oldPasswordMD5, String passwordMD5) {
        if (CollectionUtils.isEmpty(instanceInfos)) {
            return false;
        }
        //修改密码之前，提取所有存活节点。
        List<Jedis> nodeList = Lists.newArrayList();
        List<Jedis> rollbackNodeList = Lists.newArrayList();
        String masterName = null;
        List<Jedis> sentinelList = Lists.newArrayList();
        List<Jedis> rollbackSentinelList = Lists.newArrayList();

        for (InstanceInfo instanceInfo : instanceInfos) {
            String host = instanceInfo.getIp();
            try {
                int port = instanceInfo.getPort();
                int type = instanceInfo.getType();
                if (instanceInfo.isOffline()) {
                    logger.info("instanceInfo {}:{} is offline", host, port);
                    continue;
                }
                if (TypeUtil.isRedisCluster(type) || TypeUtil.isRedisStandalone(type)) {
                    Jedis jedis = redisCenter.getJedis(host, port, oldPasswordMD5);
                    nodeList.add(jedis);
                } else if (TypeUtil.isRedisSentinel(type)) {
                    if (StringUtils.isNotBlank(instanceInfo.getCmd())) {
                        masterName = instanceInfo.getCmd();
                    }
                    Jedis jedis = redisCenter.getJedis(host, port);
                    sentinelList.add(jedis);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        //打印需要配置密码的节点信息
        logger.warn("collect nodes done,list:");
        for (Jedis jedis : nodeList) {
            logger.warn("fix-password-node:" + JedisUtil.getHostPort(jedis) + " isRun:" + redisCenter.isRun(jedis.getClient().getHost(), jedis.getClient().getPort()));

        }
        for (Jedis jedis : sentinelList) {
            logger.warn("fix-sentinel-password-node:" + JedisUtil.getHostPort(jedis) + " isRun:" + redisCenter.isRun(jedis.getClient().getHost(), jedis.getClient().getPort()));
        }
        try {
            for (Jedis jedis : nodeList) {
                boolean isFix = fixNodePassword(jedis, passwordMD5);
                if (isFix) {
                    //加入待回滚列表
                    rollbackNodeList.add(jedis);
                } else {
                    //设置node密码错误回滚node
                    rollbackNodes(rollbackNodeList, oldPasswordMD5);
                    return false;
                }
            }

            if (StringUtils.isNotBlank(masterName)) {
                for (Jedis jedis : sentinelList) {
                    boolean isFix = fixSentinelPassword(jedis, masterName, passwordMD5);
                    if (isFix) {
                        //加入待回滚列表
                        rollbackSentinelList.add(jedis);
                    } else {
                        //设置sentinel密码错误回滚node+sentinel
                        rollbackNodes(rollbackNodeList, oldPasswordMD5);
                        rollbackSentinels(rollbackSentinelList, masterName, oldPasswordMD5);
                        return false;
                    }
                }
            }
            //检测所有节点密码发是否生效
            boolean allAuth = checkAuthNodes(nodeList, passwordMD5);
            if (!allAuth) {
                logger.warn("base-auth-error: batch-rollback={}", passwordMD5);
                //设置sentinel密码错误回滚node+sentinel
                rollbackNodes(rollbackNodeList, oldPasswordMD5);
                rollbackSentinels(rollbackSentinelList, masterName, oldPasswordMD5);
            }
            return allAuth;
        } finally {
            close(nodeList);
            close(sentinelList);
        }
    }

    private boolean fixSentinelPassword(Jedis jedis, String masterName, String passwordMD5) {
        String hostPort = JedisUtil.getHostPort(jedis);
        /**
         * sentinel set {masterName} auth-pass {passwordMD5}
         * sentinel flushconfig
         */
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("auth-pass", passwordMD5);
            String setResult = jedis.sentinelSet(masterName, params);
            if (setResult.equals("OK")) {
                logger.warn("config-pass sentinel success: sentinel={} master={} , auth-pass={}",
                        hostPort, masterName, passwordMD5);
                String flushResult = JedisUtil.sentinelFlushConfig(jedis);
                if (flushResult.equals("OK")) {
                    logger.warn("config rewrite success,sentinel={}", hostPort);
                } else {
                    logger.error("config rewrite success sentinel={}", hostPort);
                    return false;
                }
            } else {
                logger.error("sentinel-config-error:sentinel={} result={}",
                        JedisUtil.getHostPort(jedis), setResult);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    private void rollbackNodes(List<Jedis> rollbackNodeList, String oldPasswordMD5) {
        //设置密码错误回滚
        // rollback-nodes
        for (Jedis rollbackJedis : rollbackNodeList) {
            boolean rollback = fixNodePassword(rollbackJedis, oldPasswordMD5);
            logger.warn("node-rollback: node={} rollback={}",
                    JedisUtil.getHostPort(rollbackJedis), rollback);
        }
    }

    private void rollbackSentinels(List<Jedis> rollbackSentinelList, String masterName, String oldPasswordMD5) {
        // rollback-sentinels
        for (Jedis rollbackJedis : rollbackSentinelList) {
            boolean rollback = fixSentinelPassword(rollbackJedis, masterName, oldPasswordMD5);
            logger.warn("sentinel-rollback: node={} rollback={}",
                    JedisUtil.getHostPort(rollbackJedis), rollback);
        }
    }

    private boolean fixNodePassword(Jedis jedis, String passwordMD5) {
        String hostPort = JedisUtil.getHostPort(jedis);
        try {
            List<String> results = Lists.newArrayList();

            results.add(jedis.configSet("requirepass", passwordMD5));
            //密码设置后,无密码连接需要重新认证
            if (StringUtils.isNotBlank(passwordMD5)) {
                jedis.auth(passwordMD5);
            }
            results.add(jedis.configSet("masterauth", passwordMD5));
            if (results.get(0).equals("OK") && results.get(1).equals("OK")) {
                logger.warn("config-pass success: node={} results={}", hostPort, results);
                String rewrite = jedis.configRewrite();
                if (rewrite.equals("OK")) {
                    logger.warn("node-rewrite success: node={} result={}", hostPort, rewrite);
                } else {
                    logger.error("config rewrite error node={}", hostPort);
                }
            } else {
                logger.error("config-pass error: node={} results={}", hostPort, results);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    // 判断密码是否匹配
    private boolean checkAuthNodes(List<Jedis> nodes, String passwordMD5) {
        for (Jedis jedis : nodes) {
            try {
                String auth = jedis.auth(passwordMD5);
                if (!auth.equals("OK")) {
                    return false;
                }
            } catch (JedisDataException e) {
                //忽略无密码设置异常
                if (e.getMessage().contains("no password is set") || e.getMessage().contains("without any password configured for the default user")) {
                    logger.info("ignore ERR Client sent AUTH, but no password is set");
                } else {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (Exception e) {
                logger.error("node-auth-failed: node={} password={} error={}", JedisUtil.getHostPort(jedis),
                        passwordMD5, e.getMessage());
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean slaveOf(final long appId, final String masterHost, final int masterPort, final String slaveHost,
                           final int slavePort) {
        final Jedis slave = redisCenter.getJedis(appId, slaveHost, slavePort, Protocol.DEFAULT_TIMEOUT * 3, Protocol.DEFAULT_TIMEOUT * 3);
        try {
//            // 检查主节点是否有加载redis插件
//            redisCenter.checkAndLoadModule(appId, slaveHost, slavePort);

            boolean isSlave = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    String result = slave.slaveof(masterHost, masterPort);
                    //也有可能是OK Already connected to specified master
                    return result != null && result.startsWith("OK");
                }
            }.run();
            if (!isSlave) {
                logger.error(String.format("modifyAppConfig:ip=%s,port=%s failed", slaveHost, slavePort));
                return false;
            }
            redisCenter.configRewrite(appId, slaveHost, slavePort);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (slave != null)
                slave.close();
        }
        return true;
    }

    private void close(List<Jedis> list) {
        for (Jedis jedis : list) {
            if (jedis != null)
                jedis.close();
        }
    }
}