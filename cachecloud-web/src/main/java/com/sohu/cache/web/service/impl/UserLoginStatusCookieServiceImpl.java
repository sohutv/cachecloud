package com.sohu.cache.web.service.impl;

import com.sohu.cache.entity.AppUser;
import com.sohu.cache.login.LoginComponent;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.EnvUtil;
import com.sohu.cache.web.service.UserLoginStatusService;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.IpUtil;
import com.sohu.cache.web.util.WebUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie保护登录状态
 *
 * @author leifu
 */
@Service("userLoginStatusService")
public class UserLoginStatusCookieServiceImpl implements UserLoginStatusService {

    @Autowired
    private LoginComponent loginComponent;

    @Autowired
    protected UserService userService;

    @Autowired
    private Environment environment;

    @Override
    public String getUserNameFromLoginStatus(HttpServletRequest request) {
        if (EnvUtil.isLocal(environment)) {
            //todo for local
            return "admin";
        }
        String userName = WebUtil.getLoginCookieValue(request);
        if (StringUtils.isNotBlank(userName)) {
            return userName;
        }
        return null;
    }

    @Override
    public String getUserNameFromTicket(HttpServletRequest request) {
        String ticket = request.getParameter("ticket");
        if (StringUtils.isNotBlank(ticket)) {
            String email = loginComponent.getEmail(ticket);
            if (StringUtils.isNotBlank(email) && email.contains("@")) {
                String userName = email.substring(0, email.indexOf("@"));
                return userName;
            }
        }
        return null;
    }

    @Override
    public String getRedirectUrl(HttpServletRequest request) {
        return loginComponent.getRedirectUrl(request);
    }

    @Override
    public String getLogoutUrl() {
        return loginComponent.getLogoutUrl();
    }

    @Override
    public String getRegisterUrl(AppUser user) {
        if (user != null && user.getType() == -1) {
            return "/user/register?success=1";
        }
        return "/user/register";
    }

    @Override
    public void addLoginStatus(HttpServletRequest request, HttpServletResponse response, String userName) {
        Cookie cookie = new Cookie(LOGIN_USER_STATUS_NAME, userName);
        cookie.setDomain(request.getServerName());
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }

    @Override
    public void removeLoginStatus(HttpServletRequest request, HttpServletResponse response) {
        addLoginStatus(request, response, "");
    }
}
