package com.sohu.cache.client.service;

import java.util.List;

import com.sohu.cache.entity.AppClientExceptionStat;
import com.sohu.cache.entity.ClientInstanceException;
import com.sohu.cache.web.util.Page;
import com.sohu.tv.jedis.stat.model.ClientReportBean;


/**
 * 客户端上报异常记录
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:32
 */
public interface ClientReportExceptionService {
    
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

    /**
     * 大于collectTime期间各个实例的异常统计
     * @param ip
     * @param collectTime
     * @return
     */
    List<ClientInstanceException> getInstanceExceptionStat(String ip, long collectTime);
    
    /**
     * 批量保存
     * @param clientReportBean
     * @return
     */
    void batchSave(ClientReportBean clientReportBean);

}
