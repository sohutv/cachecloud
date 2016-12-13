package com.sohu.cache.redis.impl;

import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.protocol.RedisProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.RedisClusterNode;
import com.sohu.cache.redis.RedisConfigTemplateService;
import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.IdempotentConfirmer;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.RedisOperateEnum;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by yijunzhang on 14-8-25.
 */
public class RedisDeployCenterImpl implements RedisDeployCenter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private InstanceDao instanceDao;

    private MachineDao machineDao;

    private MachineCenter machineCenter;

    private RedisCenter redisCenter;

    private AppDao appDao;
    
    private RedisConfigTemplateService redisConfigTemplateService;

    @Override
    public boolean deployClusterInstance(long appId, List<RedisClusterNode> clusterNodes, int maxMemory) {
        if (!isExist(appId)) {
            return false;
        }
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
            boolean isMasterRun = runInstance(masterHost, masterPort, maxMemory, true);
            if (!isMasterRun) {
                return false;
            }
            if (StringUtils.isNotBlank(slaveHost)) {
                Integer slavePort = machineCenter.getAvailablePort(slaveHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
                if (slavePort == null) {
                    logger.error("slavePort={} getAvailablePort is null", slavePort);
                    return false;
                }
                boolean isSlaveRun = runInstance(slaveHost, slavePort, maxMemory, true);
                if (!isSlaveRun) {
                    return false;
                }
                clusterMap.put(new Jedis(masterHost, masterPort), new Jedis(slaveHost, slavePort));
            } else {
                clusterMap.put(new Jedis(masterHost, masterPort), null);
            }
        }

        boolean isCluster;
        try {
            isCluster = startCluster(clusterMap);
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
                redisCenter.deployRedisCollection(appId, master.getClient().getHost(), master.getClient().getPort());
                if (slave != null) {
                    saveInstance(appId, slave.getClient().getHost(), slave.getClient().getPort(),
                            maxMemory, ConstUtils.CACHE_TYPE_REDIS_CLUSTER, "");
                    redisCenter.deployRedisCollection(appId, slave.getClient().getHost(), slave.getClient().getPort());
                }
            }
        } finally {
            //关闭jedis连接
            for (Jedis master : clusterMap.keySet()) {
                master.close();
                if (clusterMap.get(master) != null) {
                    clusterMap.get(master).close();
                }
            }
        }

        return true;
    }

    private boolean clusterMeet(Jedis jedis, String host, int port) {
        boolean isSingleNode = redisCenter.isSingleClusterNode(host, port);
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

    private boolean startCluster(Map<Jedis, Jedis> clusterMap) {
        final Jedis jedis = new ArrayList<Jedis>(clusterMap.keySet()).get(0);
        //meet集群节点
        for (final Jedis master : clusterMap.keySet()) {
            boolean isMeet = new IdempotentConfirmer() {

                @Override
                public boolean execute() {
                    boolean isMeet = clusterMeet(jedis, master.getClient().getHost(), master.getClient().getPort());
                    if (!isMeet) {
                        return false;
                    }
                    return true;
                }
            }.run();
            if (!isMeet) {
                return false;
            }
            final Jedis slave = clusterMap.get(master);
            if (slave != null) {
                isMeet = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        boolean isMeet = clusterMeet(jedis, slave.getClient().getHost(), slave.getClient().getPort());
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
        int perSize = (int) Math.ceil(16384 / masterSize);
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
        for (Jedis masterJedis : clusterMap.keySet()) {
            final Jedis slaveJedis = clusterMap.get(masterJedis);
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
                        slaveJedis.getClient().getPort());
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
    public boolean deploySentinelInstance(long appId, String masterHost, String slaveHost, int maxMemory, List<String> sentinelList) {
        if (!isExist(appId)) {
            return false;
        }
        //获取端口
        Integer masterPort = machineCenter.getAvailablePort(masterHost, ConstUtils.CACHE_REDIS_STANDALONE);
        if (masterPort == null) {
            logger.error("masterHost={} getAvailablePort is null", masterHost);
            return false;
        }
        Integer slavePort = machineCenter.getAvailablePort(slaveHost, ConstUtils.CACHE_REDIS_STANDALONE);
        if (slavePort == null) {
            logger.error("slaveHost={} getAvailablePort is null", slavePort);
            return false;
        }
        //运行实例
        boolean isMasterRun = runInstance(masterHost, masterPort, maxMemory, false);
        if (!isMasterRun) {
            return false;
        }
        boolean isSlaveRun = runInstance(slaveHost, slavePort, maxMemory, false);
        if (!isSlaveRun) {
            return false;
        }
        //添加slaveof配置
        boolean isSlave = slaveOf(masterHost, masterPort, slaveHost, slavePort);
        if (!isSlave) {
            return false;
        }

        //运行sentinel实例组
        boolean isRunSentinel = runSentinelGroup(sentinelList, masterHost, masterPort, appId);
        if (!isRunSentinel) {
            return false;
        }

        //写入instanceInfo 信息
        saveInstance(appId, masterHost, masterPort, maxMemory,
                ConstUtils.CACHE_REDIS_STANDALONE, "");
        saveInstance(appId, slaveHost, slavePort, maxMemory, ConstUtils.CACHE_REDIS_STANDALONE, "");

        //启动监控trigger
        boolean isMasterDeploy = redisCenter.deployRedisCollection(appId, masterHost, masterPort);
        boolean isSlaveDeploy = redisCenter.deployRedisCollection(appId, slaveHost, slavePort);
        if (!isMasterDeploy) {
            logger.warn("host={},port={},isMasterDeploy=false", masterHost, masterPort);
        }
        if (!isSlaveDeploy) {
            logger.warn("host={},port={},isSlaveDeploy=false", slaveHost, slavePort);
        }
        return true;
    }

    @Override
    public boolean deployStandaloneInstance(long appId, String host, int maxMemory) {
        if (!isExist(appId)) {
            return false;
        }
        //获取端口
        Integer port = machineCenter.getAvailablePort(host, ConstUtils.CACHE_REDIS_STANDALONE);
        if (port == null) {
            logger.error("masterHost={} getAvailablePort is null", host);
            return false;
        }

        //运行实例
        boolean isMasterRun = runInstance(host, port, maxMemory, false);
        if (!isMasterRun) {
            return false;
        }

        //写入instanceInfo 信息
        saveInstance(appId, host, port, maxMemory, ConstUtils.CACHE_REDIS_STANDALONE,
                "");

        //启动监控trigger
        boolean isMasterDeploy = redisCenter.deployRedisCollection(appId, host, port);
        if (!isMasterDeploy) {
            logger.warn("host={},port={},isMasterDeploy=false", host, port);
        }
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

    private boolean runSentinelGroup(List<String> sentinelList, String masterHost, int masterPort, long appId) {
        for (String sentinelHost : sentinelList) {
            boolean isRun = runSentinel(sentinelHost, getMasterName(masterHost, masterPort), masterHost, masterPort,
                    appId);
            if (!isRun) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean createRunNode(String host, Integer port, int maxMemory, boolean isCluster) {
        return runInstance(host, port, maxMemory, isCluster);
    }

    private boolean runInstance(String host, Integer port, int maxMemory, boolean isCluster) {
        // 生成配置
        List<String> configs = handleCommonConfig(port, maxMemory);
        if (isCluster) {
            configs.addAll(handleClusterConfig(port));
        }
        printConfig(configs);
        String fileName;
        String runShell;
        if (isCluster) {
            runShell = RedisProtocol.getRunShell(port, true);
            fileName = RedisProtocol.getConfig(port, true);
        } else {
            runShell = RedisProtocol.getRunShell(port, false);
            fileName = RedisProtocol.getConfig(port, false);
        }
        String pathFile = machineCenter.createRemoteFile(host, fileName, configs);
        if (StringUtils.isBlank(pathFile)) {
            logger.error("createFile={} error", pathFile);
            return false;
        }
        if (isCluster) {
            //删除cluster节点配置
            String deleteNodeShell = String.format("rm -rf %s/nodes-%s.conf", MachineProtocol.DATA_DIR, port);
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
        if (!isRun(host, port)) {
            logger.error("host:{};port:{} not run", host, port);
            return false;
        } else {
            logger.warn("runInstance-fallback : redis-cli -h {} -p {} shutdown", host, port);
        }
        return true;
    }

    private boolean slaveOf(final String masterHost, final int masterPort, final String slaveHost,
            final int slavePort) {
        final Jedis slave = new Jedis(slaveHost, slavePort, Protocol.DEFAULT_TIMEOUT * 3);
        try {
            boolean isSlave = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    String result = slave.slaveof(masterHost, masterPort);
                    return result != null && result.equalsIgnoreCase("OK");
                }
            }.run();
            if (!isSlave) {
                logger.error(String.format("modifyAppConfig:ip=%s,port=%s failed", slaveHost, slavePort));
                return false;
            }
            redisCenter.configRewrite(slaveHost, slavePort);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (slave != null)
                slave.close();
        }

        return true;
    }

    private boolean runSentinel(String sentinelHost, String masterName, String masterHost, Integer masterPort,
            long appId) {
        //启动sentinel实例
        Integer sentinelPort = machineCenter.getAvailablePort(sentinelHost, ConstUtils.CACHE_REDIS_SENTINEL);
        if (sentinelPort == null) {
            logger.error("host={} getAvailablePort is null", sentinelHost);
            return false;
        }
        List<String> masterSentinelConfigs = handleSentinelConfig(masterName, masterHost, masterPort, sentinelPort);
        printConfig(masterSentinelConfigs);
        String masterSentinelFileName = RedisProtocol.getConfig(sentinelPort, false);
        String sentinelPathFile = machineCenter
                .createRemoteFile(sentinelHost, masterSentinelFileName, masterSentinelConfigs);
        if (StringUtils.isBlank(sentinelPathFile)) {
            return false;
        }
        String sentinelShell = RedisProtocol.getSentinelShell(sentinelPort);
        logger.info("sentinelMasterShell:{}", sentinelShell);
        boolean isSentinelMasterShell = machineCenter.startProcessAtPort(sentinelHost, sentinelPort, sentinelShell);
        if (!isSentinelMasterShell) {
            logger.error("sentinelMasterShell={} error", sentinelShell);
            return false;
        }
        //验证实例
        if (!isRun(sentinelHost, sentinelPort)) {
            logger.error("host:{};port:{} not run", sentinelHost, sentinelPort);
            return false;
        } else {
            logger.warn("runSentinel-fallback : redis-cli -h {} -p {} shutdown", sentinelHost, sentinelPort);
        }
        //save sentinel
        saveInstance(appId, sentinelHost, sentinelPort, 0, ConstUtils.CACHE_REDIS_SENTINEL,
                getMasterName(masterHost, masterPort));
        return true;
    }

    /**
     * 获取redis 基础配置
     *
     * @param port
     * @param maxMemory
     * @return
     */
    public List<String> handleCommonConfig(int port, int maxMemory) {
        List<String> configs = null;
        try {
            configs = redisConfigTemplateService.handleCommonConfig(port, maxMemory);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (CollectionUtils.isEmpty(configs)) {
            configs = redisConfigTemplateService.handleCommonDefaultConfig(port, maxMemory);
        }
        return configs;
    }

    private List<String> handleSentinelConfig(String masterName, String host, int port, int sentinelPort) {
        List<String> configs = null;
        try {
            configs = redisConfigTemplateService.handleSentinelConfig(masterName, host, port, sentinelPort);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (CollectionUtils.isEmpty(configs)) {
            configs = redisConfigTemplateService.handleSentinelDefaultConfig(masterName, host, port, sentinelPort);
        }
        return configs;
    }

    private List<String> handleClusterConfig(int port) {
        List<String> configs = null;
        try {
            configs = redisConfigTemplateService.handleClusterConfig(port);
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

    private boolean isRun(String host, int port) {
        final Jedis jedis = new Jedis(host, port);
        try {
            return new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    String pong = jedis.ping();
                    return pong != null && pong.equalsIgnoreCase("PONG");
                }
            }.run();
        } finally {
            jedis.close();
        }
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
            if (!modifyInstanceConfig(host, port, parameter, value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean modifyInstanceConfig(final String host, final int port, final String parameter, final String value) {
        final Jedis jedis = new Jedis(host, port, 5000);
        try {
            boolean isConfig = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    boolean isRun = redisCenter.isRun(host, port);
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
            boolean isRewrite = redisCenter.configRewrite(host, port);
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
            jedis.close();
            jedisSentinelPool.destroy();
        }
        boolean isRun = runSentinel(sentinelHost, masterName, masterHost, masterPort, appId);
        if (!isRun) {
            return false;
        }
        return true;
    }
    
    @Override
    public RedisOperateEnum addSlotsFailMaster(long appId, int lossSlotsInstanceId, String newMasterHost) throws Exception {
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
        InstanceInfo sourceMasterInstance = redisCenter.getHealthyInstanceInfo(appId);
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
        List<Integer> allLossSlots = redisCenter.getClusterLossSlots(healthyMasterHost, healthyMasterPort);
        if (CollectionUtils.isEmpty(allLossSlots)) {
            logger.warn("appId {} all slots is regular and assigned", appId);
            return RedisOperateEnum.ALREADY_SUCCESS;
        }
        // 3.2 查看目标实例丢失slots 
        List<Integer> clusterLossSlots = redisCenter.getInstanceSlots(healthyMasterHost, healthyMasterPort, lossSlotsInstanceInfo.getIp(), lossSlotsInstanceInfo.getPort());
        // 4.开启新的节点
        // 4.1 从newMasterHost找到可用的端口newMasterPort
        final Integer newMasterPort = machineCenter.getAvailablePort(newMasterHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        if (newMasterPort == null) {
            logger.error("host={} getAvailablePort is null", newMasterHost);
            return RedisOperateEnum.FAIL;
        }
        // 4.2 按照sourceMasterInstance的内存启动
        boolean isRun = runInstance(newMasterHost, newMasterPort, healthyMasterMem, true);
        if (!isRun) {
            logger.error("{}:{} is not run", newMasterHost, newMasterPort);
            return RedisOperateEnum.FAIL;
        }
        // 4.3 拷贝配置
        boolean isCopy = copyCommonConfig(healthyMasterHost, healthyMasterPort, newMasterHost, newMasterPort);
        if (!isCopy) {
            logger.error("{}:{} copy config {}:{} is error", healthyMasterHost, healthyMasterPort, newMasterHost, newMasterPort);
            return RedisOperateEnum.FAIL;
        }
        
        // 5. meet
        boolean isClusterMeet = false;
        Jedis sourceMasterJedis = null;
        try {
            sourceMasterJedis = new Jedis(healthyMasterHost, healthyMasterPort, 5000);
            isClusterMeet = clusterMeet(sourceMasterJedis, newMasterHost, newMasterPort);
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
        String addSlotsResult = "";
        Jedis newMasterJedis = null;
        Jedis healthyMasterJedis = null;
        try {
            newMasterJedis = new Jedis(newMasterHost, newMasterPort, 5000);
            healthyMasterJedis = new Jedis(healthyMasterHost, healthyMasterPort, 5000);
            //获取新的补救节点的nodid
            final String nodeId = getClusterNodeId(newMasterJedis);
            for (Integer slot : clusterLossSlots) {
                addSlotsResult = healthyMasterJedis.clusterSetSlotNode(slot, nodeId);
                logger.warn("set slot {}, result is {}", slot, addSlotsResult);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (newMasterJedis != null) {
                newMasterJedis.close();
            }
            if (healthyMasterJedis != null) {
                healthyMasterJedis.close();
            }
        }
        if (!"OK".equalsIgnoreCase(addSlotsResult)) {
            logger.warn("{}:{} set slots faily", newMasterHost, newMasterPort);
            return RedisOperateEnum.FAIL;
        }
        
        // 7.保存实例信息、并开启收集信息
        saveInstance(appId, newMasterHost, newMasterPort, healthyMasterMem, ConstUtils.CACHE_TYPE_REDIS_CLUSTER, "");
        redisCenter.deployRedisCollection(appId, newMasterHost, newMasterPort);
        
        // 休息一段时间，同步clusterNodes信息
        TimeUnit.SECONDS.sleep(2);
        
        // 8.最终打印出当前还没有补充的slots
        List<Integer> currentLossSlots = redisCenter.getClusterLossSlots(newMasterHost, newMasterPort);
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
        boolean isRun;
        if (TypeUtil.isRedisCluster(type)) {
            isRun = runInstance(slaveHost, slavePort, instanceInfo.getMem(), true);
        } else {
            isRun = runInstance(slaveHost, slavePort, instanceInfo.getMem(), false);
        }

        if (!isRun) {
            logger.error("{}:{} is not run", slaveHost, slavePort);
            return false;
        }

        boolean isCopy = copyCommonConfig(masterHost, masterPort, slaveHost, slavePort);
        if (!isCopy) {
            logger.error("{}:{} copy config {}:{} is error", masterHost, masterPort, slaveHost, slavePort);
            return false;
        }
        if (TypeUtil.isRedisCluster(type)) {
            final Jedis masterJedis = new Jedis(masterHost, masterPort, Protocol.DEFAULT_TIMEOUT);
            final Jedis slaveJedis = new Jedis(slaveHost, slavePort, Protocol.DEFAULT_TIMEOUT);
            try {

                boolean isClusterMeet = clusterMeet(masterJedis, slaveHost, slavePort);
                if (!isClusterMeet) {
                    logger.error("{}:{} cluster is failed", slaveHost, slaveHost);
                    return isClusterMeet;
                }
                final String nodeId = redisCenter.getNodeId(masterHost, masterPort);
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
                //保存配置
                masterJedis.clusterSaveConfig();
                slaveJedis.clusterSaveConfig();
                redisCenter.configRewrite(masterHost, masterPort);
                redisCenter.configRewrite(slaveHost, slavePort);
            } finally {
                masterJedis.close();
                slaveJedis.close();
            }
        } else {
            boolean isSlave = slaveOf(masterHost, masterPort, slaveHost, slavePort);
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
        //启动监控trigger
        boolean isDeploy = redisCenter.deployRedisCollection(appId, slaveHost, slavePort);
        if (!isDeploy) {
            logger.warn("host={},port={},isMasterDeploy=false", slaveHost, slavePort);
        }

        return true;
    }

    @Override
    public boolean sentinelFailover(long appId) throws Exception {
        Assert.isTrue(appId > 0);
        AppDesc appDesc = appDao.getAppDescById(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisSentinel(type)) {
            logger.warn("app={} is not sentinel", appDesc);
            return false;
        }
        final List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
        if (instanceList == null || instanceList.isEmpty()) {
            logger.warn("app={} instances is empty");
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
                    logger.warn("{} is not run");
                    continue;
                }
                boolean isSentinelFailOver = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        Jedis jedis = new Jedis(host, port, Protocol.DEFAULT_TIMEOUT);
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

    @Override
    public boolean clusterFailover(long appId, int slaveInstanceId) throws Exception {
        Assert.isTrue(appId > 0);
        Assert.isTrue(slaveInstanceId > 0);
        AppDesc appDesc = appDao.getAppDescById(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisCluster(type)) {
            logger.error("{} is not redis type", appDesc);
            return false;
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(slaveInstanceId);
        Assert.isTrue(instanceInfo != null);
        String slaveHost = instanceInfo.getIp();
        int slavePort = instanceInfo.getPort();
        final Jedis slaveJedis = new Jedis(slaveHost, slavePort);
        boolean isClusterFailOver = new IdempotentConfirmer() {
            @Override
            public boolean execute() {

                String response = slaveJedis.clusterFailoverForce();
                return response != null && response.equalsIgnoreCase("OK");
            }
        }.run();
        if (!isClusterFailOver) {
            logger.error("{}:{} clusterFailover failed", slaveHost, slavePort);
            return false;
        } else {
            logger.warn("{}:{} clusterFailover Done! ", slaveHost, slavePort);
        }
        return true;
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
    private boolean copyCommonConfig(String sourceHost, int sourcePort, String targetHost, int targetPort) {
        String[] compareConfigs = new String[] {"maxmemory-policy", "maxmemory", "cluster-node-timeout",
                "cluster-require-full-coverage", "repl-backlog-size", "appendonly", "hash-max-ziplist-entries",
                "hash-max-ziplist-value", "list-max-ziplist-entries", "list-max-ziplist-value", "set-max-intset-entries",
                "zset-max-ziplist-entries", "zset-max-ziplist-value"};
        try {
            for (String config : compareConfigs) {
                String sourceValue = getConfigValue(sourceHost, sourcePort, config);
                if (StringUtils.isBlank(sourceValue)) {
                    continue;
                }
                String targetValue = getConfigValue(targetHost, targetPort, config);
                if (StringUtils.isNotBlank(targetHost)) {
                    if (!targetValue.equals(sourceValue)) {
                        this.modifyInstanceConfig(targetHost, targetPort, config, sourceValue);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    private String getConfigValue(String host, int port, String key) {
        Jedis jedis = new Jedis(host, port, Protocol.DEFAULT_TIMEOUT * 3);
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

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public void setMachineDao(MachineDao machineDao) {
        this.machineDao = machineDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setAppDao(AppDao appDao) {
        this.appDao = appDao;
    }

    public void setRedisConfigTemplateService(RedisConfigTemplateService redisConfigTemplateService) {
        this.redisConfigTemplateService = redisConfigTemplateService;
    }
}