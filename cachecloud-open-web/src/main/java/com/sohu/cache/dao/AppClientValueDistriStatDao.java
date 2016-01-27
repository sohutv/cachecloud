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
     * 获取某个应用一段时间内值分布统计
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientValueDistriSimple> getAppValueDistriList(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);
}
