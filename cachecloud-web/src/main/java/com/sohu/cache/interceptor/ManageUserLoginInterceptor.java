package com.sohu.cache.interceptor;

import com.sohu.cache.constant.AppUserTypeEnum;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.web.service.UserLoginStatusService;
import com.sohu.cache.web.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理端登录验证
 * @author leifu
 */
public class ManageUserLoginInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(ManageUserLoginInterceptor.class);
    @Autowired
    private UserService userService;
    @Autowired
    private UserLoginStatusService userLoginStatusService;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        String userName = userLoginStatusService.getUserNameFromLoginStatus(request);
        //未登录
        if (StringUtils.isBlank(userName)) {
            String redirectUrl = userLoginStatusService.getRedirectUrl(request);
            response.sendRedirect(redirectUrl);
            return false;
        }
        AppUser user = userService.getByName(userName);
        //新用户
        if (user == null || user.getType() == -1) {
            String redirectUrl = userLoginStatusService.getRegisterUrl(user);
            response.sendRedirect(redirectUrl);
            return false;
        }
        //必须是管理员
        if (user.getType() != AppUserTypeEnum.ADMIN_USER.value()) {
            String redirectUrl = userLoginStatusService.getRedirectUrl(request);
            response.sendRedirect(redirectUrl);
            return false;
        }
        request.setAttribute("userInfo", user);
        request.setAttribute("uri", request.getRequestURI());
        return true;

    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}