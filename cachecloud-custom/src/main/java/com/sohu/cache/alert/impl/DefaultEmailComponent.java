package com.sohu.cache.alert.impl;


import com.sohu.cache.alert.EmailComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 邮件报警组件默认空实现
 * Created by yijunzhang
 */
public class DefaultEmailComponent implements EmailComponent {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean sendMail(String title, String content, List<String> emailList, List<String> ccList) {
        logger.warn("Please implement the sendMail logic.");
        return true;
    }

    @Override
    public boolean sendMail(String title, String content, List<String> emailList) {
        logger.warn("Please implement the sendMail logic.");
        return true;
    }

    @Override
    public boolean sendMailToAdmin(String title, String content) {
        logger.warn("Please implement the sendMailToAdmin logic.");
        return true;
    }

    @Override
    public String getAdminEmail() {
        logger.warn("Please implement the getAdminEmail logic.");
        return "";
    }
}
