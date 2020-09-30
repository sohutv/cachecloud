package com.sohu.cache.redis.enums;

/**
 * Created by chenshi on 2019/5/14.
 */
public enum DirEnum {

    CONF_DIR(1, "配置文件目录"),
    DATA_DIR(2, "数据文件目录"),
    LOG_DIR(3, "日志文件目录");

    private int value;
    private String info;

    DirEnum(int value, String info) {
        this.value = value;
        this.info = info;
    }

    public int getValue() {
        return value;
    }
}
