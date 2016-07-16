package com.sohu.cache.client.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.AppInstanceClientRelationService;
import com.sohu.cache.dao.AppInstanceClientRelationDao;
import com.sohu.cache.entity.AppClientCostTimeStat;
import com.sohu.cache.entity.AppInstanceClientRelation;

/**
 * 应用下节点和客户端关系服务
 * 
 * @author leifu
 * @Date 2016年5月3日
 * @Time 下午6:48:40
 */
public class AppInstanceClientRelationServiceImpl implements AppInstanceClientRelationService {

    private Logger logger = LoggerFactory.getLogger(AppInstanceClientRelationServiceImpl.class);

    private AppInstanceClientRelationDao appInstanceClientRelationDao;

    @Override
    public void batchSave(List<AppClientCostTimeStat> appClientCostTimeStatList) {
        if (CollectionUtils.isEmpty(appClientCostTimeStatList)) {
            return;
        }
        try {
            List<AppInstanceClientRelation> appInstanceClientRelationList = new ArrayList<AppInstanceClientRelation>();
            for (AppClientCostTimeStat appClientCostTimeStat : appClientCostTimeStatList) {
                AppInstanceClientRelation appInstanceClientRelation = AppInstanceClientRelation.generateFromAppClientCostTimeStat(appClientCostTimeStat);
                if (appInstanceClientRelation != null) {
                    appInstanceClientRelationList.add(appInstanceClientRelation);
                }
            }
            if (CollectionUtils.isNotEmpty(appInstanceClientRelationList)) {
                appInstanceClientRelationDao.batchSave(appInstanceClientRelationList);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public List<AppInstanceClientRelation> getAppInstanceClientRelationList(Long appId, Date date) {
        try {
            return appInstanceClientRelationDao.getAppInstanceClientRelationList(appId, date);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void setAppInstanceClientRelationDao(AppInstanceClientRelationDao appInstanceClientRelationDao) {
        this.appInstanceClientRelationDao = appInstanceClientRelationDao;
    }


}
