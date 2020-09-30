package com.sohu.cache.constant;

/**
 * 日志类型
 * @author leifu
 */
public enum AppAuditLogTypeEnum {
    // 申请应用
    APP_DESC_APPLY(1),
    // 扩容申请
    APP_SCALE_APPLY(2),
    // 应用配置修改申请
    APP_CONFIG_APPLY(3),
    // 审批
    APP_CHECK(4),
    // 修改报警阀值
    APP_CHANGE_ALERT(5),
    // 清理数据
    APP_CLEAN_DATA(6),
    // 实例配置修改申请
    INSTANCE_CONFIG_APPLY(7),
    // 键值分析
    KEY_VALUE_ANALYSIS(8),
    // flush全部数据
    FLUSHALL_DATA(9),
    // 应用诊断申请
    APP_DIAGNOSTIC_APPLY(10);

    private int value;
    

    private AppAuditLogTypeEnum(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
