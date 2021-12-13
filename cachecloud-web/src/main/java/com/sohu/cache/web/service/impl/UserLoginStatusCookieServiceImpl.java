package com.sohu.cache.web.service.impl;

import com.sohu.cache.entity.AppUser;
import com.sohu.cache.login.LoginComponent;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.EnvUtil;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.service.UserLoginStatusService;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.AESCoder;
import com.sohu.cache.web.util.WebUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Random;

/**
 * cookie保护登录状态
 *
 * @author leifu
 */
@Service("userLoginStatusService")
public class UserLoginStatusCookieServiceImpl implements UserLoginStatusService {

    private Logger logger = LoggerFactory.getLogger(UserLoginStatusCookieServiceImpl.class);

    @Autowired
    private LoginComponent loginComponent;

    @Autowired
    protected UserService userService;

    @Autowired
    private Environment environment;

    private static final String CONCATE_STR = "&&";

    @Override
    public String getUserNameFromLoginStatus(HttpServletRequest request) {
        if (EnvUtil.isLocal(environment)) {
            //todo for local
            return "admin";
        }
        String userName = null;
        String userCookie = null;
        String cookie = WebUtil.getLoginCookieValue(request);
        try {
            userCookie = AESCoder.decrypt(cookie, ConstUtils.USER_LOGIN_ENCRY_KEY);
        } catch (Exception e) {
            logger.error("getUserNameFromLoginStatus decrypt error: ", e);
        }
        if(StringUtil.isBlank(userCookie)){
            return null;
        }
        String[] userInfos = userCookie.split(CONCATE_STR);
        if(userInfos != null && userInfos.length > 0){
            userName = userInfos[0];
        }
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
        if(userName == null){
            userName = "";
        }else{
            userName = userName + CONCATE_STR + getRandomStr4Encrypt();
            try {
                userName = AESCoder.encrypt(userName,  ConstUtils.USER_LOGIN_ENCRY_KEY);
            } catch (Exception e) {
                logger.error("addLoginStatus encrypt error: ",e);
            }
        }
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

    private String getRandomStr4Encrypt(){
        return String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
    }
}
