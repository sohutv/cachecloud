package com.sohu.cache.web.service.impl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;

import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.service.UserLoginStatusService;

/**
 * cookie保护登录状态
 * @author leifu
 * @Date 2016年6月15日
 * @Time 下午1:31:17
 */
public class UserLoginStatusCookieServiceImpl implements UserLoginStatusService {

    @Override
    public long getUserIdFromLoginStatus(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String cookiesId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (LOGIN_USER_STATUS_NAME.equals(cookie.getName())) {
                    cookiesId = cookie.getValue();
                }
            }
        }
        return NumberUtils.toLong(cookiesId, -1);
    }

    @Override
    public void addLoginStatus(HttpServletRequest request, HttpServletResponse response, String userId) {
        Cookie cookie = new Cookie(LOGIN_USER_STATUS_NAME, userId);
        cookie.setDomain(ConstUtils.COOKIE_DOMAIN);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }

    @Override
    public void removeLoginStatus(HttpServletRequest request, HttpServletResponse response) {
        addLoginStatus(request, response, "");
    }

    
    
}
