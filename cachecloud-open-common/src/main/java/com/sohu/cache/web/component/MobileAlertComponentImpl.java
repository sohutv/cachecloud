package com.sohu.cache.web.component;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.constant.CacheCloudConstants;

/**
 * 手机短信报警
 * @author leifu
 * @Date 2014年11月26日
 * @Time 上午10:11:26
 */
public class MobileAlertComponentImpl implements MobileAlertComponent {

    private final Logger logger = LoggerFactory.getLogger(MobileAlertComponentImpl.class);
    /**
     * 管理员电话
     */
    private String adminPhones;

    @Override
    public void sendPhoneToAdmin(String message) {
        if (StringUtils.isBlank(message) || StringUtils.isBlank(adminPhones)) {
            logger.error("message is {}, maybe empty or adminPhones is {}, maybe empty", message, adminPhones);
        }
        sendPhone(message, Arrays.asList(adminPhones.split(CacheCloudConstants.COMMA)));
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
