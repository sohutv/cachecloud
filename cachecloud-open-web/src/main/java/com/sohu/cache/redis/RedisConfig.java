package com.sohu.cache.redis;

import com.sohu.cache.redis.enums.RedisClusterConfigEnum;
import com.sohu.cache.redis.enums.RedisConfigEnum;
import com.sohu.cache.redis.enums.RedisSentinelConfigEnum;

/**
 * redis配置项数据字典
 * Created by yijunzhang on 14-7-27.
 */
public class RedisConfig {

    private String key;

    private String value;

    private String desc;

    public RedisConfig(RedisConfigEnum configEnum, String value) {
        this.key = configEnum.getKey();
        this.value = value;
        this.desc = configEnum.getDesc();
    }

    public RedisConfig(RedisClusterConfigEnum configEnum, String value) {
        this.key = configEnum.getKey();
        this.value = value;
        this.desc = configEnum.getDesc();
    }

    public RedisConfig(RedisSentinelConfigEnum configEnum, String value) {
        this.key = configEnum.getKey();
        this.value = value;
        this.desc = configEnum.getDesc();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public String getKey() {
        return key;
    }

    public static RedisConfig transfer(RedisConfigEnum configEnum) {
        return new RedisConfig(configEnum, configEnum.getValue());
    }
}
