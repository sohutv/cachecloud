package com.sohu.cache.login.impl;

import com.sohu.cache.login.LoginComponent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by yijunzhang
 */
public class DefaultLoginComponent implements LoginComponent {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean passportCheck(String userName, String password) {
        //todo
        return true;
    }

        @Override
    public String getEmail(String ticket) {
        return null;
    }

    @Override
    public String getRedirectUrl(HttpServletRequest request) {
        StringBuffer redirectUrl = new StringBuffer();
        redirectUrl.append(request.getSession(true).getServletContext().getContextPath());
        redirectUrl.append("/manage/login?");
        // 跳转地址
        redirectUrl.append("redirectUrl");
        redirectUrl.append("=");
        redirectUrl.append(request.getRequestURI());
        // 跳转参数
        String query = request.getQueryString();
        if (StringUtils.isNotBlank(query)) {
            redirectUrl.append("?");
            try {
                redirectUrl.append(URLEncoder.encode(request.getQueryString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return redirectUrl.toString();
    }

    @Override
    public String getLogoutUrl() {
        return null;
    }
}
