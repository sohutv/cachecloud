package com.sohu.cache.client.service.impl;

import com.sohu.cache.client.service.ClientReportExceptionService;
import com.sohu.cache.client.service.ClientReportInstanceService;
import com.sohu.cache.dao.AppClientExceptionStatDao;
import com.sohu.cache.dao.AppClientVersionDao;
import com.sohu.cache.entity.AppClientExceptionStat;
import com.sohu.cache.entity.ClientInstanceException;
import com.sohu.cache.web.util.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 客户端上报异常service
 * 
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:32
 */
public class ClientReportExceptionServiceImpl implements ClientReportExceptionService {

    private final Logger logger = LoggerFactory.getLogger(ClientReportExceptionServiceImpl.class);

    /**
     * 客户端异常操作
     */
    private AppClientExceptionStatDao appClientExceptionStatDao;

    /**
     * host:port与instanceInfo简单缓存
     */
    private ClientReportInstanceService clientReportInstanceService;
    
    /**
     * 客户端ip,版本查询
     */
    private AppClientVersionDao appClientVersionDao;

    

    @Override
    public List<AppClientExceptionStat> getAppExceptionList(Long appId, long startTime, long endTime, int type,
            String clientIp, Page page) {
        try {
            return appClientExceptionStatDao.getAppExceptionList(appId, startTime, endTime, type, clientIp, page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public int getAppExceptionCount(Long appId, long startTime, long endTime, int type, String clientIp) {
        try {
            return appClientExceptionStatDao.getAppExceptionCount(appId, startTime, endTime, type, clientIp);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public List<ClientInstanceException> getInstanceExceptionStat(String ip, long collectTime) {
        try {
            return appClientExceptionStatDao.getInstanceExceptionStat(ip, collectTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void setAppClientExceptionStatDao(AppClientExceptionStatDao appClientExceptionStatDao) {
        this.appClientExceptionStatDao = appClientExceptionStatDao;
    }

    public void setAppClientVersionDao(AppClientVersionDao appClientVersionDao) {
        this.appClientVersionDao = appClientVersionDao;
    }

    public void setClientReportInstanceService(ClientReportInstanceService clientReportInstanceService) {
        this.clientReportInstanceService = clientReportInstanceService;
    }

}
