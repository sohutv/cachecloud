package com.sohu.cache.web.enums;

/**
 * 管理员
 * 
 * @author leifu
 * @Time 2014年10月16日
 */
public enum AdminEnum {
    IS_ADMIN(1), // 是管理员
    NOT_ADMIN(0); // 不是管理员

    int value;

    private AdminEnum(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
