package com.sohu.cache.web.util;

import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.constant.RedisConfigTemplateChangeEnum;
import com.sohu.cache.entity.*;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.EnvUtil;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.vo.AppDetailVO;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 邮件通知应用的申请流程(方法内是具体的文案)
 *
 * @author leifu
 */
@Component
@Slf4j
public class AppEmailUtil {
    private Logger logger = LoggerFactory.getLogger(AppEmailUtil.class);

    @Autowired(required = false)
    private EmailComponent emailComponent;

    @Autowired
    private UserService userService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private AppStatsCenter appStatsCenter;

    @Autowired
    private Environment environment;

    /**
     * 应用状态通知
     *
     * @param appDesc
     * @param appAudit
     */
    public void noticeAppResult(AppDesc appDesc, AppAudit appAudit) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        List<String> ccEmailList = getCCEmailList(appDesc, appAudit);
        String officerIds = appDesc.getOfficer();
        appDesc.setOfficer(userService.getOfficerName(officerIds));
        Map<String, Object> context = new HashMap<>();
        context.put("appDesc", appDesc);
        context.put("appAudit", appAudit);
        context.put("appDailyData", new AppDailyData());
        context.put("instanceAlertValueResultList", new ArrayList<InstanceAlertValueResult>());
        String mailContent = FreemakerUtils.createText("appAudit.ftl", configuration, context);
        AppUser appUser = userService.get(appDesc.getUserId());
        List<String> receiveEmailList = new ArrayList<>();
        userService.getOfficerUserByUserIds(officerIds).forEach(user -> receiveEmailList.add(user.getEmail()));
        if(CollectionUtils.isEmpty(receiveEmailList) && appUser != null) {
            receiveEmailList.add(appUser.getEmail());
        }
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent, receiveEmailList, ccEmailList);
    }

    /**
     * 应用状态通知
     *
     * @param applyUser
     * @param appDesc
     * @param appAudit
     */
    public void noticeAppResultWithApplyUser(AppUser applyUser, AppDesc appDesc, AppAudit appAudit) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        List<String> ccEmailList = getCCEmailList(appDesc, appAudit);
        String officerIds = appDesc.getOfficer();
        appDesc.setOfficer(userService.getOfficerName(officerIds));
        Map<String, Object> context = new HashMap<>();
        context.put("appDesc", appDesc);
        context.put("appAudit", appAudit);
        context.put("appDailyData", new AppDailyData());
        context.put("instanceAlertValueResultList", new ArrayList<InstanceAlertValueResult>());
        String mailContent = FreemakerUtils.createText("appAudit.ftl", configuration, context);
        AppUser appUser = userService.get(appDesc.getUserId());
        List<String> receiveEmailList = new ArrayList<>();
        userService.getOfficerUserByUserIds(officerIds).forEach(user -> receiveEmailList.add(user.getEmail()));
        if(CollectionUtils.isEmpty(receiveEmailList) && appUser != null) {
            receiveEmailList.add(appUser.getEmail());
        }
        receiveEmailList.add(applyUser.getEmail());
        List<String> appOfficeList = receiveEmailList.stream().distinct().collect(Collectors.toList());
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent, appOfficeList, ccEmailList);
    }

    /**
     * 应用状态通知
     *
     * @param appUser
     * @param appAudit
     */
    public void noticeAuditResult(AppUser appUser, AppAudit appAudit) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        List<String> ccEmailList = Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA));
        Map<String, Object> context = new HashMap<>();
        context.put("appAudit", appAudit);
        context.put("appDailyData", new AppDailyData());
        context.put("instanceAlertValueResultList", new ArrayList<InstanceAlertValueResult>());
        String mailContent = FreemakerUtils.createText("appAudit.ftl", configuration, context);
        List<String> receiveEmailList = new ArrayList<>();
        if(appUser != null){
            receiveEmailList.add(appUser.getEmail());
        }
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent, receiveEmailList, ccEmailList);
    }

    /**
     * API应用状态通知
     *
     * @param appDesc
     * @param appAudit
     */
    public void noticeAppResultByApi(AppDesc appDesc, AppAudit appAudit) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        List<String> ccEmailList = getCCEmailList(appDesc, appAudit);
        appDesc.setOfficer(userService.getOfficerName(appDesc.getOfficer()));
        Map<String, Object> context = new HashMap<>();
        context.put("appDesc", appDesc);
        context.put("appAudit", appAudit);
        context.put("appDailyData", new AppDailyData());
        context.put("instanceAlertValueResultList", new ArrayList<InstanceAlertValueResult>());
        String mailContent = FreemakerUtils.createText("appAudit.ftl", configuration, context);
        AppUser appUser = userService.get(appDesc.getUserId());
        emailComponent.sendMail("【CacheCloud】API应用申请通知", mailContent, Arrays.asList(appUser.getEmail()), ccEmailList);
    }

    /**
     * 重要应用抄送
     *
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
     *
     * @param groupName
     * @param applyReason
     * @param appUser
     */
    public void noticeBecomeContributor(String groupName, String applyReason, AppUser appUser) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        StringBuffer mailContent = new StringBuffer();
        mailContent.append(appUser.getChName() + "(项目组:" + groupName + ")申请成为CacheCloud贡献者<br/>");
        mailContent.append("申请理由:<br/>" + applyReason);
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent.toString(), Arrays.asList(appUser.getEmail()), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }

    /**
     * 注册用户通知
     *
     * @param appUser
     * @param appAudit
     */
    public void noticeUserResult(AppUser appUser, AppAudit appAudit) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        if (appAudit == null) {
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
     *
     * @param appUser
     * @param appId
     * @param isSuccess
     */
    public void noticeOfflineApp(AppUser appUser, Long appId, boolean isSuccess) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        AppDetailVO appDetailVO = appStatsCenter.getAppDetail(appId);
        StringBuilder mailContent = new StringBuilder();
        mailContent.append(appUser.getChName()).append(",对应用appid=").append(appId);
        mailContent.append("进行下线,操作结果是").append(isSuccess ? "下线任务执行成功" : "下线任务失败,请关注任务流日志");
        mailContent.append(",请知晓!");
        emailComponent.sendMail("【CacheCloud】状态通知", mailContent.toString(), appDetailVO.getEmailList(), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }

    public void sendRedisConfigTemplateChangeEmail(AppUser appUser, String versionName, InstanceConfig instanceConfig,
                                                   SuccessEnum successEnum, RedisConfigTemplateChangeEnum redisConfigTemplateChangeEnum) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        String mailTitle = "【CacheCloud】-Redis配置模板修改通知";
        String mailContent = String.format("%s 对 %s 配置模板 进行了%s,操作结果是%s,具体为(key=%s,value=%s,状态为%s)",
                appUser.getChName(), versionName,
                redisConfigTemplateChangeEnum.getInfo(), successEnum.info(), instanceConfig.getConfigKey(),
                instanceConfig.getConfigValue(), instanceConfig.getStatusDesc());
        emailComponent.sendMail(mailTitle, mailContent.toString(), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));

    }

    public void sendAddRedisVersionEmail(AppUser appUser, String versionName, SuccessEnum successEnum) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        String mailTitle = "【CacheCloud】- 新增Redis版本通知";
        String mailContent = String.format("%s 新增Redis版本:%s ,状态为:%s)", appUser.getChName(), versionName, successEnum.info());
        emailComponent.sendMail(mailTitle, mailContent.toString(), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));

    }

    public void sendSystemConfigDifEmail(AppUser appUser, Map<String, String> systemDifConfigMap,
                                         SuccessEnum successEnum) {
        if (EnvUtil.isDev(environment)) {
            return;
        }
        if (MapUtils.isEmpty(systemDifConfigMap)) {
            return;
        }
        String mailTitle = "【CacheCloud】-系统配置修改通知";
        StringBuffer mailContent = new StringBuffer();
        mailContent.append(appUser.getChName() + "修改了系统配置，修改结果:" + successEnum.info() + "<br/>");
        mailContent.append("具体配置如下:<br/>");
        for (Entry<String, String> entry : systemDifConfigMap.entrySet()) {
            mailContent.append(entry.getKey() + "-->" + entry.getValue() + "<br/>");
        }
        emailComponent.sendMail(mailTitle, mailContent.toString(), Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }

    /**
     * 系统通知
     *
     * @param noticeContent
     * @return
     */
    public boolean noticeAllUser(String noticeContent) {
        if (EnvUtil.isDev(environment)) {
            return false;
        }
        if (StringUtils.isBlank(noticeContent)) {
            return false;
        }
        try {
            String mailTitle = "【CacheCloud】-系统通知";
            StringBuffer mailContent = new StringBuffer();
            String[] noticeArray = noticeContent.split(ConstUtils.NEXT_LINE);
            for (String noticeLine : noticeArray) {
                mailContent.append(noticeLine).append("<br/>");
            }
            List<String> emailList = new ArrayList<String>();
            List<AppUser> appUserList = userService.getUserList(null);
            if (CollectionUtils.isEmpty(appUserList)) {
                return false;
            }
            for (AppUser appUser : appUserList) {
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

}
