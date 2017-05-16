package com.sohu.cache.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppDailyData;

/**
 * 应用日报
 * 
 * @author leifu
 * @Date 2017年1月19日
 * @Time 上午10:25:39
 */
public interface AppDailyDao {

    void save(AppDailyData appDailyData);
    
    List<AppDailyData> getAppDailyList(@Param("appId") long appId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
