package com.sohu.cache.web.enums;

/**
 * Created by chenshi on 2018/8/23.
 */
public enum RedisVersionEnum {

    Redis3_0_7(1),
//    Redis3_2(2),
//    Redis4_0(3);
    Not_bind(0),
    Is_bind(1),
    Redis_installed(1),
    Redis_uninstalled(0),
    Redis_installException(-1);


    private int value;

    private RedisVersionEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
