package com.sohu.cache.task.tasks.diagnosticTask;

import com.sohu.cache.constant.DiagnosticTypeEnum;
import com.sohu.cache.entity.DiagnosticTaskRecord;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @Author: rucao
 * @Date: 2020/6/9 15:53
 */
@Component("InstanceScanKeyTask")
@Scope(SCOPE_PROTOTYPE)
public class InstanceScanKeyTask extends BaseTask {

    private String host;

    private int port;

    private long appId;

    private String pattern;

    private long auditId;

    private int size;

    private long parentTaskId;

    private final static int SCAN_COUNT = 100;

    private final static String CONDITION_TEMPLATE = "pattern:{0};size:{1}";

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 检查实例是否运行
        taskStepList.add("checkIsRun");
        // scan
        taskStepList.add("scanKey");
        return taskStepList;
    }

    /**
     * 1.初始化参数
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

        host = MapUtils.getString(paramMap, TaskConstants.HOST_KEY);
        if (StringUtils.isBlank(host)) {
            logger.error(marker, "task {} host is empty", taskId);
            return TaskFlowStatusEnum.ABORT;
        }

        port = MapUtils.getIntValue(paramMap, TaskConstants.PORT_KEY);
        if (port <= 0) {
            logger.error(marker, "task {} port {} is wrong", taskId, port);
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

        parentTaskId = MapUtils.getLongValue(paramMap, "parentTaskId");

        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.检查run以及slave
     *
     * @return
     */
    public TaskFlowStatusEnum checkIsRun() {
        if (!redisCenter.isRun(appId, host, port)) {
            logger.error(marker, "{} {}:{} is not run", appId, host, port);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 3.scanKey
     *
     * @return
     */
    public TaskFlowStatusEnum scanKey() {
        DiagnosticTaskRecord record = new DiagnosticTaskRecord();
        record.setAppId(appId);
        record.setAuditId(auditId);
        String hostPost = host + ":" + port;
        record.setNode(hostPost);
        String condition = MessageFormat.format(CONDITION_TEMPLATE, pattern, size);
        record.setDiagnosticCondition(condition);
        record.setTaskId(taskId);
        record.setParentTaskId(parentTaskId);
        record.setType(DiagnosticTypeEnum.SCAN_KEY.getType());
        record.setStatus(0);
        diagnosticTaskRecordDao.insertDiagnosticTaskRecord(record);
        long recordId = record.getId();

        long startTime = System.currentTimeMillis();
        Jedis jedis = null;
        try {
            jedis = redisCenter.getJedis(appId, host, port);

            long dbSize = jedis.dbSize();
            if (dbSize == 0) {
                logger.info(marker, "{} {}:{} dbsize is {}", appId, host, port, dbSize);
                diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 1, System.currentTimeMillis() - startTime);
                return TaskFlowStatusEnum.SUCCESS;
            }
            logger.info(marker, "{} {}:{} total key is {} ", appId, host, port, dbSize);

            // scan参数
            byte[] cursor = "0".getBytes(Charset.forName("UTF-8"));
            ScanParams scanParams = StringUtil.isBlank(pattern) ?
                    new ScanParams().count(Math.min(SCAN_COUNT, size)) :
                    new ScanParams().match(pattern).count(Math.min(SCAN_COUNT, size));

            long count = 0;
            int totalSplit = 10;
            int curSplit = 1;

            List<String> result = new ArrayList<>();
            while (true) {
                try {
                    ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
                    cursor = scanResult.getCursorAsBytes();
                    List<byte[]> keyList = scanResult.getResult();

                    if (CollectionUtils.isNotEmpty(keyList)) {
                        result.addAll(keyList.stream().map(byteKey -> new String(byteKey)).collect(Collectors.toList()));
                    }
                    count += keyList.size();
                    if (count > dbSize / totalSplit * curSplit) {
                        logger.info(marker, "{} {}:{} has already scan {}% {} key ", appId, host, port, curSplit * 10, count);
                        curSplit++;
                    }
                    // @TODO暂时写死
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (Exception e) {
                    logger.error(marker, e.getMessage(), e);
                } finally {
                    //防止无限循环
                    if (result.size() >= size || Arrays.equals("0".getBytes(Charset.forName("UTF-8")), cursor)) {
                        break;
                    }
                }
            }
            //结果存redis
            String redisScanKey = ConstUtils.getInstanceScanKey(taskId, hostPost);
            assistRedisService.del(redisScanKey);
            assistRedisService.rpushList(redisScanKey, result);
            //更新记录
            long cost = System.currentTimeMillis() - startTime;
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, redisScanKey, 1, cost);

            logger.info(marker, "{} {}:{} scan key successfully, cost time is {} ms, total key is {}", appId, host, port, cost, count);
            return TaskFlowStatusEnum.SUCCESS;
        } catch (RuntimeException e) {
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 2, 0);
            throw e;
        } catch (Exception e) {
            logger.error(marker, "redis-cli -h {} -p {} admin auth error", host, port);
            logger.error(marker, "scan key appId {} {}:{}  error:" + e.getMessage(), appId, host, port, e);
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 2, 0);
            return TaskFlowStatusEnum.ABORT;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }
}
