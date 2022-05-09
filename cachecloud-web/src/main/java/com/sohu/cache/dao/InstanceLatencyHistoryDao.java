package com.sohu.cache.dao;

import com.sohu.cache.entity.AppClientStatisticGather;
import com.sohu.cache.entity.InstanceLatencyHistory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Author: rucao
 * @Date: 2020/5/7 4:57 下午
 */
@Repository
public interface InstanceLatencyHistoryDao {
    int batchSave(List<InstanceLatencyHistory> instanceLatencyHistoryList);

    List<Map<String, Object>> getAppLatencyStats(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    int getAppLatencyStatsCount(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<Map<String, Object>> getAppLatencyStatsGroupByInstance(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<Map<String, Object>> getAppLatencyInfo(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("event") String event);

    List<AppClientStatisticGather> getAppLatencyCountStat(@Param("startTime") long startTime, @Param("endTime") long endTime);
}
