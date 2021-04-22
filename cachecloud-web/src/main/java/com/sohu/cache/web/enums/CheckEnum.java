package com.sohu.cache.web.enums;

/**
 * Created by chenshi on 2021/1/12.
 */
public enum CheckEnum {

    //正常
    CONSISTENCE(1),
    //异常
    INCONSISTENCE(2),
    //
    EXCEPTION(3);

    private int value;

    CheckEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
