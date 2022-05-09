package com.sohu.cache.alert;

import java.util.List;

/**
 * 邮件服务
 * @author leifu
 */
public interface EmailComponent {
	/**
     * 发送邮件
     * @param title
     * @param content
     * @param emailList
     * @param ccList(抄送)
     * @return
     */
    boolean sendMail(String title, String content, List<String> emailList, List<String> ccList);

    boolean sendDailyMail(String title, String content, List<String> emailList, List<String> ccList);

    /**
     * 发送邮件
     * @param title
     * @param content
     * @param emailList
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
