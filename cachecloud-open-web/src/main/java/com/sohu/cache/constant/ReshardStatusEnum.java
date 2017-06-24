package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Reshard状态
 */
public enum ReshardStatusEnum {
    RUNNING(0, "运行中"), 
    FINISH(1, "完成"),
    ERROR(2, "出错");

    private int value;
    private String info;

    private final static Map<Integer, ReshardStatusEnum> MAP = new HashMap<Integer, ReshardStatusEnum>();
    static {
        for (ReshardStatusEnum reshardStatusEnum : ReshardStatusEnum.values()) {
            MAP.put(reshardStatusEnum.getValue(), reshardStatusEnum);
        }
    }

    public static ReshardStatusEnum getReshardStatusEnum(int value) {
        return MAP.get(value);
    }

    private ReshardStatusEnum(int value, String info) {
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