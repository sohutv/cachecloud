package com.sohu.cache.task.tasks.diagnosticTask;

import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceRoleEnum;
import com.sohu.cache.task.constant.ScanCleanConstants;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.entity.RedisServerNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @Author: zengyizhao
 * @Date: 2022/5/26
 */
@Component("AppScanCleanKeyTask")
@Scope(SCOPE_PROTOTYPE)
public class AppScanCleanKeyTask extends BaseTask {
    private long appId;

    private long auditId;

    private String pattern;

    private String nodes;

    private List<InstanceInfo> instanceList;

    private List<RedisServerNode> redisServerNodes;

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 1. 检查集群参数
        taskStepList.add("checkAppParam");
        // 2. redis server big key分析
        taskStepList.add("createAppScanCleanKeyTask");
        taskStepList.add("waitAppScanCleanKeyTaskFinish");
        // 3. 工单审批
        taskStepList.add("updateAudit");
        return taskStepList;
    }

    /**
     * 0.初始化参数
     *
     * @return
     */
    @Override
    public TaskFlowStatusEnum init() {
        super.init();

        appId = MapUtils.getLongValue(paramMap, TaskConstants.APPID_KEY);
        if (appId <= 0) {
            logger.error(marker, "task {} appId {} is wrong", taskId, appId);
            return TaskFlowStatusEnum.ABORT;
        }

        auditId = MapUtils.getLongValue(paramMap, TaskConstants.AUDIT_ID_KEY);
        if (auditId <= 0) {
            logger.error(marker, "task {} auditId {} is wrong", taskId, auditId);
            return TaskFlowStatusEnum.ABORT;
        }

        pattern = MapUtils.getString(paramMap, "pattern");
        if (StringUtils.isBlank(pattern)) {
            logger.info(marker, "task {} pattern is empty", taskId);
        }

        instanceList = appService.getAppOnlineInstanceInfo(appId);

        //redis server list
        nodes = MapUtils.getString(paramMap, ScanCleanConstants.POINTED_NODES);
        Integer operateType = MapUtils.getInteger(paramMap, ScanCleanConstants.OPERATE_TYPE);
        if(operateType == null){
            logger.error(marker, "task {} operateType is illegal", taskId);
            return TaskFlowStatusEnum.ABORT;
        }
        boolean nodesAndJudge = getNodesAndJudge(operateType);
        if(!nodesAndJudge){
            logger.error(marker, "task {} pointed nodes is illegal", taskId);
            return TaskFlowStatusEnum.ABORT;
        }
        if (CollectionUtils.isEmpty(redisServerNodes)) {
            logger.error(marker, "task {} redisServerNodes is empty", taskId);
            return TaskFlowStatusEnum.ABORT;
        }
        logger.info(marker, "task {} user paramMap node: {}", taskId, nodes);
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 1.检查应用参数
     *
     * @return
     */
    public TaskFlowStatusEnum checkAppParam() {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            logger.error(marker, "appId {} appDesc is null", appId);
            return TaskFlowStatusEnum.ABORT;
        }
        if (!appDesc.isOnline()) {
            logger.error(marker, "appId {} is must be online, ", appId);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.1.创建scan clean key子任务
     */
    public TaskFlowStatusEnum createAppScanCleanKeyTask() {
        redisServerNodes = buildRedisServerNodes(redisServerNodes, appId);
        paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);

        // 每个server的dbsize
        Map<String, Long> redisServerDbSizeMap = new HashMap<>();
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long dbSize = redisCenter.getDbSize(appId, redisServerNode.getIp(), redisServerNode.getPort());
            logger.info(marker, "appId {} {}:{} dbSize is {} ", appId, host, port, dbSize);
            redisServerDbSizeMap.put(host + ":" + port, dbSize);
        }

        for (RedisServerNode redisServerNode : redisServerNodes) {
            //跳过已经执行完毕的节点
            if (redisServerNode.getTaskId() > 0) {
                continue;
            }
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            try {
                long dbSize = redisServerDbSizeMap.get(host + ":" + port);
                long childTaskId = taskService.addInstanceScanCleanKeyTask(appId, auditId, host, port, paramMap, taskId);
                redisServerNode.setTaskId(childTaskId);
                logger.info(marker, "appId {} {}:{} dbsize:{} redis scanCleanKeyTask create successfully", appId, host, port, dbSize);
            } catch (Exception e) {
                logger.error(marker, "appId {} {}:{}  redis scanCleanKeyTask create fail", appId, host, port);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.2.等待scanCleankey子任务完成
     *
     * @return
     */
    public TaskFlowStatusEnum waitAppScanCleanKeyTaskFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long childTaskId = redisServerNode.getTaskId();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId, ScanCleanConstants.REDIS_SCAN_CLEAN_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "appId {} {}:{} instanceScanKeyTask execute fail", appId, host, port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "appId {} {}:{} instanceScanKeyTask execute successfully", appId, host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 3.通过初审：资源分配
     */
    public TaskFlowStatusEnum updateAudit() {
        try {
            AppDesc appDesc = appService.getByAppId(appId);
            appAuditDao.updateAppAudit(auditId, AppCheckEnum.APP_PASS.value());
            StringBuffer content = new StringBuffer();
            content.append(String.format("应用(%s-%s)的scan clean key完成", appDesc.getAppId(), appDesc.getName()));
            return TaskFlowStatusEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(marker, e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
    }

    private boolean getNodesAndJudge(int operateType){
        boolean judgePointedNodeFlag = true;
        List<InstanceInfo> instanceInfoList = new ArrayList<>();
        redisServerNodes = new ArrayList<>();
        if(ScanCleanConstants.NODE_TYPE_MASTER.equals(nodes)){
            instanceInfoList = instanceList.stream().filter(instanceInfo -> InstanceRoleEnum.MASTER.getInfo().equals(instanceInfo.getRoleDesc())).collect(Collectors.toList());

        }else if(ScanCleanConstants.NODE_TYPE_SLAVE.equals(nodes)){
            instanceInfoList = instanceList.stream().filter(instanceInfo -> InstanceRoleEnum.SLAVE.getInfo().equals(instanceInfo.getRoleDesc())).collect(Collectors.toList());
        }else if(nodes != null){
            List<InstanceInfo> instanceInfos = new ArrayList<>();
            String[] split = nodes.split(",");
            for (String nodeStr : split){
                instanceList.forEach(instanceInfo -> {
                    if(instanceInfo.getHostPort().equals(nodeStr)){
                        instanceInfos.add(instanceInfo);
                    }
                });
            }
            instanceInfoList = instanceInfos;
        }
        instanceInfoList.forEach(instanceInfo -> redisServerNodes.add(new RedisServerNode(instanceInfo.getIp(), instanceInfo.getPort())));
        if(operateType == 1 || operateType == 2){
            Optional<InstanceInfo> slaveExist = instanceInfoList.stream().filter(instanceInfo -> InstanceRoleEnum.SLAVE.getInfo().equals(instanceInfo.getRoleDesc())).findFirst();
            if(slaveExist.isPresent()){
                judgePointedNodeFlag = false;
            }
        }
        //仅扫描时，不增加节点角色类型校验
//        else if(operateType == 0){
//            Optional<InstanceInfo> masterExist = instanceInfoList.stream().filter(instanceInfo -> InstanceRoleEnum.MASTER.getInfo().equals(instanceInfo.getRoleDesc())).findFirst();
//            if(masterExist.isPresent()){
//                judgePointedNodeFlag = false;
//            }
//        }
        return judgePointedNodeFlag;
    }

}
