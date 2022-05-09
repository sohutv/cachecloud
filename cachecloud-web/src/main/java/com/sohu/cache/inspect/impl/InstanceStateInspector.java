package com.sohu.cache.inspect.impl;

import com.sohu.cache.alert.impl.BaseAlertService;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.web.enums.AlertTypeEnum;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.FreemakerUtils;
import freemarker.template.Configuration;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InstanceStateInspector extends BaseAlertService {

    /**
     * 实例相关
     */
    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private AppService appService;
    @Autowired
    private UserService userService;
    @Autowired
    private Configuration configuration;

    public boolean inspect() {

        List<InstanceAlertValueResult> alertInstInfo = new ArrayList<>();
        List<InstanceInfo> heartStopInstances = instanceDao.getAllHeartStopInstance();
        if (!CollectionUtils.isEmpty(heartStopInstances)) {
            for (InstanceInfo info : heartStopInstances) {
                long appId = info.getAppId();
                AppDesc appDesc = appService.getByAppId(appId);
                appDesc.setOfficer(userService.getOfficerName(appDesc.getOfficer()));
                InstanceAlertValueResult instanceAlert = new InstanceAlertValueResult();
                instanceAlert.setInstanceInfo(info);
                instanceAlert.setAppId(appId);
                instanceAlert.setAppDesc(appDesc);
                instanceAlert.setOtherInfo(InstanceStatusEnum.getByStatus(info.getStatus()).getInfo());
                alertInstInfo.add(instanceAlert);
            }
            String emailTitle = String.format("Redis实例异常状态监控报警");
            Map<String, Object> context = new HashMap<>();
            context.put("instanceAlertValueResultList", alertInstInfo);
            String emailContent = FreemakerUtils.createText("instanceState.ftl", configuration, context);
            appAlertRecordService.saveAlertInfoByType(AlertTypeEnum.INATANCE_EXCEPTION_STATE_MONITOR, emailTitle, null, alertInstInfo);
            emailComponent.sendMailToAdmin(emailTitle, emailContent);
            logger.info(emailContent);
        }
        return true;
    }

}
