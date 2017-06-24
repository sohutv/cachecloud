package com.sohu.cache.redis.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 实例报警检测周期枚举
 * @author leifu
 * @Date 2017年6月14日
 * @Time 上午10:21:29
 */
public enum InstanceAlertCheckCycleEnum {
    ONE_MINUTE(1, "1分钟"),
    FIVE_MINUTE(2, "5分钟"),
    HALF_HOUR(3, "30分钟"),
    ONE_HOUR(4, "1小时"),
    ONE_DAY(5, "1天"),
    ;
    
    private final static List<InstanceAlertCheckCycleEnum> instanceAlertCheckCycleEnumList = new ArrayList<InstanceAlertCheckCycleEnum>();
    static {
        for (InstanceAlertCheckCycleEnum instanceAlertCheckCycleEnum : InstanceAlertCheckCycleEnum.values()) {
            instanceAlertCheckCycleEnumList.add(instanceAlertCheckCycleEnum);
        }
    }

    private int value;
    
    private String info;

    private InstanceAlertCheckCycleEnum(int value, String info) {
        this.value = value;
        this.info = info;
    }

    public static List<InstanceAlertCheckCycleEnum> getInstanceAlertCheckCycleEnumList() {
        return instanceAlertCheckCycleEnumList;
    }

    public int getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }
}