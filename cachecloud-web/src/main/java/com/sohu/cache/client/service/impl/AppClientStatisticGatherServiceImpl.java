package com.sohu.cache.client.service.impl;

import com.sohu.cache.client.service.AppClientStatisticGatherService;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.AppClientStatisticGather;
import com.sohu.cache.entity.AppStats;
import com.sohu.cache.entity.TimeBetween;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.task.tasks.daily.TopologyExamTask;
import com.sohu.cache.web.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rucao on 2019/12/29
 */
@Slf4j
@Service("appClientStatisticGatherService")
public class AppClientStatisticGatherServiceImpl implements AppClientStatisticGatherService {
    @Autowired
    private AppClientExceptionStatisticsDao appClientExceptionStatisticsDao;
    @Autowired
    private AppClientCommandStatisticsDao appClientCommandStatisticsDao;
    @Resource
    private AppStatsDao appStatsDao;
    @Resource
    private InstanceSlowLogDao instanceSlowLogDao;
    @Autowired
    private InstanceLatencyHistoryDao instanceLatencyHistoryDao;
    @Autowired
    private AppClientStatisticGatherDao appClientStatisticGatherDao;
    @Autowired
    private AppStatsCenter appStatsCenter;
    @Autowired
    TopologyExamTask topologyExamTask;

    @Override
    public void bathSave(long startTime, long endTime) {
        try {
            List<AppClientStatisticGather> appClientConnExpStatList = appClientExceptionStatisticsDao.getAppClientConnExpStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appClientConnExpStatList)) {
                appClientStatisticGatherDao.batchSaveConnExpStats(appClientConnExpStatList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> appClientCmdExpStatList = appClientExceptionStatisticsDao.getAppClientCmdExpStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appClientCmdExpStatList)) {
                appClientStatisticGatherDao.batchSaveCmdExpStats(appClientCmdExpStatList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> appClientCmdStatList = appClientCommandStatisticsDao.getAppClientCmdStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appClientCmdStatList)) {
                appClientStatisticGatherDao.batchSaveCmdStats(appClientCmdStatList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
//        try {
//            List<AppClientStatisticGather> appMemFragRatioList = appStatsDao.getMemFragRatios(startTime, endTime);
//            if (CollectionUtils.isNotEmpty(appMemFragRatioList)) {
//                appClientStatisticGatherDao.batchSaveMemFragRatio(appMemFragRatioList);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
        try {
            List<AppClientStatisticGather> appSlowLogCountList = instanceSlowLogDao.getAppSlowLogCountStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appSlowLogCountList)) {
                appClientStatisticGatherDao.batchSaveSlowLogCount(appSlowLogCountList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> appLatencyCountList = instanceLatencyHistoryDao.getAppLatencyCountStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appLatencyCountList)) {
                appClientStatisticGatherDao.batchSaveLatencyCount(appLatencyCountList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> appStatsList = appStatsDao.gatherAppsStats(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appStatsList)) {
                appClientStatisticGatherDao.batchSaveAppStats(appStatsList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> connClientList = appStatsCenter.getOnlineAppConnClients();
            if (CollectionUtils.isNotEmpty(connClientList)) {
                appClientStatisticGatherDao.batchSaveConnClients(connClientList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> topologyExamList = topologyExamTask.checkAppsTopology(null);
            if (CollectionUtils.isNotEmpty(topologyExamList)) {
                appClientStatisticGatherDao.batchSaveTopologyExam(topologyExamList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void bathAdd(long startTime, long endTime) {
        try {
            List<AppClientStatisticGather> appClientConnExpStatList = appClientExceptionStatisticsDao.getAppClientConnExpStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appClientConnExpStatList)) {
                appClientStatisticGatherDao.batchAddConnExpStats(appClientConnExpStatList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> appClientCmdExpStatList = appClientExceptionStatisticsDao.getAppClientCmdExpStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appClientCmdExpStatList)) {
                appClientStatisticGatherDao.batchAddCmdExpStats(appClientCmdExpStatList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> appClientCmdStatList = appClientCommandStatisticsDao.getAppClientCmdStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appClientCmdStatList)) {
                appClientStatisticGatherDao.batchAddCmdStats(appClientCmdStatList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
//        try {
//            TimeBetween timeBetween = fillWithDateFormat(startTime);
//            long startTime_new = timeBetween.getStartTime();
//            long endTime_new = timeBetween.getEndTime();
//            List<AppClientStatisticGather> appMemFragRatioList = appStatsDao.getMemFragRatios(startTime_new, endTime_new);
//            if (CollectionUtils.isNotEmpty(appMemFragRatioList)) {
//                appClientStatisticGatherDao.batchSaveMemFragRatio(appMemFragRatioList);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
        try {
            List<AppClientStatisticGather> appSlowLogCountList = instanceSlowLogDao.getAppSlowLogCountStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appSlowLogCountList)) {
                appClientStatisticGatherDao.batchAddSlowLogCount(appSlowLogCountList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> appLatencyCountList = instanceLatencyHistoryDao.getAppLatencyCountStat(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appLatencyCountList)) {
                appClientStatisticGatherDao.batchAddLatencyCount(appLatencyCountList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            List<AppClientStatisticGather> appStatsList = appStatsDao.gatherAppsStats(startTime, endTime);
            if (CollectionUtils.isNotEmpty(appStatsList)) {
                appClientStatisticGatherDao.batchSaveAppStats(appStatsList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    @Override
    public void bathAddServerCmdCount(long startTime, long endTime) {
        //添加server端统计的命令调用次数
        //每小时执行一次
        try {
            String gatherTimePre = null;
            if(startTime == endTime){
                gatherTimePre = DateUtil.formatYYYY_MM_dd(DateUtil.parseYYYYMMddHH(String.valueOf(startTime)));
            }
            String gatherTime = gatherTimePre;
            List<AppStats> appStatsList = appStatsDao.getAppHourStatsByTime(startTime, endTime);
            List<AppClientStatisticGather> gatherList = new ArrayList<>(appStatsList.size());
            appStatsList.forEach(appStats -> {
                AppClientStatisticGather appClientStatisticGather = new AppClientStatisticGather();
                appClientStatisticGather.setAppId(appStats.getAppId());
                appClientStatisticGather.setServerCmdCount(appStats.getCommandCount());
                if(gatherTime != null){
                    appClientStatisticGather.setGatherTime(gatherTime);
                } else {
                    try {
                        String gatherTimeOne = DateUtil.formatYYYY_MM_dd(DateUtil.parseYYYYMMddHH(String.valueOf(appStats.getCollectTime())));
                        appClientStatisticGather.setGatherTime(gatherTimeOne);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return;
                    }
                }
                gatherList.add(appClientStatisticGather);
            });
            if (CollectionUtils.isNotEmpty(appStatsList)) {
                appClientStatisticGatherDao.batchAddAppServerCmdCount(gatherList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private TimeBetween fillWithDateFormat(long startTime) throws Exception {
        String startTimeStr = Long.toString(startTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = sdf.parse(startTimeStr);

        Date startDate_new = DateUtils.addMinutes(date, 5);
        Date endDate_new = DateUtils.addMinutes(startDate_new, 5);
        long startTime_new = NumberUtils.toLong(DateUtil.formatDate(startDate_new, "yyyyMMddHHmm00"));
        long endTime_new = NumberUtils.toLong(DateUtil.formatDate(endDate_new, "yyyyMMddHHmm00"));
        return new TimeBetween(startTime_new, endTime_new, startDate_new, endDate_new);
    }
}
