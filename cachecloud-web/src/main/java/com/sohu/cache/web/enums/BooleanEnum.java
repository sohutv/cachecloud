package com.sohu.cache.web.enums;

/**
 * Created by rucao on 2020/1/17
 */
public enum BooleanEnum {
    OTHER(-1),
    FALSE(0),
    TRUE(1);

    int value;

    BooleanEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
