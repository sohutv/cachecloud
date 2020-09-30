package com.sohu.cache.client.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rucao on 2019/12/13
 */
public interface AppClientReportExceptionService {

    /**
     * 处理上报数据
     *
     * @param exceptionModels
     * @return
     */
    void batchSave(long appId, String clientIp, String redisPoolConfig, long currentMin, List<Map<String, Object>> exceptionModels);

    /**
     * 获取某个应用一段时间内的异常信息列表
     *
     * @param appId
     * @param startTime
     * @param endTime
     * @param type
     * @return
     */
    Map<String, List<Map<String, Object>>> getAppExceptionStatisticsMap(Long appId, String clientIp, long startTime, long endTime, Integer type);

    /**
     * client_ip, node, sum(count) sum_count, sum(cost) sum_cost
     *
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> getDistinctClientNodeStatistics(Long appId, String clientIp, long startTime, long endTime, Integer type);

    /**
     * client-redisPoolConfig 列表
     *
     * @param appId
     * @param type
     * @param startTime
     * @param endTime
     * @return
     */
    Map<String, String> getAppDistinctClientConfig(Long appId, Integer type, long startTime, long endTime);

    Map<String, List<String>> getAppClientConfigs(Long appId, Integer type, long startTime, long endTime);

    /**
     * @param clientIp
     * @param node
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> getLatencyCommandDetailByNode(String clientIp, String node, long startTime, long endTime);

    List<Map<String, Object>> getLatencyCommandDetailByNodeV2(String node, long searchTime);


    /**
     * @param appId
     * @param searchTime
     * @return
     */
    Map<String, Map<String, Object>> getSumCmdExpStatGroupByNode(long appId, long searchTime);

    /**
     * @param nodeSet
     * @param searchTime
     * @return
     */
    Map<String, List<Map<String, Object>>> getLatencyCommandDetails(Set<String> nodeSet, long searchTime);
}
