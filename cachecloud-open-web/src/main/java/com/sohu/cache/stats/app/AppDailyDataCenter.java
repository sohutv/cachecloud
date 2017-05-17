package com.sohu.cache.stats.app;

import java.util.Date;

import com.sohu.cache.entity.AppDailyData;


/**
 * 应用日数据统计
 * @author leifu
 * @Date 2016年8月10日
 * @Time 下午5:11:03
 */
public interface AppDailyDataCenter {

    /**
     * 发送所有应用日报
     */
    int sendAppDailyEmail();

    /**
     * 发送单个应用日报
     */
    boolean sendAppDailyEmail(long appId, Date startDate, Date endDate);
    
    /**
     * 获取单天应用日报
     */
    AppDailyData getAppDailyData(long appId, Date date);
    
    
}
