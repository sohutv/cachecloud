package com.sohu.cache.task.constant;

import com.sohu.cache.util.NumberUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ilde time时间范围
 *
 * @author fulei
 */
public enum IdleTimeDistriEnum {
    BETWEEN_MIN_TO_0_HOURS("-2147483648_0", "非法", 1),
    BETWEEN_0_TO_1_HOURS("0_1", "0-1小时", 2),
    BETWEEN_1_TO_2_HOURS("1_2", "1-2小时", 3),
    BETWEEN_2_TO_5_HOURS("2_5", "2-5小时", 4),
    BETWEEN_5_TO_10_HOURS("5_10", "5-10小时", 5),
    BETWEEN_10_TO_24_HOURS("10_24", "10-24小时", 6),
    BETWEEN_24_TO_120_HOURS("24_120", "1-5天", 7),
    BETWEEN_120_TO_240_HOURS("120_240", "5-10天", 8),
    BETWEEN_240_TO_720_HOURS("240_720", "10-30天", 9),
    BETWEEN_720_TO_MAX_HOURS("720_2147483647", "30天以上", 10);

    public static final Map<String, IdleTimeDistriEnum> MAP;

    static {
        Map<String, IdleTimeDistriEnum> tmpMap = new HashMap<>();
        for (IdleTimeDistriEnum enumObject : IdleTimeDistriEnum.values()) {
            tmpMap.put(enumObject.getValue(), enumObject);
        }
        MAP = Collections.unmodifiableMap(tmpMap);
    }

    private String value;
    private String info;
    private int type;

    private IdleTimeDistriEnum(String value, String info, int type) {
        this.value = value;
        this.info = info;
        this.type = type;
    }

    public static IdleTimeDistriEnum getByValue(String targetValue) {
        return MAP.get(targetValue);
    }

    /**
     * 查看Idle在哪个区间
     *
     * @param costTime
     * @return
     */
    public static IdleTimeDistriEnum getRightIdleDistri(long costTime) {
        IdleTimeDistriEnum[] enumArr = IdleTimeDistriEnum.values();
        for (IdleTimeDistriEnum enumObject : enumArr) {
            if (isInSize(enumObject, costTime)) {
                return enumObject;
            }
        }
        return null;
    }

    /**
     * 确定length在指定区间
     *
     * @param enumObject
     * @param costTime
     * @return
     */
    private static boolean isInSize(IdleTimeDistriEnum enumObject, long costTime) {
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
