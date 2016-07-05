package com.sohu.cache.inspect.impl;

import com.sohu.cache.alert.impl.BaseAlertService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 应用客户端连接监控
 * 
 * @author leifu
 * @Date 2016年6月16日
 * @Time 上午9:44:34
 */
public class AppClientConnInspector extends BaseAlertService implements Inspector {

    /**
     * app统计相关
     */
    private AppStatsCenter appStatsCenter;

    /**
     * 实例统计相关
     */
    private InstanceStatsCenter instanceStatsCenter;

    @Override
    public boolean inspect(Map<InspectParamEnum, Object> paramMap) {
        Long appId = MapUtils.getLong(paramMap, InspectParamEnum.SPLIT_KEY);
        AppDetailVO appDetailVO = appStatsCenter.getAppDetail(appId);
        if (appDetailVO == null) {
            logger.warn("appId {} appDetailVO is empty", appId);
            return true;
        }
        List<InstanceInfo> appInstanceInfoList = (List<InstanceInfo>) paramMap.get(InspectParamEnum.INSTANCE_LIST);
        if (CollectionUtils.isEmpty(appInstanceInfoList)) {
            logger.warn("appId {} instanceList is empty", appId);
            return true;
        }
        // 报警阀值
        int appClientConnThreshold = getClientConnThreshold(appDetailVO.getAppDesc());
        int appClientConnNum = appDetailVO.getConn();
        // 阀值乘以分片个数
        int instanceCount = appInstanceInfoList.size();
        if (appClientConnNum > appClientConnThreshold * instanceCount) {
            alertAppClientConn(appDetailVO, appClientConnThreshold, instanceCount);
        } else {
            for (InstanceInfo instanceInfo : appInstanceInfoList) {
                if (instanceInfo == null) {
                    continue;
                }
                if (instanceInfo.isOffline()) {
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
                if (instanceStats == null) {
                    continue;
                }
                double instanceClientConnNum = instanceStats.getCurrConnections();
                // 大于标准值
                if (instanceClientConnNum > appClientConnThreshold) {
                    alertInstanceClientConn(instanceStats, appDetailVO, appClientConnThreshold);
                }
            }
        }
        return true;
    }

    /**
     * 获取报警阀值(如果用户预设超过系统预设，以系统为准，反之以用户为准)
     * @param appDesc
     * @return
     */
    private int getClientConnThreshold(AppDesc appDesc) {
        int userClientConnThreshold = appDesc.getClientConnAlertValue();
        int systemClientConnThreshold =  ConstUtils.APP_CLIENT_CONN_THRESHOLD;
        return userClientConnThreshold > systemClientConnThreshold ? systemClientConnThreshold : userClientConnThreshold;
    }

    /**
     * 应用连接数报警
     * @param appDetailVO
     * @param appClientConnThreshold
     * @param instanceCount
     */
    private void alertAppClientConn(final AppDetailVO appDetailVO, final int appClientConnThreshold, final int instanceCount) {
        AppDesc appDesc = appDetailVO.getAppDesc();
        String content = String.format("应用(%s)-客户端连接数报警-预设阀值每个分片为%s-现已达到%s(分片个数:%s)-请及时关注",
                appDesc.getAppId(), appClientConnThreshold, appDetailVO.getConn(), instanceCount);
        String title = "CacheCloud系统-客户端连接数报警";
        logger.warn("app title {}", title);
        logger.warn("app content {}", content);
        emailComponent.sendMail(title, content, appDetailVO.getEmailList(),
                Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }

    /**
     * 单个分片连接数报警
     * @param instanceStats
     * @param appDetailVO
     * @param appClientConnThreshold
     */
    private void alertInstanceClientConn(final InstanceStats instanceStats, final AppDetailVO appDetailVO,
            final int appClientConnThreshold) {
        String instanceHostPort = instanceStats.getIp() + ":" + instanceStats.getPort();
        String content = String.format("分片(%s,应用(%s))客户端连接数报警-预设%s-现已达到%s-请及时关注", instanceHostPort,
                instanceStats.getAppId(), appClientConnThreshold, instanceStats.getCurrConnections());
        String title = "CacheCloud系统-分片客户端连接数报警";
        logger.warn("instance title {}", title);
        logger.warn("instace content {}", content);
        emailComponent.sendMail(title, content, appDetailVO.getEmailList(),
                Arrays.asList(emailComponent.getAdminEmail().split(ConstUtils.COMMA)));
    }

    public void setAppStatsCenter(AppStatsCenter appStatsCenter) {
        this.appStatsCenter = appStatsCenter;
    }

    public void setInstanceStatsCenter(InstanceStatsCenter instanceStatsCenter) {
        this.instanceStatsCenter = instanceStatsCenter;
    }

}
