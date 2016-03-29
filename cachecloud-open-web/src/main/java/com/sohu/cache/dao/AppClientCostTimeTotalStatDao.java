package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppClientCostTimeTotalStat;

/**
 * 基于应用全局耗时统计(uniquekey: app_id, command, collect_time)
 * @author leifu
 * @Date 2015年6月26日
 * @Time 下午4:24:24
 */
public interface AppClientCostTimeTotalStatDao {

    /**
     * 保存基于应用的耗时统计
     * @param appClientCostTimeTotalStat
     */
    void save(AppClientCostTimeTotalStat appClientCostTimeTotalStat);

    /**
     * 获取应用指定时间内调用过的命令
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<String> getAppDistinctCommand(@Param("appId") Long appId, @Param("startTime") long startTime,
            @Param("endTime") long endTime);

    /**
     * 获取应用指定时间内某个命令的耗时统计
     * @param appId
     * @param command
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientCostTimeTotalStat> getAppClientCommandStat(@Param("appId") Long appId,
            @Param("command") String command, @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 批量保存
     * @param appClientCostTimeTotalStatList
     */
    void batchSave(@Param("appClientCostTimeTotalStatList") List<AppClientCostTimeTotalStat> appClientCostTimeTotalStatList);
}
