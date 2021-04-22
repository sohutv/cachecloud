package com.sohu.cache.stats.admin.impl;

import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.ResourceDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.stats.admin.CoreAppsStatCenter;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.util.FreemakerUtils;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by rucao on 2020/3/25
 */
@Service("coreAppsStatCenter")
@Slf4j
public class CoreAppsStatCenterImpl implements CoreAppsStatCenter {

    @Autowired
    private AppService appService;
    @Autowired
    private AppDao appDao;
    @Autowired
    private UserService userService;
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private EmailComponent emailComponent;
    @Autowired
    private Configuration configuration;
    @Autowired
    private MachineCenter machineCenter;

    @Override
    public boolean sendExpAppsStatDataEmail(String searchDate) {
        try {
            log.info("sendExpAppsStatDataEmail");
            if (StringUtil.isBlank(searchDate)) {
                //默认发送前一天的日报
                Date startDate = DateUtils.addDays(new Date(), -1);
                searchDate = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            }
            List<AppDesc> appDescList = appDao.getOnlineAppsNonTest();
            appDescList.forEach(appDesc -> {
                String versionName = Optional.ofNullable(resourceDao.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
                appDesc.setVersionName(versionName);
                appDesc.setOfficer(userService.getOfficerName(appDesc.getOfficer()));
            });
            Map<String, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(appDesc -> String.valueOf(appDesc.getAppId()), Function.identity()));


            Map<String, List<Map<String, Object>>> appClientGatherStatGroup = appService.getFilterAppClientStatGather(-1, searchDate);

            // 获取异常的宿主或容器信息
            Map<String, Object> exceptionMachineEnv = machineCenter.getExceptionMachineEnv(DateUtils.addDays(new Date(), -1));

            noticeExpAppsDaily(searchDate, appDescMap, appClientGatherStatGroup,exceptionMachineEnv);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void noticeExpAppsDaily(String searchDate, Map<String, AppDesc> appDescMap, Map<String, List<Map<String, Object>>> appClientGatherStatGroup,Map<String,Object> exceptionMachineEnv) {
        String title = String.format("【CacheCloud】%s应用日报", searchDate);
        Map<String, Object> context = new HashMap<>();
        context.put("appDescMap", appDescMap);
        context.put("appClientGatherStatGroup", appClientGatherStatGroup);
        context.put("exceptionMachineEnv", exceptionMachineEnv);
        context.put("searchDate", searchDate);
        String mailContent = FreemakerUtils.createText("expAppsDaily.ftl", configuration, context);
        log.info("noticeExpAppsDaily sendMailToAdmin, title:{}, mailContent:{}", title, mailContent);
        // 发送管理员
        emailComponent.sendMailToAdmin(title, mailContent);
        log.info("noticeExpAppsDaily success");
    }


}
