package com.sohu.cache.login.impl;

import com.sohu.cache.login.LoginComponent;
import com.sohu.cache.utils.EnvCustomUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yijunzhang
 */
public class DefaultLoginComponent implements LoginComponent {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value(value = "${server.port:8080}")
    private String serverPort;

    private static final String RELATIVE_URL = "/manage/loginCheck";

    /**
     * it is open for change
     * @param userName
     * @param password
     * @return
     */
    @Override
    public boolean passportCheck(String userName, String password) {
        //default password login check
        if(EnvCustomUtil.pwdswitch){
            String url = getUrl() + RELATIVE_URL;
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("name", userName);
            requestMap.put("password", password);
            Map<String, Object> map = restTemplate.postForObject(url, requestMap, Map.class);
            if(map != null && map.get("status") != null && Integer.parseInt(map.get("status").toString()) == 200){
                return true;
            }
            return false;
        }
        //todo need to implement by your own business
        return true;
    }

    private String getUrl() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }
        if(address != null){
            return "http://" + address.getHostAddress() + ":" + this.serverPort;
        }
        return "http://127.0.0.1:" + this.serverPort;
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
