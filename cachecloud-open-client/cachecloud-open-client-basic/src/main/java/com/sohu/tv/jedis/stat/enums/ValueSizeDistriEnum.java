package com.sohu.tv.jedis.stat.enums;

import java.util.HashMap;
import java.util.Map;

import com.sohu.tv.jedis.stat.utils.NumberUtil;

/**
 * 值分布
 * 
 * @author leifu
 * @Time 2014年7月21日
 */
public enum ValueSizeDistriEnum {
    // 单位字节
    BETWEEN_MIN_TO_0_BYTE("-2147483648_0", "非法", 0),
    BETWEEN_0_TO_50_BYTE("0_50", "0-0.05k", 1),
    BETWEEN_50_TO_100_BYTE("50_100", "0.05k-0.1k", 2),
    BETWEEN_100_TO_200_BYTE("100_200", "0.1k-0.2k", 3),
    BETWEEN_200_TO_500_BYTE("200_500", "0.2k-0.5k", 4),
    BETWEEN_500_TO_1024_BYTE("500_1024", "0.5k-1k", 5),
    BETWEEN_1024_TO_2048_BYTE("1024_2048", "1-2k", 6),
    BETWEEN_2048_TO_5120_BYTE("2048_5120", "2-5k", 7),
    BETWEEN_5120_TO_10240_BYTE("5120_10240", "5-10k", 8),
    BETWEEN_10240_TO_20480_BYTE("10240_20480", "10-20k", 9),
    BETWEEN_20480_TO_51200_BYTE("20480_51200", "20-50k", 10),
    BETWEEN_51200_TO_102400_BYTE("51200_102400", "50-100k", 11),
    BETWEEN_102400_TO_204800_BYTE("102400_204800", "100-200k", 12),
    BETWEEN_204800_TO_512000_BYTE("204800_512000", "200-500k", 13),
    BETWEEN_512000_TO_MAX_BYTE("512000_2147483647", "500k以上", 14);

    private String value;
    private String info;
    private int type;

    private ValueSizeDistriEnum(String value, String info, int type) {
        this.value = value;
        this.info = info;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }
    
    public final static Map<String, ValueSizeDistriEnum> VALUE_MAP = new HashMap<String, ValueSizeDistriEnum>();
    static {
        for (ValueSizeDistriEnum enumObject : ValueSizeDistriEnum.values()) {
            VALUE_MAP.put(enumObject.getValue(), enumObject);
        }
    }
    
    public static ValueSizeDistriEnum getByValue(String targetValue){
        return VALUE_MAP.get(targetValue);
    }
    
    public final static Map<Integer, ValueSizeDistriEnum> TYPE_MAP = new HashMap<Integer, ValueSizeDistriEnum>();
    static {
        for (ValueSizeDistriEnum enumObject : ValueSizeDistriEnum.values()) {
            TYPE_MAP.put(enumObject.getType(), enumObject);
        }
    }
    
    public static ValueSizeDistriEnum getByType(int targetType){
        return TYPE_MAP.get(targetType);
    }

    /**
     * 查看length在哪个区间
     * 
     * @param length
     * @return
     */
    public static ValueSizeDistriEnum getRightSizeBetween(int size) {
        ValueSizeDistriEnum[] enumArr = ValueSizeDistriEnum.values();
        for (ValueSizeDistriEnum enumObject : enumArr) {
            if (isInSize(enumObject, size)) {
                return enumObject;
            }
        }
        return null;
    }

    /**
     * 确定length在指定区间
     * 
     * @param enumObject
     * @param size
     * @return
     */
    private static boolean isInSize(ValueSizeDistriEnum enumObject, int size) {
        String value = enumObject.getValue();
        int index = value.indexOf("_");
        int start = NumberUtil.toInt(value.substring(0, index));
        int end = NumberUtil.toInt(value.substring(index + 1));
        if (size >= start && size < end) {
            return true;
        }
        return false;
    }

}
