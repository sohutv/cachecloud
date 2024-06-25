package com.sohu.cache.redis.enums;

import com.sohu.cache.util.ConstUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 实例报警类型枚举
 * @author leifu
 * @Date 2017年6月14日
 * @Time 上午10:22:10
 */
public enum AppTypeToAlertTypeEnum {
    REDIS_ALERT(0, ConstUtils.REDIS);

    private final static List<AppTypeToAlertTypeEnum> appTypeToAlertTypeEnumList = new ArrayList<>();
    static {
        for (AppTypeToAlertTypeEnum instanceAlertTypeEnum : AppTypeToAlertTypeEnum.values()) {
            appTypeToAlertTypeEnumList.add(instanceAlertTypeEnum);
        }
    }

    /**
     * 报警应用类型
     */
    private int type;

    /**
     * 报警应用描述
     */
    private String info;

    private AppTypeToAlertTypeEnum(int type, String info) {
        this.type = type;
        this.info = info;
    }

    public int getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }

    public static Optional<AppTypeToAlertTypeEnum> getAppTypeToAlertTypeEnum(String appType) {
        return appTypeToAlertTypeEnumList.stream().filter(appTypeToAlertTypeEnum -> appTypeToAlertTypeEnum.getInfo().equals(appType)).findFirst();
    }

}