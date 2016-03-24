package com.sohu.cache.web.component;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

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
	 * your company send email codes
	 */
    private boolean disabled = true;							// 关闭 true | 开启 false
    private String adminName = "Admin";
    private String adminEmail = "CacheCloud@dafycredit.com";
    private String hostName = "cas1.dafycredit.com";

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
    	if(disabled){
    		return Boolean.FALSE;
    	}
    	
    	if(emailList==null || emailList.size()==0){
            logger.error("send email fail, nobody.");
    		return Boolean.FALSE;
    	}

		try {
	    	SimpleEmail email = new SimpleEmail();
			email.setCharset("UTF-8");
	    	
			// email.setAuthentication("username", "password");				// 登陆邮件服务器的用户名和密码 （内网，可省略）

    		List <InternetAddress> clist = new ArrayList <InternetAddress>(); 
    		for(String e : emailList){	
    			clist.add(new InternetAddress(e)); 							// 接收人
    		}
    		email.setTo(clist);
    		
//    		List <InternetAddress> cclist = new ArrayList <InternetAddress>(); 
//    		for(String e : ccList){	
//    			cclist.add(new InternetAddress(e)); 						// 抄送人
//    		}
//    		email.setCc(cclist);
    		
			email.setHostName(hostName);									// smtp host
			email.setFrom(adminEmail, adminName);							// 发送人
			email.setSubject(title);										// 标题
			email.setMsg(content);											// 邮件内容
			email.send();
			
			logger.info("Send email successful!");
		} catch (EmailException | AddressException e) {
            logger.error("send email error, subject: {}, emailList: {}, ccList: {}", title, emailList, ccList);
            logger.error(e.getMessage(), e);
    		return Boolean.FALSE;
		}

        return Boolean.TRUE;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    @Override
    public String getAdminEmail() {
        return adminEmail;
    }

}
