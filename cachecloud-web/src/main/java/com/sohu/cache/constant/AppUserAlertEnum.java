package com.sohu.cache.constant;

/**
 * 用户类型
 * @author leifu
 * @Time 2014年10月21日
 */
public enum AppUserAlertEnum {

    //0:不报警
    NO(0),
    //1:接收报警
    YES(1);

    private Integer value;

    private AppUserAlertEnum(Integer value) {
        this.value = value;
    }

    public Integer value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
