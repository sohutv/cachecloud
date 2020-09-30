package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

/**
 * 时间维度枚举
 * 
 * @author leifu
 * @Date 2016年8月1日
 * @Time 下午3:50:38
 */
public enum TimeDimensionalityEnum {
    MINUTE(0, "以分钟为维度"),
    HOUR(1, "以小时为维度");

    private int index;

    private String info;

    private static final Map<Integer, TimeDimensionalityEnum> MAP = new HashMap<Integer, TimeDimensionalityEnum>();
    static {
        for (TimeDimensionalityEnum timeDimensionalityEnum : TimeDimensionalityEnum.values()) {
            MAP.put(timeDimensionalityEnum.getIndex(), timeDimensionalityEnum);
        }
    }

    private TimeDimensionalityEnum(int index, String info) {
        this.index = index;
        this.info = info;
    }

    public int getIndex() {
        return index;
    }

    public String getInfo() {
        return info;
    }

    public static TimeDimensionalityEnum getTimeDimensionalityEnumByIndex(String index) {
        return MAP.get(NumberUtils.toInt(index));
    }

}
