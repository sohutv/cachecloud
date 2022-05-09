package com.sohu.cache.task.tasks.diagnosticTask;


import com.sohu.cache.constant.DiagnosticTypeEnum;
import com.sohu.cache.entity.DiagnosticTaskRecord;
import com.sohu.cache.entity.InstanceSlotModel;
import com.sohu.cache.redis.util.PipelineUtil;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.Pair;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisRedirectionException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @Author: rucao
 * @Date: 2020/6/9 15:53
 */
@Component("InstanceSlotAnalysisTask")
@Scope(SCOPE_PROTOTYPE)
public class InstanceSlotAnalysisTask extends BaseTask {

    private String host;

    private int port;

    private long appId;

    private long auditId;

    private long parentTaskId;

    private static double ERROR_FACTOR = 1;
    private static String COUNT_ERROR_FORMAT = "countkeys:{0}; error:{1}; benchmark:{2}";

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 检查实例是否运行
        taskStepList.add("checkIsRun");
        // slotAnalysis
        taskStepList.add("slotAnalysis");
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
    public TaskFlowStatusEnum slotAnalysis() {
        DiagnosticTaskRecord record = new DiagnosticTaskRecord();
        record.setAppId(appId);
        record.setAuditId(auditId);
        String hostPost = host + ":" + port;
        record.setNode(hostPost);
        record.setTaskId(taskId);
        record.setParentTaskId(parentTaskId);
        record.setType(DiagnosticTypeEnum.SLOT_ANALYSIS.getType());
        record.setStatus(0);
        diagnosticTaskRecordDao.insertDiagnosticTaskRecord(record);
        long recordId = record.getId();

        /**
         * 扫描删除，计时开始*/
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


            Map<String, String> result = new HashMap<>();

            Pipeline pipeline = jedis.pipelined();
            InstanceSlotModel instanceSlotModel = (InstanceSlotModel) MapUtils.getObject(redisCenter.getClusterSlotsMap(appId), hostPost);
            if (instanceSlotModel != null) {
                List<Integer> slotList = instanceSlotModel.getSlotList();
                //set benchmark
                long benchmark = 0l;
                for (Integer slot : slotList) {
                    benchmark = jedis.clusterCountKeysInSlot(slot);
                    if (benchmark > 0) {
                        break;
                    }
                }
                slotList.stream().forEach(slot -> PipelineUtil.clusterCountKeysInSlot(pipeline, slot));
                List<Object> objectList = new ArrayList<>();
                try {
                    objectList = pipeline.syncAndReturnAll();
                } catch (JedisRedirectionException e) {
                    logger.error(marker, "redisSlotAnalysis appId {} {}:{}  JedisRedirectionException:" + e.getMessage(), appId, host, port, e);
                }

                List<Object> countObjectList = objectList;
                long finalBenchmark = benchmark;
                result = IntStream.range(0, slotList.size())
                        .filter(i -> (countObjectList.get(i) != null) && (countObjectList.get(i) instanceof Long))
                        .mapToObj(i -> new Pair<>(String.valueOf(slotList.get(i)), getCountAndError((Long) countObjectList.get(i), finalBenchmark)))
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            }

            //结果存redis
            String redisSlotAnalysis = ConstUtils.getInstanceSlotAnalysis(taskId, hostPost);
            assistRedisService.del(redisSlotAnalysis);
            assistRedisService.hmset(redisSlotAnalysis, result);
            long cost = System.currentTimeMillis() - startTime;
            /**
             * 计时结束*/
            //更新记录
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, redisSlotAnalysis, 1, cost);

            logger.info(marker, "{} {}:{} redisSlotAnalysis successfully, cost time is {} ms", appId, host, port, cost);
            return TaskFlowStatusEnum.SUCCESS;
        } catch (RuntimeException e) {
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 2, 0);
            throw e;
        } catch (Exception e) {
            logger.error(marker, "redis-cli -h {} -p {} admin auth error", host, port);
            logger.error(marker, "redisSlotAnalysis appId {} {}:{}  error:" + e.getMessage(), appId, host, port, e);
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, "", 2, 0);
            return TaskFlowStatusEnum.ABORT;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    private double getRealError(long count, long benchmark) {
        if (benchmark <= 0) {
            return count == benchmark ? 0 : 100;
        }
        return Math.abs(count - benchmark) * 1.0 / benchmark;
    }

    private String getCountAndError(long count, long benchmark) {
        double err = getRealError(count, benchmark);
        return err > ERROR_FACTOR ?
                MessageFormat.format(COUNT_ERROR_FORMAT, String.valueOf(count), String.format("%.2f", err), String.valueOf(benchmark))
                : "countkeys:" + count;
    }
}