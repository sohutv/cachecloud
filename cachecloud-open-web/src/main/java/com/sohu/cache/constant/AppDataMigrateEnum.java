package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis迁移类型枚举
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午3:02:50
 */
public enum AppDataMigrateEnum {
    REDIS_NODE(0, "single"),
    REDIS_CLUSTER_NODE(1, "redis cluster"),
    RDB_FILE(2, "rdb file"),
    TWEMPROXY(3, "twemproxy"),
    AOF_FILE(4, "aof file");

    private int index;

    private String type;

    private static Map<Integer, AppDataMigrateEnum> MAP = new HashMap<Integer, AppDataMigrateEnum>();
    static {
        for (AppDataMigrateEnum redisMigrateEnum : AppDataMigrateEnum.values()) {
            MAP.put(redisMigrateEnum.getIndex(), redisMigrateEnum);
        }
    }

    public static AppDataMigrateEnum getByIndex(int index) {
        return MAP.get(index);
    }

    private AppDataMigrateEnum(int index, String type) {
        this.index = index;
        this.type = type;
    }
    
    public static boolean isFileType(AppDataMigrateEnum appDataMigrateEnum) {
        if (RDB_FILE.equals(appDataMigrateEnum) || AOF_FILE.equals(appDataMigrateEnum)) {
            return true;
        }
        return false;
    }

    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

}
