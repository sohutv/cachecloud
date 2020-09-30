package com.sohu.tv.cachecloud.client.redis.crossroom.builder;

import redis.clients.jedis.PipelineCluster;

/**
 * 跨机房客户端builder
 * 
 * @author leifu
 * @Date 2016年9月21日
 * @Time 上午10:44:51
 */
public class RedisCrossRoomClientBuilder {

    /**
     * redis cluster
     * 
     * @param majorAppId
     * @return
     */
    public static RedisClusterCrossRoomClientBuilder redisCluster(long majorAppId, PipelineCluster majorPipelineCluster,
            long minorAppId, PipelineCluster minorPipelineCluster) {
        return new RedisClusterCrossRoomClientBuilder(majorAppId, majorPipelineCluster, minorAppId, minorPipelineCluster);
    }

}
