package com.sohu.cache.web.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户登录状态信息服务
 * 
 * @author leifu
 * @Date 2016年6月15日
 * @Time 下午1:24:09
 */
public interface UserLoginStatusService {
    public final static String LOGIN_USER_STATUS_NAME = "CACHE_CLOUD_USER_STATUS";

    /**
     * 获取用户登录状态信息
     * 
     * @param request
     * @return
     */
    long getUserIdFromLoginStatus(HttpServletRequest request);

    /**
     * 添加用户登录状态信息
     * 
     * @param request
     * @param response
     * @param userId
     */
    void addLoginStatus(HttpServletRequest request, HttpServletResponse response, String userId);

    /**
     * 移除用户登录状态信息
     * 
     * @param request
     * @param response
     */
    void removeLoginStatus(HttpServletRequest request, HttpServletResponse response);
}
