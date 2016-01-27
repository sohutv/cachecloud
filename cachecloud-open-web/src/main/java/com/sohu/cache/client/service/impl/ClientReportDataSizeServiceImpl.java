package com.sohu.cache.client.service.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.sohu.cache.client.service.ClientReportDataExecuteService;
import com.sohu.cache.dao.AppClientReportDataSizeDao;
import com.sohu.cache.entity.AppClientDataSizeStat;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;

/**
 * 记录客户端统计map的尺寸
 * 
 * @author leifu
 * @Date 2015年7月13日
 * @Time 下午3:00:40
 */
public class ClientReportDataSizeServiceImpl implements ClientReportDataExecuteService {

    private AppClientReportDataSizeDao appClientReportDataSizeDao;
    
    @Override
    public void execute(String clientIp, long collectTime, long reportTime, Map<String, Object> otherInfo) {
        int costMapSize = MapUtils.getInteger(otherInfo, ClientReportConstant.COST_MAP_SIZE, 0);
        int valueMapSize = MapUtils.getInteger(otherInfo, ClientReportConstant.VALUE_MAP_SIZE, 0);
        int exceptionMapSize = MapUtils.getInteger(otherInfo, ClientReportConstant.EXCEPTION_MAP_SIZE, 0);
        int collectMapSize = MapUtils.getInteger(otherInfo, ClientReportConstant.COLLECTION_MAP_SIZE, 0);
        
        //设置实体
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
        
    }

    public void setAppClientReportDataSizeDao(AppClientReportDataSizeDao appClientReportDataSizeDao) {
        this.appClientReportDataSizeDao = appClientReportDataSizeDao;
    }
    
    
    
}
