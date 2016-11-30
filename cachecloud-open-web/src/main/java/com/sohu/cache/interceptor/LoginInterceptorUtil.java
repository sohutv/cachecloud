package com.sohu.cache.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.sohu.cache.util.ConstUtils;

/**
 * @author leifu
 * @Date 2016年11月23日
 * @Time 下午3:23:54
 */
public class LoginInterceptorUtil {

    /**
     * 获取登录跳转地址
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static String getLoginRedirectUrl(HttpServletRequest request) throws Exception {
        StringBuffer redirectUrl = new StringBuffer();
        redirectUrl.append(request.getSession(true).getServletContext().getContextPath());
        redirectUrl.append("/manage/login?");
        // 跳转地址
        redirectUrl.append(ConstUtils.RREDIRECT_URL_PARAM);
        redirectUrl.append("=");
        redirectUrl.append(request.getRequestURI());
        // 跳转参数
        String query = request.getQueryString();
        if (StringUtils.isNotBlank(query)) {
            redirectUrl.append("?");
            redirectUrl.append(java.net.URLEncoder.encode(request.getQueryString(), "UTF-8"));
        }
        return redirectUrl.toString();
    }

}
