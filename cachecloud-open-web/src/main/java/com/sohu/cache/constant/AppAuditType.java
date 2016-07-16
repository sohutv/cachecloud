package com.sohu.cache.constant;

/**
 * Created by yijunzhang on 14-10-20.
 */
public enum AppAuditType {

    APP_AUDIT(0),
    APP_SCALE(1),
    APP_MODIFY_CONFIG(2),
    REGISTER_USER_APPLY(3),
    INSTANCE_MODIFY_CONFIG(4);

    private int value;

    AppAuditType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
