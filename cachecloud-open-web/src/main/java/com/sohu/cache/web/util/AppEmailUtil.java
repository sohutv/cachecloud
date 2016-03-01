package com.sohu.cache.web.util;

import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.entity.AppAudit;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.component.EmailComponent;
import com.sohu.cache.web.service.UserService;

import org.apache.velocity.app.VelocityEngine;

import java.util.Arrays;

/**
 * 邮件通知应用的申请流程(方法内是具体的文案)
 *
 * @author leifu
 * @Time 2014年10月16日
 */
public class AppEmailUtil {

    private EmailComponent emailComponent;

    private UserService userService;

    private VelocityEngine velocityEngine;

    /**
     * 应用状态通知
     * @param appDesc
     * @param appAudit
     */
    public void noticeAppResult(AppDesc appDesc, AppAudit appAudit) {
        String mailContent = VelocityUtils.createText(velocityEngine, appDesc, appAudit, "appAudit.vm", "UTF-8");
        AppUser appUser = userService.get(appDesc.getUserId());
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent, Arrays.asList(appUser.getEmail()), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }
    
    /**
     * 贡献者通知
     * @param groupName
     * @param applyReason
     * @param appUser
     */
    public void noticeBecomeContributor(String groupName, String applyReason, AppUser appUser) {
    	StringBuffer mailContent = new StringBuffer();
    	mailContent.append(appUser.getChName() + "(项目组:"+groupName+")申请成为CacheCloud贡献者<br/>");
    	mailContent.append("申请理由:<br/>" + applyReason);
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent.toString(), Arrays.asList(appUser.getEmail()), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
	}
    
    /**
     * 注册用户通知
     * @param appUser
     * @param appAudit
     */
    public void noticeUserResult(AppUser appUser, AppAudit appAudit) {
        if(appAudit == null){
            return;
        }
        StringBuffer mailContent = new StringBuffer();
        if (AppCheckEnum.APP_WATING_CHECK.value().equals(appAudit.getStatus())) {
            mailContent.append(appUser.getChName() + "申请想成为CacheCloud用户，请管理员帮忙处理！<br/>");
        } else if (AppCheckEnum.APP_PASS.value().equals(appAudit.getStatus())) {
            mailContent.append("您的用户申请已经审批通过，您可以登录正常Cachecloud了！<br/>");
        } else if (AppCheckEnum.APP_REJECT.value().equals(appAudit.getStatus())) {
            mailContent.append("您的用户申请被驳回，原因是: " + appAudit.getRefuseReason());
        }
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent.toString(), Arrays.asList(appUser.getEmail()), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }
    
    /**
     * 下线应用通知
     * @param appUser
     * @param appId
     * @param isSuccess
     */
    public void noticeOfflineApp(AppUser appUser, Long appId, boolean isSuccess) {
        StringBuilder mailContent = new StringBuilder();
        mailContent.append(appUser.getChName()).append(",对应用appid=").append(appId);
        mailContent.append("进行下线,操作结果是").append(isSuccess?"成功":"失败");
        mailContent.append(",请知晓!");
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent.toString(), Arrays.asList(appUser.getEmail()), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }

    public void setEmailComponent(EmailComponent emailComponent) {
        this.emailComponent = emailComponent;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
}
