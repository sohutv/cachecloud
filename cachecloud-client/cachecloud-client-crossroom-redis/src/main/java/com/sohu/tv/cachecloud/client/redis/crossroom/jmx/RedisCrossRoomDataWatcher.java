package com.sohu.tv.cachecloud.client.redis.crossroom.jmx;

import com.sohu.tv.cachecloud.client.redis.crossroom.enums.HystrixStatCountTypeEnum;
import com.sohu.tv.cachecloud.client.redis.crossroom.stat.RedisCrossRoomClientStatusCollector;
import com.sohu.tv.cachecloud.client.redis.crossroom.stat.RedisCrossRoomHystrixStat;
import com.sohu.tv.cc.client.spectator.util.AtomicLongMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 跨机房Redis jmx数据
 * 
 * @author leifu
 * @Date 2016年4月26日
 * @Time 上午10:55:31
 */
public class RedisCrossRoomDataWatcher implements RedisCrossRoomDataWatcherMBean {

    @Override
    public Map<String, Long> getRedisHystrixCommandStat() {
        Map<String, Long> map = RedisCrossRoomHystrixStat.COUNT_MAP.asMap();
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> resultMap = new HashMap<String, Long>();
        for (Entry<String, Long> entry : map.entrySet()) {
            resultMap.put(entry.getKey(), entry.getValue());
        }
        return resultMap;
    }

    @Override
    public Map<String, Long> getRedisHystrixFallbackStat() {
        Map<String, Long> map = RedisCrossRoomHystrixStat.FALLBACK_COUNT_MAP.asMap();
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> resultMap = new HashMap<String, Long>();
        for (Entry<String, Long> entry : map.entrySet()) {
            resultMap.put(entry.getKey(), entry.getValue());
        }
        return resultMap;
    }

    @Override
    public Map<String, Map<String, Long>> getRecentReadStat() {
        Map<String, AtomicLongMap<Integer>> map = RedisCrossRoomClientStatusCollector.getRecentMinuteData(5);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, Long>> result = new HashMap<String, Map<String, Long>>();
        for (Entry<String, AtomicLongMap<Integer>> entry : map.entrySet()) {
            String key = entry.getKey();
            Map<String, Long> tempMap = new HashMap<String, Long>();
            for (Entry<Integer, Long> typeCountEntry : entry.getValue().asMap().entrySet()) {
                int type = typeCountEntry.getKey();
                HystrixStatCountTypeEnum hystrixStatCountTypeEnum = HystrixStatCountTypeEnum.getHystrixStatCountTypeEnum(type);
                if (hystrixStatCountTypeEnum == null) {
                    continue;
                }
                tempMap.put(hystrixStatCountTypeEnum.getInfo(), typeCountEntry.getValue());
            }
            result.put(key, tempMap);
        }
        return result;
    }

}
