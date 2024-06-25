package com.sohu.cache.task.tasks.diagnosticTask;

import com.sohu.cache.constant.DiagnosticTypeEnum;
import com.sohu.cache.entity.DiagnosticTaskRecord;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @Author: rucao
 * @Date: 2020/6/9 15:53
 */
@Component("InstanceHotKeyTask")
@Scope(SCOPE_PROTOTYPE)
public class InstanceHotKeyTask extends BaseTask {

    private String host;

    private int port;

    private long appId;

    private String command;

    private long auditId;

    private long parentTaskId;

    private final static String CONDITION_TEMPLATE = "command:{0}";

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 检查实例是否运行
        taskStepList.add("checkIsRun");
        // delete key
        taskStepList.add("hotKey");
        return taskStepList;
    }

    /**
     * 1.初始化参数
     */
    @Override
    public TaskStepFlowEnum.TaskFlowStatusEnum init() {
        super.init();
        appId = MapUtils.getLongValue(paramMap, TaskConstants.APPID_KEY);
        if (appId <= 0) {
            logger.error(marker, "task {} appId {} is wrong", taskId, appId);
            return TaskStepFlowEnum.TaskFlowStatusEnum.ABORT;
        }

        auditId = MapUtils.getLongValue(paramMap, TaskConstants.AUDIT_ID_KEY);
        if (auditId <= 0) {
            logger.error(marker, "task {} auditId {} is wrong", taskId, auditId);
            return TaskStepFlowEnum.TaskFlowStatusEnum.ABORT;
        }

        host = MapUtils.getString(paramMap, TaskConstants.HOST_KEY);
        if (StringUtils.isBlank(host)) {
            logger.error(marker, "task {} host is empty", taskId);
            return TaskStepFlowEnum.TaskFlowStatusEnum.ABORT;
        }

        port = MapUtils.getIntValue(paramMap, TaskConstants.PORT_KEY);
        if (port <= 0) {
            logger.error(marker, "task {} port {} is wrong", taskId, port);
            return TaskStepFlowEnum.TaskFlowStatusEnum.ABORT;
        }

        command = MapUtils.getString(paramMap, "command");
        if (StringUtils.isBlank(command)) {
            logger.error(marker, "task {} command {} is wrong", taskId, command);
            return TaskStepFlowEnum.TaskFlowStatusEnum.ABORT;
        }

        parentTaskId = MapUtils.getLongValue(paramMap, "parentTaskId");

        return TaskStepFlowEnum.TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.检查run以及slave
     *
     * @return
     */
    public TaskStepFlowEnum.TaskFlowStatusEnum checkIsRun() {
        if (!redisCenter.isRun(appId, host, port)) {
            logger.error(marker, "{} {}:{} is not run", appId, host, port);
            return TaskStepFlowEnum.TaskFlowStatusEnum.ABORT;
        }
        return TaskStepFlowEnum.TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 3.scanKey
     *
     * @return
     */
    public TaskStepFlowEnum.TaskFlowStatusEnum hotKey() {
        DiagnosticTaskRecord record = new DiagnosticTaskRecord();
        record.setAppId(appId);
        record.setAuditId(auditId);
        String hostPost = host + ":" + port;
        record.setNode(hostPost);
        record.setDiagnosticCondition(MessageFormat.format(CONDITION_TEMPLATE, command));
        record.setTaskId(taskId);
        record.setParentTaskId(parentTaskId);
        record.setType(DiagnosticTypeEnum.HOT_KEY.getType());
        record.setStatus(0);
        diagnosticTaskRecordDao.insertDiagnosticTaskRecord(record);
        long recordId = record.getId();

        /**
         * 扫描删除，计时开始*/
        long startTime = System.currentTimeMillis();
        Jedis jedis = null;
        try {
            jedis = redisCenter.getAdminJedis(appId, host, port);
            long dbSize = jedis.dbSize();
            if (dbSize == 0) {
                logger.info(marker, "{} {}:{} dbsize is {}", appId, host, port, dbSize);
                diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 1, System.currentTimeMillis() - startTime);
                return TaskStepFlowEnum.TaskFlowStatusEnum.SUCCESS;
            }
            logger.info(marker, "{} {}:{} total key is {} ", appId, host, port, dbSize);

            String result = redisCenter.executeAdminCommand(appId, host, port, command, 60000 * 6);

            //结果存redis
            String redisHotKey = ConstUtils.getInstanceHotKey(taskId, hostPost);
            assistRedisService.del(redisHotKey);
            assistRedisService.set(redisHotKey, result);
            long cost = System.currentTimeMillis() - startTime;
            /**
             * 计时结束*/
            //更新记录
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, redisHotKey, 1, cost);

            logger.info(marker, "{} {}:{} hotkey successfully, cost time is {} ms", appId, host, port, cost);
            return TaskStepFlowEnum.TaskFlowStatusEnum.SUCCESS;
        } catch (RuntimeException e) {
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 2, 0);
            throw e;
        } catch (Exception e) {
            logger.error(marker, "redis-cli -h {} -p {} admin auth error", host, port);
            logger.error(marker, "del key appId {} {}:{}  error:" + e.getMessage(), appId, host, port, e);
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 2, 0);
            return TaskStepFlowEnum.TaskFlowStatusEnum.ABORT;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }
}
