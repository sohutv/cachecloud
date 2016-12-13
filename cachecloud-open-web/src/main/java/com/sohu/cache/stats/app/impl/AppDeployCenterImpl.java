package com.sohu.cache.stats.app.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.sohu.cache.async.NamedThreadFactory;
import com.sohu.cache.constant.AppAuditLogTypeEnum;
import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.constant.DataFormatCheckResult;
import com.sohu.cache.constant.HorizontalResult;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.AppAuditDao;
import com.sohu.cache.dao.AppAuditLogDao;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppAudit;
import com.sohu.cache.entity.AppAuditLog;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceSlotModel;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.RedisClusterNode;
import com.sohu.cache.redis.RedisClusterReshard;
import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.redis.ReshardProcess;
import com.sohu.cache.stats.app.AppDeployCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.AppEmailUtil;

import redis.clients.jedis.HostAndPort;

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
            // 保存应用和用户的关系
            appService.saveAppToUser(appDesc.getAppId(), appDesc.getUserId());
            // 更新appKey
            long appId = appDesc.getAppId();
            appService.updateAppKey(appId);

            // 保存应用审批信息
            AppAudit appAudit = new AppAudit();
            appAudit.setAppId(appId);
            appAudit.setUserId(appUser.getId());
            appAudit.setUserName(appUser.getName());
            appAudit.setModifyTime(new Date());
            appAudit.setParam1(memSize);
            appAudit.setParam2(appDesc.getTypeDesc());
            appAudit.setInfo("类型:" + appDesc.getTypeDesc() + ";初始申请空间:" + memSize);
            appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
            appAudit.setType(AppAuditType.APP_AUDIT.getValue());
            appAuditDao.insertAppAudit(appAudit);

            // 发邮件
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
    public DataFormatCheckResult checkAppDeployDetail(Long appAuditId, String appDeployText) {
        if (appAuditId == null) {
            logger.error("appAuditId is null");
            return DataFormatCheckResult.fail("审核id不能为空!");
        }
        if (StringUtils.isBlank(appDeployText)) {
            logger.error("appDeployText is null");
            return DataFormatCheckResult.fail("部署节点列表不能为空!");
        }
        String[] nodeInfoList = appDeployText.split(ConstUtils.NEXT_LINE);
        if (nodeInfoList == null || nodeInfoList.length == 0) {
            logger.error("nodeInfoList is null");
            return DataFormatCheckResult.fail("部署节点列表不能为空!");
        }
        AppAudit appAudit = appAuditDao.getAppAudit(appAuditId);
        if (appAudit == null) {
            logger.error("appAudit:id={} is not exist", appAuditId);
            return DataFormatCheckResult.fail(String.format("审核id=%s不存在", appAuditId));
        }
        long appId = appAudit.getAppId();
        AppDesc appDesc = appService.getByAppId(appId);
        if (appDesc == null) {
            logger.error("appDesc:id={} is not exist");
            return DataFormatCheckResult.fail(String.format("appId=%s不存在", appId));
        }
        int type = appDesc.getType();
        //检查每一行
        for (String nodeInfo : nodeInfoList) {
            nodeInfo = StringUtils.trim(nodeInfo);
            if (StringUtils.isBlank(nodeInfo)) {
                return DataFormatCheckResult.fail(String.format("部署列表%s中存在空行", appDeployText));
            }
            String[] array = nodeInfo.split(ConstUtils.COLON);
            if (array == null || array.length == 0) {
                return DataFormatCheckResult.fail(String.format("部署列表%s中存在空行", appDeployText));
            }
            String masterHost = null;
            String memSize = null;
            String slaveHost = null;
            if (TypeUtil.isRedisCluster(type)) {
                if (array.length == 2) {
                    masterHost = array[0];
                    memSize = array[1];
                } else if (array.length == 3) {
                    masterHost = array[0];
                    memSize = array[1];
                    slaveHost = array[2];
                } else {
                    return DataFormatCheckResult.fail(String.format("部署列表中%s, 格式错误!", nodeInfo));
                }
            } else if (TypeUtil.isRedisSentinel(type)) {
                if (array.length == 3) {
                    masterHost = array[0];
                    memSize = array[1];
                    slaveHost = array[2];
                } else if (array.length == 1) {
                    masterHost = array[0];
                } else {
                    return DataFormatCheckResult.fail(String.format("部署列表中%s, 格式错误!", nodeInfo));
                }
            } else if (TypeUtil.isRedisStandalone(type)) {
                if (array.length == 2) {
                    masterHost = array[0];
                    memSize = array[1];
                } else {
                    return DataFormatCheckResult.fail(String.format("部署列表中%s, 格式错误!", nodeInfo));
                }
            }
            if (!checkHostExist(masterHost)) {
                return DataFormatCheckResult.fail(String.format("%s中的ip=%s不存在，请在机器管理中添加!", nodeInfo, masterHost));
            }
            if (StringUtils.isNotBlank(memSize) && !NumberUtils.isDigits(memSize)) {
                return DataFormatCheckResult.fail(String.format("%s中的中的memSize=%s不是整数!", nodeInfo, memSize));
            }
            if (StringUtils.isNotBlank(slaveHost) && !checkHostExist(slaveHost)) {
                return DataFormatCheckResult.fail(String.format("%s中的ip=%s不存在，请在机器管理中添加!", nodeInfo, slaveHost));
            }
        }
        //检查sentinel类型:数据节点一行，sentinel节点多行
        if (TypeUtil.isRedisSentinel(type)) {
            return checkSentinelAppDeploy(nodeInfoList);
        //检查单点类型:只能有一行数据节点
        } else if (TypeUtil.isRedisStandalone(type)) {
            return checkStandaloneAppDeploy(nodeInfoList);
        } 
        return DataFormatCheckResult.success("应用部署格式正确，可以开始部署了!");
    }

    /**
     * 检查单点格式
     * @param nodeInfoList
     * @return
     */
    private DataFormatCheckResult checkStandaloneAppDeploy(String[] nodeInfoList) {
        int redisLineNum = 0;
        for (String nodeInfo : nodeInfoList) {
            nodeInfo = StringUtils.trim(nodeInfo);
            String[] array = nodeInfo.split(ConstUtils.COLON);
            if (array.length == 2) {
                redisLineNum++;
            }
        }
        // redis节点只有一行
        if (redisLineNum != 1) {
            return DataFormatCheckResult.fail("应用部署格式错误, Standalone格式必须是一行masterIp:memSize(M)");
        }
        return DataFormatCheckResult.success("应用部署格式正确，可以开始部署了!");
    }

    /**
     * 检查redis sentinel格式
     * @param nodeInfoList
     * @return
     */
    private DataFormatCheckResult checkSentinelAppDeploy(String[] nodeInfoList) {
        int redisLineNum = 0;
        int sentinelLineNum = 0;
        for (String nodeInfo : nodeInfoList) {
            nodeInfo = StringUtils.trim(nodeInfo);
            String[] array = nodeInfo.split(ConstUtils.COLON);
            if (array.length == 3) {
                redisLineNum++;
            } else if (array.length == 1) {
                sentinelLineNum++;
            }
        }
        // redis节点只有redisLineMustNum行
        final int redisLineMustNum = 1;
        if (redisLineNum < redisLineMustNum) {
            return DataFormatCheckResult.fail("应用部署格式错误, Sentinel应用中必须有Redis数据节点!");
        } else if (redisLineNum > redisLineMustNum) {
            return DataFormatCheckResult.fail("应用部署格式错误, Sentinel应用中Redis数据节点只能有一行!");
        }  
        
        // sentinel节点至少有sentinelLessNum个
        final int sentinelLessNum = 3;
        if (sentinelLineNum < sentinelLessNum) {
            return DataFormatCheckResult.fail("应用部署格式错误, Sentinel应用中Sentinel节点至少要有" + sentinelLessNum + "个!");
        }
        return DataFormatCheckResult.success("应用部署格式正确，可以开始部署了!");
    }

    /**
     * 查看host是否存在
     * @param host
     * @return
     */
    private boolean checkHostExist(String host) {
        try {
            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(host);
            if (machineInfo == null) {
                return false;
            }
            if (machineInfo.isOffline()) {
                logger.warn("host {} is offline", host);
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean allocateResourceApp(Long appAuditId, List<String> nodeInfoList, AppUser auditUser) {
        if (appAuditId == null || appAuditId <= 0L) {
            logger.error("appAuditId is null");
            return false;
        }
        if (nodeInfoList == null || nodeInfoList.isEmpty()) {
            logger.error("nodeInfoList is null");
            return false;
        }
        AppAudit appAudit = appAuditDao.getAppAudit(appAuditId);
        if (appAudit == null) {
            logger.error("appAudit:id={} is not exist", appAuditId);
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
    public boolean addHorizontalNodes(Long appId, String masterHost, String slaveHost, int memory) {
        //1. 寻找主从节点的可用端口
        Integer masterPort = machineCenter.getAvailablePort(masterHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        if (masterPort == null) {
            logger.error("master host={} getAvailablePort is null", masterHost);
            return false;
        }
        Integer slavePort = 0;
        boolean hasSlave = StringUtils.isNotBlank(slaveHost);
        if (hasSlave) {
            slavePort = machineCenter.getAvailablePort(slaveHost, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
            if (slavePort == null) {
                logger.error("slave host={} getAvailablePort is null", slaveHost);
                return false;
            }
        }

        //2. 启动主从节点
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
        
        //3. 获取应用下有效节点
        Set<HostAndPort> clusterHosts = getEffectiveInstanceList(appId);
        
        
        //4. 添加新节点: meet,复制，不做slot分配
        RedisClusterReshard clusterReshard = new RedisClusterReshard(clusterHosts, redisCenter);
        boolean joinCluster = clusterReshard.joinCluster(masterHost, masterPort, slaveHost, slavePort);
        if (joinCluster) {
            //5. 保存实例,开启统计功能
            saveInstance(appId, masterHost, masterPort, memory);
            redisCenter.deployRedisCollection(appId, masterHost, masterPort);
            if (hasSlave) {
                saveInstance(appId, slaveHost, slavePort, memory);
                redisCenter.deployRedisCollection(appId, slaveHost, slavePort);
            }
        }
        return joinCluster;
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
    
    private boolean isInProcess(Long appId) {
        ReshardProcess process = processMap.get(appId);
        if (process != null && process.getStatus() == 0) {
            logger.warn("appId={} is inProcess", appId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ConcurrentMap<Long, ReshardProcess> getHorizontalProcess() {
        return processMap;
    }

    private InstanceInfo saveInstance(long appId, String host, int port, int maxMemory) {
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setAppId(appId);
        MachineInfo machineInfo = machineCenter.getMachineInfoByIp(host);
        instanceInfo.setHostId(machineInfo.getId());
        instanceInfo.setConn(0);
        instanceInfo.setMem(maxMemory);
        instanceInfo.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
        instanceInfo.setPort(port);
        instanceInfo.setType(ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        instanceInfo.setCmd("");
        instanceInfo.setIp(host);
        instanceDao.saveInstance(instanceInfo);
        return instanceInfo;
    }
    
    @Override
	public HorizontalResult checkHorizontal(long appId, long appAuditId, long sourceId, long targetId, int startSlot,
			int endSlot, int migrateType) {
    	// 0.当前应用正在迁移
        boolean isInProcess = isInProcess(appId);
    	if (isInProcess) {
			return HorizontalResult.fail(String.format("appId=%s正在迁移!", appId));
    	}
		// 1.应用信息
		AppDesc appDesc = appService.getByAppId(appId);
		if (appDesc == null) {
			return HorizontalResult.fail("应用信息为空");
		}
		
		// 2.0 源实例ID不能等于目标实例ID
		if (sourceId == targetId) {
            return HorizontalResult.fail(String.format("源实例ID=%s不能等于目标实例ID=%s", sourceId, targetId));
		}
		
		// 2.1 源实例信息
		InstanceInfo sourceInstanceInfo = instanceDao.getInstanceInfoById(sourceId);
		if (sourceInstanceInfo == null) {
			return HorizontalResult.fail(String.format("源实例id=%s为空", sourceId));
		}
		// 2.2 对比源实例的appId是否正确
		long sourceAppId = sourceInstanceInfo.getAppId();
		if (sourceAppId != appId) {
			return HorizontalResult.fail(String.format("源实例id=%s不属于appId=%s", sourceId, appId));
		}
		// 2.3 源实例是否在线
		boolean sourceIsRun = redisCenter.isRun(sourceInstanceInfo.getIp(), sourceInstanceInfo.getPort());
		if (!sourceIsRun) {
			return HorizontalResult.fail(String.format("源实例%s必须运行中", sourceInstanceInfo.getHostPort()));
		}
		// 2.4必须是master节点
		boolean sourceIsMaster = redisCenter.isMaster(sourceInstanceInfo.getIp(), sourceInstanceInfo.getPort());
		if (!sourceIsMaster) {
			return HorizontalResult.fail(String.format("源实例%s必须是主节点", sourceInstanceInfo.getHostPort()));
		}
		

		// 3.1 目标实例信息
		InstanceInfo targetInstanceInfo = instanceDao.getInstanceInfoById(targetId);
		if (targetInstanceInfo == null) {
			return HorizontalResult.fail(String.format("目标实例id=%s为空", targetId));
		}
		// 3.2 对比目标实例的appId是否正确
		long targetAppId = targetInstanceInfo.getAppId();
		if (targetAppId != appId) {
			return HorizontalResult.fail(String.format("目标实例id=%s不属于appId=%s", targetId, appId));
		}
		// 3.3 目标实例是否在线
		boolean targetIsRun = redisCenter.isRun(targetInstanceInfo.getIp(), targetInstanceInfo.getPort());
		if (!targetIsRun) {
			return HorizontalResult.fail(String.format("目标实例%s必须运行中", targetInstanceInfo.getHostPort()));
		}
		// 3.4 必须是master节点
		boolean targetIsMaster = redisCenter.isMaster(targetInstanceInfo.getIp(), targetInstanceInfo.getPort());
		if (!targetIsMaster) {
			return HorizontalResult.fail(String.format("目标实例%s必须是主节点", targetInstanceInfo.getHostPort()));
		}
		
		// 4.startSlot和endSlot是否在源实例中
		// 4.1 判断数值
		int maxSlot = 16383;
		if (startSlot < 0 || startSlot > maxSlot) {
			return HorizontalResult.fail(String.format("startSlot=%s必须在0-%s", startSlot, maxSlot));
		}
		if (endSlot < 0 || endSlot > maxSlot) {
			return HorizontalResult.fail(String.format("endSlot=%s必须在0-%s", endSlot, maxSlot));
		}
		if (startSlot > endSlot) {
			return HorizontalResult.fail("startSlot不能大于endSlot");
		}
		
		// 4.2 判断startSlot和endSlot属于sourceId
		// 获取所有slot分布
		Map<String, InstanceSlotModel> clusterSlotsMap = redisCenter.getClusterSlotsMap(appId);
		if (MapUtils.isEmpty(clusterSlotsMap)) {
			return HorizontalResult.fail("无法获取slot分布!");
		}
		// 获取源实例负责的slot
		String sourceHostPort = sourceInstanceInfo.getHostPort();
		InstanceSlotModel instanceSlotModel = clusterSlotsMap.get(sourceHostPort);
		if (instanceSlotModel == null || CollectionUtils.isEmpty(instanceSlotModel.getSlotList())) {
			return HorizontalResult.fail("源实例上没有slot!");
		}
		List<Integer> slotList = instanceSlotModel.getSlotList();
		for (int i = startSlot; i <= endSlot; i++) {
			if (!slotList.contains(i)) {
				return HorizontalResult.fail(String.format("源实例没有包含尽startSlot=%s到endSlot=%s", startSlot, endSlot));
			}
		}
		
		//5.是否支持批量，版本要大于等于3.0.6
		String sourceRedisVersion = redisCenter.getRedisVersion(sourceInstanceInfo.getIp(), sourceInstanceInfo.getPort());
		if (StringUtils.isBlank(sourceRedisVersion)) {
            return HorizontalResult.fail(String.format("源实例%s版本为空", sourceInstanceInfo.getHostPort()));
		}
	    String targetRedisVersion = redisCenter.getRedisVersion(targetInstanceInfo.getIp(), targetInstanceInfo.getPort());
	    if (StringUtils.isBlank(targetRedisVersion)) {
            return HorizontalResult.fail(String.format("目标实例%s版本为空", targetInstanceInfo.getHostPort()));
        }
	    RedisVersion sourceRedisVersionModel = getRedisVersion(sourceRedisVersion);
	    //选择了批量，但是当前版本不支持pipeline
	    if (migrateType == 1 && !sourceRedisVersionModel.isSupportPipelineMigrate()) {
            return HorizontalResult.fail(String.format("源实例%s版本为%s,不支持pipeline migrate!", sourceInstanceInfo.getHostPort(), sourceRedisVersion));
	    }
	    
	    RedisVersion targetRedisVersionModel = getRedisVersion(targetRedisVersion);
	    //选择了批量，但是当前版本不支持pipeline
        if (migrateType == 1 && !targetRedisVersionModel.isSupportPipelineMigrate()) {
            return HorizontalResult.fail(String.format("目标实例%s版本为%s,不支持pipeline migrate!", targetInstanceInfo.getHostPort(), targetRedisVersion));
        }
		
		return HorizontalResult.checkSuccess();
	}

    private RedisVersion getRedisVersion(String redisVersion) {
        String[] versionArr = redisVersion.split("\\.");
        if (versionArr.length == 1) {
            return new RedisVersion(NumberUtils.toInt(versionArr[0]), 0, 0);
        } else if (versionArr.length == 2) {
            return new RedisVersion(NumberUtils.toInt(versionArr[0]), NumberUtils.toInt(versionArr[1]), 0);
        } else if (versionArr.length >= 3) {
            return new RedisVersion(NumberUtils.toInt(versionArr[0]), NumberUtils.toInt(versionArr[1]),
                    NumberUtils.toInt(versionArr[2]));
        }
        return null;
    }
	
	private class RedisVersion {
	    int majorVersion;
	    int minorVersion;
	    int patchVersion;
        public RedisVersion(int majorVersion, int minorVersion, int patchVersion) {
            super();
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.patchVersion = patchVersion;
        }
        
        /**
         * 大于等于3.0.6
         * @return
         */
        public boolean isSupportPipelineMigrate() {
            if (majorVersion < 3) {
                return false;
            } else if (majorVersion == 3) {
                if (minorVersion > 0) {
                    return true;
                } else {
                    return patchVersion >= 6;
                }
            } else {
                return true;
            }
        }

        @Override
        public String toString() {
            return "RedisVersion [majorVersion=" + majorVersion + ", minorVersion=" + minorVersion + ", patchVersion="
                    + patchVersion + "]";
        }
	}
	
	/**
	 * 获取应用下有效节点
	 * @param appId
	 * @return
	 */
	private Set<HostAndPort> getEffectiveInstanceList(long appId) {
	    Set<HostAndPort> clusterHosts = new HashSet<HostAndPort>();
	    //全部节点
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        for (InstanceInfo instance : instanceInfos) {
            if (instance.isOffline()) {
                continue;
            }
            clusterHosts.add(new HostAndPort(instance.getIp(), instance.getPort()));
        }
        return clusterHosts;
	}

    @Override
	public HorizontalResult startHorizontal(final long appId, final long appAuditId, long sourceId, final long targetId, final int startSlot,
            final int endSlot, final int migrateType) {
		final InstanceInfo sourceInstanceInfo = instanceDao.getInstanceInfoById(sourceId);
		final InstanceInfo targetInstanceInfo = instanceDao.getInstanceInfoById(targetId);
        processThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //所有节点用户clustersetslot
                Set<HostAndPort> clusterHosts = getEffectiveInstanceList(appId);
                RedisClusterReshard clusterReshard = new RedisClusterReshard(clusterHosts, redisCenter);
                //添加进度
                processMap.put(appId, clusterReshard.getReshardProcess());
                boolean joinCluster = clusterReshard.migrateSlot(sourceInstanceInfo, targetInstanceInfo, startSlot, endSlot, migrateType == 1);
                logger.warn("async:appId={} joinCluster={} done result={}", appId, joinCluster, clusterReshard.getReshardProcess());
                if (joinCluster) {
                    // 改变审核状态
                    appAuditDao.updateAppAudit(appAuditId, AppCheckEnum.APP_ALLOCATE_RESOURCE.value());
                    if (targetInstanceInfo != null && targetInstanceInfo.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                    	targetInstanceInfo.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
                        instanceDao.update(targetInstanceInfo);
                    }
                }
            }
        });
        logger.warn("reshard appId={} instance={}:{} deploy done", appId, targetInstanceInfo.getIp(), targetInstanceInfo.getPort());
		return HorizontalResult.scaleSuccess();
	}
    
    @Override
    public DataFormatCheckResult checkHorizontalNodes(Long appAuditId, String masterSizeSlave) {
        if (appAuditId == null) {
            logger.error("appAuditId is null");
            return DataFormatCheckResult.fail("审核id不能为空!");
        }
        if (StringUtils.isBlank(masterSizeSlave)) {
            logger.error("masterSizeSlave is null");
            return DataFormatCheckResult.fail("添加节点不能为空!");
        }
        AppAudit appAudit = appAuditDao.getAppAudit(appAuditId);
        if (appAudit == null) {
            logger.error("appAudit:id={} is not exist", appAuditId);
            return DataFormatCheckResult.fail(String.format("审核id=%s不存在", appAuditId));
        }
        long appId = appAudit.getAppId();
        AppDesc appDesc = appService.getByAppId(appId);
        if (appDesc == null) {
            logger.error("appDesc:id={} is not exist");
            return DataFormatCheckResult.fail(String.format("appId=%s不存在", appId));
        }
        //节点数组 master:memSize:slave
        String[] array = masterSizeSlave.split(ConstUtils.COLON);
        if (array == null || array.length == 0) {
            return DataFormatCheckResult.fail(String.format("添加节点%s格式错误", masterSizeSlave));
        }
        //检查格式
        String masterHost = null;
        String memSize = null;
        String slaveHost = null;
        if (array.length == 2) {
            masterHost = array[0];
            memSize = array[1];
        } else if (array.length == 3) {
            masterHost = array[0];
            memSize = array[1];
            slaveHost = array[2];
        } else {
            return DataFormatCheckResult.fail(String.format("添加节点%s, 格式错误!", masterSizeSlave));
        }
        //检查主节点机器是否存在
        if (!checkHostExist(masterHost)) {
            return DataFormatCheckResult.fail(String.format("%s中的ip=%s不存在，请在机器管理中添加!", masterSizeSlave, masterHost));
        }
        //检查memSize格式
        if (StringUtils.isNotBlank(memSize) && !NumberUtils.isDigits(memSize)) {
            return DataFormatCheckResult.fail(String.format("%s中的中的memSize=%s不是整数!", masterSizeSlave, memSize));
        }
        //检查从节点格式
        if (StringUtils.isNotBlank(slaveHost) && !checkHostExist(slaveHost)) {
            return DataFormatCheckResult.fail(String.format("%s中的ip=%s不存在，请在机器管理中添加!", masterSizeSlave, slaveHost));
        }
        return DataFormatCheckResult.success("添加节点格式正确，可以开始部署了!");
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
