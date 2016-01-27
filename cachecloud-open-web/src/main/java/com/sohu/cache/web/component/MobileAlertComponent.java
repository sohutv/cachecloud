package com.sohu.cache.web.component;


import java.util.List;

/**
 * 短信报警
 * @author leifu
 * @Date 2014年11月26日
 * @Time 上午10:11:26
 */
public interface MobileAlertComponent {
    
    /**
     * 发短信给管理员
     * @param message
     */
    void sendPhoneToAdmin(String message);
    
    /**
     * 发短信给指定号码集合
     * @param message
     * @param phoneList
     */
    void sendPhone(String message, List<String> phoneList);
}
