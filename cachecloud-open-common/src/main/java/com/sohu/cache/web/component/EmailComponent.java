package com.sohu.cache.web.component;

import java.util.List;

/**
 * 邮件服务
 * @author leifu
 * @Date 2015-6-2
 * @Time 上午10:56:35
 */
public interface EmailComponent {
	/**
     * 发送邮件
     * @param title
     * @param content
     * @param emails
     * @param cc(抄送)
     * @return
     */
    boolean sendMail(String title, String content, List<String> emailList, List<String> ccList);
	
	
    /**
     * 发送邮件
     * @param title
     * @param content
     * @param emails
     * @return
     */
    boolean sendMail(String title, String content, List<String> emailList);
    
    /**
     * 发送管理员邮件
     * @param title
     * @param content
     * @return
     */
    boolean sendMailToAdmin(String title, String content);

    
    /**
     * 获取管理员邮件组
     * @return
     */
    String getAdminEmail();

}
