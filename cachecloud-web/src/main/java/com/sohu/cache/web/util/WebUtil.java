package com.sohu.cache.web.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * web相关工具
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
public class WebUtil {

    public static final String LOGIN_USER_STATUS_NAME = "CACHE_CLOUD_USER_STATUS";

    /**
     * 从request中获取客户端ip
     * 
     * @param request
     * @return
     */
    public static String getIp(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        String addr = getHeaderValue(req, "X-Forwarded-For");
        if (StringUtils.isNotEmpty(addr) && addr.contains(",")) {
            addr = addr.split(",")[0];
        }
        if (StringUtils.isEmpty(addr)) {
            addr = getHeaderValue(req, "X-Real-IP");
        }
        if (StringUtils.isEmpty(addr)) {
            addr = req.getRemoteAddr();
        }
        return addr;
    }
    
    /**
     * 获取请求的完整url
     * @param request
     * @return
     */
    public static String getUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if(queryString != null) {
            url += "?" + request.getQueryString();
        }
        return url;
    }
    
    /**
     * 获取ServletRequest header value
     * @param request
     * @param name
     * @return
     */
    public static String getHeaderValue(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        if(v == null) {
            return null;
        }
        return v.trim();
    }
    
    /**
     * 从request属性中获取对象
     * @param request
     * @return
     */
    public static void setEmailAttribute(ServletRequest request, String email) {
        request.setAttribute("email", email);
    }
    
    /**
     * 从request属性中获取对象
     * @param request
     * @return
     */
    public static String getEmailAttribute(ServletRequest request) {
        Object email = request.getAttribute("email");
        if(email == null) {
            return null;
        }
        return email.toString();
    }
    
    /**
     * 从request属性中获取对象
     * @param request
     * @return
     */
    public static void setAttribute(ServletRequest request, String name, Object obj) {
        request.setAttribute(name, obj);
    }
    
    /**
     * 设置对象到request属性中
     * @param request
     * @return
     */
    public static Object getAttribute(ServletRequest request, String name) {
        return request.getAttribute(name);
    }
    
    /**
     * 输出内容到页面
     * @param response
     * @param result
     * @throws IOException
     */
    public static void print(HttpServletResponse response, String result) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(result);
        out.flush();
        out.close();
        out = null;
    }
    
    /**
     * 获取登录的cookie的值
     * 
     * @param request
     * @return
     */
    public static String getLoginCookieValue(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, LOGIN_USER_STATUS_NAME);
        if(cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
    
    /**
     * 获取登录的cookie
     * 
     * @param request
     * @return
     */
    public static Cookie getLoginCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, LOGIN_USER_STATUS_NAME);
    }

    /**
     * 设置登录的cookie
     */
    public static void setLoginCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(LOGIN_USER_STATUS_NAME, value);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 移除登录的cookie
     */
    public static void deleteLoginCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(LOGIN_USER_STATUS_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    
    /**
     * 跳转
     * @param response
     * @param request
     * @param path
     * @throws IOException 
     */
    public static void redirect(HttpServletResponse response, HttpServletRequest request, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }
}
