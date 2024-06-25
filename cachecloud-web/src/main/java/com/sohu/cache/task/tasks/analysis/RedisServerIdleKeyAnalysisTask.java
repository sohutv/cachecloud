package com.sohu.cache.task.tasks.analysis;

import com.google.common.util.concurrent.AtomicLongMap;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.IdleTimeDistriEnum;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * redis server空闲key分析
 *
 * @author fulei
 */
@Component("RedisServerIdleKeyAnalysisTask")
@Scope(SCOPE_PROTOTYPE)
public class RedisServerIdleKeyAnalysisTask extends BaseTask {

    private String host;

    private int port;

    private long appId;

    private long auditId;

    /**
     * 扫描master
     */
    private final static int SCAN_COUNT = 100;

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        //检查实例是否运行
        taskStepList.add("checkIsRun");
        //idle key分析
        taskStepList.add("idleKeyAnalysis");
        return taskStepList;
    }

    /**
     * 初始化参数
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

        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum checkIsRun() {
        if (!redisCenter.isRun(appId, host, port)) {
            logger.error(marker, "{} {}:{} is not run", appId, host, port);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum idleKeyAnalysis() {
        long startTime = System.currentTimeMillis();
        //本地结果集
        AtomicLongMap<IdleTimeDistriEnum> idleTimeCountMap = AtomicLongMap.create();
        Jedis jedis = null;

        try {
            jedis = redisCenter.getAdminJedis(appId, host, port);
            jedis.readonly();

            //如果
            long dbSize = jedis.dbSize();
            if (dbSize == 0) {
                logger.info(marker, "{} {}:{} dbsize is {}", appId, host, port, dbSize);
                return TaskFlowStatusEnum.SUCCESS;
            }
            logger.info(marker, "{} {}:{} total key is {} ", appId, host, port, dbSize);

            ScanParams scanParams = new ScanParams().count(SCAN_COUNT);
            byte[] cursor = "0".getBytes(Charset.forName("UTF-8"));

            long count = 0;
            int totalSplit = 10;
            int curSplit = 1;
            while (true) {
                int retryTimes = 10;
                try {
                    ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
                    cursor = scanResult.getCursorAsBytes();
                    List<byte[]> keyList = scanResult.getResult();

                    //pipeline object idle
                    Pipeline pipeline = jedis.pipelined();
                    keyList.stream().forEach(key -> pipeline.objectIdletime(key));
                    List<Object> idleTimeList;
                    try {
                        idleTimeList = pipeline.syncAndReturnAll();
                    } catch (JedisRedirectionException e) {
                        continue;// ignore
                    }
                    idleTimeList.stream()
                            .filter(obj -> obj != null && (obj instanceof Long))
                            .forEach(obj -> {
                                long idleSeconds = (long) obj;
                                long idleHours = idleSeconds / 3600;
                                IdleTimeDistriEnum idleTimeDistriEnum = IdleTimeDistriEnum
                                        .getRightIdleDistri(idleHours);
                                if (idleTimeDistriEnum == null) {
                                    logger.error(marker, "idleHours {} {} IdleTimeDistriEnum is null", idleHours,
                                            idleSeconds);
                                } else {
                                    idleTimeCountMap.incrementAndGet(idleTimeDistriEnum);
                                }
                            });
                    count += keyList.size();
                    if (count > dbSize / totalSplit * curSplit) {
                        logger.info(marker, "{} {}:{} has already anlysis {}% {} key ", appId, host, port,
                                curSplit * 10, count);
                        curSplit++;
                    }
                    // @TODO暂时写死
                    TimeUnit.MILLISECONDS.sleep(2);
                } catch (Exception e) {
                    logger.error(marker, e.getMessage(), e);
                } finally {
                    //防止无限循环
                    if (Arrays.equals("0".getBytes(Charset.forName("UTF-8")), cursor)) {
                        break;
                    }
                }
            }
            logger.info(marker, "{} {}:{} analysis idle key successfully, cost time is {} ms, total key is {}", appId,
                    host, port, (System.currentTimeMillis() - startTime), count);

            String idleKeyResultKey = ConstUtils.getRedisServerIdleKey(appId, auditId);
            idleTimeCountMap.asMap().entrySet().stream().forEach(entry -> {
                String member = entry.getKey().getValue();
                assistRedisService.zincrby(idleKeyResultKey, entry.getValue(), member);
                logger.info(marker, "{} {} {}:{} idle distri {} {}", idleKeyResultKey, appId, host, port,
                        entry.getKey(), entry.getValue());
            });

            return TaskFlowStatusEnum.SUCCESS;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error(marker, e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}
