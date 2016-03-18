package com.sohu.cache.stats.app.impl;

import com.sohu.cache.async.NamedThreadFactory;
import com.sohu.cache.constant.AppAuditLogTypeEnum;
import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.AppAuditDao;
import com.sohu.cache.dao.AppAuditLogDao;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.*;
import com.sohu.cache.stats.app.AppDeployCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.AppEmailUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import redis.clients.jedis.HostAndPort;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by yijunzhang on 14-10-20.
 */
public class AppDeployCenterImpl implements AppDeployCenter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AppService appService;

    private RedisDeployCenter redisDeployCenter;

    private RedisCenter redisCenter;

    private AppEmailUtil appEmailUtil;

    private AppAuditDao appAuditDao;

    private MachineCenter machineCenter;

    private InstanceDao instanceDao;

    private AppAuditLogDao appAuditLogDao;
    
    private AppDao appDao;

    private ConcurrentMap<Long, ReshardProcess> processMap = new ConcurrentSkipListMap<Long, ReshardProcess>();

    private ExecutorService processThreadPool = new ThreadPoolExecutor(0, 256,
            0L, TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>(),
            new NamedThreadFactory("redis-cluster-reshard", false));

    @Override
    public boolean createApp(AppDesc appDesc, AppUser appUser, String memSize) {
        try {
            appService.save(appDesc);
            // 保存应用和应用的关系
            appService.saveAppToUser(appDesc.getAppId(), appDesc.getUserId());

            // 保存应用审批信息
            AppAudit appAudit = new AppAudit();
            appAudit.setAppId(appDesc.getAppId());
            appAudit.setUserId(appUser.getId());
            appAudit.setUserName(appUser.getName());
            appAudit.setModifyTime(new Date());
            appAudit.setParam1(memSize);
            appAudit.setParam2(appDesc.getTypeDesc());
            appAudit.setInfo("类型:" + appDesc.getTypeDesc() + ";初始申请空间:" + memSize);
            appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
            appAudit.setType(AppAuditType.APP_AUDIT.getValue());
            appAuditDao.insertAppAudit(appAudit);

            // 发邮件 -1表示申请
            appEmailUtil.noticeAppResult(appDesc, appAudit);

            // 保存申请日志
            AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(),
                    AppAuditLogTypeEnum.APP_DESC_APPLY);
            if (appAuditLog != null) {
                appAuditLogDao.save(appAuditLog);
            }

            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean allocateResourceApp(Long appAuditId, List<String> nodeInfoList, AppUser auditUser) {
        if (appAuditId == null || appAuditId <= 0L) {
            logger.error("appId is null");
            return false;
        }
        if (nodeInfoList == null || nodeInfoList.isEmpty()) {
            logger.error("nodeInfoList is null");
            return false;
        }
        AppAudit appAudit = appAuditDao.getAppAudit(appAuditId);
        if (appAudit == null) {
            logger.error("appAudit:id={} is not exist");
            return false;
        }
        long appId = appAudit.getAppId();
        AppDesc appDesc = appService.getByAppId(appId);
        if (appDesc == null) {
            logger.error("appDesc:id={} is not exist");
            return false;
        }
        int type = appDesc.getType();
        List<String[]> nodes = new ArrayList<String[]>();
        for (String nodeInfo : nodeInfoList) {
            nodeInfo = StringUtils.trim(nodeInfo);
            if (StringUtils.isBlank(nodeInfo)) {
                continue;
            }
            String[] array = nodeInfo.split(":");
//            if (array.length < 2) {
//                logger.error("error nodeInfo:{}", Arrays.toString(array));
//                continue;
//            }
            nodes.add(array);
        }

        boolean isAudited = false;
        if (TypeUtil.isRedisType(type)) {
            if (TypeUtil.isRedisCluster(type)) {
                isAudited = deployCluster(appId, nodes);
            } else if (nodes.size() > 0) {
                if (TypeUtil.isRedisSentinel(type)) {
                    isAudited = deploySentinel(appId, nodes);
                } else {
                    isAudited = deployStandalone(appId, nodes.get(0));
                }
            } else {
                logger.error("nodeInfoList={} is error");
            }
        } else {
            logger.error("unknown type : {}", type);
            return false;
        }

        //审核通过
        if (isAudited) {
            // 改变审核状态
            appAuditDao.updateAppAudit(appAudit.getId(), AppCheckEnum.APP_ALLOCATE_RESOURCE.value());
        }

        return true;
    }

    @Override
    public boolean offLineApp(Long appId) {
        Assert.isTrue(appId != null && appId > 0L);
        AppDesc appDesc = appService.getByAppId(appId);
        if (appDesc == null) {
            logger.error("appId={} not exist");
            return false;
        }
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        int type = appDesc.getType();
        if (instanceInfos != null) {
            for (InstanceInfo instanceInfo : instanceInfos) {
                final String ip = instanceInfo.getIp();
                final int port = instanceInfo.getPort();
                if (TypeUtil.isRedisType(type)) {
                    //取消收集
                    redisCenter.unDeployRedisCollection(appId, ip, port);
                    redisCenter.unDeployRedisSlowLogCollection(appId, ip, port);
                    boolean isShutdown = redisCenter.shutdown(ip, port);
                    if (!isShutdown) {
                        logger.error("{}:{} redis not shutdown!", ip, port);
                        return false;
                    }
                }
                //更新实例下线
                instanceInfo.setStatus(InstanceStatusEnum.OFFLINE_STATUS.getStatus());
                instanceDao.update(instanceInfo);
            }
        }
        //更新应用信息
        appDesc.setStatus(AppStatusEnum.STATUS_OFFLINE.getStatus());
        appService.update(appDesc);
        return true;
    }

    @Override
    public boolean modifyAppConfig(Long appId, Long appAuditId, String key, String value) {
        Assert.isTrue(appId != null && appId > 0L);
        Assert.isTrue(appAuditId != null && appAuditId > 0L);
        Assert.isTrue(StringUtils.isNotBlank(key));
        Assert.isTrue(StringUtils.isNotBlank(value));
        boolean isModify = redisDeployCenter.modifyAppConfig(appId, key, value);
        if (isModify) {
            // 改变审核状态
            appAuditDao.updateAppAudit(appAuditId, AppCheckEnum.APP_ALLOCATE_RESOURCE.value());
        }
        return isModify;
    }

    private boolean deploySentinel(long appId, List<String[]> nodes) {
        //数据节点
        String[] dataNodeInfo = nodes.get(0);
        String master = dataNodeInfo[0];
        int memory = NumberUtils.createInteger(dataNodeInfo[1]);
        String slave = dataNodeInfo[2];
        // sentinel节点
        List<String> sentinelList = new ArrayList<String>();
        if (nodes.size() < 2) {
            logger.error("sentinelList is none,don't generate sentinel app!");
            return false;
        }

        // sentinel节点
        for (int i = 1; i < nodes.size(); i++) {
            String[] nodeInfo = nodes.get(i);
            if (nodeInfo.length == 0 || StringUtils.isBlank(nodeInfo[0])) {
                logger.error("sentinel line {} may be empty", i);
                return false;
            }
            sentinelList.add(nodeInfo[0]);
        }
        
        return redisDeployCenter.deploySentinelInstance(appId, master, slave, memory, sentinelList);
    }

    private boolean deployCluster(long appId, List<String[]> nodes) {
        List<RedisClusterNode> clusterNodes = new ArrayList<RedisClusterNode>();
        int maxMemory = 0;
        for (String[] array : nodes) {
            String master = array[0];
            int memory = NumberUtils.createInteger(array[1]);
            String slave = null;
            if (array.length > 2) {
                slave = array[2];
            }
            RedisClusterNode node = new RedisClusterNode(master, slave);
            maxMemory = memory;
            clusterNodes.add(node);
        }
        return redisDeployCenter.deployClusterInstance(appId, clusterNodes, maxMemory);
    }

    private boolean deployStandalone(long appId, String[] nodeInfo) {
        String host = nodeInfo[0];
        int memory = NumberUtils.createInteger(nodeInfo[1]);
        return redisDeployCenter.deployStandaloneInstance(appId, host, memory);
    }

    @Override
    public boolean verticalExpansion(Long appId, Long appAuditId, final int memory) {
        Assert.isTrue(appId != null && appId > 0L);
        Assert.isTrue(appAuditId != null && appAuditId > 0L);
        Assert.isTrue(memory > 0);
        boolean isInProcess = isInProcess(appId);
        if (isInProcess) {
            return false;
        }
        AppDesc appDesc = appService.getByAppId(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisType(type)) {
            logger.error("appId={};type={} is not redis!", appDesc, type);
            return false;
        }
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        if (instanceInfos == null || instanceInfos.isEmpty()) {
            logger.error("instanceInfos is null");
            return false;
        }
        for (InstanceInfo instanceInfo : instanceInfos) {
            int instanceType = instanceInfo.getType();
            if (TypeUtil.isRedisSentinel(instanceType)) {
                continue;
            }
            // 下线实例不做操作
            if (instanceInfo.isOffline()) {
                continue;
            }
            String host = instanceInfo.getIp();
            int port = instanceInfo.getPort();

            final long maxMemoryBytes = Long.valueOf(memory) * 1024 * 1024;
            boolean isConfig = redisDeployCenter.modifyInstanceConfig(host, port, "maxmemory", String.valueOf(maxMemoryBytes));
            if (!isConfig) {
                logger.error("{}:{} set maxMemory error", host, port);
                return false;
            }
            //更新instanceInfo配置
            instanceInfo.setMem(memory);
            instanceDao.update(instanceInfo);
        }
        // 改变审核状态
        appAuditDao.updateAppAudit(appAuditId, AppCheckEnum.APP_ALLOCATE_RESOURCE.value());
        return true;
    }

    @Override
    public boolean addAppClusterSharding(Long appId, String masterHost, String slaveHost, int memory) {
        Assert.isTrue(appId != null && appId > 0L);
        Assert.isTrue(StringUtils.isNotBlank(masterHost));
        Assert.isTrue(memory > 0);
        AppDesc appDesc = appService.getByAppId(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisCluster(type)) {
            logger.error("appId={};type={} is not redis cluster!", appDesc, type);
            return false;
        }
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        if (instanceInfos == null || instanceInfos.isEmpty()) {
            logger.error("app:{} instanceInfos isEmpty", appId);
            return false;
        }
        Integer masterPort = machineCenter.getAvailablePort(masterHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        if (masterPort == null) {
            logger.error("host={} getAvailablePort is null", masterHost);
            return false;
        }
        Integer slavePort = 0;
        boolean hasSlave = StringUtils.isNotBlank(slaveHost);
        if (hasSlave) {
            slavePort = machineCenter.getAvailablePort(slaveHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
            if (slavePort == null) {
                logger.error("host={} getAvailablePort is null", slaveHost);
                return false;
            }
        }

        //运行节点
        boolean isMasterCreate = redisDeployCenter.createRunNode(masterHost, masterPort, memory, true);
        if (!isMasterCreate) {
            logger.error("createRunNode master failed {}:{}", masterHost, masterPort);
            return false;
        }
        if (hasSlave) {
            //运行节点
            boolean isSlaveCreate = redisDeployCenter.createRunNode(slaveHost, slavePort, memory, true);
            if (!isSlaveCreate) {
                logger.error("createRunNode slave failed {}:{}", slaveHost, slavePort);
                return false;
            }
        }
        Set<HostAndPort> clusterHosts = new LinkedHashSet<HostAndPort>();
        for (InstanceInfo instance : instanceInfos) {
            clusterHosts.add(new HostAndPort(instance.getIp(), instance.getPort()));
        }
        clusterHosts.add(new HostAndPort(masterHost, masterPort));
        if (hasSlave) {
            clusterHosts.add(new HostAndPort(slaveHost, slavePort));
        }
        RedisClusterReshard clusterReshard = new RedisClusterReshard(clusterHosts);
        boolean joinCluster = clusterReshard.joinCluster(masterHost, masterPort, slaveHost, slavePort);
        if (joinCluster) {
            //保存实例
            InstanceInfo instanceInfo = saveInstance(appId, 0, masterHost, masterPort, memory);
            redisCenter.deployRedisCollection(appId, masterHost, masterPort);
            if (hasSlave) {
                saveInstance(appId, instanceInfo.getParentId(), slaveHost, slavePort, memory);
                redisCenter.deployRedisCollection(appId, slaveHost, slavePort);
            }
        }
        return joinCluster;
    }

    @Override
    public boolean offLineClusterNode(final Long appId, final String host, final int port) {
        Assert.isTrue(appId != null && appId > 0L);
        Assert.isTrue(StringUtils.isNotBlank(host));
        Assert.isTrue(port > 0);
        AppDesc appDesc = appService.getByAppId(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisCluster(type)) {
            logger.error("appId={};type={} is not redis cluster!", appDesc, type);
            return false;
        }
        boolean isInProcess = isInProcess(appId);
        if (isInProcess) {
            return false;
        }
        final List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        if (instanceInfos == null || instanceInfos.isEmpty()) {
            logger.error("app:{} instanceInfos isEmpty", appId);
            return false;
        }
        Set<HostAndPort> clusterHosts = new LinkedHashSet<HostAndPort>();
        for (InstanceInfo instance : instanceInfos) {
            clusterHosts.add(new HostAndPort(instance.getIp(), instance.getPort()));
        }
        processThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Set<HostAndPort> clusterHosts = new LinkedHashSet<HostAndPort>();
                for (InstanceInfo instance : instanceInfos) {
                    clusterHosts.add(new HostAndPort(instance.getIp(), instance.getPort()));
                }
                RedisClusterReshard clusterReshard = new RedisClusterReshard(clusterHosts);
                //添加进度
                processMap.put(appId, clusterReshard.getReshardProcess());

                boolean joinCluster = clusterReshard.offLineMaster(host, port);
                if (joinCluster) {
                    InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(host, port);
                    if (instanceInfo != null) {
                        //更新实例下线
                        instanceInfo.setStatus(InstanceStatusEnum.OFFLINE_STATUS.getStatus());
                        instanceDao.update(instanceInfo);
                    }
                }
                logger.warn("async:appId={} joinCluster={} done result={}", appId, joinCluster, clusterReshard.getReshardProcess());
            }
        });

        return false;
    }

    @Override
    public boolean cleanAppData(long appId, AppUser appUser) {
        try {
            AppDesc appDesc = appDao.getAppDescById(appId);
            if (appDesc == null) {
                return false;
            }
            if (TypeUtil.isRedisType(appDesc.getType())) {
                return redisCenter.cleanAppData(appDesc, appUser);
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
    

    @Override
    public boolean horizontalExpansion(final Long appId, final String host, final int port, final Long appAuditId) {
        Assert.isTrue(appId != null && appId > 0L);
        Assert.isTrue(StringUtils.isNotBlank(host));
        Assert.isTrue(port > 0);
        boolean isInProcess = isInProcess(appId);
        if (isInProcess) {
            return false;
        }
        AppDesc appDesc = appService.getByAppId(appId);
        Assert.isTrue(appDesc != null);
        int type = appDesc.getType();
        if (!TypeUtil.isRedisCluster(type)) {
            logger.error("appId={};type={} is not redis cluster!", appDesc, type);
            return false;
        }
        final List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        if (instanceInfos == null || instanceInfos.isEmpty()) {
            logger.error("app:{} instanceInfos isEmpty", appId);
            return false;
        }
        processThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Set<HostAndPort> clusterHosts = new LinkedHashSet<HostAndPort>();
                for (InstanceInfo instance : instanceInfos) {
                    clusterHosts.add(new HostAndPort(instance.getIp(), instance.getPort()));
                }
                RedisClusterReshard clusterReshard = new RedisClusterReshard(clusterHosts);
                //添加进度
                processMap.put(appId, clusterReshard.getReshardProcess());

                boolean joinCluster = clusterReshard.joinNewMaster(host, port);
                logger.warn("async:appId={} joinCluster={} done result={}", appId, joinCluster, clusterReshard.getReshardProcess());
                if (joinCluster) {
                    // 改变审核状态
                    appAuditDao.updateAppAudit(appAuditId, AppCheckEnum.APP_ALLOCATE_RESOURCE.value());
                    InstanceInfo instanceInfo = instanceDao.getAllInstByIpAndPort(host, port);
                    if (instanceInfo != null && instanceInfo.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                        instanceInfo.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
                        instanceDao.update(instanceInfo);
                    }
                }
            }
        });

        logger.warn("reshard appId={} instance={}:{} deploy done", appId, host, port);
        return true;
    }

    private boolean isInProcess(Long appId) {
        ReshardProcess process = processMap.get(appId);
        if (process != null && process.getStatus() == 0) {
            logger.warn("appId={} isInProcess", appId, process.getStatus());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ConcurrentMap<Long, ReshardProcess> getHorizontalProcess() {
        return processMap;
    }

    private InstanceInfo saveInstance(long appId, int parentId, String host, int port, int maxMemory) {
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setAppId(appId);
        MachineInfo machineInfo = machineCenter.getMachineInfoByIp(host);
        instanceInfo.setHostId(machineInfo.getId());
        instanceInfo.setConn(0);
        instanceInfo.setMem(maxMemory);
        instanceInfo.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
        instanceInfo.setPort(port);
        instanceInfo.setType(ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        instanceInfo.setParentId(parentId);
        instanceInfo.setCmd("");
        instanceInfo.setIp(host);
        instanceDao.saveInstance(instanceInfo);
        return instanceInfo;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    public void setRedisDeployCenter(RedisDeployCenter redisDeployCenter) {
        this.redisDeployCenter = redisDeployCenter;
    }

    public void setAppEmailUtil(AppEmailUtil appEmailUtil) {
        this.appEmailUtil = appEmailUtil;
    }

    public void setAppAuditDao(AppAuditDao appAuditDao) {
        this.appAuditDao = appAuditDao;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public void setAppAuditLogDao(AppAuditLogDao appAuditLogDao) {
        this.appAuditLogDao = appAuditLogDao;
    }

    public void setAppDao(AppDao appDao) {
        this.appDao = appDao;
    }

}
