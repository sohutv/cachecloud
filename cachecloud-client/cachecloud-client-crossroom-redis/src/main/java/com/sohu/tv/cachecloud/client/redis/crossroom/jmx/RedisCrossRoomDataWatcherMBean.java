package com.sohu.tv.cachecloud.client.redis.crossroom.jmx;

import java.util.Map;

/**
 * 跨机房Redis jmx数据
 * 
 * @author leifu
 * @Date 2016年4月26日
 * @Time 上午10:55:31
 */
public interface RedisCrossRoomDataWatcherMBean {

    public Map<String, Long> getRedisHystrixCommandStat();

    public Map<String, Long> getRedisHystrixFallbackStat();

    public Map<String, Map<String,Long>> getRecentReadStat();
    
}
