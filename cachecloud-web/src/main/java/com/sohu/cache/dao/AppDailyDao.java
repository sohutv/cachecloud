package com.sohu.cache.dao;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppDailyData;

import java.util.List;

/**
 * 应用日报
 * 
 * @author leifu
 * @Date 2017年1月19日
 * @Time 上午10:25:39
 */
public interface AppDailyDao {

    void save(AppDailyData appDailyData);
    
    AppDailyData getAppDaily(@Param("appId") long appId, @Param("date") String date);

    List<AppDailyData> getAppDailyList(@Param("appId") long appId, @Param("startDate") long startDate, @Param("endDate") long endDate);

}
