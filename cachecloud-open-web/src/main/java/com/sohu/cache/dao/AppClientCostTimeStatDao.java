package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppClientCostTimeStat;

/**
 * 客户端耗时dao
 * 
 * @author leifu
 * @Date 2015年1月20日
 * @Time 上午11:50:01
 */
public interface AppClientCostTimeStatDao {

    /**
     * 
     * @param appClientCostTimeStat
     */
    void save(AppClientCostTimeStat appClientCostTimeStat);

    /**
     * 查询应用一段时间内所有客户端和实例关系表
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientCostTimeStat> getAppDistinctClientAndInstance(@Param("appId") Long appId,
            @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 查询应用一段时间内某个命令、某对客户端和实例的耗时统计信息
     * @param appId
     * @param command
     * @param instanceId
     * @param clientIp
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientCostTimeStat> getAppCommandClientToInstanceStat(@Param("appId") Long appId,
            @Param("command") String command, @Param("instanceId") long instanceId,
            @Param("clientIp") String clientIp, @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 批量更新
     * @param appClientCostTimeStatList
     * @return
     */
    int batchSave(@Param("appClientCostTimeStatList") List<AppClientCostTimeStat> appClientCostTimeStatList);

}
