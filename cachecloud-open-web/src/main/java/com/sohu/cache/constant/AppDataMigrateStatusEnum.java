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

    START(0, "开始"),
    END(1, "结束"),
    ERROR(-1, "失败")
    ;

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
