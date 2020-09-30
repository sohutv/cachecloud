package com.sohu.tv.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * cachecloud-redis客户端builder
 *
 * @author leifu
 */
public class ClientBuilder {

    private final static Logger logger = LoggerFactory.getLogger(ClientBuilder.class);

    private static volatile Map<Long, Object> builderMap = new HashMap<>();
    private static final Lock LOCK = new ReentrantLock();

    /**
     * 构造redis cluster的builder
     *
     * @param appId
     * @return
     */
    public static RedisClusterBuilder redisCluster(final long appId) {
        RedisClusterBuilder redisClusterBuilder = (RedisClusterBuilder) builderMap.get(appId);
        if (redisClusterBuilder == null) {
            LOCK.lock();
            try {
                redisClusterBuilder = (RedisClusterBuilder) builderMap.get(appId);
                if (redisClusterBuilder == null) {
                    redisClusterBuilder = new RedisClusterBuilder(appId);
                    builderMap.put(appId, redisClusterBuilder);
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return redisClusterBuilder;
    }

    /**
     * 构造redis sentinel的builder
     *
     * @param appId
     * @return
     */
    public static RedisSentinelBuilder redisSentinel(final long appId) {
        RedisSentinelBuilder redisSentinelBuilder = (RedisSentinelBuilder) builderMap.get(appId);
        if (redisSentinelBuilder == null) {
            LOCK.lock();
            try {
                redisSentinelBuilder = (RedisSentinelBuilder) builderMap.get(appId);
                if (redisSentinelBuilder == null) {
                    redisSentinelBuilder = new RedisSentinelBuilder(appId);
                    builderMap.put(appId, redisSentinelBuilder);
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return redisSentinelBuilder;
    }

    /**
     * 构造redis standalone的builder
     *
     * @param appId
     * @return
     */
    public static RedisStandaloneBuilder redisStandalone(final long appId) {
        RedisStandaloneBuilder redisStandaloneBuilder = (RedisStandaloneBuilder) builderMap.get(appId);
        if (redisStandaloneBuilder == null) {
            LOCK.lock();
            try {
                redisStandaloneBuilder = (RedisStandaloneBuilder) builderMap.get(appId);
                if (redisStandaloneBuilder == null) {
                    redisStandaloneBuilder = new RedisStandaloneBuilder(appId);
                    builderMap.put(appId, redisStandaloneBuilder);
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return redisStandaloneBuilder;
    }
}
