package com.sohu.cache.task.constant;

/**
 * Created by chenshi on 2020/7/7.
 */
public enum PushEnum {

    NO(0, "未推送"),
    YES(1, "已推送"),
    NO_WITH_MODIFY(2, "未推送，有新修改"),
    YES_WITH_MODIFY(3, "已推送，有新修改"),
    COMPILEING(4,"编译中");

    private int value;

    private String desc;

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    PushEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
