package com.sohu.cache.entity;

import com.sohu.cache.web.enums.AdminEnum;
import com.sohu.cache.web.enums.LoginEnum;
import lombok.Data;

/**
 * 登录结果
 *
 * @author leifu
 */
@Data
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

}
