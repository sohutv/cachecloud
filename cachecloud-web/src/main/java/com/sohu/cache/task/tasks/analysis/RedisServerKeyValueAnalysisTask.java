package com.sohu.cache.task.tasks.analysis;

import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.constant.ValueSizeDistriEnum;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisException;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * value size分析 memory usage结果
 *
 * @author fulei
 */
@Component("RedisServerKeyValueAnalysisTask")
@Scope(SCOPE_PROTOTYPE)
public class RedisServerKeyValueAnalysisTask extends BaseTask {

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
        taskStepList.add("keyValueAnalysis");
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

    public TaskFlowStatusEnum keyValueAnalysis() {
        long startTime = System.currentTimeMillis();

        //本地结果集
        Map<ValueSizeDistriEnum, Long> valueSizeCountMap = new HashMap<ValueSizeDistriEnum, Long>();

        Jedis jedis = null;
        try {
            jedis = redisCenter.getJedis(appId, host, port);;
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

                    for (byte[] key : keyList) {
                        Long memoryUsage = null;
                        try {
                            // key 可能不存在,报 ERR no such key
                            memoryUsage = jedis.memoryUsage(new String(key, Charset.forName("UTF-8")));
                        } catch (JedisException e) {
                            logger.warn("memoryUsage-error: key={}", new String(key, Charset.forName("UTF-8")));
                            logger.error("memoryUsage-error: ", e);
                            //ignore
                        }
                        if (memoryUsage == null) {
                            continue;
                        }
                        ValueSizeDistriEnum valueSizeDistriEnum = ValueSizeDistriEnum.getRightSizeBetween(memoryUsage);
                        if (valueSizeDistriEnum == null) {
                            logger.warn("key {} valueBytes {} is wrong", key, memoryUsage);
                            continue;
                        }
                        if (valueSizeCountMap.containsKey(valueSizeDistriEnum)) {
                            valueSizeCountMap.put(valueSizeDistriEnum, valueSizeCountMap.get(valueSizeDistriEnum) + 1);
                        } else {
                            valueSizeCountMap.put(valueSizeDistriEnum, 1L);
                        }
                    }
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
            logger.info(marker, "{} {}:{} analysis key value size successfully, cost time is {} ms, total key is {}",
                    appId, host, port, (System.currentTimeMillis() - startTime), count);

            if (MapUtils.isNotEmpty(valueSizeCountMap)) {
                String keyValueSizeResultKey = ConstUtils.getRedisServerValueSizeKey(appId, auditId);
                for (Entry<ValueSizeDistriEnum, Long> entry : valueSizeCountMap.entrySet()) {
                    String valueSizeDistri = entry.getKey().getValue();
                    assistRedisService.zincrby(keyValueSizeResultKey, entry.getValue(), valueSizeDistri);
                    logger.info(marker, "{} {} {}:{} value size distri {} {}", keyValueSizeResultKey, appId, host, port,
                            valueSizeDistri, entry.getValue());
                }
            } else {
                logger.error(marker, "{} {}:{} value size distri is empty", appId, host, port);
            }

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
