package com.sohu.cache.client.service;

import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/12/16
 */
public interface AppClientReportCommandService {

    /**
     * @param clientIp
     * @param currentMin
     * @param commandStatsModels
     */
    void batchSave(long appId, String clientIp, long currentMin, List<Map<String, Object>> commandStatsModels);

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
     * 获取某个应用一段时间内某个命令的单个客户端统计信息, key-clientIp
     *
     * @param appId
     * @param command
     * @param startTime
     * @param endTime
     * @return
     */
    Map<String, List<Map<String, Object>>> getAppCommandClientStatistics(Long appId, String command, long startTime, long endTime, String clientIp);

    /**
     * 锁定命令，查看某个命令被哪些client调用, key-command
     *
     * @param appId
     * @param command
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> getAppClientStatisticsByCommand(Long appId, String command, long startTime, long endTime);

    /**
     * 取一段时间内某个应用的客户端ip列表
     *
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<String> getAppDistinctClients(Long appId, long startTime, long endTime);

    /**
     * @param appId
     * @param startTime
     * @param endTime
     * @param command
     * @return
     */
    List<Map<String, Object>> getSumCmdStatByCmd(Long appId, long startTime, long endTime, String command);

    List<Map<String, Object>> getSumCmdStatByClient(Long appId, long startTime, long endTime, String clientIp);
}
