package com.sohu.cache.web.enums;

/**
* @Description:    机器部署类型
* @Author:         caoru
* @CreateDate:     2018/10/8 18:32
*/
public enum UseTypeEnum {

    Machine_special(0),
    Machine_test(1),
    Machine_mix(2);

    private int value;

    private UseTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
