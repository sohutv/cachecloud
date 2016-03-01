package com.sohu.cache.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sohu.cache.constant.AppUserTypeEnum;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.util.ConstUtils;

/**
 * 管理员登录验证
 * @author leifu
 * @Date 2014年10月28日
 * @Time 下午1:35:14
 */
public class ManageUserLoginInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(ManageUserLoginInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(true);
        Object object = session.getAttribute(ConstUtils.LOGIN_USER_SESSION_NAME);
        AppUser user = object == null ? null : (AppUser) object;

        //必须是管理员
        if (user == null || user.getType() != AppUserTypeEnum.ADMIN_USER.value()) {
            String path = session.getServletContext().getContextPath();
            response.sendRedirect(path + "/manage/login");
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