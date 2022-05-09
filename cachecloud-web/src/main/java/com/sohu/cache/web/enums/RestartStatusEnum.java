package com.sohu.cache.web.enums;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/13 16:10
 * @Description:
 */
public enum RestartStatusEnum {
    /**
     * 状态：0等待，1运行，2成功，3失败，4配置修改待重启
     */
    WAITING(0),
    RUNNING(1),
    SUCCESS(2),
    FAIL(3),
    NEED_RESTART(4),
    RESTART_AFTER_CONFIG(5),
    INTERUPT(6);

    private int value;

    RestartStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
