package com.sohu.cache.redis.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实例报警比较枚举
 * @author leifu
 * @Date 2017年6月14日
 * @Time 上午10:20:47
 */
public enum InstanceAlertCompareTypeEnum {
    LESS_THAN(1, "小于"),
    EQUAL(2, "等于"),
    MORE_THAN(3, "大于"),
    NOT_EQUAL(4, "不等于");
    
    private final static List<InstanceAlertCompareTypeEnum> instanceAlertCompareTypeEnumList = new ArrayList<InstanceAlertCompareTypeEnum>();
    static {
        for (InstanceAlertCompareTypeEnum instanceAlertCompareTypeEnum : InstanceAlertCompareTypeEnum.values()) {
            instanceAlertCompareTypeEnumList.add(instanceAlertCompareTypeEnum);
        }
    }
    
    private final static Map<Integer, InstanceAlertCompareTypeEnum> instanceAlertCompareTypeEnumMap = new HashMap<Integer, InstanceAlertCompareTypeEnum>();
    static {
        for (InstanceAlertCompareTypeEnum instanceAlertCompareTypeEnum : InstanceAlertCompareTypeEnum.values()) {
            instanceAlertCompareTypeEnumMap.put(instanceAlertCompareTypeEnum.getValue(), instanceAlertCompareTypeEnum);
        }
    }
    
    private int value;
    
    private String info;

    private InstanceAlertCompareTypeEnum(int value, String info) {
        this.value = value;
        this.info = info;
    }

    public static List<InstanceAlertCompareTypeEnum> getInstanceAlertCompareTypeEnumList() {
        return instanceAlertCompareTypeEnumList;
    }
    
    public static InstanceAlertCompareTypeEnum getInstanceAlertCompareTypeEnum(int value) {
        return instanceAlertCompareTypeEnumMap.get(value);
    }

    public int getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }
}