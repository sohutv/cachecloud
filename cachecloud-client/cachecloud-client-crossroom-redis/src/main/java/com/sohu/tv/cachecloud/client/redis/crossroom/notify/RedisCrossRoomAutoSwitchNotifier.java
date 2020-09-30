package com.sohu.tv.cachecloud.client.redis.crossroom.notify;

import com.sohu.tv.cachecloud.client.redis.crossroom.entity.RedisCrossRoomTopology;

/**
 * major和minor进行交换通知
 * @author leifu
 * @Date 2016年9月19日
 * @Time 下午2:39:25
 */
public interface RedisCrossRoomAutoSwitchNotifier {
    
    /**
     * 通知业务端
     * @param redisCrossRoomTopology
     */
    boolean notify(RedisCrossRoomTopology redisCrossRoomTopology);
}
