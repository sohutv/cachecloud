package com.sohu.cache.client.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.AppInstanceClientRelationService;
import com.sohu.cache.client.service.ClientReportCostDistriService;
import com.sohu.cache.client.service.ClientReportInstanceService;
import com.sohu.cache.dao.AppClientCostTimeStatDao;
import com.sohu.cache.dao.AppClientCostTimeTotalStatDao;
import com.sohu.cache.entity.AppClientCostTimeStat;
import com.sohu.cache.entity.AppClientCostTimeTotalStat;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;
import com.sohu.tv.jedis.stat.enums.ClientCollectDataTypeEnum;
import com.sohu.tv.jedis.stat.model.ClientReportBean;

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
    
    @Override
    public void batchSave(ClientReportBean clientReportBean) {
        try {
            // 1.client上报
            final String clientIp = clientReportBean.getClientIp();
            final long collectTime = clientReportBean.getCollectTime();
            final long reportTime = clientReportBean.getReportTimeStamp();
            final List<Map<String, Object>> datas = clientReportBean.getDatas();
            if (datas == null || datas.isEmpty()) {
                logger.warn("datas field {} is empty", clientReportBean);
                return;
            }
            
            // 2.结果集
            List<AppClientCostTimeStat> appClientCostTimeStatList = new ArrayList<AppClientCostTimeStat>();

            // 3.解析结果
            for (Map<String, Object> map : datas) {
                Integer clientDataType = MapUtils.getInteger(map, ClientReportConstant.CLIENT_DATA_TYPE, -1);
                ClientCollectDataTypeEnum clientCollectDataTypeEnum = ClientCollectDataTypeEnum.MAP.get(clientDataType);
                if (clientCollectDataTypeEnum == null) {
                    continue;
                }
                if (ClientCollectDataTypeEnum.COST_TIME_DISTRI_TYPE.equals(clientCollectDataTypeEnum)) {
                    AppClientCostTimeStat appClientCostTimeStat = generate(clientIp, collectTime, reportTime, map);
                    if (appClientCostTimeStat != null) {
                        appClientCostTimeStatList.add(appClientCostTimeStat);
                    }
                }
            }
            
            if (CollectionUtils.isNotEmpty(appClientCostTimeStatList)) {
                // 4.批量保存
                appClientCostTimeStatDao.batchSave(appClientCostTimeStatList);
                // 5.合并app统计结果
                List<AppClientCostTimeTotalStat> appClientCostTimeTotalStatList = mergeAppClientCostTimeStat(appClientCostTimeStatList);
                if (CollectionUtils.isNotEmpty(appClientCostTimeTotalStatList)) {
                    appClientCostTimeTotalStatDao.batchSave(appClientCostTimeTotalStatList);
                }
                // 6.保存应用下节点和客户端关系
                appInstanceClientRelationService.batchSave(appClientCostTimeStatList);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * 合并以app为单位的
     * @param appClientCostTimeStatList
     */
    private List<AppClientCostTimeTotalStat> mergeAppClientCostTimeStat(List<AppClientCostTimeStat> appClientCostTimeStatList) {
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
                AppClientCostTimeTotalStat appClientCostTimeTotalStat = AppClientCostTimeTotalStat.getFromAppClientCostTimeStat(appClientCostTimeStat);
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
            if (totalCount > 0) {
                mean = totalCost / totalCount;
                mean = NumberUtils.toDouble(df.format(mean));
            }
            
            //添加到结果集
            resultList.add(new AppClientCostTimeTotalStat(-1, appId, collectTime, new Date(), command,
                    totalCount, totalCost, median, mean, ninetyPercentMax, ninetyNinePercentMax, hundredMax,
                    maxInstanceHost, maxInstancePort, maxInstanceId, maxClientIp));
        }
        return resultList;
    }
    
    private AppClientCostTimeStat generate(String clientIp, long collectTime, long reportTime, Map<String, Object> map) {
        try {
            Integer count = MapUtils.getInteger(map, ClientReportConstant.COST_COUNT, 0);
            String command = MapUtils.getString(map, ClientReportConstant.COST_COMMAND, "");
            if (StringUtils.isBlank(command)) {
                logger.warn("command is empty!");
                return null;
            }
            String hostPort = MapUtils.getString(map, ClientReportConstant.COST_HOST_PORT, "");
            if (StringUtils.isBlank(hostPort)) {
                logger.warn("hostPort is empty", hostPort);
                return null;
            }
            int index = hostPort.indexOf(":");
            if (index <= 0) {
                logger.warn("hostPort {} format is wrong", hostPort);
                return null;
            }
            String host = hostPort.substring(0, index);
            int port = NumberUtils.toInt(hostPort.substring(index + 1));

            // 实例信息
            InstanceInfo instanceInfo = clientReportInstanceService.getInstanceInfoByHostPort(host, port);
            if (instanceInfo == null) {
//                logger.warn("instanceInfo is empty, host is {}, port is {}", host, port);
                return null;
            }
            long appId = instanceInfo.getAppId();
            // 耗时分布详情
            double mean = MapUtils.getDouble(map, ClientReportConstant.COST_TIME_MEAN, 0.0);
            Integer median = MapUtils.getInteger(map, ClientReportConstant.COST_TIME_MEDIAN, 0);
            Integer ninetyPercentMax = MapUtils.getInteger(map, ClientReportConstant.COST_TIME_90_MAX, 0);
            Integer ninetyNinePercentMax = MapUtils.getInteger(map, ClientReportConstant.COST_TIME_99_MAX, 0);
            Integer hunredMax = MapUtils.getInteger(map, ClientReportConstant.COST_TIME_100_MAX, 0);

            AppClientCostTimeStat stat = new AppClientCostTimeStat();
            stat.setAppId(appId);
            stat.setClientIp(clientIp);
            stat.setReportTime(new Date(reportTime));
            stat.setCollectTime(collectTime);
            stat.setCreateTime(new Date());
            stat.setCommand(command);
            stat.setCount(count);
            stat.setInstanceHost(host);
            stat.setInstancePort(port);
            stat.setMean(NumberUtils.toDouble(new DecimalFormat("#.00").format(mean)));
            stat.setMedian(median);
            stat.setNinetyPercentMax(ninetyPercentMax);
            stat.setNinetyNinePercentMax(ninetyNinePercentMax);
            stat.setHundredMax(hunredMax);
            stat.setInstanceId(instanceInfo.getId());

            return stat;
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
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
                if (endId > maxId) {
                    endId = maxId;
                }
                deleteCount += appClientCostTimeStatDao.deleteByIds(startId, endId);
                startId += batchSize;
                endId += batchSize;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.warn("batch delete before collectTime {} cost time is {} ms", collectTime, (System.currentTimeMillis() - startTime));
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
