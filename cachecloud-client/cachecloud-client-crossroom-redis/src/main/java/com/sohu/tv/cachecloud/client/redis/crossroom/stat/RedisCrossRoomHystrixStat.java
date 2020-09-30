package com.sohu.tv.cachecloud.client.redis.crossroom.stat;

import com.google.common.util.concurrent.AtomicLongMap;

/**
 * @author leifu
 * @Date 2016年4月26日
 * @Time 上午10:45:06
 */
public class RedisCrossRoomHystrixStat {

    public static final AtomicLongMap<String> FALLBACK_COUNT_MAP = AtomicLongMap.create();

    public static final AtomicLongMap<String> COUNT_MAP = AtomicLongMap.create();

    public static void counterFallBack(String key) {
        FALLBACK_COUNT_MAP.incrementAndGet(key);
    }

    public static void counter(String key) {
        COUNT_MAP.incrementAndGet(key);
    }
}
