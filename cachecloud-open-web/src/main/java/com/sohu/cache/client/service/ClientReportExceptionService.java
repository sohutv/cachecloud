package com.sohu.cache.client.service;

import java.util.List;

import com.sohu.cache.entity.AppClientExceptionStat;
import com.sohu.cache.web.util.Page;


/**
 * 客户端上报异常记录
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:32
 */
public interface ClientReportExceptionService extends ClientReportDataExecuteService{
    
    /**
     * 获取客户端异常列表
     * @param appId 应用id
     * @param startTime 开始收集时间
     * @param endTime 结束收集时间
     * @param type 异常类型(ClientExceptionType)
     * @param clientIp 客户端ip
     * @return
     */
    List<AppClientExceptionStat> getAppExceptionList(Long appId, long startTime, long endTime, int type, String clientIp, Page page);

    /**
     * 获取客户端异常个数
     * @param appId 应用id
     * @param startTime 开始收集时间
     * @param endTime 结束收集时间
     * @param type 异常类型(ClientExceptionType)
     * @param clientIp 客户端ip
     * @return
     */
    int getAppExceptionCount(Long appId, long startTime, long endTime, int type, String clientIp);

}
