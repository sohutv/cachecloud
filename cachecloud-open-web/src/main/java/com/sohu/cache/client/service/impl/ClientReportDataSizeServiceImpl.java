package com.sohu.cache.client.service.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.ClientReportDataSizeService;
import com.sohu.cache.dao.AppClientReportDataSizeDao;
import com.sohu.cache.entity.AppClientDataSizeStat;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;
import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * 记录客户端统计map的尺寸
 * 
 * @author leifu
 * @Date 2015年7月13日
 * @Time 下午3:00:40
 */
public class ClientReportDataSizeServiceImpl implements ClientReportDataSizeService {
    
    private Logger logger = LoggerFactory.getLogger(ClientReportDataSizeServiceImpl.class);

    private AppClientReportDataSizeDao appClientReportDataSizeDao;
    
    @Override
    public void save(ClientReportBean clientReportBean) {
        try {
            // 1.client上报
            final String clientIp = clientReportBean.getClientIp();
            final long collectTime = clientReportBean.getCollectTime();
            final long reportTime = clientReportBean.getReportTimeStamp();
            final Map<String, Object> otherInfo = clientReportBean.getOtherInfo();
            if (otherInfo == null || otherInfo.isEmpty()) {
                logger.warn("otherInfo field is empty, client ip {}", clientIp);
                return;
            }
            int costMapSize = MapUtils.getInteger(otherInfo, ClientReportConstant.COST_MAP_SIZE, 0);
            int valueMapSize = MapUtils.getInteger(otherInfo, ClientReportConstant.VALUE_MAP_SIZE, 0);
            int exceptionMapSize = MapUtils.getInteger(otherInfo, ClientReportConstant.EXCEPTION_MAP_SIZE, 0);
            int collectMapSize = MapUtils.getInteger(otherInfo, ClientReportConstant.COLLECTION_MAP_SIZE, 0);
            // 只记录大于minSize
            int minSize = 100;
            if (costMapSize < minSize && valueMapSize < minSize && exceptionMapSize < minSize && collectMapSize < minSize) {
                return;
            }
            // 设置实体
            AppClientDataSizeStat appClientDataSizeStat = new AppClientDataSizeStat();
            appClientDataSizeStat.setClientIp(clientIp);
            appClientDataSizeStat.setCollectTime(collectTime);
            appClientDataSizeStat.setCollectMapSize(collectMapSize);
            appClientDataSizeStat.setCostMapSize(costMapSize);
            appClientDataSizeStat.setValueMapSize(valueMapSize);
            appClientDataSizeStat.setExceptionMapSize(exceptionMapSize);
            appClientDataSizeStat.setCreateTime(new Date());
            appClientDataSizeStat.setReportTime(new Date(reportTime));
            
            appClientReportDataSizeDao.save(appClientDataSizeStat);
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setAppClientReportDataSizeDao(AppClientReportDataSizeDao appClientReportDataSizeDao) {
        this.appClientReportDataSizeDao = appClientReportDataSizeDao;
    }

    

}
