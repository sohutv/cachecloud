package com.sohu.cache.web.enums;

/**
 * Created by chenshi on 2021/3/8.
 */
public enum ModuleEnum {

    BLOOMFILTER_NAME("bf"), REDISSEARCH_NAME("search"),REDISMODULEOSS_NAME("search"),
    BLOOMFILTER_SO("redisbloom.so"),REDISSEARCH_SO("redisearch.so"),MODULE_OSS_SO("module-oss.so");

    private String value;

    ModuleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
