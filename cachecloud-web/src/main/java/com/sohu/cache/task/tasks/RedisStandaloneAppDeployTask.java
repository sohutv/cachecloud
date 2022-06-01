package com.sohu.cache.task.tasks;

import com.alibaba.fastjson.JSONArray;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineStats;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceStatusEnum;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.entity.RedisServerNode;
import com.sohu.cache.util.EnvUtil;
import com.sohu.cache.web.enums.SuccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * <p>
 * Description:RedisStandalone部署任务流
 * </p>
 *
 * @author chenshi
 * @version 1.0
 * @date 2019/1/9
 */
@Component("RedisStandaloneAppDeployTask")
@Scope(SCOPE_PROTOTYPE)
public class RedisStandaloneAppDeployTask extends BaseTask {
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
     * 每台机器redis server实例个数
     */
    private int masterPerMachine;

    /**
     * 每个实例的最大内存(MB)
     */
    private int maxMemory;

    private List<RedisServerNode> redisServerNodes;

    /**
     * Redis版本
     */
    private String version;

    /**
     * Redis模块信息
     */
    private String moduleInfo;

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
        taskStepList.add("saveInstanceNodes");
        //7. 创建redis server job
        taskStepList.add("createRedisServerTask");
        //8. 等待redis server job完成
        taskStepList.add("waitRedisServerFinish");
        //9. 更改实例状态
        taskStepList.add("updateInstanceStatus");
        //10. 开始收集部署
        taskStepList.add("deployCollection");
        //11. 设置密码
        taskStepList.add("setPasswd");
        //12. 装载组件
        taskStepList.add("loadModule");
        //12. 审核
        taskStepList.add("updateAudit");
        //13. 更新机器分配状态
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
                logger.error(marker, "standalone task {} redisServerNodes is empty", taskId);
                return TaskFlowStatusEnum.ABORT;
            }
        }
        // redis版本
        version = MapUtils.getString(paramMap, TaskConstants.VERSION_KEY);

        // 模块安装
        moduleInfo = MapUtils.getString(paramMap, TaskConstants.MODULE_KEY);

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
        for (String redisServerIp : redisServerMachineList) {
            MachineStats machineStats = machineStatsDao.getMachineStatsByIp(redisServerIp);
            if (machineStats == null) {
                logger.error(marker, "{} redis server machineStats is null", redisServerIp);
                return TaskFlowStatusEnum.ABORT;
            }
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(redisServerIp);
            // 机器是否分配 isAllocate
            /*if (machineInfo == null || machineInfo.getIsAllocating() == 1) {
                logger.error(marker, "redis server machine info {} {} allocating is 1", machineInfo.getIp(), redisServerIp);
                return TaskFlowStatusEnum.ABORT;
            }*/
            if (!checkMachineStatIsUpdate(machineStats)) {
                logger.error(marker, "redis server machine stats {} update_time is {}, may be not updated recently", machineInfo.getIp(), machineStats.getUpdateTimeFormat());
                return TaskFlowStatusEnum.ABORT;
            }
            long memoryFree = NumberUtils.toLong(machineStats.getMemoryFree()) / 1024 / 1024;
            long memoryNeed = Long.valueOf(masterPerMachine) * maxMemory;
            if (memoryNeed > memoryFree * 0.85) {
                logger.error(marker, "standalone : {} need {} MB, but memoryFree is {} MB", redisServerIp, memoryNeed, memoryFree);
                return TaskFlowStatusEnum.ABORT;
            }
        }

        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum checkMachineConnect() {
        // redis server
        for (String redisServerIp : redisServerMachineList) {
            boolean isConnected = checkMachineIsConnect(redisServerIp);
            if (!isConnected) {
                logger.error(marker, "standalone: redisServer {} is not connected", redisServerIp);
                return TaskFlowStatusEnum.ABORT;
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
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
        // 设置master节点
        for (RedisServerNode redisServerNode : redisServerNodes) {
            if (redisServerNode.isMaster()) {
                redisServerNodes.remove(redisServerNode);
            }
        }
        //设置环境变量
        paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 保存实例
     *
     * @return
     */
    public TaskFlowStatusEnum saveInstanceNodes() {
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
                    logger.error(marker, "standalone : {} {} {} redis server childTaskId is {}", appId, host, port, childTaskId);
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
     */
    public TaskFlowStatusEnum waitRedisServerFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            long childTaskId = redisServerNode.getTaskId();
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId, TaskConstants.REDIS_SERVER_INSTALL_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "standalone : {}:{} redis server task fail", host, port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "standalone : {}:{} redis server task finish successfully", host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 更新实例状态
     *
     * @return
     */
    public TaskFlowStatusEnum updateInstanceStatus() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            instanceDao.updateStatus(appId, host, port, InstanceStatusEnum.GOOD_STATUS.getStatus());
        }
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

    /**
     * 设置密码
     *
     * @return
     */
    public TaskFlowStatusEnum setPasswd() {
        try {
            if (appId > 0) {
                // 设置密码
                String passwd = String.valueOf(appId);
                redisDeployCenter.fixPassword(appId, passwd);
                // 密码校验逻辑
                boolean checkFlag = redisDeployCenter.checkAuths(appId);
                logger.info(marker, "check app standalone passwd:{}", checkFlag);
                if (!checkFlag) {
                    logger.error(marker, "check app standalone passwd:{} error!", passwd);
                    return TaskFlowStatusEnum.ABORT;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum loadModule(){
        if (!StringUtils.isEmpty(moduleInfo)) {
            for (String versionId : moduleInfo.split(";")) {
                if (!StringUtils.isEmpty(versionId)) {
                    Map map = redisCenter.loadModule(appId, Integer.parseInt(versionId));
                    Integer status = MapUtils.getInteger(map, "status");
                    String message = MapUtils.getString(map, "message");
                    logger.info(marker, "module load info status:{} message:{}",status, message);
                    if (status != SuccessEnum.SUCCESS.value()) {
                        return TaskFlowStatusEnum.ABORT;
                    }
                }
            }
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
