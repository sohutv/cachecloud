package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 迁移状态
 * @author leifu
 * @Date 2016-6-9
 * @Time 下午7:53:28
 */
public enum AppDataMigrateStatusEnum {


    PREPARE(-1,"准备阶段"),
    START(0, "全量同步"),
    FULL_END(3,"增量同步"),
    END(1, "同步结束"),
    ERROR(2, "同步异常");

    private int status;

    private String info;

    private static Map<Integer, AppDataMigrateStatusEnum> MAP = new HashMap<Integer, AppDataMigrateStatusEnum>();
    static {
        for (AppDataMigrateStatusEnum rppDataMigrateStatusEnum : AppDataMigrateStatusEnum.values()) {
            MAP.put(rppDataMigrateStatusEnum.getStatus(), rppDataMigrateStatusEnum);
        }
    }

    public static AppDataMigrateStatusEnum getByStatus(int status) {
        return MAP.get(status);
    }

    private AppDataMigrateStatusEnum(int status, String info) {
        this.status = status;
        this.info = info;
    }

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }


}
