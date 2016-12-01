package com.sohu.cache.web.util;

import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.constant.RedisConfigTemplateChangeEnum;
import com.sohu.cache.entity.AppAudit;
import com.sohu.cache.entity.AppDailyData;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceConfig;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.component.EmailComponent;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.vo.AppDetailVO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    
    private AppStatsCenter appStatsCenter;
    
    private Logger logger = Logger.getLogger(AppEmailUtil.class);

    
    /**
     * 应用状态通知
     * @param appDesc
     * @param appAudit
     */
    public void noticeAppResult(AppDesc appDesc, AppAudit appAudit) {
        List<String> ccEmailList = getCCEmailList(appDesc, appAudit);
        String mailContent = VelocityUtils.createText(velocityEngine, appDesc, appAudit, new AppDailyData(), new ArrayList<InstanceAlertValueResult>(), "appAudit.vm", "UTF-8");
        AppUser appUser = userService.get(appDesc.getUserId());
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent, Arrays.asList(appUser.getEmail()), ccEmailList);
    }
    
    /**
     * 重要应用抄送
     * @param appDesc
     * @param appAudit
     * @return
     */
    private List<String> getCCEmailList(AppDesc appDesc, AppAudit appAudit) {
        Set<String> ccEmailSet = new LinkedHashSet<String>();
        for (String email : emailComponent.getAdminEmail().split(ConstUtils.COMMA)) {
            ccEmailSet.add(email);
        }
        //S级别，且是开通邮件
        if (appDesc.isSuperImportant() && AppAuditType.APP_AUDIT.getValue() == appAudit.getType()) {
            ccEmailSet.addAll(ConstUtils.LEADER_EMAIL_LIST);
        }
        return new ArrayList<String>(ccEmailSet);
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
        AppDetailVO appDetailVO = appStatsCenter.getAppDetail(appId);
        StringBuilder mailContent = new StringBuilder();
        mailContent.append(appUser.getChName()).append(",对应用appid=").append(appId);
        mailContent.append("进行下线,操作结果是").append(isSuccess?"成功":"失败");
        mailContent.append(",请知晓!");
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent.toString(), appDetailVO.getEmailList(), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }
    
    public void sendRedisConfigTemplateChangeEmail(AppUser appUser, InstanceConfig instanceConfig,
            SuccessEnum successEnum, RedisConfigTemplateChangeEnum redisConfigTemplateChangeEnum) {
        String mailTitle = "【CacheCloud】-Redis配置模板修改通知";
        String mailContent = String.format("%s 对Redis配置模板 进行了%s,操作结果是%s,具体为(key=%s,value=%s,状态为%s)",
                appUser.getChName(),
                redisConfigTemplateChangeEnum.getInfo(), successEnum.info(), instanceConfig.getConfigKey(),
                instanceConfig.getConfigValue(), instanceConfig.getStatusDesc());
        emailComponent.sendMail(mailTitle, mailContent.toString(), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
        
    }
    
    public void sendSystemConfigDifEmail(AppUser appUser, Map<String, String> systemDifConfigMap,
            SuccessEnum successEnum) {
        if (MapUtils.isEmpty(systemDifConfigMap)) {
            return;
        }
        String mailTitle = "【CacheCloud】-系统配置修改通知";
        StringBuffer mailContent = new StringBuffer();
        mailContent.append(appUser.getChName() + "修改了系统配置，修改结果:" + successEnum.info() + "<br/>");
        mailContent.append("具体配置如下:<br/>");
        for(Entry<String, String> entry : systemDifConfigMap.entrySet()) {
            mailContent.append(entry.getKey() + "-->" + entry.getValue() + "<br/>");
        }
        emailComponent.sendMail(mailTitle, mailContent.toString(), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }
    
    /**
     * 系统通知
     * @param noticeContent
     * @return
     */
    public boolean noticeAllUser(String noticeContent) {
        if (StringUtils.isBlank(noticeContent)) {
            return false;
        }
        try {
            String mailTitle = "【CacheCloud】-系统通知";
            StringBuffer mailContent = new StringBuffer();
            String[] noticeArray = noticeContent.split(ConstUtils.NEXT_LINE);
            for(String noticeLine : noticeArray) {
                mailContent.append(noticeLine).append("<br/>");
            }
            List<String> emailList = new ArrayList<String>();
            List<AppUser> appUserList = userService.getUserList(null);
            if (CollectionUtils.isEmpty(appUserList)) {
                return false;
            }
            for (AppUser appUser: appUserList) {
                String email = appUser.getEmail();
                if (StringUtils.isBlank(email)) {
                    continue;
                }
                emailList.add(email);
            }
            return emailComponent.sendMail(mailTitle, mailContent.toString(), emailList, Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA))); 
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
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

    public void setAppStatsCenter(AppStatsCenter appStatsCenter) {
        this.appStatsCenter = appStatsCenter;
    }
    
}
