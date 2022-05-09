package com.sohu.cache.web.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/27 14:40
 * @Description: 比较类型-redis配置项检测
 */
public enum CompareTypeEnum {

    EQUAL(1, "等于"),
    NOT_EQUAL(2, "不等于"),
    LESS_THAN(3, "小于"),
    MORE_THAN(4, "大于");

    private int type;//比较类型

    private String info;//比较类型说明

    private static Map<Integer, CompareTypeEnum> MAP = new HashMap<Integer, CompareTypeEnum>();

    static {
        for (CompareTypeEnum compareTypeEnum : CompareTypeEnum.values()) {
            MAP.put(compareTypeEnum.getType(), compareTypeEnum);
        }
    }

    CompareTypeEnum(int type, String info) {
        this.type = type;
        this.info = info;
    }

    public static CompareTypeEnum getByType(int type) {
        return MAP.get(type);
    }

    public int getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }
}
