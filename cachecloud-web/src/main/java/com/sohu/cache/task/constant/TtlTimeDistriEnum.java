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
public enum TtlTimeDistriEnum {
    BETWEEN_PERSIST_HOURS("-1_-1", "不过期", 1),
    BETWEEN_0_TO_1_HOURS("0_1", "0-1小时", 2),
    BETWEEN_1_TO_2_HOURS("1_2", "1-2小时", 3),
    BETWEEN_2_TO_5_HOURS("2_5", "2-5小时", 4),
    BETWEEN_5_TO_10_HOURS("5_10", "5-10小时", 5),
    BETWEEN_10_TO_24_HOURS("10_24", "10-24小时", 6),
    BETWEEN_24_TO_120_HOURS("24_120", "1-5天", 7),
    BETWEEN_120_TO_240_HOURS("120_240", "5-10天", 8),
    BETWEEN_240_TO_720_HOURS("240_720", "10-30天", 9),
    BETWEEN_720_TO_MAX_HOURS("720_2147483647", "30天以上", 10),
    ;

    public final static Map<String, TtlTimeDistriEnum> MAP;

    static {
        Map<String, TtlTimeDistriEnum> tmpMap = new HashMap<>();
        for (TtlTimeDistriEnum enumObject : TtlTimeDistriEnum.values()) {
            tmpMap.put(enumObject.getValue(), enumObject);
        }
        MAP = Collections.unmodifiableMap(tmpMap);
    }

    private String value;
    private String info;
    private int type;

    private TtlTimeDistriEnum(String value, String info, int type) {
        this.value = value;
        this.info = info;
        this.type = type;
    }

    public static TtlTimeDistriEnum getByValue(String targetValue) {
        return MAP.get(targetValue);
    }

    /**
     * 计算ttl所属于的区间
     *
     * @return
     */
    public static TtlTimeDistriEnum getRightTtlDistri(long ttl) {
        TtlTimeDistriEnum[] enumArr = TtlTimeDistriEnum.values();
        for (TtlTimeDistriEnum enumObject : enumArr) {
            if (isInSize(enumObject, ttl)) {
                return enumObject;
            }
        }
        return null;
    }

    /**
     * @param enumObject
     * @param costTime
     * @return
     */
    private static boolean isInSize(TtlTimeDistriEnum enumObject, long costTime) {
        String value = enumObject.getValue();
        int index = value.indexOf("_");
        int start = NumberUtil.toInt(value.substring(0, index));
        int end = NumberUtil.toInt(value.substring(index + 1));
        if (costTime >= start && costTime < end) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(getRightTtlDistri(-2));
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
