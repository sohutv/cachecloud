package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用发布状态
 * 
 * @author leifu
 * @Time 2014年6月28日
 */
public enum AppStatusEnum {
    STATUS_INITIALIZE(0, "未分配"),
    STATUS_ALLOCATED(1, "已申请未审批"),
    STATUS_PUBLISHED(2, "运行中"),
    STATUS_OFFLINE(3, "已下线"),
    STATUS_DENY(4, "驳回");

    private int status;

    private String info;

    private static Map<Integer, AppStatusEnum> MAP = new HashMap<Integer, AppStatusEnum>();
    static {
        for (AppStatusEnum appStatusEnum : AppStatusEnum.values()) {
            MAP.put(appStatusEnum.getStatus(), appStatusEnum);
        }
    }

    private AppStatusEnum(int status, String info) {
        this.status = status;
        this.info = info;
    }

    public static AppStatusEnum getByStatus(int status) {
        return MAP.get(status);
    }

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

}
