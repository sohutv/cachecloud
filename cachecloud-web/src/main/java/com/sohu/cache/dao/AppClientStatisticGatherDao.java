package com.sohu.cache.dao;

import com.sohu.cache.entity.AppClientStatisticGather;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/12/29
 */
@Repository
public interface AppClientStatisticGatherDao {
    @Deprecated
    int batchSave();

    int batchSaveCmdStats(List<AppClientStatisticGather> list);
    int batchAddCmdStats(List<AppClientStatisticGather> list);

    int batchSaveConnExpStats(List<AppClientStatisticGather> list);
    int batchAddConnExpStats(List<AppClientStatisticGather> list);

    int batchSaveCmdExpStats(List<AppClientStatisticGather> list);
    int batchAddCmdExpStats(List<AppClientStatisticGather> list);

    int batchSaveMemFragRatio(List<AppClientStatisticGather> list);

    int batchSaveSlowLogCount(List<AppClientStatisticGather> list);
    int batchAddSlowLogCount(List<AppClientStatisticGather> list);

    int batchSaveLatencyCount(List<AppClientStatisticGather> list);
    int batchAddLatencyCount(List<AppClientStatisticGather> list);

    int batchSaveAppStats(List<AppClientStatisticGather> list);

    int batchAddAppServerCmdCount(List<AppClientStatisticGather> list);

    int batchSaveConnClients(List<AppClientStatisticGather> list);

    int batchSaveTopologyExam(List<AppClientStatisticGather> list);

    List<Map<String, Object>> getAppClientStatisticByGatherTime(@Param("appId") long appId, @Param("gatherTime") String gatherTime);

    List<AppClientStatisticGather> getTopologyExamFailedByGatherTime(@Param("gatherTime") String gatherTime);

    List<Map<String, Object>> getExpAppStatisticByGatherTime(@Param("gatherTime") String gatherTime);
}
