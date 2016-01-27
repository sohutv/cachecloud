package com.sohu.cache.web.enums;

/**
 * 登录状态
 * 
 * @author leifu
 * @Time 2014年10月16日
 */
public enum LoginEnum {
    LOGIN_SUCCESS(1), // 成功
    LOGIN_WRONG_USER_OR_PASSWORD(0), // 用户名或者密码错误
    LOGIN_USER_NOT_EXIST(-1), // 不是cachecloud用户
    LOGIN_NOT_ADMIN(-2);// 不是超级管理员

    int value;

    private LoginEnum(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}