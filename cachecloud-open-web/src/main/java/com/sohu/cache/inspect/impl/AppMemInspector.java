package com.sohu.cache.inspect.impl;

import com.sohu.cache.alert.impl.BaseAlertService;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.inspect.InspectParamEnum;
import com.sohu.cache.inspect.Inspector;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.vo.AppDetailVO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by yijunzhang on 15-1-20.
 */
public class AppMemInspector extends BaseAlertService implements Inspector {

    /**
     * app统计相关
     */
    private AppStatsCenter appStatsCenter;

    /**
     * 应用相关dao
     */
    private AppDao appDao;

    /**
     * 实例统计相关
     */
    private InstanceStatsCenter instanceStatsCenter;

    @Override
    public boolean inspect(Map<InspectParamEnum, Object> paramMap) {
        Long appId = MapUtils.getLong(paramMap, InspectParamEnum.SPLIT_KEY);
        List<AppDesc> appDescList = new ArrayList<AppDesc>();
        AppDesc app = appDao.getAppDescById(appId);
        if (app != null) {
            appDescList.add(app);
        }
        if (CollectionUtils.isEmpty(appDescList)) {
            logger.error("appList is empty, appId={}", appId);
            return true;
        }
        for (AppDesc appDesc : appDescList) {
            //测试不检查
            if(appDesc.getIsTest() == 1){
                continue;
            }
            long checkAppId = appDesc.getAppId();
            AppDetailVO appDetailVO = appStatsCenter.getAppDetail(checkAppId);
            if (appDetailVO == null) {
                continue;
            }
            double appMemUsePercent = appDetailVO.getMemUsePercent();
            int appUseSetMemAlertValue = appDesc.getMemAlertValue();
            // 先检查应用的内存使用率是否超过阀值，如果没有再检查分片
            if (appMemUsePercent > appUseSetMemAlertValue) {
                // 报警
                alertAppMemUse(appDetailVO);
            } else {
                List<InstanceInfo> appInstanceInfoList = (List<InstanceInfo>) paramMap.get(InspectParamEnum.INSTANCE_LIST);
                if (CollectionUtils.isNotEmpty(appInstanceInfoList)) {
                    for (InstanceInfo instanceInfo : appInstanceInfoList) {
                        if (instanceInfo == null) {
                            continue;
                        }
                        if (!TypeUtil.isRedisType(instanceInfo.getType())) {
                            continue;
                        }
                        // 忽略sentinel观察者
                        if (TypeUtil.isRedisSentinel(instanceInfo.getType())) {
                            continue;
                        }
                        long instanceId = instanceInfo.getId();
                        InstanceStats instanceStats = instanceStatsCenter.getInstanceStats(instanceId);
                        if(instanceStats == null){
                            continue;
                        }
                        double instanceMemUsePercent = instanceStats.getMemUsePercent();
                        // 大于标准值
                        if (instanceMemUsePercent > appUseSetMemAlertValue) {
                            alertInstanceMemUse(instanceStats, appDetailVO);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param appDetailVO
     */
    private void alertAppMemUse(final AppDetailVO appDetailVO) {
        AppDesc appDesc = appDetailVO.getAppDesc();
        String content = String.format("应用(%s)-内存使用率报警-预设百分之%s-现已达到百分之%s-请及时关注",
                appDesc.getAppId(), appDesc.getMemAlertValue(), appDetailVO.getMemUsePercent());
        String title = "CacheCloud系统-应用内存使用率报警";
        emailComponent.sendMail(title, content, appDetailVO.getEmailList(),
                Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
        // TODO 临时注释掉
        // mobileAlertComponent.sendPhone(content,
        // appDetailVO.getPhoneList());

    }

    private void alertInstanceMemUse(final InstanceStats instanceStats, final AppDetailVO appDetailVO) {
        String instanceInfo = instanceStats.getIp() + ":" + instanceStats.getPort();
        String content = String.format("分片(%s,应用(%s))内存使用率报警-预设百分之%s-现已达到百分之%s-应用的内存使用率百分之%s-请及时关注",
                instanceInfo,
                instanceStats.getAppId(), appDetailVO.getAppDesc().getMemAlertValue(),
                instanceStats.getMemUsePercent(), appDetailVO.getMemUsePercent());
        String title = "CacheCloud系统-分片内存使用率报警";
        emailComponent.sendMail(title, content, appDetailVO.getEmailList(),
                Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
        // TODO 临时注释掉
        // mobileAlertComponent.sendPhone(content,
        // appDetailVO.getPhoneList());
    }

    public void setAppStatsCenter(AppStatsCenter appStatsCenter) {
        this.appStatsCenter = appStatsCenter;
    }

    public void setAppDao(AppDao appDao) {
        this.appDao = appDao;
    }

    public void setInstanceStatsCenter(InstanceStatsCenter instanceStatsCenter) {
        this.instanceStatsCenter = instanceStatsCenter;
    }

}
