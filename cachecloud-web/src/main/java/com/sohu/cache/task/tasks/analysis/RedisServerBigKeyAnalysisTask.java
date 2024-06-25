package com.sohu.cache.task.tasks.analysis;

import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.RedisDataStructureTypeEnum;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.entity.InstanceBigKey;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * bigkey分析
 *
 * @author fulei
 */
@Component("RedisServerBigKeyAnalysisTask")
@Scope(SCOPE_PROTOTYPE)
public class RedisServerBigKeyAnalysisTask extends BaseTask {

    private String host;

    private int port;

    private long appId;

    private long auditId;

    private final static int SCAN_COUNT = 100;

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 检查实例是否运行
        taskStepList.add("checkIsRun");
        // big key分析
        taskStepList.add("bigKeyAnalysis");
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
     * 3. bigkey分析
     *
     * @return
     */
    public TaskFlowStatusEnum bigKeyAnalysis() {
        long startTime = System.currentTimeMillis();

        InstanceStats instanceStats = instanceStatsDao.getInstanceStatsByHost(host, port);

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

            // scan参数
            byte[] cursor = "0".getBytes(Charset.forName("UTF-8"));
            ScanParams scanParams = new ScanParams().count(SCAN_COUNT);

            long count = 0;
            int totalSplit = 10;
            int curSplit = 1;
            while (true) {
                try {
                    ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
                    cursor = scanResult.getCursorAsBytes();
                    List<byte[]> keyList = scanResult.getResult();

                    // 使用pipeline获取type
                    Pipeline pipeline = jedis.pipelined();
                    keyList.stream().forEach(key -> pipeline.type(key));

                    List<Object> typeObjectList;
                    try {
                        typeObjectList = pipeline.syncAndReturnAll();
                    } catch (JedisRedirectionException e) {
                        continue; // ignore
                    }

                    List<Object> typeList = typeObjectList;
                    // key type Map
                    Map<byte[], String> keyTypeMap = IntStream.range(0, keyList.size())
                            .filter(i -> !"none".equalsIgnoreCase(String.valueOf(typeList.get(i)))
                                    && (typeList.get(i) instanceof String))
                            .mapToObj(i -> new Pair<>(keyList.get(i), String.valueOf(typeList.get(i))))
                            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

                    Pipeline pipeline2 = jedis.pipelined();
                    // 计算长度pipeline
                    keyTypeMap.entrySet().stream().forEach(entry -> {
                        byte[] key = entry.getKey();
                        String type = entry.getValue();
                        if (RedisDataStructureTypeEnum.string.getValue().equals(type)) {
                            pipeline2.strlen(key);
                        } else if (RedisDataStructureTypeEnum.hash.getValue().equals(type)) {
                            pipeline2.hlen(key);
                        } else if (RedisDataStructureTypeEnum.list.getValue().equals(type)) {
                            pipeline2.llen(key);
                        } else if (RedisDataStructureTypeEnum.set.getValue().equals(type)) {
                            pipeline2.scard(key);
                        } else if (RedisDataStructureTypeEnum.zset.getValue().equals(type)) {
                            pipeline2.zcard(key);
                        }
                    });

                    List<Object> lengthList;
                    try {
                        lengthList = pipeline2.syncAndReturnAll();
                    } catch (JedisRedirectionException e) {
                        continue;// ignore
                    }
                    List<InstanceBigKey> instanceBigKeyList = IntStream.range(0, lengthList.size())
                            .filter(i -> (typeList.get(i) instanceof String) && (lengthList.get(i) instanceof Long))
                            .mapToObj(i -> {
                                long length = (long) lengthList.get(i);
                                String type = String.valueOf(typeList.get(i));
                                byte[] key = keyList.get(i);
                                boolean isBigKey = checkIsBigKey(type, length);
                                if (!isBigKey) {
                                    return null;
                                }
                                InstanceBigKey instanceBigKey = new InstanceBigKey();
                                instanceBigKey.setAppId(appId);
                                instanceBigKey.setInstanceId(instanceStats.getInstId());
                                instanceBigKey.setAuditId(auditId);
                                instanceBigKey.setIp(host);
                                instanceBigKey.setPort(port);
                                instanceBigKey.setRole(instanceStats.getRole());
                                instanceBigKey.setBigKey(new String(key));
                                instanceBigKey.setLength(length);
                                instanceBigKey.setType(type);
                                instanceBigKey.setCreateTime(new Date());
                                return instanceBigKey;
                            })
                            .filter(instanceBigKey -> instanceBigKey != null)
                            .collect(Collectors.toList());

                    if (CollectionUtils.isNotEmpty(instanceBigKeyList)) {
                        instanceBigKeyDao.batchSave(instanceBigKeyList);
                    }
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
            logger.info(marker, "{} {}:{} analysis bigkey key successfully, cost time is {} ms, total key is {}", appId,
                    host, port, (System.currentTimeMillis() - startTime), count);
            return TaskFlowStatusEnum.SUCCESS;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error(marker, "redis-cli -h {} -p {} adminauth error", host, port);
            logger.error(marker, "bigkey custinsId {} {}:{} bigkey connect error:" + e.getMessage(), appId, host, port,
                    e);
            return TaskFlowStatusEnum.ABORT;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    /**
     * 按照类型判断是否为bigkey
     *
     * @param type
     * @param length
     * @return
     */
    private boolean checkIsBigKey(String type, long length) {
        if (RedisDataStructureTypeEnum.string.getValue().equals(type)) {
            return length > ConstUtils.STRING_MAX_LENGTH;
        } else if (RedisDataStructureTypeEnum.hash.getValue().equals(type)) {
            return length > ConstUtils.HASH_MAX_LENGTH;
        } else if (RedisDataStructureTypeEnum.list.getValue().equals(type)) {
            return length > ConstUtils.LIST_MAX_LENGTH;
        } else if (RedisDataStructureTypeEnum.set.getValue().equals(type)) {
            return length > ConstUtils.SET_MAX_LENGTH;
        } else if (RedisDataStructureTypeEnum.zset.getValue().equals(type)) {
            return length > ConstUtils.ZSET_MAX_LENGTH;
        }
        return false;
    }

}
