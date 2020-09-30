package com.sohu.cache.web.enums;

/**
 * Created by chenshi on 2019/5/21.
 */
public enum PodStatusEnum {

    OFFLINE(0),ONLINE(1);

    private int value;

    PodStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
