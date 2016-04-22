package com.sohu.tv.jedis.stat.enums;

import java.util.HashMap;
import java.util.Map;

import com.sohu.tv.jedis.stat.utils.NumberUtil;

/**
 * 耗时分布
 * 
 * @author leifu
 * @Date 2015年1月13日
 * @Time 下午5:22:28
 */
public enum CostTimeDistriEnum {
    // 单位字节
    BETWEEN_MIN_TO_0_MS("-2147483648_0", "非法", 1),
    BETWEEN_0_TO_1_MS("0_1", "0-1毫秒", 2),
    BETWEEN_1_TO_2_MS("1_2", "1-2毫秒", 3),
    BETWEEN_2_TO_5_MS("2_5", "2-5毫秒", 4),
    BETWEEN_5_TO_10_MS("5_10", "5-10毫秒", 5),
    BETWEEN_10_TO_50_MS("10_50", "10-50毫秒", 6),
    BETWEEN_50_TO_100_MS("50_100", "50-100毫秒", 7),
    BETWEEN_100_TO_MAX_MS("100_MAX", "100毫秒以上", 8);

    private String value;
    private String info;
    private int type;

    private CostTimeDistriEnum(String value, String info, int type) {
        this.value = value;
        this.info = info;
        this.type = type;
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

    public final static Map<String, CostTimeDistriEnum> MAP = new HashMap<String, CostTimeDistriEnum>();
    static {
        for (CostTimeDistriEnum enumObject : CostTimeDistriEnum.values()) {
            MAP.put(enumObject.getValue(), enumObject);
        }
    }
    
    public static CostTimeDistriEnum getByValue(String targetValue){
        return MAP.get(targetValue);
    }

    /**
     * 查看length在哪个区间
     * 
     * @param length
     * @return
     */
    public static CostTimeDistriEnum getRightCostDistri(int costTime) {
        CostTimeDistriEnum[] enumArr = CostTimeDistriEnum.values();
        for (CostTimeDistriEnum enumObject : enumArr) {
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
     * @param size
     * @return
     */
    private static boolean isInSize(CostTimeDistriEnum enumObject, int costTime) {
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
        // 获取活在字节区间
        // CostTimeDistriEnum a = getRightCostDistri(6);
    }

}
