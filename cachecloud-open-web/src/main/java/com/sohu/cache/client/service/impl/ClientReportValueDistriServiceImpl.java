package com.sohu.cache.client.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.ClientReportInstanceService;
import com.sohu.cache.client.service.ClientReportValueDistriService;
import com.sohu.cache.dao.AppClientValueDistriStatDao;
import com.sohu.cache.entity.AppClientValueDistriSimple;
import com.sohu.cache.entity.AppClientValueDistriStat;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;
import com.sohu.tv.jedis.stat.enums.ClientCollectDataTypeEnum;
import com.sohu.tv.jedis.stat.enums.ValueSizeDistriEnum;
import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * 客户端上报值分布service
 * 
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:32
 */
public class ClientReportValueDistriServiceImpl implements ClientReportValueDistriService {

	private final Logger logger = LoggerFactory.getLogger(ClientReportValueDistriServiceImpl.class);

	/**
	 * 客户端统计值分布数据操作
	 */
	private AppClientValueDistriStatDao appClientValueDistriStatDao;

	/**
     * host:port与instanceInfo简单缓存
     */
    private ClientReportInstanceService clientReportInstanceService;

	@Override
	public List<AppClientValueDistriSimple> getAppValueDistriList(long appId, long startTime, long endTime) {
	    try {
            return appClientValueDistriStatDao.getAppValueDistriList(appId, startTime, endTime);
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
            List<AppClientValueDistriStat> appClientValueDistriStatList = new ArrayList<AppClientValueDistriStat>();

            // 3.解析
            for (Map<String, Object> map : datas) {
                Integer clientDataType = MapUtils.getInteger(map, ClientReportConstant.CLIENT_DATA_TYPE, -1);
                ClientCollectDataTypeEnum clientCollectDataTypeEnum = ClientCollectDataTypeEnum.MAP.get(clientDataType);
                if (clientCollectDataTypeEnum == null) {
                    continue;
                }
                if (ClientCollectDataTypeEnum.VALUE_LENGTH_DISTRI_TYPE.equals(clientCollectDataTypeEnum)) {
                    AppClientValueDistriStat appClientValueDistriStat = generate(clientIp, collectTime, reportTime, map);
                    if (appClientValueDistriStat != null) {
                        appClientValueDistriStatList.add(appClientValueDistriStat);
                    }
                }
            }
            
            // 4.保存
            if (CollectionUtils.isNotEmpty(appClientValueDistriStatList)) {
                appClientValueDistriStatDao.batchSave(appClientValueDistriStatList);
            }
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
	
	private AppClientValueDistriStat generate(String clientIp, long collectTime, long reportTime, Map<String, Object> map) {
        String valueDistri = MapUtils.getString(map, ClientReportConstant.VALUE_DISTRI, "");
        ValueSizeDistriEnum valueSizeDistriEnum = ValueSizeDistriEnum.getByValue(valueDistri);
        if (valueSizeDistriEnum == null) {
            logger.warn("valueDistri {} is wrong, not in enums {}", valueDistri, ValueSizeDistriEnum.values());
        }

        // 次数
        Integer count = MapUtils.getInteger(map, ClientReportConstant.VALUE_COUNT, 0);

        // 命令
        String command = MapUtils.getString(map, ClientReportConstant.VALUE_COMMAND, "");
        if (StringUtils.isBlank(command)) {
            logger.warn("command is empty!");
            return null;
        }

        // 实例host:port
        String hostPort = MapUtils.getString(map, ClientReportConstant.VALUE_HOST_PORT, "");
        if (StringUtils.isEmpty(hostPort)) {
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
            //logger.warn("instanceInfo is empty, host is {}, port is {}", host, port);
            return null;
        }
        long appId = instanceInfo.getAppId();

        AppClientValueDistriStat stat = new AppClientValueDistriStat();
        stat.setAppId(appId);
        stat.setClientIp(clientIp);
        stat.setReportTime(new Date(reportTime));
        stat.setCollectTime(collectTime);
        stat.setCreateTime(new Date());
        stat.setCommand(command);
        stat.setDistributeValue(valueDistri);
        stat.setDistributeType(valueSizeDistriEnum.getType());
        stat.setCount(count);
        stat.setInstanceHost(host);
        stat.setInstancePort(port);
        stat.setInstanceId(instanceInfo.getId());

        return stat;
    }
	
	@Override
    public int deleteBeforeCollectTime(long collectTime) {
	    long startTime = System.currentTimeMillis();
	    int deleteCount = 0;
        try {
            int batchSize = 10000;
            long minId = appClientValueDistriStatDao.getTableMinimumId();
            long maxId = appClientValueDistriStatDao.getMinimumIdByCollectTime(collectTime);
            if (minId > maxId) {
                return deleteCount;
            }
            long startId = minId;
            long endId = startId + batchSize;
            while (startId < maxId) {
                if (endId > maxId) {
                    endId = maxId;
                }
                deleteCount += appClientValueDistriStatDao.deleteByIds(startId, endId);
                startId += batchSize;
                endId += batchSize;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.warn("batch delete before collectTime {} cost time is {} ms", collectTime, (System.currentTimeMillis() - startTime));
        return deleteCount;
    }


	public void setClientReportInstanceService(ClientReportInstanceService clientReportInstanceService) {
        this.clientReportInstanceService = clientReportInstanceService;
    }

    public void setAppClientValueDistriStatDao(AppClientValueDistriStatDao appClientValueDistriStatDao) {
		this.appClientValueDistriStatDao = appClientValueDistriStatDao;
	}

}
