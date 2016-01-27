package com.sohu.tv.builder;

/**
 * cachecloud-redis客户端builder
 * @author leifu
 * @Date 2015年2月5日
 * @Time 下午12:11:26
 */
public class ClientBuilder {

    /**
     * 构造redis cluster的builder
     *
     * @param appId
     * @return
     */
    public static RedisClusterBuilder redisCluster(final long appId) {
        return new RedisClusterBuilder(appId);
    }

    /**
     * 构造redis sentinel的builder
     *
     * @param appId
     * @return
     */
    public static RedisSentinelBuilder redisSentinel(final long appId) {
        return new RedisSentinelBuilder(appId);
    }

    /**
     * 构造redis standalone的builder
     * @param appId
     * @return
     */
    public static RedisStandaloneBuilder redisStandalone(final long appId) {
        return new RedisStandaloneBuilder(appId);
    }
}
