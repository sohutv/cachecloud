package com.sohu.cache.client.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.client.service.ClientReportCostDistriService;
import com.sohu.cache.client.service.ClientReportDataService;
import com.sohu.cache.client.service.ClientReportDataSizeService;
import com.sohu.cache.client.service.ClientReportExceptionService;
import com.sohu.cache.client.service.ClientReportValueDistriService;
import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * cachecloud客户端数据统一处理
 * 
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:01
 */
public class ClientReportDataServiceImpl implements ClientReportDataService {
    public static final String CLIENT_REPORT_POOL ="client-report-pool";
    
    private AsyncService asyncService;
    
    private final Logger logger = LoggerFactory.getLogger(ClientReportDataServiceImpl.class);
    
    private ClientReportCostDistriService clientReportCostDistriService;
    
    private ClientReportValueDistriService clientReportValueDistriService;
    
    private ClientReportExceptionService clientReportExceptionService;
    
    private ClientReportDataSizeService clientReportDataSizeService;
    
    public void init() {
        asyncService.assemblePool(getThreadPoolKey(), AsyncThreadPoolFactory.CLIENT_REPORT_THREAD_POOL);
    }
    
    private String getThreadPoolKey() {
        return CLIENT_REPORT_POOL;
    }
    
    @Override
    public boolean deal(final ClientReportBean clientReportBean) {
        try {
            // 上报的数据
            final String clientIp = clientReportBean.getClientIp();
            final List<Map<String, Object>> datas = clientReportBean.getDatas();
            if (datas == null || datas.isEmpty()) {
                logger.warn("datas field {} is empty", clientReportBean);
                return false;
            }
            String key = getThreadPoolKey() + "_" + clientIp;
            asyncService.submitFuture(getThreadPoolKey(), new KeyCallable<Boolean>(key) {
                @Override
                public Boolean execute() {
                    try {
                        
                        clientReportCostDistriService.batchSave(clientReportBean);
                        clientReportValueDistriService.batchSave(clientReportBean);
                        clientReportExceptionService.batchSave(clientReportBean);
                        clientReportDataSizeService.save(clientReportBean);
                        return true;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        return false;
                    }
                }
            });
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public void setAsyncService(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    public void setClientReportCostDistriService(ClientReportCostDistriService clientReportCostDistriService) {
        this.clientReportCostDistriService = clientReportCostDistriService;
    }

    public void setClientReportExceptionService(ClientReportExceptionService clientReportExceptionService) {
        this.clientReportExceptionService = clientReportExceptionService;
    }

    public void setClientReportDataSizeService(ClientReportDataSizeService clientReportDataSizeService) {
        this.clientReportDataSizeService = clientReportDataSizeService;
    }

    public void setClientReportValueDistriService(ClientReportValueDistriService clientReportValueDistriService) {
        this.clientReportValueDistriService = clientReportValueDistriService;
    }

}
