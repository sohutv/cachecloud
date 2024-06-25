package com.sohu.cache.task.tasks.analysis;

import com.google.common.util.concurrent.AtomicLongMap;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.constant.TtlTimeDistriEnum;
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

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * key过期时间分析
 *
 * @author fulei
 */
@Component("RedisServerKeyTtlAnalysisTask")
@Scope(SCOPE_PROTOTYPE)
public class RedisServerKeyTtlAnalysisTask extends BaseTask {

    private String host;

    private int port;

    private long appId;

    private long auditId;

    /**
     * 扫描slave
     */
    private final static int SCAN_COUNT = 100;

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 检查实例是否运行
        taskStepList.add("checkIsRun");
        // key类型分析
        taskStepList.add("keyTtlAnalysis");
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

    public TaskFlowStatusEnum keyTtlAnalysis() {
        long startTime = System.currentTimeMillis();

        //本地结果集
        AtomicLongMap<TtlTimeDistriEnum> ttlTimeCountMap = AtomicLongMap.create();

        Jedis jedis = null;
        try {
            jedis = redisCenter.getAdminJedis(appId, host, port);
            jedis.readonly();

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
                try {
                    ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
                    cursor = scanResult.getCursorAsBytes();
                    List<byte[]> keyList = scanResult.getResult();

                    Pipeline pipeline = jedis.pipelined();
                    keyList.stream().forEach(key -> pipeline.ttl(key));

                    List<Object> ttlObjectList;
                    try {
                        ttlObjectList = pipeline.syncAndReturnAll();
                    } catch (JedisRedirectionException e) {
                        continue;// ignore
                    }
                    ttlObjectList.stream()
                            .filter(obj -> obj != null && (obj instanceof Long))
                            .forEach(obj -> {
                                long ttlSeconds = (long) obj;
                                TtlTimeDistriEnum ttlTimeDistriEnum;
                                if (ttlSeconds == -1) {
                                    ttlTimeDistriEnum = TtlTimeDistriEnum.BETWEEN_PERSIST_HOURS;
                                } else {
                                    long ttlHours = ttlSeconds / 3600;
                                    ttlTimeDistriEnum = TtlTimeDistriEnum.getRightTtlDistri(ttlHours);
                                }
                                if (ttlTimeDistriEnum == null) {
                                    logger.error(marker, "ttlSeconds {} TtlTimeDistriEnum is null", ttlSeconds);
                                }
                                ttlTimeCountMap.incrementAndGet(ttlTimeDistriEnum);
                            });

                    count += keyList.size();
                    if (count > dbSize / totalSplit * curSplit) {
                        logger.info(marker, "{} {}:{} has already anlysis {}% {} key ", appId, host, port,
                                curSplit * 10, count);
                        curSplit++;
                    }
                } catch (Exception e) {
                    logger.error(marker, e.getMessage(), e);
                } finally {
                    //防止无限循环
                    if (Arrays.equals("0".getBytes(Charset.forName("UTF-8")), cursor)) {
                        break;
                    }
                }
            }
            logger.info(marker, "{} {}:{} analysis key ttl successfully, cost time is {} ms, total key is {}", appId,
                    host, port, (System.currentTimeMillis() - startTime), count);

            String keyTtlResultKey = ConstUtils.getRedisServerTtlKey(appId, auditId);
            ttlTimeCountMap.asMap().entrySet().stream().forEach(entry -> {
                String ttlDistri = entry.getKey().getValue();
                assistRedisService.zincrby(keyTtlResultKey, entry.getValue(), ttlDistri);
                logger.info(marker, "{} {} {}:{} ttl distri {} {}", keyTtlResultKey, appId, host, port, ttlDistri,
                        entry.getValue());
            });

            return TaskFlowStatusEnum.SUCCESS;
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
