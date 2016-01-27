package com.sohu.tv.cachecloud.client.basic.component;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 发送邮件服务
 * @author leifu
 * @Date 2015年2月2日
 * @Time 上午11:30:38
 */
public class EmailComponentImpl implements EmailComponent {
    private final Logger logger = LoggerFactory.getLogger(EmailComponentImpl.class);

    /**
     * 管理员邮件列表
     */
    private String adminEmail;

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
         * your company send short message codes
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
