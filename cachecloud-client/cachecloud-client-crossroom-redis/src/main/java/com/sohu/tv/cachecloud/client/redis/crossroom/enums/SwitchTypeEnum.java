package com.sohu.tv.cachecloud.client.redis.crossroom.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 切换类型
 * @author leifu
 * @Date 2016年9月26日
 * @Time 下午5:18:19
 */
public enum SwitchTypeEnum {
    AUTO(1, "auto-switch"),
    MANUAL(2, "manual-switch");
    
    
    private final static Map<Integer, SwitchTypeEnum> MAP = new HashMap<Integer, SwitchTypeEnum>();
    static {
        for (SwitchTypeEnum switchTypeEnum : SwitchTypeEnum.values()) {
            MAP.put(switchTypeEnum.getValue(), switchTypeEnum);
        }
    }
    
    public static SwitchTypeEnum getSwitchTypeEnum(int type) {
        return MAP.get(type);
    }
    
    private int value;
    
    private String info;
    
    private SwitchTypeEnum(int value, String info) {
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
