package com.sohu.cache.filter.security;

import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * XSS 请求包装器
 * 拦截获取参数的方法，统一进行 HTML 转义
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value != null) {
            return HtmlUtils.htmlEscape(value);
        }
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            String[] escapedValues = new String[length];
            for (int i = 0; i < length; i++) {
                escapedValues[i] = HtmlUtils.htmlEscape(values[i]);
            }
            return escapedValues;
        }
        return null;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value != null) {
            return HtmlUtils.htmlEscape(value);
        }
        return null;
    }
}