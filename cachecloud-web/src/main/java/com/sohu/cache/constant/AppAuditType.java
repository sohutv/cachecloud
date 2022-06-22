package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yijunzhang on 14-10-20.
 */
public enum AppAuditType {
    APP_AUDIT(0, "申请集群"),
    APP_SCALE(1, "集群容量变更"),
    APP_MODIFY_CONFIG(2, "集群修改配置"),
    REGISTER_USER_APPLY(3, "用户注册"),
    INSTANCE_MODIFY_CONFIG(4, "实例修改配置"),
    APP_MONITOR_CONFIG(5, "全局报警配置修改"),
    KEY_ANALYSIS(6, "键值分析"),
    FLUSHALL_DATA(7, "清理数据"),
    APP_DIAGNOSTIC(8, "应用诊断"),
    APP_OFFLINE(10, "应用下线"),
    APP_MIGRATE(11, "应用数据迁移"),
    APP_IMPORT(12, "应用导入"),
    SCAN_CLEAN(13, "数据分析清理");

    private final static Map<Integer, AppAuditType> MAP = new HashMap<Integer, AppAuditType>();

    static {
        for (AppAuditType appAuditType : AppAuditType.values()) {
            MAP.put(appAuditType.getValue(), appAuditType);
        }
    }

    public static AppAuditType getAppAuditType(int value) {
        return MAP.get(value);
    }

    private int value;

    private String info;

    private AppAuditType(int value, String info) {
        this.value = value;
        this.info = info;
    }

    public int getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }
}
