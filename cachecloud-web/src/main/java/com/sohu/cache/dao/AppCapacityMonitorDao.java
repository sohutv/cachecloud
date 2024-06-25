package com.sohu.cache.dao;

import com.sohu.cache.entity.AppCapacityMonitor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2022/10/10 9:38
 * @Description: 应用容量（自动扩容、缩容审计报表）
 */
public interface AppCapacityMonitorDao {

    /**
     * 保存应用容量监控信息
     *
     * @param appCapacityMonitor
     * @return
     */
    public int save(AppCapacityMonitor appCapacityMonitor);

    /**
     * 批量保存应用容量监控信息
     *
     * @param appCapacityMonitorList
     * @return
     */
    public int batchSave(List<AppCapacityMonitor> appCapacityMonitorList);

    /**
     * 更新应用容量监控信息
     *
     * @param appCapacityMonitor
     * @return
     */
    public int update(AppCapacityMonitor appCapacityMonitor);

    /**
     * 更新应用缩容计划
     *
     * @param appCapacityMonitor
     * @return
     */
    public int updateAppCapacityReduceSchedule(AppCapacityMonitor appCapacityMonitor);

    public int updateAppUsedMemHistory(@Param("appId") long appId, @Param("memUsedHistory") long memUsedHistory);

    /**
     * 根据appId查询应用容量监控信息
     * @param appId
     * @return
     */
    public AppCapacityMonitor getAppCapacityMonitorByAppId(long appId);

    /**
     * 根据appId查询所有在线应用容量监控信息
     * @return
     */
    public List<AppCapacityMonitor> getAppCapacityMonitorAll();


    /**
     * 根据appId查询应用容量监控信息
     * @param appCapacityMonitor
     * @return
     */
//    public List<AppCapacityMonitor> getAppCapacityMonitorByCondition(AppCapacityMonitor appCapacityMonitor);

}
