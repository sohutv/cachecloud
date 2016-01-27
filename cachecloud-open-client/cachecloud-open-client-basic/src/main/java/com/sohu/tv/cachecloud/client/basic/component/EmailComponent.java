package com.sohu.tv.cachecloud.client.basic.component;

import java.util.List;

/**
 * 邮件报警
 * @author leifu
 * @Date 2015年2月2日
 * @Time 上午11:37:39
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
