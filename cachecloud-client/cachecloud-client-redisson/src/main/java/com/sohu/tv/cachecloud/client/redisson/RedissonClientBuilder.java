package com.sohu.tv.cachecloud.client.redisson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yijunzhang
 */
public class RedissonClientBuilder {

    private final static Logger logger = LoggerFactory.getLogger(RedissonClientBuilder.class);

    private static volatile Map<Long, Object> builderMap = new HashMap<>();

    private static final Lock LOCK = new ReentrantLock();

    /**
     * 创建RedisClusterClientBuilder对象
     *
     * @param appId
     * @param password
     * @return
     */
    public static RedissonClusterClientBuilder redisCluster(long appId, String password) {
        RedissonClusterClientBuilder redissonClusterClientBuilder = (RedissonClusterClientBuilder) builderMap
                .get(appId);
        if (redissonClusterClientBuilder == null) {
            LOCK.lock();
            try {
                redissonClusterClientBuilder = (RedissonClusterClientBuilder) builderMap.get(appId);
                if (redissonClusterClientBuilder == null) {
                    redissonClusterClientBuilder = new RedissonClusterClientBuilder(appId, password);
                    builderMap.put(appId, redissonClusterClientBuilder);
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return redissonClusterClientBuilder;
    }

    /**
     * 创建RedisClusterClientBuilder对象
     *
     * @param appId
     * @param password
     * @return
     */
    public static RedissonSentinelClientBuilder redisSentinel(long appId, String password) {
        RedissonSentinelClientBuilder redissonSentinelClientBuilder = (RedissonSentinelClientBuilder) builderMap
                .get(appId);
        if (redissonSentinelClientBuilder == null) {
            LOCK.lock();
            try {
                redissonSentinelClientBuilder = (RedissonSentinelClientBuilder) builderMap.get(appId);
                if (redissonSentinelClientBuilder == null) {
                    redissonSentinelClientBuilder = new RedissonSentinelClientBuilder(appId, password);
                    builderMap.put(appId, redissonSentinelClientBuilder);
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return redissonSentinelClientBuilder;
    }

    public static RedissonStandaloneClientBuilder redisStandalone(long appId, String password) {
        RedissonStandaloneClientBuilder redisStandaloneClientBuilder = (RedissonStandaloneClientBuilder) builderMap
                .get(appId);
        if (redisStandaloneClientBuilder == null) {
            LOCK.lock();
            try {
                redisStandaloneClientBuilder = (RedissonStandaloneClientBuilder) builderMap.get(appId);
                if (redisStandaloneClientBuilder == null) {
                    redisStandaloneClientBuilder = new RedissonStandaloneClientBuilder(appId, password);
                    builderMap.put(appId, redisStandaloneClientBuilder);
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return redisStandaloneClientBuilder;
    }

}
