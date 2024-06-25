package com.sohu.cache.client.service.impl;

import com.google.common.collect.Maps;
import com.sohu.cache.client.service.AppClientReportExceptionService;
import com.sohu.cache.dao.AppClientExceptionStatisticsDao;
import com.sohu.cache.dao.AppClientLatencyCommandDao;
import com.sohu.cache.entity.AppClientExceptionStatistics;
import com.sohu.cache.entity.AppClientLatencyCommand;
import com.sohu.cache.report.ReportDataComponent;
import com.sohu.cache.util.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rucao on 2019/12/13
 */
@Slf4j
@Service("appClientReportExceptionService")
public class AppClientReportExceptionServiceImpl implements AppClientReportExceptionService {

    private static int ARGS_MAX_LEN = 255;
    @Autowired
    private AppClientExceptionStatisticsDao appClientExceptionStatisticsDao;
    @Autowired
    private AppClientLatencyCommandDao appClientLatencyCommandDao;

    @Autowired
    private ReportDataComponent reportDataComponent;

    @Override
    public void batchSave(long appId, String clientIp, String redisPoolConfig, long currentMin, List<Map<String, Object>> exceptionModels) {
        try {
            // 1.client上报
            if (CollectionUtils.isEmpty(exceptionModels)) {
                log.warn("exceptionModels is empty:{},{}", clientIp, currentMin);
                return;
            }
            // 2.解析
            List<AppClientExceptionStatistics> appClientExceptionStatisticsList = exceptionModels.stream()
                    .map(exceptionModel -> generate(appId, clientIp, redisPoolConfig, currentMin, exceptionModel))
                    .filter(exceptionStatistics -> (exceptionStatistics != null))
                    .collect(Collectors.toList());
            // 4.批量保存
            if (CollectionUtils.isNotEmpty(appClientExceptionStatisticsList)) {
                appClientExceptionStatisticsDao.batchSave(appClientExceptionStatisticsList);
                //上报数据
                reportDataComponent.reportExceptionData(appClientExceptionStatisticsList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, List<Map<String, Object>>> getAppExceptionStatisticsMap(Long appId, String clientIp, long startTime, long endTime, Integer type) {
        try {
            List<Map<String, Object>> appClientExceptionStatisticsList = appClientExceptionStatisticsDao.getAppExceptionStatistics(appId, clientIp, startTime, endTime, type);
            Map<String, List<Map<String, Object>>> exceptionStatisticsMap = Maps.newHashMap();
            appClientExceptionStatisticsList.stream().forEach(exceptionStatistic -> {
                String client_ip = MapUtils.getString(exceptionStatistic, "client_ip");
                ArrayList commandStatisticList = (ArrayList) MapUtils.getObject(exceptionStatisticsMap, client_ip);
                if (CollectionUtils.isEmpty(commandStatisticList)) {
                    commandStatisticList = Lists.newArrayList();
                    exceptionStatisticsMap.put(client_ip, commandStatisticList);
                }
                commandStatisticList.add(exceptionStatistic);
            });
            return exceptionStatisticsMap;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyMap();
    }

    @Override
    public List<Map<String, Object>> getDistinctClientNodeStatistics(Long appId, String clientIp, long startTime, long endTime, Integer type) {
        try {
            return appClientExceptionStatisticsDao.getDistinctClientNodeStatistics(appId, clientIp, startTime, endTime, type);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Map<String, String> getAppDistinctClientConfig(Long appId, Integer type, long startTime, long endTime) {
        try {
            List<Map<String, String>> clientConfigList = appClientExceptionStatisticsDao.getAppDistinctClientConfig(appId, type, startTime, endTime);
            Map<String, String> clientConfigMap = clientConfigList.stream().collect(Collectors.toMap
                    (clientConfig -> MapUtils.getString(clientConfig, "client_ip"),
                            clientConfig -> MapUtils.getString(clientConfig, "redis_pool_config"),
                            (key1, key2) -> key2));
            return clientConfigMap;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, List<String>> getAppClientConfigs(Long appId, Integer type, long startTime, long endTime) {
        try {
            List<Map<String, String>> clientConfigList = appClientExceptionStatisticsDao.getAppClientConfigs(appId, type, startTime, endTime);
            Map<String, List<String>> clientConfigMap = Maps.newHashMap();
            clientConfigList.stream().forEach(clientConfig -> {
                        String client_ip = MapUtils.getString(clientConfig, "client_ip");
                        String redis_pool_config = MapUtils.getString(clientConfig, "redis_pool_config");
                        String change_time = MapUtils.getString(clientConfig, "change_time");

                        String time_config = "变更时间：" + change_time + "   配置：" + redis_pool_config;
                        List<String> configTimeList;
                        if (clientConfigMap.containsKey(client_ip)) {
                            configTimeList = clientConfigMap.get(client_ip);
                        } else {
                            configTimeList = Lists.newArrayList();
                            clientConfigMap.put(client_ip, configTimeList);
                        }
                        configTimeList.add(time_config);
                    }
            );
            return clientConfigMap;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public List<Map<String, Object>> getLatencyCommandDetailByNode(String clientIp, String node, long startTime, long endTime) {
        try {
            List<String> latencyCommandIdsList = appClientExceptionStatisticsDao.getLatencyCommandsByNode(clientIp, startTime, endTime, node);
            String latencyCommandIdsStr = StringUtils.join(latencyCommandIdsList, ",");
            List<Long> ids = Arrays.stream(latencyCommandIdsStr.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            return appClientLatencyCommandDao.getLatencyCommandByIds(ids);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<Map<String, Object>> getLatencyCommandDetailByNodeV2(String node, long searchTime) {
        try {
            List<String> latencyCommandIdsList = appClientExceptionStatisticsDao.getLatencyCommandsByNodeV2(node, searchTime);
            String latencyCommandIdsStr = StringUtils.join(latencyCommandIdsList, ",");
            List<Long> ids = Arrays.stream(latencyCommandIdsStr.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            List<Map<String, Object>> latencyCommandList = appClientLatencyCommandDao.getLatencyCommandByIds(ids);
            return latencyCommandList;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Map<String, Map<String, Object>> getSumCmdExpStatGroupByNode(long appId, long searchTime) {
        try {
            List<Map<String, Object>> sumCmdExpStatList = appClientExceptionStatisticsDao.getSumCmdExpStatGroupByNode(appId, searchTime);
            return sumCmdExpStatList.stream().collect(Collectors.toMap(sumCmdExpStat -> MapUtils.getString(sumCmdExpStat, "node", ""), sumCmdExpStat -> sumCmdExpStat, (key1, key2) -> key2));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_MAP;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getLatencyCommandDetails(Set<String> nodeSet, long searchTime) {
        try {
            return nodeSet.stream().collect(Collectors.toMap(node -> node, node -> getLatencyCommandDetailByNodeV2(node, searchTime)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_MAP;
    }


    private AppClientExceptionStatistics generate(long appId, String clientIp, String redisPoolConfig, long currentMin, Map<String, Object> exceptionModel) {
        try {
            AppClientExceptionStatistics appClientExceptionStatistics = (AppClientExceptionStatistics) MapUtil.mapToObject(exceptionModel, AppClientExceptionStatistics.class);
            if (appClientExceptionStatistics != null) {
                appClientExceptionStatistics.setAppId(appId);
                appClientExceptionStatistics.setClientIp(clientIp);
                appClientExceptionStatistics.setRedisPoolConfig(redisPoolConfig);
                appClientExceptionStatistics.setCurrentMin(currentMin);
                List<Map<String, Object>> commandFailedModels = (List) exceptionModel.get("commandFailedModels");
                if (CollectionUtils.isNotEmpty(commandFailedModels)) {
                    List<AppClientLatencyCommand> appClientLatencyCommandList = commandFailedModels.stream()
                            .map(commandFailedModel -> {
                                try {
                                    String args = MapUtils.getString(commandFailedModel, "args", "");
                                    if (StringUtils.isNotBlank(args) && args.length() > ARGS_MAX_LEN) {
                                        String args_new = args.substring(0, ARGS_MAX_LEN);
                                        commandFailedModel.put("args", args_new);
                                    }
                                    return (AppClientLatencyCommand) MapUtil.mapToObject(commandFailedModel, AppClientLatencyCommand.class);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .filter(latencyCommand -> (latencyCommand != null))
                            .collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(appClientLatencyCommandList)){
                        appClientLatencyCommandDao.batchSave(appClientLatencyCommandList);
                        String latencyCommands = appClientLatencyCommandList.stream()
                                .map(appClientLatencyCommand -> String.valueOf(appClientLatencyCommand.getId()))
                                .collect(Collectors.joining(","));
                        appClientExceptionStatistics.setLatencyCommands(latencyCommands);
                    }
                }
            }
            return appClientExceptionStatistics;
        } catch (Exception e) {
            log.error("generate appClientCommandStatistics exceptionModel: {}, error {}, {}", exceptionModel, e.getMessage(), e);
            return null;
        }

    }


}
