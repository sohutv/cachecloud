package com.sohu.cache.web.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.util.ConstUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 邮件服务
 * 
 * @author leifu
 * @Date 2015-6-2
 * @Time 上午10:56:35
 */
public class EmailComponentImpl implements EmailComponent {
    private final Logger logger = LoggerFactory.getLogger(EmailComponentImpl.class);

    /**
     * 管理员
     */
    private String adminEmail = ConstUtils.EMAILS;

    @Override
    public boolean sendMailToAdmin(String title, String content) {
        return sendMail(title, content, Arrays.asList(adminEmail));
    }

    @Override
    public boolean sendMail(String title, String content, List<String> emailList) {
        return sendMail(title, content, emailList, null);
    }

    @Override
    public boolean sendMail(String title, String content, List<String> emailList, List<String> ccList) {
        /**
         * your company send email codes
         */
        return true;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    @Override
    public String getAdminEmail() {
        return adminEmail;
    }

}
