package com.sohu.cache.client.service;

import com.sohu.cache.entity.AppClientValueDistriSimple;

import java.util.List;


/**
 * 客户端值分布服务
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:37
 */
public interface ClientReportValueDistriService {
    
    /**
     * 获取某个应用一段时间内值分布统计
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientValueDistriSimple> getAppValueDistriList(long appId, long startTime, long endTime);

    /**
     * 删除指定收集日期前的数据
     * @param collectTime
     * @return
     */
    int deleteBeforeCollectTime(long collectTime);

}
