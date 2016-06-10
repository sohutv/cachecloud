package com.sohu.cache.constant;

/**
 * Redis-Migrate-Tool常量
 * @author leifu
 * @Date 2016-6-10
 * @Time 上午9:23:30
 */
public enum RedisMigrateToolConstant {
    Stats("Stats"),
    Keyspace("Group"),
    Clients("Clients"),
    Memory("Memory"),
    Server("Server");

    private String value;

    RedisMigrateToolConstant(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static RedisMigrateToolConstant value(String input) {
        RedisMigrateToolConstant[] constants = RedisMigrateToolConstant.values();
        for (RedisMigrateToolConstant constant : constants) {
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
