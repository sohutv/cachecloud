package com.sohu.cache.web.service;

import com.sohu.cache.entity.AppUser;

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
    String getUserNameFromLoginStatus(HttpServletRequest request);

    /**
     * 解析ticket
     *
     * @param request
     * @return
     */
    String getUserNameFromTicket(HttpServletRequest request);

    /**
     * 获取登录跳转地址
     *
     * @param request
     * @return
     * @throws Exception
     */
    String getRedirectUrl(HttpServletRequest request);

    /**
     * 获取注销登录地址
     *
     * @return
     * @throws Exception
     */
    String getLogoutUrl();

    /**
     * 获取新用户注册地址
     *
     * @return
     * @throws Exception
     */
    String getRegisterUrl(AppUser user);

    /**
     * 添加用户登录状态信息
     * 
     * @param request
     * @param response
     * @param userName
     */
    void addLoginStatus(HttpServletRequest request, HttpServletResponse response, String userName);

    /**
     * 移除用户登录状态信息
     * 
     * @param request
     * @param response
     */
    void removeLoginStatus(HttpServletRequest request, HttpServletResponse response);
}
