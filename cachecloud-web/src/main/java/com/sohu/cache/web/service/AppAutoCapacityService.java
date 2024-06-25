package com.sohu.cache.web.service;

import com.sohu.cache.entity.AppCapacityMonitor;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;

import java.util.Map;

/**
 * 容量监控
 * @author zengyizhao
 * @Date 2022年10月9日
 */
public interface AppAutoCapacityService {
    
    /**
     * 检查是否需自动扩容，并扩容处理
     *
     * @param appDesc
     * @param appMemUsePercent
     * @param instanceStatsMap
     * @return
     */
    void checkAndExpandCapacity(AppDesc appDesc, int appMemUsePercent, Map<InstanceInfo, InstanceStats> instanceStatsMap);

    void updateAppCapacityMonitor(AppCapacityMonitor appCapacityMonitor);

    void updateAppMemUsedHistory();
}
