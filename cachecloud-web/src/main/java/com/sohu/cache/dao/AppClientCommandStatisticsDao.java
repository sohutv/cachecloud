package com.sohu.cache.dao;

import com.sohu.cache.entity.AppClientCommandStatistics;
import com.sohu.cache.entity.AppClientStatisticGather;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/12/16
 */
@Repository
public interface AppClientCommandStatisticsDao {
    /**
     * 批量保存
     *
     * @param appClientCommandStatisticsList
     * @return
     */
    int batchSave(List<AppClientCommandStatistics> appClientCommandStatisticsList);

    /**
     * 获取应用指定时间内调用过的命令
     *
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<String> getAppDistinctCommand(@Param("appId") Long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 获取应用指定时间内d的客户端ip
     *
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<String> getAppDistinctClients(@Param("appId") Long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 获取某个应用一段时间内某个命令的统计信息列表
     *
     * @param appId
     * @param startTime
     * @param endTime
     * @param command
     * @return
     */
    List<Map<String, Object>> getAppCommandStatistics(@Param("appId") Long appId, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("command") String command, @Param("clientIp") String clientIp);


    /**
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientStatisticGather> getAppClientCmdStat(@Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * @param appId
     * @param startTime
     * @param endTime
     * @param command
     * @return
     */
    List<Map<String, Object>> getSumCmdStatByCmd(@Param("appId") Long appId, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("command") String command);

    List<Map<String, Object>> getSumCmdStatByClient(@Param("appId") Long appId, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("clientIp") String clientIp);

    int cleanCommandStatisticsBeforeCurrentMin(@Param("currentMin") long currentMin);
}
