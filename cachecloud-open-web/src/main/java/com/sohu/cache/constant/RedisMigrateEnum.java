package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis迁移类型枚举
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午3:02:50
 */
public enum RedisMigrateEnum {
    REDIS_NODE(0, "single"),
    REDIS_CLUSTER_NODE(1, "redis cluster"),
    RDB_FILE(2, "rdb file"),
    TWEMPROXY(3, "twemproxy");

    private int index;

    private String type;

    private static Map<Integer, RedisMigrateEnum> MAP = new HashMap<Integer, RedisMigrateEnum>();
    static {
        for (RedisMigrateEnum redisMigrateEnum : RedisMigrateEnum.values()) {
            MAP.put(redisMigrateEnum.getIndex(), redisMigrateEnum);
        }
    }

    public static RedisMigrateEnum getByStatus(int status) {
        return MAP.get(status);
    }

    private RedisMigrateEnum(int index, String type) {
        this.index = index;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

}
