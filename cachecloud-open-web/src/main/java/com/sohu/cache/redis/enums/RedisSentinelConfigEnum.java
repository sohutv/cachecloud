package com.sohu.cache.redis.enums;

/**
 * Created by yijunzhang on 14-8-25.
 */
public enum RedisSentinelConfigEnum {
    PORT("port", "%d", "sentinel实例端口"),
    DIR("dir", "/tmp", "文件目录"),
    MONITOR("sentinel monitor", "%s %s %d %d", "master名称定义和最少参与监控的sentinel数,格式:masterName ip port num"),
    DOWN_AFTER_MILLISECONDS("sentinel down-after-milliseconds", "%s 20000", "Sentinel判定服务器断线的毫秒数,默认:20秒"),
    FAILOVER_TIMEOUT("sentinel failover-timeout", "%s 180000", "故障迁移超时时间,默认:3分钟"),
    PARALLEL_SYNCS("sentinel parallel-syncs", "%s 1", "在执行故障转移时,最多有多少个从服务器同时对新的主服务器进行同步,默认:1");

    private String key;

    private String value;

    private String desc;

    RedisSentinelConfigEnum(String key, String value, String desc) {
        this.key = key;
        this.value = value;
        this.desc = desc;
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

    public static RedisSentinelConfigEnum get(String key) {
        if (key == null) {
            return null;
        }
        for (RedisSentinelConfigEnum config : RedisSentinelConfigEnum.values()) {
            if (config.key.equals(key)) {
                return config;
            }
        }
        return null;
    }


}
