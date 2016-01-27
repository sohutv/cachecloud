package com.sohu.cache.client.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.ClientReportCostDistriService;
import com.sohu.cache.dao.AppClientCostTimeStatDao;
import com.sohu.cache.dao.AppClientCostTimeTotalStatDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppClientCostTimeStat;
import com.sohu.cache.entity.AppClientCostTimeTotalStat;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;

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
     * 实例操作
     */
    private InstanceDao instanceDao;

    @Override
    public void execute(String clientIp, long collectTime, long reportTime, Map<String, Object> map) {
        try {
            Integer count = MapUtils.getInteger(map, ClientReportConstant.COST_COUNT, 0);
            String command = MapUtils.getString(map, ClientReportConstant.COST_COMMAND, "");
            if (StringUtils.isBlank(command)) {
                logger.warn("command is empty!");
                return;
            }
            String hostPort = MapUtils.getString(map, ClientReportConstant.COST_HOST_PORT, "");
            if (StringUtils.isBlank(hostPort)) {
                logger.warn("hostPort is empty", hostPort);
                return;
            }
            int index = hostPort.indexOf(":");
            if (index <= 0) {
                logger.warn("hostPort {} format is wrong", hostPort);
                return;
            }
            String host = hostPort.substring(0, index);
            int port = NumberUtils.toInt(hostPort.substring(index + 1));

            // 实例信息
            InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(host, port);
            if (instanceInfo == null) {
                logger.warn("instanceInfo is empty, host is {}, port is {}", host, port);
                return;
            }
            long appId = instanceInfo.getAppId();
            // 耗时分布详情
            Double mean = MapUtils.getDouble(map, ClientReportConstant.COST_TIME_MEAN, 0.0);
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
            stat.setMean(mean);
            stat.setMedian(median);
            stat.setNinetyPercentMax(ninetyPercentMax);
            stat.setNinetyNinePercentMax(ninetyNinePercentMax);
            stat.setHundredMax(hunredMax);
            stat.setInstanceId(instanceInfo.getId());

            appClientCostTimeStatDao.save(stat);
            
            //上卷到应用
            AppClientCostTimeTotalStat appClientCostTimeTotalStat = AppClientCostTimeTotalStat.getFromAppClientCostTimeStat(stat);
            appClientCostTimeTotalStatDao.save(appClientCostTimeTotalStat);
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

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
    public List<AppClientCostTimeStat> getAppDistinctClientAndInstance(Long appId, long startTime, long endTime) {
        try {
            return appClientCostTimeStatDao.getAppDistinctClientAndInstance(appId, startTime, endTime);
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

    public void setAppClientCostTimeStatDao(AppClientCostTimeStatDao appClientCostTimeStatDao) {
        this.appClientCostTimeStatDao = appClientCostTimeStatDao;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setAppClientCostTimeTotalStatDao(AppClientCostTimeTotalStatDao appClientCostTimeTotalStatDao) {
        this.appClientCostTimeTotalStatDao = appClientCostTimeTotalStatDao;
    }

    

   

}
