package com.sohu.cache.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户登录状态工具(session实现)
 * 
 * @author leifu
 * @Date 2016年5月12日
 * @Time 下午3:15:14
 */
public class UserLoginStatusUtil {
    private static Logger logger = LoggerFactory.getLogger(UserLoginStatusUtil.class);

    private final static String LOGIN_USER_SESSION_NAME = "CACHE_CLOUD_USER_SESSION";

    public static long getUserIdFromLoginStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object object = session.getAttribute(LOGIN_USER_SESSION_NAME);
        return object == null ? -1 : NumberUtils.toLong(object.toString());
    }

    public static void addLoginStatus(HttpServletRequest request, HttpServletResponse response, String userId) {
        request.getSession().setAttribute(LOGIN_USER_SESSION_NAME, userId);
    }

    public static void removeLoginStatus(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute(LOGIN_USER_SESSION_NAME);
    }

}
