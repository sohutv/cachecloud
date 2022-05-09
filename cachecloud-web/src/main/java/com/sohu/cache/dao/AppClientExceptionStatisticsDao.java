package com.sohu.cache.dao;

import com.sohu.cache.entity.AppClientExceptionStatistics;
import com.sohu.cache.entity.AppClientStatisticGather;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/12/16
 */
@Repository
public interface AppClientExceptionStatisticsDao {
    /**
     * 批量保存
     *
     * @param appClientExceptionStatisticsList
     * @return
     */
    int batchSave(List<AppClientExceptionStatistics> appClientExceptionStatisticsList);

    /**
     * 获取某个应用一段时间内的异常信息列表
     *
     * @param appId
     * @param startTime
     * @param endTime
     * @param type
     * @return
     */
    List<Map<String, Object>> getAppExceptionStatistics(@Param("appId") Long appId, @Param("clientIp") String clientIp, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("type") Integer type);

    /**
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> getDistinctClientNodeStatistics(@Param("appId") Long appId, @Param("clientIp") String clientIp, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("type") Integer type);

    /**
     * 获取应用指定时间内d的客户端ip
     *
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, String>> getAppDistinctClientConfig(@Param("appId") Long appId, @Param("type") Integer type, @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<Map<String, String>> getAppClientConfigs(@Param("appId") Long appId, @Param("type") Integer type, @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * @param clientIp
     * @param startTime
     * @param endTime
     * @param node
     * @return
     */
    List<String> getLatencyCommandsByNode(@Param("clientIp") String clientIp, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("node") String node);

    List<String> getLatencyCommandsByNodeV2(@Param("node") String node, @Param("searchTime") long searchTime);

    /**
     * @param appId
     * @param searchTime
     * @return
     */
    List<Map<String, Object>> getSumCmdExpStatGroupByNode(@Param("appId") long appId, @Param("searchTime") long searchTime);

    /**
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientStatisticGather> getAppClientConnExpStat(@Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 获取指定日期连接异常个数
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    int getAppClientConnExpCount(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientStatisticGather> getAppClientCmdExpStat(@Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 获取指定日期超时异常个数
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    int getAppClientCmdExpCount(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

}
