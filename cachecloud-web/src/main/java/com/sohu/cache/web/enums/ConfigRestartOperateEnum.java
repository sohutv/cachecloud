package com.sohu.cache.web.enums;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/11/16 16:10
 * @Description: 配置、重启操作类型
 */
public enum ConfigRestartOperateEnum {
    /**
     * 操作类型（0:滚动重启，1:修改配置强制重启；2：修改配置）
     */
    RESTART(0),
    CONFIG_RESTART(1),
    CONFIG(2);

    private int value;

    ConfigRestartOperateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
