package com.sohu.cache.task.tasks.diagnosticTask;

import com.sohu.cache.constant.DiagnosticTypeEnum;
import com.sohu.cache.entity.DiagnosticTaskRecord;
import com.sohu.cache.redis.util.PipelineUtil;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisRedirectionException;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


/**
 * @Author: rucao
 * @Date: 2020/6/9 15:53
 */
@Component("InstanceBigKeyTask")
@Scope(SCOPE_PROTOTYPE)
public class InstanceBigKeyTask extends BaseTask {

    private String host;
    private int port;
    private long appId;
    private long fromBytes;
    private long toBytes;
    private int size;
    private long auditId;
    private long parentTaskId;

    private final static int SCAN_COUNT = 100;
    private final static String CONDITION_TEMPLATE = "fromBytes:{0}K;size:{1}";


    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 检查实例是否运行
        taskStepList.add("checkIsRun");
        // delete key
        taskStepList.add("bigKey");
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

        fromBytes = MapUtils.getLongValue(paramMap, "fromBytes");
        if (fromBytes <= 0) {
            logger.info(marker, "task {} fromBytes is empty", taskId);
        }

        size = MapUtils.getIntValue(paramMap, "size");
        if (size <= -2) {
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
    public TaskFlowStatusEnum bigKey() {
        DiagnosticTaskRecord record = new DiagnosticTaskRecord();
        record.setAppId(appId);
        record.setAuditId(auditId);
        String hostPost = host + ":" + port;
        record.setNode(hostPost);
        String condition = MessageFormat.format(CONDITION_TEMPLATE, String.valueOf(fromBytes), size);
        record.setDiagnosticCondition(condition);
        record.setTaskId(taskId);
        record.setParentTaskId(parentTaskId);
        record.setType(DiagnosticTypeEnum.BIG_KEY.getType());
        record.setStatus(0);
        diagnosticTaskRecordDao.insertDiagnosticTaskRecord(record);
        long recordId = record.getId();

        /**
         * 扫描bigkey，计时开始*/
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
            ScanParams scanParams = new ScanParams().count(SCAN_COUNT);

            long count = 0;
            int totalSplit = 10;
            int curSplit = 1;

            Map<String, String> result = new HashMap<>();
            while (true) {
                try {
                    ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
                    cursor = scanResult.getCursorAsBytes();
                    List<byte[]> keyList = scanResult.getResult();

                    Pipeline pipeline = jedis.pipelined();
                    if (CollectionUtils.isNotEmpty(keyList)) {
                        List<String> keyStrList = keyList.stream().map(byteKey -> new String(byteKey)).collect(Collectors.toList());
                        keyStrList.stream().forEach(keyStr -> PipelineUtil.memoryUsage(pipeline, keyStr));
                        List<Object> memObjectList;
                        try {
                            memObjectList = pipeline.syncAndReturnAll();
                        } catch (JedisRedirectionException e) {
                            continue; // ignore
                        }
                        List<Object> memUsedList = memObjectList;
                        Map<String, String> keyMemMap = IntStream.range(0, keyList.size())
                                .filter(i -> !"none".equalsIgnoreCase(String.valueOf(memUsedList.get(i)))
                                        && (memUsedList.get(i) instanceof Long)
                                        && Long.valueOf(String.valueOf(memUsedList.get(i))) >= fromBytes * 1024)
                                .mapToObj(i -> new Pair<>(keyStrList.get(i), String.valueOf(memUsedList.get(i))))
                                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
                        result.putAll(keyMemMap);
                    }
                    count += keyList.size();
                    if (count > dbSize / totalSplit * curSplit) {
                        logger.info(marker, "{} {}:{} has already scan&check key {}% {} key ", appId, host, port, curSplit * 10, count);
                        curSplit++;
                    }
                    // @TODO暂时写死
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (Exception e) {
                    logger.error(marker, e.getMessage(), e);
                } finally {
                    //防止无限循环

                    if ((size > 0 ? result.size() >= size : 1 != 1) || Arrays.equals("0".getBytes(Charset.forName("UTF-8")), cursor)) {
                        break;
                    }
                }
            }
            //结果存redis
            String redisBigKey = ConstUtils.getInstanceBigKey(taskId, hostPost);
            assistRedisService.del(redisBigKey);
            assistRedisService.hmset(redisBigKey, result);
            long cost = System.currentTimeMillis() - startTime;
            /**
             * 计时结束*/
            //更新记录
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, redisBigKey, 1, cost);

            logger.info(marker, "{} {}:{} instanceBigKeyTask successfully, cost time is {} ms, total key is {}", appId, host, port, cost, count);
            return TaskFlowStatusEnum.SUCCESS;
        } catch (RuntimeException e) {
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 2, 0);
            throw e;
        } catch (Exception e) {
            logger.error(marker, "redis-cli -h {} -p {} admin auth error", host, port);
            logger.error(marker, "instanceBigKeyTask appId {} {}:{}  error:" + e.getMessage(), appId, host, port, e);
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 2, 0);
            return TaskFlowStatusEnum.ABORT;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }
}
