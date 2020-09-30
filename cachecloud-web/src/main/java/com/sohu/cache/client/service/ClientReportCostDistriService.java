package com.sohu.cache.client.service;

import com.sohu.cache.entity.AppClientCostTimeStat;
import com.sohu.cache.entity.AppClientCostTimeTotalStat;

import java.util.List;

/**
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:28
 */
public interface ClientReportCostDistriService {

    /**
     * 获取一段时间内某个应用执行的命令列表
     * 
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<String> getAppDistinctCommand(Long appId, long startTime, long endTime);
    

    /**
     * 获取一段时间内某个应用某个命令单个客户端当个实例的统计信息
     * @param appId
     * @param command
     * @param instanceId
     * @param clientIp
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientCostTimeStat> getAppCommandClientToInstanceStat(Long appId, String command, Long instanceId,
            String clientIp, long startTime, long endTime);
    
    /**
     * 获取一个应用一段时间内某个命令的统计信息
     * @param appId
     * @param command
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientCostTimeTotalStat> getAppClientCommandTotalStat(Long appId, String command, long startTime, long endTime);


    /**
     * 删除collectTime之前的数据
     * @param collectTime
     */
    int deleteBeforeCollectTime(long collectTime);
    
}
