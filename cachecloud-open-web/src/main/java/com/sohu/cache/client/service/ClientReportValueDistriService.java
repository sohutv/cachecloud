package com.sohu.cache.client.service;

import java.util.List;

import com.sohu.cache.entity.AppClientValueDistriSimple;


/**
 * 客户端值分布服务
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:37
 */
public interface ClientReportValueDistriService extends ClientReportDataExecuteService{
    
    /**
     * 获取某个应用一段时间内值分布统计
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientValueDistriSimple> getAppValueDistriList(long appId, long startTime, long endTime);

}
