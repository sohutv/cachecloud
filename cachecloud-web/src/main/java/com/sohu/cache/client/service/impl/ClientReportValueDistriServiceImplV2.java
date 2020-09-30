package com.sohu.cache.client.service.impl;

import com.sohu.cache.client.service.ClientReportInstanceService;
import com.sohu.cache.client.service.ClientReportValueDistriService;
import com.sohu.cache.dao.AppClientValueStatDao;
import com.sohu.cache.entity.AppClientValueDistriSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 客户端上报值分布serviceV2
 * 
 * @author leifu
 * @Date 2016年5月5日
 * @Time 上午10:23:00
 */
public class ClientReportValueDistriServiceImplV2 implements ClientReportValueDistriService {

    private final Logger logger = LoggerFactory.getLogger(ClientReportValueDistriServiceImplV2.class);

    public static Set<String> excludeCommands = new HashSet<String>();
    static {
        excludeCommands.add("ping");
        excludeCommands.add("quit");
    }
    
    /**
     * 客户端统计值分布数据操作
     */
    private AppClientValueStatDao appClientValueStatDao;

    /**
     * host:port与instanceInfo简单缓存
     */
    private ClientReportInstanceService clientReportInstanceService;

    @Override
    public List<AppClientValueDistriSimple> getAppValueDistriList(long appId, long startTime, long endTime) {
        try {
            return appClientValueStatDao.getAppValueDistriList(appId, startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public int deleteBeforeCollectTime(long collectTime) {
        try {
            return appClientValueStatDao.deleteBeforeCollectTime(collectTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    public void setClientReportInstanceService(ClientReportInstanceService clientReportInstanceService) {
        this.clientReportInstanceService = clientReportInstanceService;
    }

    public void setAppClientValueStatDao(AppClientValueStatDao appClientValueStatDao) {
        this.appClientValueStatDao = appClientValueStatDao;
    }

}
