package com.sohu.cache.task.tasks;

import com.alibaba.fastjson.JSONArray;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineStats;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceStatusEnum;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.entity.RedisSentinelNode;
import com.sohu.cache.task.entity.RedisServerNode;
import com.sohu.cache.util.EnvUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * <p>
 * Description: Redis Sentinel应用部署任务流
 * </p>
 *
 * @author chenshi
 * @version 1.0
 * @date 2019/1/10
 */
@Component("RedisSentinelAppDeployTask")
@Scope(SCOPE_PROTOTYPE)
public class RedisSentinelAppDeployTask extends BaseTask {

    /**
     * 应用id
     */
    private long appId;

    /**
     * 审核id
     */
    private long auditId;

    /**
     * redis server机器列表
     */
    private List<String> redisServerMachineList;

    /**
     * redis sentinel机器列表
     */
    private List<String> redisSentinelMachineList;

    /**
     * 每台机器redis server实例个数
     */
    private int masterPerMachine;

    /**
     * 每台机器sentinel实例个数
     */
    private int sentinelPerMachine;

    /**
     * 每个实例的最大内存(MB)
     */
    private int maxMemory;

    private List<RedisServerNode> redisServerNodes;

    private List<RedisSentinelNode> redisSentinelNodes;

    private String version;

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        //1. 参数初始化
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        //2. 检查资源
        taskStepList.add("checkResourceAllow");
        //3. 检查机器可用性
        taskStepList.add("checkMachineConnect");
        //4. 更新机器分配状态
        taskStepList.add("updateMachineAllocateTrue");
        //5. 获取实例列表
        taskStepList.add("generateInstanceNodes");
        //6. 保存实例列表
        taskStepList.add("saveRedisInstanceNodes");
        taskStepList.add("saveSentinelInstanceNodes");
        //7. 创建redis server job
        taskStepList.add("createRedisServerTask");
        //8. 等待redis server job完成
        taskStepList.add("waitRedisServerFinish");
        /*
            9. 设置密码已放到实例配置文件中，启动后自动带有密码，
            此步骤是设置app_desc表的密码字段，避免后续访问操作失败
         */
        taskStepList.add("setPasswd");
        //10. 更改实例状态
        taskStepList.add("updateInstanceStatus");
        //11. 主从复制
        taskStepList.add("configReplication");
        //12. 创建redis sentinel job
        taskStepList.add("createRedisSentinelTask");
        //13. 等待redis sentinel job完成
        taskStepList.add("waitRedisSentinelFinish");
        //14. 更改实例状态
        taskStepList.add("updateSentinelInstanceStatus");
        //15. 开始收集部署
        taskStepList.add("deployCollection");
        //16. 重置sentinel实例状态
        taskStepList.add("sentinelReset");
        //17. 审核
        taskStepList.add("updateAudit");
        //18. 更新机器分配状态
        taskStepList.add("updateMachineAllocateFalse");
        return taskStepList;
    }

    @Override
    public TaskFlowStatusEnum init() {
        super.init();

        appId = MapUtils.getLongValue(paramMap, TaskConstants.APPID_KEY);
        if (appId <= 0) {
            logger.error(marker, "task {} appId {} is wrong", taskId, appId);
            return TaskFlowStatusEnum.ABORT;
        }

        //审核id
        auditId = MapUtils.getLongValue(paramMap, TaskConstants.AUDIT_ID_KEY);
//        if (auditId <= 0) {
//            logger.error(marker, "task {} auditId {} is wrong", taskId, auditId);
//            return TaskFlowStatusEnum.ABORT;
//        }

        //maxMemory
        maxMemory = MapUtils.getIntValue(paramMap, TaskConstants.REDIS_SERVER_MAX_MEMORY_KEY);
        if (maxMemory <= 0 || maxMemory > TaskConstants.MAX_MEMORY_LIMIT) {
            logger.error(marker, "task {} maxMemory {} is wrong", taskId, maxMemory);
            return TaskFlowStatusEnum.ABORT;
        }

        //masterPerMachine
        masterPerMachine = MapUtils.getIntValue(paramMap, TaskConstants.MASTER_PER_MACHINE_KEY);
        if (masterPerMachine <= 0 || masterPerMachine > TaskConstants.MAX_MASTER_PER_MACHINE) {
            logger.error(marker, "task {} masterPerMachine {} is wrong", taskId, masterPerMachine);
            return TaskFlowStatusEnum.ABORT;
        }

        //sentinelPerMachine
        sentinelPerMachine = MapUtils.getIntValue(paramMap, TaskConstants.SENTINEL_PER_MACHINE_KEY);
        if (sentinelPerMachine <= 0) {
            logger.error(marker, "task {} sentinelPerMachine {} is wrong", taskId, sentinelPerMachine);
            return TaskFlowStatusEnum.ABORT;
        }

        //redis server machine
        String redisServerMachineStr = MapUtils.getString(paramMap, TaskConstants.REDIS_SERVER_MACHINE_LIST_KEY);
        redisServerMachineList = JSONArray.parseArray(redisServerMachineStr, String.class);
        if (CollectionUtils.isEmpty(redisServerMachineList)) {
            logger.error(marker, "task {} redisServerMachineList is empty", taskId);
            return TaskFlowStatusEnum.ABORT;
        }

        //redis server list
        String redisServerNodesStr = MapUtils.getString(paramMap, TaskConstants.REDIS_SERVER_NODES_KEY);
        if (StringUtils.isNotBlank(redisServerNodesStr)) {
            redisServerNodes = JSONArray.parseArray(redisServerNodesStr, RedisServerNode.class);
            if (CollectionUtils.isEmpty(redisServerNodes)) {
                logger.error(marker, "task {} redisServerNodes is empty", taskId);
                return TaskFlowStatusEnum.ABORT;
            }
        }

        //redis sentinel machine
        String redisSentinelMachineStr = MapUtils.getString(paramMap, TaskConstants.REDIS_SENTINEL_MACHINE_LIST_KEY);
        redisSentinelMachineList = JSONArray.parseArray(redisSentinelMachineStr, String.class);
        if (CollectionUtils.isEmpty(redisSentinelMachineList)) {
            logger.error(marker, "task {} redisSentinelMachineList is empty", taskId);
            return TaskFlowStatusEnum.ABORT;
        }

        //redis sentinel list
        String redisSentinelNodesStr = MapUtils.getString(paramMap, TaskConstants.REDIS_SENTINEL_NODES_KEY);
        if (StringUtils.isNotBlank(redisSentinelNodesStr)) {
            redisSentinelNodes = JSONArray.parseArray(redisSentinelNodesStr, RedisSentinelNode.class);
            if (CollectionUtils.isEmpty(redisSentinelNodes)) {
                logger.error(marker, "task {} redisSentinelNodes is empty", taskId);
                return TaskFlowStatusEnum.ABORT;
            }
        }
        version = MapUtils.getString(paramMap, TaskConstants.VERSION_KEY);

        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 检查资源是否充足
     *
     * @return
     */
    public TaskFlowStatusEnum checkResourceAllow() {
        if (EnvUtil.isLocal(environment)) {
            return TaskFlowStatusEnum.SUCCESS;
        }
        // 容量和代理
        long memoryNeed = Long.valueOf(masterPerMachine) * maxMemory;
        TaskFlowStatusEnum taskFlowStatusEnum = checkResourceAllow(redisServerMachineList, memoryNeed);
        if(taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)){
            return TaskFlowStatusEnum.ABORT;
        }
        // sentinel
        for (String redisSentinelIp : redisSentinelMachineList) {
            MachineStats machineStats = machineStatsDao.getMachineStatsByIp(redisSentinelIp);
            if (machineStats == null) {
                logger.error(marker, "{} redis sentinel machineStats is null", redisSentinelIp);
                return TaskFlowStatusEnum.ABORT;
            }
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(redisSentinelIp);
            if (machineInfo == null || machineInfo.getIsAllocating() == 1) {
                logger.error(marker, "redis sentinel machine {} allocating is 1", redisSentinelIp);
                return TaskFlowStatusEnum.ABORT;
            }
            if (!checkMachineStatIsUpdate(machineStats)) {
                logger.error(marker, "redis sentinel machine stats {} update_time is {}, may be not updated recently", machineInfo.getIp(), machineStats.getUpdateTimeFormat());
                return TaskFlowStatusEnum.ABORT;
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }


    public TaskFlowStatusEnum checkMachineConnect() {
        // redis server
        TaskFlowStatusEnum taskFlowStatusEnum = checkMachineConnect(redisServerMachineList, "sentinelredisServer {} is not connected");
        if(taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)){
            return TaskFlowStatusEnum.ABORT;
        }
        // redis sentinel
        return checkMachineConnect(redisSentinelMachineList, "redisSentinel {} is not connected");
    }

    /**
     * 生成实例
     *
     * @return
     */
    public TaskFlowStatusEnum generateInstanceNodes() {

        redisServerNodes = instancePortService.generateRedisServerNodeList(appId, redisServerMachineList, masterPerMachine, maxMemory);
        if (CollectionUtils.isEmpty(redisServerNodes)) {
            logger.warn(marker, "redisServerNodes is empty, appId is {}, redisServerMachineList is {}, masterPerMachine is {}, maxMemory is {}",
                    appId, redisServerMachineList, masterPerMachine, maxMemory);
            return TaskFlowStatusEnum.ABORT;
        }
        //只取一对
        redisServerNodes = redisServerNodes.subList(0, 2);

        redisSentinelNodes = instancePortService.generateRedisSentinelNodeList(appId, redisSentinelMachineList, sentinelPerMachine);
        if (CollectionUtils.isEmpty(redisSentinelNodes)) {
            logger.warn(marker, "redisSentinelNodes is empty, appId is {}, redisSentinelMachineList is {}, masterPerMachine is {}",
                    appId, redisSentinelMachineList, sentinelPerMachine);
            return TaskFlowStatusEnum.ABORT;
        }

        //设置环境变量
        paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);
        paramMap.put(TaskConstants.REDIS_SENTINEL_NODES_KEY, redisSentinelNodes);

        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 保存redis实例信息
     */
    public TaskFlowStatusEnum saveRedisInstanceNodes() {
        if (CollectionUtils.isEmpty(redisServerNodes)) {
            logger.warn(marker, "redisServerNodes is empty");
            return TaskFlowStatusEnum.ABORT;
        }
        for (RedisServerNode redisServerNode : redisServerNodes) {
            Integer instanceId = saveInstance(appId, redisServerNode.getIp(), redisServerNode.getPort(), maxMemory,
                    InstanceTypeEnum.REDIS_SERVER, InstanceStatusEnum.NEW_STATUS, "");
            if (instanceId == null || instanceId.equals(0)) {
                return TaskFlowStatusEnum.ABORT;
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 保存sentinel实例信息
     */
    public TaskFlowStatusEnum saveSentinelInstanceNodes() {
        if (CollectionUtils.isEmpty(redisSentinelNodes)) {
            logger.warn(marker, "redisSentinelNodes is empty");
            return TaskFlowStatusEnum.ABORT;
        }
        // 获取sentinel masterName
        String masterName = "";
        List<RedisServerNode> masterRedisServerNodes = getMasterRedisServerNodes();
        if (masterRedisServerNodes.size() == 1) {
            masterName = masterRedisServerNodes.get(0).getMasterName();
            logger.info("sentinel masterName :" + masterName);
        } else {
            logger.error(marker, "appId {} masterRedisServerNodes {} is empty", appId, masterRedisServerNodes);
            return TaskFlowStatusEnum.ABORT;
        }
        // 保存sentinel实例信息
        if (!StringUtils.isEmpty(masterName)) {
            for (RedisSentinelNode redisSentinelNode : redisSentinelNodes) {
                Integer instanceId = saveInstance(appId, redisSentinelNode.getIp(), redisSentinelNode.getPort(), 0,
                        InstanceTypeEnum.REDIS_SENTINEL, InstanceStatusEnum.NEW_STATUS, masterName);
                if (instanceId == null || instanceId.equals(0)) {
                    return TaskFlowStatusEnum.ABORT;
                }
            }
        } else {
            logger.error(marker, "appId {} masterName {} is empty", appId, masterName);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 创建任务
     *
     * @return
     */
    public TaskFlowStatusEnum createRedisServerTask() {
        if (CollectionUtils.isEmpty(redisServerNodes)) {
            logger.warn(marker, "{} redisServerNodes is emtpy", taskId);
            return TaskFlowStatusEnum.ABORT;
        }
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            try {
                long childTaskId = taskService.addRedisServerInstallTask(appId, host, port, redisServerNode.getMaxmemory(), version, false, taskId);
                if (childTaskId <= 0) {
                    logger.error(marker, "{} {} {} redis server childTaskId is {}", appId, host, port, childTaskId);
                    return TaskFlowStatusEnum.ABORT;
                }
                redisServerNode.setTaskId(childTaskId);
                paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);
                logger.info(marker, "{}:{} redis server task created successfully", host, port);
            } catch (Exception e) {
                logger.error(marker, "{}:{} redis server task created failly", host, port);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 等待任务完成
     *
     * @return
     * @TODO 超时时间
     */
    public TaskFlowStatusEnum waitRedisServerFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            long childTaskId = redisServerNode.getTaskId();
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId, TaskConstants.REDIS_SERVER_INSTALL_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "{}:{} redis server task fail", host, port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "{}:{} redis server task finish successfully", host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 建立主从关系
     *
     * @return
     */
    public TaskFlowStatusEnum configReplication() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String masterHost = redisServerNode.getMasterHost();
            int masterPort = redisServerNode.getMasterPort();
            if (masterHost == null || masterPort <= 0) {
                continue;
            }
            String slaveHost = redisServerNode.getIp();
            int slavePort = redisServerNode.getPort();
            //幂等
            boolean isSlaveOf = redisDeployCenter.slaveOf(appId, masterHost, masterPort, slaveHost, slavePort);
            if (!isSlaveOf) {
                logger.error(marker, "{}:{} slaveof {}:{} fail", slaveHost, slavePort, masterHost, masterPort);
                return TaskFlowStatusEnum.ABORT;
            }
            logger.info(marker, "{}:{} slaveof {}:{} successfully", slaveHost, slavePort, masterHost, masterPort);
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 创建redis sentinel任务
     *
     * @return
     */
    public TaskFlowStatusEnum createRedisSentinelTask() {
        List<RedisServerNode> masterRedisServerNodes = getMasterRedisServerNodes();

        // 计算quorum
        int quorum = getQuorum(redisSentinelNodes.size());
        for (RedisSentinelNode redisSentinelNode : redisSentinelNodes) {
            String sentinelHost = redisSentinelNode.getIp();
            int sentinelPort = redisSentinelNode.getPort();
            try {
                long childTaskId = taskService.addRedisSentinelInstallTask(appId, sentinelHost, sentinelPort, masterRedisServerNodes, quorum, version, taskId);
                if (childTaskId <= 0) {
                    logger.error(marker, "{} {} {} sentinel childTaskId is {}", appId, sentinelHost, sentinelPort, childTaskId);
                    return TaskFlowStatusEnum.ABORT;
                }
                redisSentinelNode.setTaskId(childTaskId);
                paramMap.put(TaskConstants.REDIS_SENTINEL_NODES_KEY, redisSentinelNodes);
                logger.info(marker, "{}:{} redis sentinel task created successfully, childTaskId is {}", sentinelHost, sentinelPort, childTaskId);
            } catch (Exception e) {
                logger.error(marker, "{}:{} redis sentinel task created failly", sentinelHost, sentinelPort);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 等待所有redis sentinel启动
     *
     * @return
     */
    public TaskFlowStatusEnum waitRedisSentinelFinish() {
        for (RedisSentinelNode redisSentinelNode : redisSentinelNodes) {
            long childTaskId = redisSentinelNode.getTaskId();
            String host = redisSentinelNode.getIp();
            int port = redisSentinelNode.getPort();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId, TaskConstants.REDIS_SENTINEL_INSTALL_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "{} {}:{} redis sentinel task fail", appId, host, port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "{} {}:{} redis sentinel task success", appId, host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 获取所有master节点
     *
     * @return
     */
    private List<RedisServerNode> getMasterRedisServerNodes() {
        AppDesc appDesc = appDao.getAppDescById(appId);
        String appFullName = appDesc.getName();
        if (StringUtils.isBlank(appFullName)) {
            logger.error(marker, "appId {} fullName is empty", appId);
            return Collections.emptyList();
        }
        List<RedisServerNode> masterRedisServerNodes = new ArrayList<RedisServerNode>();
        for (RedisServerNode redisServerNode : redisServerNodes) {
            if (!redisServerNode.isMaster()) {
                continue;
            }
            String masterName = generateMasterName(appFullName, redisServerNode.getPort());
            redisServerNode.setMasterName(masterName);
            masterRedisServerNodes.add(redisServerNode);
        }
        return masterRedisServerNodes;
    }

    /**
     * 更新实例状态
     *
     * @return
     */
    public TaskFlowStatusEnum updateInstanceStatus() {
        redisServerNodes.forEach(redisServerNode ->
            instanceDao.updateStatus(appId, redisServerNode.getIp(), redisServerNode.getPort(), InstanceStatusEnum.GOOD_STATUS.getStatus())
        );
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 更新实例状态
     *
     * @return
     */
    public TaskFlowStatusEnum updateSentinelInstanceStatus() {
        redisSentinelNodes.forEach(redisSentinelNode ->
            instanceDao.updateStatus(appId, redisSentinelNode.getIp(), redisSentinelNode.getPort(), InstanceStatusEnum.GOOD_STATUS.getStatus())
        );
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 部署实例收集
     *
     * @return
     */
    public TaskFlowStatusEnum deployCollection() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            boolean isDeploy = redisCenter.sendDeployRedisRelateCollectionMsg(appId, host, port);
            if (!isDeploy) {
                logger.error(marker, "{} {}:{} deploy fail", appId, host, port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "{} {}:{} deploy sucessfully", appId, host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum setPasswd() {

        try {
            if (appId > 0) {
                // 设置密码
                appService.updateAppPwd(appId, String.valueOf(appId));
                // 密码校验逻辑
                boolean checkFlag = redisDeployCenter.checkAuths(appId);
                logger.info(marker, "check app sentinel passwd:{}", checkFlag);
                if (!checkFlag) {
                    logger.error(marker, "check app sentinel passwd error, appId:{}!", appId);
                    return TaskFlowStatusEnum.ABORT;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 清理sentinel实例状态
     *
     * @return
     */
    public TaskFlowStatusEnum sentinelReset() {

        try {
            redisDeployCenter.sentinelReset(appId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }

        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 通过初审：资源分配
     *
     * @return
     */
    public TaskFlowStatusEnum updateAudit() {
        try {
            if (auditId > 0) {
                appAuditDao.updateAppAudit(auditId, AppCheckEnum.APP_ALLOCATE_RESOURCE.value());
            }
            return TaskFlowStatusEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(marker, e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
    }

    public TaskFlowStatusEnum updateMachineAllocateFalse() {
        return updateMachineAllocateStatus(0);
    }

    public TaskFlowStatusEnum updateMachineAllocateTrue() {
        return updateMachineAllocateStatus(1);
    }

    public TaskFlowStatusEnum updateMachineAllocateStatus(int status) {
        try {
            Set<String> allMachineSet = new HashSet<String>();
            allMachineSet.addAll(redisServerMachineList);
            for (String ip : allMachineSet) {
                machineDao.updateMachineAllocate(ip, status);
            }
            return TaskFlowStatusEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(marker, e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
    }

}
