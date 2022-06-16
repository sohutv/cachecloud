package com.sohu.cache.client.service.impl;

import com.google.common.collect.Maps;
import com.sohu.cache.client.service.AppClientReportCommandService;
import com.sohu.cache.dao.AppClientCommandStatisticsDao;
import com.sohu.cache.entity.AppClientCommandStatistics;
import com.sohu.cache.report.ReportDataComponent;
import com.sohu.cache.util.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by rucao on 2019/12/16
 */
@Service
@Slf4j
public class AppClientReportCommandServiceImpl implements AppClientReportCommandService {
    @Autowired
    private AppClientCommandStatisticsDao appClientCommandStatisticsDao;

    @Autowired
    private ReportDataComponent reportDataComponent;

    @Override
    public void batchSave(long appId, String clientIp, long currentMin, List<Map<String, Object>> commandStatsModels) {
        try {
            // 1.client上报
            if (CollectionUtils.isEmpty(commandStatsModels)) {
                log.warn("commandStatsModels is empty:{},{}", clientIp, currentMin);
                return;
            }
            // 2.解析
            List<AppClientCommandStatistics> appClientCommandStatisticsList = commandStatsModels.stream()
                    .filter(appClientCommandStatistics -> generate(appId, clientIp, currentMin, appClientCommandStatistics) != null)
                    .map(appClientCommandStatistics -> generate(appId, clientIp, currentMin, appClientCommandStatistics))
                    .collect(Collectors.toList());
            // 4.批量保存
            if (CollectionUtils.isNotEmpty(appClientCommandStatisticsList)) {
                appClientCommandStatisticsDao.batchSave(appClientCommandStatisticsList);
                //上报数据
                reportDataComponent.reportCommandData(appClientCommandStatisticsList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getAppDistinctCommand(Long appId, long startTime, long endTime) {
        try {
            return appClientCommandStatisticsDao.getAppDistinctCommand(appId, startTime, endTime);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, List<Map<String, Object>>> getAppCommandClientStatistics(Long appId, String command, long startTime, long endTime, String clientIp) {
        try {
            List<Map<String, Object>> appClientCommandStatisticsList = appClientCommandStatisticsDao.getAppCommandStatistics(appId, startTime, endTime, command, clientIp);
            Map<String, List<Map<String, Object>>> commandStatisticsMap = Maps.newHashMap();
            appClientCommandStatisticsList.stream().forEach(commandStatistic -> {
                String client_ip = MapUtils.getString(commandStatistic, "client_ip");
                ArrayList commandStatisticList = (ArrayList) MapUtils.getObject(commandStatisticsMap, client_ip);
                if (CollectionUtils.isEmpty(commandStatisticList)) {
                    commandStatisticList = Lists.newArrayList();
                    commandStatisticsMap.put(client_ip, commandStatisticList);
                }
                commandStatisticList.add(commandStatistic);
            });
            return commandStatisticsMap;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public List<Map<String, Object>> getAppClientStatisticsByCommand(Long appId, String command, long startTime, long endTime) {
        try {
            List<Map<String, Object>> getAppClientStatisticsByCommand = appClientCommandStatisticsDao.getAppCommandStatistics(appId, startTime, endTime, command, null);
            return getAppClientStatisticsByCommand;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getAppDistinctClients(Long appId, long startTime, long endTime) {
        try {
            return appClientCommandStatisticsDao.getAppDistinctClients(appId, startTime, endTime);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getSumCmdStatByCmd(Long appId, long startTime, long endTime, String command) {
        try {
            return appClientCommandStatisticsDao.getSumCmdStatByCmd(appId, startTime, endTime, command);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getSumCmdStatByClient(Long appId, long startTime, long endTime, String clientIp) {
        try {
            return appClientCommandStatisticsDao.getSumCmdStatByClient(appId, startTime, endTime, clientIp);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private AppClientCommandStatistics generate(long appId, String clientIp, long currentMin, Map<String, Object> commandStatsModel) {
        try {
            AppClientCommandStatistics appClientCommandStatistics = (AppClientCommandStatistics) MapUtil.mapToObject(commandStatsModel, AppClientCommandStatistics.class);
            if (appClientCommandStatistics != null) {
                appClientCommandStatistics.setAppId(appId);
                appClientCommandStatistics.setClientIp(clientIp);
                appClientCommandStatistics.setCurrentMin(currentMin);
            }
            return appClientCommandStatistics;
        } catch (Exception e) {
            log.error("generate appClientCommandStatistics error {}, {}", e.getMessage(), e);
            return null;
        }
    }
}
