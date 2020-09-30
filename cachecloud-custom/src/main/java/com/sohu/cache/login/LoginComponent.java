package com.sohu.cache.login;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yijunzhang
 */
public interface LoginComponent {

    /**
     * 检测登录状态
     *
     * @param userName
     * @param password
     * @return
     */
    boolean passportCheck(String userName, String password);

    /**
     * 根据ticket获取email
     * @param ticket
     * @return
     */
    String getEmail(String ticket);

    /**
     * 获取登录跳转地址
     *
     * @param request
     * @return
     * @throws Exception
     */
    String getRedirectUrl(HttpServletRequest request);

    /**
     * SSO logout url
     *
     * @return
     * @throws Exception
     */
    String getLogoutUrl();
}
