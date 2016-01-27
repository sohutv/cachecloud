package com.sohu.cache.entity;

import com.sohu.cache.web.enums.AdminEnum;
import com.sohu.cache.web.enums.LoginEnum;

/**
 * 登录结果
 * 
 * @author leifu
 * @Time 2014年10月16日
 */
public class LoginResult {
    /**
     * 登录验证结果
     */
    private LoginEnum loginEnum;

    /**
     * 是否是管理员
     */
    private AdminEnum adminEnum;

    public LoginResult(LoginEnum loginEnum, AdminEnum adminEnum) {
        this.loginEnum = loginEnum;
        this.adminEnum = adminEnum;
    }

    public LoginResult() {
    }

    public LoginEnum getLoginEnum() {
        return loginEnum;
    }

    public void setLoginEnum(LoginEnum loginEnum) {
        this.loginEnum = loginEnum;
    }

    public AdminEnum getAdminEnum() {
        return adminEnum;
    }

    public void setAdminEnum(AdminEnum adminEnum) {
        this.adminEnum = adminEnum;
    }

}
