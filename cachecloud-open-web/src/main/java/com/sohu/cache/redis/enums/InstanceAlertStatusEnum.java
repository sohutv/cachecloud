package com.sohu.cache.redis.enums;

/**
 * 实例报警有效性枚举
 * @author leifu
 * @Date 2017年6月14日
 * @Time 上午10:22:39
 */
public enum InstanceAlertStatusEnum {
    YES(1, "有效"),
    NO(0, "无效");

    private int value;
    
    private String info;

    private InstanceAlertStatusEnum(int value, String info) {
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