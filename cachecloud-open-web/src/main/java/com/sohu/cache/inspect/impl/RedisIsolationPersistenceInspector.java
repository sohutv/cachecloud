package com.sohu.cache.inspect.impl;

import com.sohu.cache.alert.impl.BaseAlertService;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.inspect.InspectParamEnum;
import com.sohu.cache.inspect.Inspector;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.enums.RedisInfoEnum;
import com.sohu.cache.util.IdempotentConfirmer;
import com.sohu.cache.util.TypeUtil;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yijunzhang on 15-1-30.
 */
public class RedisIsolationPersistenceInspector extends BaseAlertService implements Inspector {
    
    public static final int REDIS_DEFAULT_TIME = 5000;
    
    private RedisCenter redisCenter;

    @Override
    public boolean inspect(Map<InspectParamEnum, Object> paramMap) {
        final String host = MapUtils.getString(paramMap, InspectParamEnum.SPLIT_KEY);
        List<InstanceInfo> list = (List<InstanceInfo>) paramMap.get(InspectParamEnum.INSTANCE_LIST);
        outer:
        for (InstanceInfo info : list) {
            final int port = info.getPort();
            final int type = info.getType();
            final long appId = info.getAppId();
            int status = info.getStatus();
            //非正常节点
            if (status != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                continue;
            }
            if (TypeUtil.isRedisDataType(type)) {
                Jedis jedis = redisCenter.getJedis(appId, host, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
                try {
                    Map<String, String> persistenceMap = parseMap(jedis);
                    if (persistenceMap.isEmpty()) {
                        logger.error("{}:{} get persistenceMap failed", host, port);
                        continue;
                    }
                    if (!isAofEnabled(persistenceMap)) {
                        continue;
                    }
                    long aofCurrentSize = MapUtils.getLongValue(persistenceMap, RedisInfoEnum.aof_current_size.getValue());
                    long aofBaseSize = MapUtils.getLongValue(persistenceMap, RedisInfoEnum.aof_base_size.getValue());
                    //阀值大于60%
                    long aofThresholdSize = (long) (aofBaseSize * 1.6);
                    double percentage = getPercentage(aofCurrentSize, aofBaseSize);
                    if (aofCurrentSize >= aofThresholdSize
                            //大于64Mb
                            && aofCurrentSize > (64 * 1024 * 1024)) {
                        //bgRewriteAof
                        boolean isInvoke = invokeBgRewriteAof(jedis);
                        if (!isInvoke) {
                            logger.error("{}:{} invokeBgRewriteAof failed", host, port);
                            continue;
                        } else {
                            logger.warn("{}:{} invokeBgRewriteAof started percentage={}", host, port, percentage);
                        }
                        while (true) {
                            try {
                                //before wait 1s
                                TimeUnit.SECONDS.sleep(1);
                                Map<String, String> loopMap = parseMap(jedis);
                                Integer aofRewriteInProgress = MapUtils.getInteger(loopMap, "aof_rewrite_in_progress", null);
                                if (aofRewriteInProgress == null) {
                                    logger.error("loop watch:{}:{} return failed", host, port);
                                    break;
                                } else if (aofRewriteInProgress <= 0) {
                                    //bgrewriteaof Done
                                    logger.warn("{}:{} bgrewriteaof Done lastSize:{}Mb,currentSize:{}Mb", host, port, getMb(aofCurrentSize), getMb(MapUtils.getLongValue(loopMap, "aof_current_size")));
                                    break;
                                } else {
                                    //wait 1s
                                    TimeUnit.SECONDS.sleep(1);
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    } else {
                        if (percentage > 50D) {
                            long currentSize = getMb(aofCurrentSize);
                            logger.info("checked {}:{} aof increase percentage:{}% currentSize:{}Mb", host, port, percentage, currentSize > 0 ? currentSize : "<1");
                        }
                    }
                } finally {
                    jedis.close();
                }
            }
        }
        return true;
    }

    private long getMb(long bytes) {
        return (long) (bytes / 1024 / 1024);
    }

    private boolean isAofEnabled(Map<String, String> infoMap) {
        Integer aofEnabled = MapUtils.getInteger(infoMap, "aof_enabled", null);
        return aofEnabled != null && aofEnabled == 1;
    }

    private double getPercentage(long aofCurrentSize, long aofBaseSize) {
        if (aofBaseSize == 0) {
            return 0.0D;
        }
        String format = String.format("%.2f", (Double.valueOf(aofCurrentSize - aofBaseSize) * 100 / aofBaseSize));
        return Double.parseDouble(format);
    }

    private Map<String, String> parseMap(final Jedis jedis) {
        final StringBuilder builder = new StringBuilder();
        boolean isInfo = new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String persistenceInfo = null;
                try {
                    persistenceInfo = jedis.info("Persistence");
                } catch (Exception e) {
                    logger.warn(e.getMessage() + "-{}:{}", jedis.getClient().getHost(), jedis.getClient().getPort(), e.getMessage());
                }
                boolean isOk = StringUtils.isNotBlank(persistenceInfo);
                if (isOk) {
                    builder.append(persistenceInfo);
                }
                return isOk;
            }
        }.run();
        if (!isInfo) {
            logger.error("{}:{} info Persistence failed", jedis.getClient().getHost(), jedis.getClient().getPort());
            return Collections.emptyMap();
        }
        String persistenceInfo = builder.toString();
        if (StringUtils.isBlank(persistenceInfo)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<String, String>();
        String[] array = persistenceInfo.split("\r\n");
        for (String line : array) {
            String[] cells = line.split(":");
            if (cells.length > 1) {
                map.put(cells[0], cells[1]);
            }
        }

        return map;
    }

    public boolean invokeBgRewriteAof(final Jedis jedis) {
        return new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                try {
                    String response = jedis.bgrewriteaof();
                    if (response != null && response.contains("rewriting started")) {
                        return true;
                    }
                } catch (Exception e) {
                    String message = e.getMessage();
                    if (message.contains("rewriting already")) {
                        return true;
                    }
                    logger.error(message, e);
                }
                return false;
            }
        }.run();
    }

	public void setRedisCenter(RedisCenter redisCenter) {
		this.redisCenter = redisCenter;
	}

}
