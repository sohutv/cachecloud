package com.sohu.cache.task.constant;

public enum RedisDataStructureTypeEnum {
    string("string"),
    hash("hash"),
    list("list"),
    set("set"),
    zset("zset");

    private String value;

    private RedisDataStructureTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
