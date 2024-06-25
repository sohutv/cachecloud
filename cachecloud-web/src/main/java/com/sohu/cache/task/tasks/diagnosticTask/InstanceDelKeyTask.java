package com.sohu.cache.task.tasks.diagnosticTask;

import com.sohu.cache.constant.DiagnosticTypeEnum;
import com.sohu.cache.constant.SymbolConstant;
import com.sohu.cache.entity.DiagnosticTaskRecord;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum;
import com.sohu.cache.util.StringUtil;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @Author: rucao
 * @Date: 2020/6/9 15:53
 */
@Component("InstanceDelKeyTask")
@Scope(SCOPE_PROTOTYPE)
public class InstanceDelKeyTask extends BaseTask {

    private String host;

    private int port;

    private long appId;

    private String pattern;

    /**
     * 支持多个pattern，将pattern按照逗号分隔
     */
    private List<String> patterns;

    private long auditId;

    private long parentTaskId;

    private final static int SCAN_COUNT = 100;

    private final static String CONDITION_TEMPLATE = "pattern:{0}";


    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 检查实例是否运行
        taskStepList.add("checkIsRun");
        // delete key
        taskStepList.add("delKey");
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

        pattern = MapUtils.getString(paramMap, "pattern");
        patterns = new ArrayList<>();
        if (StringUtils.isBlank(pattern)) {
            logger.info(marker, "task {} pattern is empty", taskId);
        } else {
            if(pattern.contains(SymbolConstant.COMMA)){
                patterns = Arrays.asList(pattern.split(","));
            }else{
                patterns.add(pattern);
            }
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
    public TaskStepFlowEnum.TaskFlowStatusEnum delKey() {
        DiagnosticTaskRecord record = new DiagnosticTaskRecord();
        record.setAppId(appId);
        record.setAuditId(auditId);
        String hostPost = host + ":" + port;
        record.setNode(hostPost);
        record.setDiagnosticCondition(MessageFormat.format(CONDITION_TEMPLATE, patterns));
        record.setTaskId(taskId);
        record.setParentTaskId(parentTaskId);
        record.setType(DiagnosticTypeEnum.DEL_KEY.getType());
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

            // scan参数
            byte[] cursor = "0".getBytes(Charset.forName("UTF-8"));

            long count = 0;
            int totalSplit = 10;
            int curSplit = 1;

            int needScanTimes = 1;
            if(patterns.size() > 0){
                needScanTimes = patterns.size();
            }
            for(int i = 0; i < needScanTimes; i++){
                String curPattern = null;
                if(patterns.size() > 0){
                    curPattern = patterns.get(i);
                }
                ScanParams scanParams = StringUtil.isBlank(curPattern) ?
                        new ScanParams().count(SCAN_COUNT) :
                        new ScanParams().match(curPattern).count(SCAN_COUNT);
                while (true) {
                    try {
                        ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
                        cursor = scanResult.getCursorAsBytes();
                        List<byte[]> keyList = scanResult.getResult();

                        //pipeline unlink
                        Pipeline pipeline = jedis.pipelined();
                        keyList.stream().forEach(key -> pipeline.unlink(key));
                        List<Object> unlinkList;
                        try {
                            unlinkList = pipeline.syncAndReturnAll();
                        } catch (JedisRedirectionException e) {
                            continue;// ignoreu
                        }

                        count += keyList.size();
                        if (count > dbSize / totalSplit * curSplit) {
                            logger.info(marker, "{} {}:{} has already delete {}% {} key ", appId, host, port, curSplit * 10, count);
                            curSplit++;
                        }
                        // @TODO暂时写死
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (Exception e) {
                        logger.error(marker, e.getMessage(), e);
                    } finally {
                        //防止无限循环
                        if (Arrays.equals("0".getBytes(Charset.forName("UTF-8")), cursor)) {
                            break;
                        }
                    }
                }
            }

            long cost = System.currentTimeMillis() - startTime;
            /**
             * 计时结束*/
            //更新记录
            diagnosticTaskRecordDao.updateDiagnosticStatus(recordId, String.valueOf(count), 1, cost);
            logger.info(marker, "{} {}:{} del key successfully, cost time is {} ms, total key is {}", appId, host, port, cost, count);
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
