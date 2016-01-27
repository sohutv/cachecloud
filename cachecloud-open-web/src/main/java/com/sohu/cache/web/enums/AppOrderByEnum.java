package com.sohu.cache.web.enums;

/**
 * app列表排序
 * @author leifu
 * @Date 2014年11月14日
 * @Time 上午10:55:47
 */
public enum AppOrderByEnum {
    HIT_PERCENTAGE_HIGH_TO_LOW("hit_percentage_high_to_low"),
    HIT_PERCENTAGE_LOW_TO_HIGH("hit_percentage_low_to_high");
    
    private String value;

    private AppOrderByEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
}
