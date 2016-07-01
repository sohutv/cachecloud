package com.sohu.cache.web.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.util.HttpRequestUtil;

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
    private String adminPhones = ConstUtils.PHONES;

    @Override
    public void sendPhoneToAdmin(String message) {
        if (StringUtils.isBlank(message) || StringUtils.isBlank(adminPhones)) {
            logger.error("message is {}, maybe empty or adminPhones is {}, maybe empty", message, adminPhones);
        }
        sendPhone(message, Arrays.asList(adminPhones.split(ConstUtils.COMMA)));
    }

    @Override
    public void sendPhone(String message, List<String> phoneList) {
        String alertUrl = ConstUtils.MOBILE_ALERT_INTERFACE;
        if (StringUtils.isBlank(alertUrl)) {
            logger.error("mobileAlertInterface url is empty!");
            return;
        }
        if (StringUtils.isBlank(message) || phoneList == null || phoneList.isEmpty()) {
            logger.error("message is {}, phoneList is {} both maybe empty!", message, phoneList);
            return;
        }
        String charSet = "UTF-8";
        String phone = StringUtils.join(phoneList, ConstUtils.COMMA);
        Map<String, String> postMap = new HashMap<String, String>();
        postMap.put("msg", message);
        postMap.put("phone", phone);
        String responseStr = HttpRequestUtil.doPost(alertUrl, postMap, charSet);
        if (StringUtils.isBlank(responseStr)) {
            logger.error("发送短信失败 : url:{}", alertUrl);
        }
        logger.warn("send Done!");
    }

    public void setAdminPhones(String adminPhones) {
        this.adminPhones = adminPhones;
    }
    
}
