package com.sohu.tv.cachecloud.client.redis.crossroom.mark;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自动切换主从接口
 * @author leifu
 * @Date 2016年9月20日
 * @Time 上午10:53:01
 */
public interface RedisCrossRoomAutoSwitchInterface {
   
    /**
     * 默认不开启
     */
    public final static AtomicBoolean AUTO_SWITCH_ENABLED = new AtomicBoolean(Boolean.FALSE);

}
