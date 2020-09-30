package com.sohu.cache.client.service.impl;

import com.sohu.cache.client.service.AppInstanceClientRelationService;
import com.sohu.cache.client.service.ClientReportCostDistriService;
import com.sohu.cache.client.service.ClientReportInstanceService;
import com.sohu.cache.dao.AppClientCostTimeStatDao;
import com.sohu.cache.dao.AppClientCostTimeTotalStatDao;
import com.sohu.cache.entity.AppClientCostTimeStat;
import com.sohu.cache.entity.AppClientCostTimeTotalStat;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * 客户端上报耗时分布
 *
 * @author leifu
 * @Date 2015年1月19日
 * @Time 下午1:49:39
 */
public class ClientReportCostDistriServiceImpl implements ClientReportCostDistriService {

    private final Logger logger = LoggerFactory.getLogger(ClientReportCostDistriServiceImpl.class);

    /**
     * 客户端耗时操作
     */
    private AppClientCostTimeStatDao appClientCostTimeStatDao;

    /**
     * 基于应用的客户端耗时操作
     */
    private AppClientCostTimeTotalStatDao appClientCostTimeTotalStatDao;

    /**
     * host:port与instanceInfo简单缓存
     */
    private ClientReportInstanceService clientReportInstanceService;

    /**
     * 应用下节点和客户端关系
     */
    private AppInstanceClientRelationService appInstanceClientRelationService;

    @Override
    public List<String> getAppDistinctCommand(Long appId, long startTime, long endTime) {
        try {
            return appClientCostTimeTotalStatDao.getAppDistinctCommand(appId, startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<AppClientCostTimeStat> getAppCommandClientToInstanceStat(Long appId, String command, Long instanceId,
            String clientIp, long startTime, long endTime) {
        try {
            return appClientCostTimeStatDao.getAppCommandClientToInstanceStat(appId, command, instanceId, clientIp,
                    startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<AppClientCostTimeTotalStat> getAppClientCommandTotalStat(Long appId, String command, long startTime,
            long endTime) {
        try {
            return appClientCostTimeTotalStatDao.getAppClientCommandStat(appId, command, startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 合并以app为单位的
     *
     * @param appClientCostTimeStatList
     */
    private List<AppClientCostTimeTotalStat> mergeAppClientCostTimeStat(
            List<AppClientCostTimeStat> appClientCostTimeStatList) {
        // 1. merge后的结果
        List<AppClientCostTimeTotalStat> resultList = new ArrayList<AppClientCostTimeTotalStat>();

        // 2. 以appid_command_collectTime为key，拆分appClientCostTimeStatList
        Map<String, List<AppClientCostTimeStat>> map = new HashMap<String, List<AppClientCostTimeStat>>();
        for (AppClientCostTimeStat appClientCostTimeStat : appClientCostTimeStatList) {
            long appId = appClientCostTimeStat.getAppId();
            String command = appClientCostTimeStat.getCommand();
            long collectTime = appClientCostTimeStat.getCollectTime();
            String key = appId + "_" + command + "_" + collectTime;
            if (map.containsKey(key)) {
                map.get(key).add(appClientCostTimeStat);
            } else {
                List<AppClientCostTimeStat> list = new ArrayList<AppClientCostTimeStat>();
                list.add(appClientCostTimeStat);
                map.put(key, list);
            }
        }

        // 3.生成结果
        for (Entry<String, List<AppClientCostTimeStat>> entry : map.entrySet()) {
            String key = entry.getKey();
            String[] items = key.split("_");
            long appId = NumberUtils.toLong(items[0]);
            String command = items[1];
            long collectTime = NumberUtils.toLong(items[2]);

            double totalCost = 0.0;
            long totalCount = 0;
            int median = 0;
            int ninetyPercentMax = 0;
            int ninetyNinePercentMax = 0;
            int hundredMax = 0;
            String maxInstanceHost = "";
            int maxInstancePort = 0;
            long maxInstanceId = 0;
            String maxClientIp = "";
            double mean = 0.0;
            for (AppClientCostTimeStat appClientCostTimeStat : entry.getValue()) {
                AppClientCostTimeTotalStat appClientCostTimeTotalStat = AppClientCostTimeTotalStat
                        .getFromAppClientCostTimeStat(appClientCostTimeStat);
                totalCost += appClientCostTimeTotalStat.getTotalCost();
                totalCount += appClientCostTimeTotalStat.getTotalCount();
                if (appClientCostTimeTotalStat.getMedian() > median) {
                    median = appClientCostTimeTotalStat.getMedian();
                }
                if (appClientCostTimeTotalStat.getNinetyPercentMax() > ninetyPercentMax) {
                    ninetyPercentMax = appClientCostTimeTotalStat.getNinetyPercentMax();
                }
                if (appClientCostTimeTotalStat.getNinetyNinePercentMax() > ninetyNinePercentMax) {
                    ninetyNinePercentMax = appClientCostTimeTotalStat.getNinetyNinePercentMax();
                }
                if (appClientCostTimeTotalStat.getHundredMax() > hundredMax) {
                    hundredMax = appClientCostTimeTotalStat.getHundredMax();
                    maxInstanceHost = appClientCostTimeTotalStat.getMaxInstanceHost();
                    maxInstancePort = appClientCostTimeTotalStat.getMaxInstancePort();
                    maxInstanceId = appClientCostTimeTotalStat.getMaxInstanceId();
                    maxClientIp = appClientCostTimeTotalStat.getMaxClientIp();
                }
            }
            DecimalFormat df = new DecimalFormat("0.00");
            totalCost = NumberUtils.toDouble(df.format(totalCost));

            //平均值
            if (totalCount > 0 && totalCost > 0) {
                double tmp = totalCost / totalCount;
                mean = NumberUtils.toDouble(df.format(tmp), mean);
            }

            //添加到结果集
            resultList.add(new AppClientCostTimeTotalStat(-1, appId, collectTime, new Date(), command,
                    totalCount, totalCost, median, mean, ninetyPercentMax, ninetyNinePercentMax, hundredMax,
                    maxInstanceHost, maxInstancePort, maxInstanceId, maxClientIp));
        }
        return resultList;
    }

    /**
     * 1.获取最小的id
     * 2.获取date的id
     * 3.按照id批量删除
     */
    @Override
    public int deleteBeforeCollectTime(long collectTime) {
        long startTime = System.currentTimeMillis();
        int deleteCount = 0;
        try {
            int batchSize = 10000;
            long minId = appClientCostTimeStatDao.getTableMinimumId();
            long maxId = appClientCostTimeStatDao.getMinimumIdByCollectTime(collectTime);
            if (minId > maxId) {
                return deleteCount;
            }
            long startId = minId;
            long endId = startId + batchSize;
            while (startId < maxId) {
                long start = System.currentTimeMillis();
                if (endId > maxId) {
                    endId = maxId;
                }
                deleteCount += appClientCostTimeStatDao.deleteByIds(startId, endId);
                startId += batchSize;
                endId += batchSize;
                logger.info("delete count={} ,cost :{}ms",deleteCount,(System.currentTimeMillis()-start));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.warn("batch delete before collectTime={} , deleteCount {} ,cost time is {} s", collectTime,deleteCount,
                (System.currentTimeMillis() - startTime)/1000);
        return deleteCount;
    }

    public void setAppClientCostTimeStatDao(AppClientCostTimeStatDao appClientCostTimeStatDao) {
        this.appClientCostTimeStatDao = appClientCostTimeStatDao;
    }

    public void setClientReportInstanceService(ClientReportInstanceService clientReportInstanceService) {
        this.clientReportInstanceService = clientReportInstanceService;
    }

    public void setAppClientCostTimeTotalStatDao(AppClientCostTimeTotalStatDao appClientCostTimeTotalStatDao) {
        this.appClientCostTimeTotalStatDao = appClientCostTimeTotalStatDao;
    }

    public void setAppInstanceClientRelationService(AppInstanceClientRelationService appInstanceClientRelationService) {
        this.appInstanceClientRelationService = appInstanceClientRelationService;
    }


}
