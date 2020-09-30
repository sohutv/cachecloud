package com.sohu.cache.task.util;

import com.sohu.cache.alert.WeChatComponent;
import com.sohu.cache.dao.MachineRoomDao;
import com.sohu.cache.entity.AppAudit;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.UserService;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 微信通知
 */
@Component
public class AppWechatUtil {

    private Logger logger = LoggerFactory.getLogger(AppWechatUtil.class);

    @Autowired(required = false)
    private WeChatComponent weChatComponent;

    @Autowired
    private UserService userService;

    @Autowired
    private MachineRoomDao machineRoomDao;

    @Autowired
    private AppService appService;

    @Autowired
    private AppStatsCenter appStatsCenter;

    /**
     * 应用状态通知
     *
     * @param appDesc
     * @param appAudit
     */
    public void noticeAppResult(AppDesc appDesc, AppAudit appAudit) {
        try {
            long userId = appDesc.getUserId();
            AppUser appUser = userService.get(userId);

            long applyUserId = appAudit.getUserId();
            AppUser applyAppUser = userService.get(applyUserId);

            StringBuffer appCreateWeChatContent = new StringBuffer();
            appCreateWeChatContent
                    .append(String.format("<div class=\"highlight\">申请类型: %s </div>", appAudit.getTypeDesc()));
            appCreateWeChatContent.append(String.format("申请描述: %s <br/>", appAudit.getInfo()));
            appCreateWeChatContent.append(String.format("申请时间: %s <br/>", appAudit.getCreateTimeFormat()));
            appCreateWeChatContent.append(String.format("申请人员: %s <br/>", applyAppUser.getChName()));
            appCreateWeChatContent
                    .append(String.format("<div class=\"highlight\">申请状态: %s </div>", appAudit.getStatusDesc()));
            appCreateWeChatContent.append(String.format("集群名称: %s<br/>", appDesc.getName()));
            //appCreateWeChatContent.append(String.format("集群容量: %s GB<br/>", appDesc.getForecastMem()));
            //appCreateWeChatContent.append(String.format("集群机房: %s(%s) <br/>", machineRoomName, machineLogicName));
            if (StringUtils.isNotBlank(appAudit.getRefuseReason())) {
                appCreateWeChatContent
                        .append(String.format("<div class=\"highlight\">处理描述: %s </div>", appAudit.getRefuseReason()));
            }
            Set<String> weChatSet = new HashSet<String>();
            //weChatSet.addAll(ConstUtils.getAdminWeChatList());
            weChatSet.add(appUser.getName());
            weChatSet.add(applyAppUser.getName());

            weChatComponent.sendWeChat(ConstUtils.NOTICE_TITLE, appCreateWeChatContent.toString(),
                    new ArrayList<String>(weChatSet));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 集群认领
     *
     * @param appDesc
     * @param operateUser
     */
    public void noticeAppClaim(AppDesc appDesc, AppUser operateUser) {
        try {
            //            String machineRoomName = appDesc.getMachineRoom();
            //            String machineLogicName;
            //            MachineRoom machineRoom = machineRoomDao.getByName(machineRoomName);
            //            if (appDesc.isMemcached()) {
            //                machineLogicName = machineRoomName;
            //            } else {
            //                machineLogicName = machineRoom.getLogicName();
            //            }

            StringBuffer appCreateWeChatContent = new StringBuffer();
            appCreateWeChatContent.append(String.format("<div class=\"highlight\">事件类型: %s </div>", "集群认领"));
            appCreateWeChatContent.append(String.format("认领人员: %s <br/>", operateUser.getChName()));
            appCreateWeChatContent.append(String
                    .format("认领时间: %s <br/>", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            appCreateWeChatContent.append(String.format("集群名称: %s <br/>", appDesc.getName()));
            //appCreateWeChatContent.append(String.format("集群类型: %s <br/>", appDesc.getDbTypeDesc()));
            //appCreateWeChatContent.append(String.format("集群机房: %s(%s) <br/>", machineRoomName, machineLogicName));

            Set<String> weChatSet = new HashSet<String>();
            //weChatSet.addAll(ConstUtils.getAdminWeChatList());
            weChatSet.add(operateUser.getName());

            weChatComponent.sendWeChat(ConstUtils.NOTICE_TITLE, appCreateWeChatContent.toString(),
                    new ArrayList<String>(weChatSet));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void noticeRmtSyncFinish(long sourceAppId, long targetAppId) {
        try {
            AppDesc sourceAppDesc = appService.getByAppId(sourceAppId);
            AppDesc targetAppDesc = appService.getByAppId(targetAppId);

            StringBuffer content = new StringBuffer();
            content.append("<div class=\"highlight\">扩容同步完成</div>");
            content.append(String.format("源集群 : %s <br/>", sourceAppDesc.getName()));
            content.append(String.format("目集群 : %s <br/>", targetAppDesc.getName()));
            content.append("可以执行redis.sh --update");
            weChatComponent.sendWeChatToAdmin(ConstUtils.NOTICE_TITLE, content.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void noticeTaskAbort(long taskId, String stepName) {
        try {
            StringBuffer content = new StringBuffer();
            content.append(String.format("<div class=\"highlight\">任务id=%s,stepName=%s中断</div>，请查看", taskId, stepName));
            weChatComponent.sendWeChatToAdmin(ConstUtils.NOTICE_TITLE, content.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void noticeAppScaleStop(long sourceAppId, long targetAppId) {
        try {
            AppDesc sourceAppDesc = appService.getByAppId(sourceAppId);
            AppDesc targetAppDesc = appService.getByAppId(targetAppId);
            StringBuffer content = new StringBuffer();
            content.append(String.format("<div class=\"highlight\">集群%s->%s的rmt被强制中断,请查看cc任务日志</div>",
                    sourceAppDesc.getName(), targetAppDesc.getName()));
            weChatComponent.sendWeChatToAdmin(ConstUtils.NOTICE_TITLE, content.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void noticeRmtUseMaster(long sourceAppId, long targetAppId) {
        try {
            StringBuffer content = new StringBuffer();
            content.append(String.format("<div class=\"highlight\">集群迁移%s->%s，使用了master做source</div>", sourceAppId,
                    targetAppId));
            weChatComponent.sendWeChatToAdmin(ConstUtils.NOTICE_TITLE, content.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 野生实例
     *
     * @param ip
     * @param port
     */
    public void noticeWildInstance(String ip, int port) {
        try {
            StringBuffer content = new StringBuffer();
            content.append(String.format("%s:%s is not in instance_info", ip, port));
            weChatComponent.sendWeChatToAdmin(ConstUtils.NOTICE_TITLE, content.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
