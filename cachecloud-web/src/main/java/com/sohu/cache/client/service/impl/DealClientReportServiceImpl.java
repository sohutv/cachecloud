package com.sohu.cache.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.client.AppClientReportModel;
import com.sohu.cache.client.service.AppClientReportCommandService;
import com.sohu.cache.client.service.AppClientReportExceptionService;
import com.sohu.cache.client.service.DealClientReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/12/13
 */
@Slf4j
public class DealClientReportServiceImpl implements DealClientReportService {
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private AppClientReportCommandService appClientReportCommandService;
    @Autowired
    private AppClientReportExceptionService appClientReportExceptionService;

    @Override
    public void init() {
        asyncService.assemblePool(getThreadPoolKey(), AsyncThreadPoolFactory.CLIENT_REPORT_THREAD_POOL);
    }

    @Override
    public boolean deal(final AppClientReportModel appClientReportModel) {
        try {
            // 上报的数据
            final long appId = appClientReportModel.getAppId();
            final String clientIp = appClientReportModel.getClientIp();
            final String redisPoolConfig = JSON.toJSONString(appClientReportModel.getConfig());
            final long currentMin = appClientReportModel.getCurrentMin();
            final List<Map<String, Object>> commandStatsModels = appClientReportModel.getCommandStatsModels();
            final List<Map<String, Object>> exceptionModels = appClientReportModel.getExceptionModels();
            String key = getThreadPoolKey() + "_" + clientIp;
            asyncService.submitFuture(getThreadPoolKey(), new KeyCallable<Boolean>(key) {
                @Override
                public Boolean execute() {
                    try {
                        if (CollectionUtils.isNotEmpty(commandStatsModels)) {
                            appClientReportCommandService.batchSave(appId, clientIp, currentMin, commandStatsModels);
                        }
                        if (CollectionUtils.isNotEmpty(exceptionModels)) {
                            appClientReportExceptionService.batchSave(appId, clientIp, redisPoolConfig, currentMin, exceptionModels);
                        }
                        return true;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return false;
                    }
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private String getThreadPoolKey() {
        return AsyncThreadPoolFactory.CLIENT_REPORT_POOL;
    }
}
