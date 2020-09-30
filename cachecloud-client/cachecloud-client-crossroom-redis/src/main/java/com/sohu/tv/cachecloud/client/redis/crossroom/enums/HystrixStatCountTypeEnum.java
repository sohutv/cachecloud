package com.sohu.tv.cachecloud.client.redis.crossroom.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * hystrix各种类型访问量计数
 * @author leifu
 * @Date 2016年9月19日
 * @Time 下午4:31:51
 */
public enum HystrixStatCountTypeEnum {
    ALL(1, "all"),
    RUN(2, "run"),
    FALLBACK_ALL(3, "fallback-all"),
    FALLBACK_RUN(4, "fallback-run"),
    FALLBACK_FALLBACK(5, "fallback-fallback");
    
    
    private final static Map<Integer, HystrixStatCountTypeEnum> MAP = new HashMap<Integer, HystrixStatCountTypeEnum>();
    static {
        for (HystrixStatCountTypeEnum hystrixStatCountTypeEnum : HystrixStatCountTypeEnum.values()) {
            MAP.put(hystrixStatCountTypeEnum.getValue(), hystrixStatCountTypeEnum);
        }
    }
    
    public static HystrixStatCountTypeEnum getHystrixStatCountTypeEnum(int type) {
        return MAP.get(type);
    }
    
    private int value;
    
    private String info;
    
    private HystrixStatCountTypeEnum(int value, String info) {
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
