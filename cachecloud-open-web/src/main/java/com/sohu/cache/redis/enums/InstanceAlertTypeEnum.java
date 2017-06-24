package com.sohu.cache.redis.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 实例报警类型枚举
 * @author leifu
 * @Date 2017年6月14日
 * @Time 上午10:22:10
 */
public enum InstanceAlertTypeEnum {
    ALL_ALERT(1, "全局报警"),
    INSTANCE_ALERT(2, "实例报警");

    
    private final static List<InstanceAlertTypeEnum> instanceAlertTypeEnumList = new ArrayList<InstanceAlertTypeEnum>();
    static {
        for (InstanceAlertTypeEnum instanceAlertTypeEnum : InstanceAlertTypeEnum.values()) {
            instanceAlertTypeEnumList.add(instanceAlertTypeEnum);
        }
    }
    
    private int value;
    
    private String info;

    private InstanceAlertTypeEnum(int value, String info) {
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