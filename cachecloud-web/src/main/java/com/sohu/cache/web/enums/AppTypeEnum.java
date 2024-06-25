package com.sohu.cache.web.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用类型
 */
public enum AppTypeEnum {
    TWEMPROXY(1, "twemproxy"),
    REDIS_CLUSTER(2, "Redis-Cluster"),
    CODIS(3, "codis"),
    PIKA(4, "pika"),
    REDIS_SENTINEL(5, "Redis-Sentinel"),
    REDIS_STANDALONE(6, "Redis-Standalone"),
    MEMCACHED(7, "memcached"),
    PIKA_SENTINEL(8, "pika-sentinel");

    private int type;

    private String info;

    private static Map<Integer, AppTypeEnum> MAP = new HashMap<Integer, AppTypeEnum>();

    static {
        for (AppTypeEnum appTypeEnum : AppTypeEnum.values()) {
            MAP.put(appTypeEnum.getType(), appTypeEnum);
        }
    }

    AppTypeEnum(int type, String info) {
        this.type = type;
        this.info = info;
    }

    public static AppTypeEnum getByType(int type) {
        return MAP.get(type);
    }

    public int getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }

}