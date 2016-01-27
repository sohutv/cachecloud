package com.sohu.tv.cachecloud.client.basic.component;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.cachecloud.client.basic.util.StringUtil;

/**
 * 手机短信报警
 * @author leifu
 * @Date 2015年2月2日
 * @Time 上午11:36:01
 */
public class MobileAlertComponentImpl implements MobileAlertComponent {

    private final Logger logger = LoggerFactory.getLogger(MobileAlertComponentImpl.class);
    /**
     * 管理员电话
     */
    private String adminPhones;

    private final static String COMMA = ",";
    

    @Override
    public void sendPhoneToAdmin(String message) {
        if (StringUtil.isBlank(message) || StringUtil.isBlank(adminPhones)) {
            logger.error("message is {}, maybe empty or adminPhones is {}, maybe empty", message, adminPhones);
        }
        sendPhone(message, Arrays.asList(adminPhones.split(COMMA)));
    }

    @Override
    public void sendPhone(String message, List<String> phoneList) {
        /**
         * your company send short message codes
         */
    }

    public void setAdminPhones(String adminPhones) {
        this.adminPhones = adminPhones;
    }
    
}
