package com.sohu.tv.cachecloud.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yijunzhang on 2018-12-18
 */
public class LettuceClientBuilder {

    private final static Logger logger = LoggerFactory.getLogger(LettuceClientBuilder.class);

    private static volatile Map<Long, Object> builderMap = new HashMap<>();

    private static final Lock LOCK = new ReentrantLock();

    /**
     * 创建RedisClusterClientBuilder对象
     *
     * @param appId
     * @param password
     * @return
     */
    public static RedisClusterClientBuilder redisCluster(long appId, String password) {
        RedisClusterClientBuilder redisClusterClientBuilder = (RedisClusterClientBuilder) builderMap.get(appId);
        if (redisClusterClientBuilder == null) {
            LOCK.lock();
            try {
                redisClusterClientBuilder = (RedisClusterClientBuilder) builderMap.get(appId);
                if (redisClusterClientBuilder == null) {
                    redisClusterClientBuilder = new RedisClusterClientBuilder(appId, password);
                    builderMap.put(appId, redisClusterClientBuilder);
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return redisClusterClientBuilder;
    }

    /**
     * 创建RedisSentinelClientBuilder对象
     *
     * @param appId
     * @param password
     * @return
     */
    public static RedisSentinelClientBuilder redisSentinel(long appId, String password) {
        RedisSentinelClientBuilder redisSentinelClientBuilder = (RedisSentinelClientBuilder) builderMap.get(appId);
        if (redisSentinelClientBuilder == null) {
            LOCK.lock();
            try {
                redisSentinelClientBuilder = (RedisSentinelClientBuilder) builderMap.get(appId);
                if (redisSentinelClientBuilder == null) {
                    redisSentinelClientBuilder = new RedisSentinelClientBuilder(appId, password);
                    builderMap.put(appId, redisSentinelClientBuilder);
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return redisSentinelClientBuilder;
    }

    public static RedisStandaloneClientBuilder redisStandalone(long appId, String password) {
        RedisStandaloneClientBuilder redisStandaloneClientBuilder = (RedisStandaloneClientBuilder) builderMap.get(appId);
        if (redisStandaloneClientBuilder == null) {
            LOCK.lock();
            try {
                redisStandaloneClientBuilder = (RedisStandaloneClientBuilder) builderMap.get(appId);
                if (redisStandaloneClientBuilder == null) {
                    redisStandaloneClientBuilder = new RedisStandaloneClientBuilder(appId, password);
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
