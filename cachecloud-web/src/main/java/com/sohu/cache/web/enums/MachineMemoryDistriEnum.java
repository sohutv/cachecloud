package com.sohu.cache.web.enums;


import com.sohu.cache.util.NumberUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 机器内存使用或分配区间
 */
public enum MachineMemoryDistriEnum {
    BETWEEN_0_TO_1_PERCENT("0_1", "0-1%", 2),
    BETWEEN_1_TO_10_PERCENT("1_10", "1-10%", 3),
    BETWEEN_10_TO_25_PERCENT("10_25", "10-25%", 4),
    BETWEEN_25_TO_50_PERCENT("25_50", "25-50%", 5),
    BETWEEN_50_TO_75_PERCENT("50_75", "50-75%", 6),
    BETWEEN_75_TO_100_PERCENT("75_90", "75-90%", 7),
    BETWEEN_90_TO_100_PERCENT("90_100", "90-100%", 8),
    BETWEEN_100_TO_1000_PERCENT("100_1000", "100%以上", 9);

    public final static Map<String, MachineMemoryDistriEnum> MAP;

    static {
        Map<String, MachineMemoryDistriEnum> tmpMap = new HashMap<>();
        for (MachineMemoryDistriEnum enumObject : MachineMemoryDistriEnum.values()) {
            tmpMap.put(enumObject.getValue(), enumObject);
        }
        MAP = Collections.unmodifiableMap(tmpMap);
    }

    private String value;
    private String info;
    private int type;

    private MachineMemoryDistriEnum(String value, String info, int type) {
        this.value = value;
        this.info = info;
        this.type = type;
    }

    public static MachineMemoryDistriEnum getByValue(String targetValue) {
        return MAP.get(targetValue);
    }

    /**
     * @param percent
     * @return
     */
    public static MachineMemoryDistriEnum getRightPercentDistri(int percent) {
        MachineMemoryDistriEnum[] enumArr = MachineMemoryDistriEnum.values();
        for (MachineMemoryDistriEnum enumObject : enumArr) {
            if (isInSize(enumObject, percent)) {
                return enumObject;
            }
        }
        return null;
    }

    /**
     * @param enumObject
     * @return
     */
    private static boolean isInSize(MachineMemoryDistriEnum enumObject, long costTime) {
        String value = enumObject.getValue();
        int index = value.indexOf("_");
        int start = NumberUtil.toInt(value.substring(0, index));
        int end = NumberUtil.toInt(value.substring(index + 1));
        if (costTime >= start && costTime < end) {
            return true;
        }
        return false;
    }

    public String getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }

    public int getType() {
        return type;
    }
}
