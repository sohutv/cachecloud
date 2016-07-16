package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppClientValueDistriSimple;
import com.sohu.cache.entity.AppClientValueDistriStat;


/**
 * 客户端值分布dao
 * @author leifu
 * @Date 2015年1月20日
 * @Time 上午11:50:45
 */
public interface AppClientValueDistriStatDao {
    
    /**
     * 保存值分布
     * @param appClientValueDistriStat
     */
    void save(AppClientValueDistriStat appClientValueDistriStat);
    
    /**
     * 批量保存值分布
     * @param appClientValueDistriStatList
     * @return
     */
    int batchSave(@Param("appClientValueDistriStatList") List<AppClientValueDistriStat> appClientValueDistriStatList);

    /**
     * 获取某个应用一段时间内值分布统计
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientValueDistriSimple> getAppValueDistriList(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 获取最小id
     * @return
     */
    long getTableMinimumId();

    /**
     * 按照collectTime获取最小id
     * @param collectTime
     * @return
     */
    long getMinimumIdByCollectTime(@Param("collectTime") long collectTime);

    /**
     * 按照id区间删除
     * @param startId
     * @param endId
     */
    long deleteByIds(@Param("startId") long startId, @Param("endId") long endId);

    
}
