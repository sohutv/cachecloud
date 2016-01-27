package com.sohu.cache.constant;

/**
 * 用户类型
 * @author leifu
 * @Time 2014年10月21日
 */
public enum AppUserTypeEnum {

    //管理员
    ADMIN_USER(0),
    //普通用户
    REGULAR_USER(2),
    //不存在用户
    NO_USER(-1);
    
    private Integer value;

    private AppUserTypeEnum(Integer value) {
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
