package com.sohu.cache.web.druid;

import com.alibaba.druid.support.http.StatViewServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns="/druid/*",
    initParams={
        //@WebInitParam(name="allow",value="10.7.37.38/24"),// IP白名单(没有配置或者为空，则允许所有访问)
        //@WebInitParam(name="deny",value="10.2.40.90/24"),// IP黑名单 (存在共同时，deny优先于allow)
        @WebInitParam(name="loginUsername",value="admin"),// 用户名
        @WebInitParam(name="loginPassword",value="admin"),// 密码
        @WebInitParam(name="resetEnable",value="false")// 禁用HTML页面上的“Reset All”功能
})
public class DruidStatViewServlet extends StatViewServlet {

    private static final long serialVersionUID = -2688872071445249539L;

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected String process(String url) {
        return super.process(url);
    }
}
