package com.sohu.cache.stats.admin.impl;

import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.ResourceDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.stats.admin.CoreAppsStatCenter;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.util.VelocityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private VelocityEngine velocityEngine;

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
            Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));


            Map<String, List<Map<String, Object>>> appClientGatherStatGroup = appService.getFilterAppClientStatGather(-1, searchDate);

            noticeExpAppsDaily(searchDate, appDescMap, appClientGatherStatGroup);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void noticeExpAppsDaily(String searchDate, Map<Long, AppDesc> appDescMap, Map<String, List<Map<String, Object>>> appClientGatherStatGroup) {
        String title = String.format("【CacheCloud】%s应用日报", searchDate);
        String mailContent = VelocityUtils.createExpAppsText(velocityEngine, searchDate, appDescMap, appClientGatherStatGroup, "expAppsDaily.vm", "UTF-8");
        log.info("noticeExpAppsDaily sendMailToAdmin, title:{}, mailContent:{}", title, mailContent);
        emailComponent.sendMailToAdmin(title, mailContent);
        log.info("noticeExpAppsDaily success");
    }
}
