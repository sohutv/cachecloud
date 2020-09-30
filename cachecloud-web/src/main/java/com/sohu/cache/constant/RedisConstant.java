package com.sohu.cache.constant;

/**
 * redis 相关常量
 * Created by yijunzhang on 14-6-10.
 */
public enum RedisConstant {
    Stats("Stats"),
    Keyspace("Keyspace"),
    Commandstats("Commandstats"),
    Replication("Replication"),
    Clients("Clients"),
    CPU("CPU"),
    Memory("Memory"),
    Server("Server"),
    Persistence("Persistence"),
    CollectTime("CollectTime"),
    DIFF("diff");

    private String value;

    RedisConstant(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static RedisConstant value(String input) {
        RedisConstant[] constants = RedisConstant.values();
        for (RedisConstant constant : constants) {
            if (constant.value.equals(input)) {
                return constant;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

}
