package com.sohu.cache.task.tasks.diagnosticTask;

import com.alibaba.fastjson.JSONArray;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.entity.RedisServerNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @Author: rucao
 * @Date: 2020/6/9 17:36
 */
@Component("AppScanKeyTask")
@Scope(SCOPE_PROTOTYPE)
public class AppScanKeyTask extends BaseTask {
    private long appId;

    private long auditId;

    private String pattern;

    private int size;

    private List<RedisServerNode> redisServerNodes;

    private final static long SCANKEY_SLEEP_BASE = 20000000;


    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 1. 检查集群参数
        taskStepList.add("checkAppParam");
        // 2. redis server big key分析
        taskStepList.add("createAppScanKeyTask");
        taskStepList.add("waitAppScanKeyTaskFinish");
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

        size = MapUtils.getIntValue(paramMap, "size");
        if (size <= 0) {
            logger.error(marker, "task {} size {} is wrong", taskId, size);
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
            logger.info(marker, "user paramMap node: {}", redisServerNodesStr);
        }

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
     * 2.1.创建scan key子任务
     */
    public TaskFlowStatusEnum createAppScanKeyTask() {
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

        long keyCounter = 0;
        int factor = 1;
        for (RedisServerNode redisServerNode : redisServerNodes) {
            //跳过已经执行完毕的节点
            if (redisServerNode.getTaskId() > 0) {
                continue;
            }
            long sleepCounter = factor * SCANKEY_SLEEP_BASE;

            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            try {
                long dbSize = redisServerDbSizeMap.get(host + ":" + port);
                keyCounter += dbSize;

                long childTaskId = taskService.addInstanceScanKeyTask(appId, auditId, host, port, pattern, size, taskId);
                redisServerNode.setTaskId(childTaskId);
                logger.info(marker, "appId {} {}:{}  redis scanKeyTask create successfully", appId, host, port);

                // 超额就sleep
                if (keyCounter > sleepCounter) {
                    factor++;
                    sleepSeconds(120);
                }
            } catch (Exception e) {
                logger.error(marker, "appId {} {}:{}  redis scanKeyTask create fail", appId, host, port);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.2.等待scankey子任务完成
     *
     * @return
     */
    public TaskFlowStatusEnum waitAppScanKeyTaskFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long childTaskId = redisServerNode.getTaskId();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId, TaskConstants.REDIS_SERVER_DIAGNOSTIC_TIMEOUT);
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
            content.append(String.format("应用(%s-%s)的scan key完成", appDesc.getAppId(), appDesc.getName()));

            return TaskFlowStatusEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(marker, e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
    }
}
