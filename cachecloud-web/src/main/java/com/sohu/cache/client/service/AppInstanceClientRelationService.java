package com.sohu.cache.client.service;

import java.util.Date;
import java.util.List;

import com.sohu.cache.entity.AppClientCostTimeStat;
import com.sohu.cache.entity.AppInstanceClientRelation;

/**
 * 应用下节点和客户端关系服务
 * 
 * @author leifu
 * @Date 2016年5月3日
 * @Time 下午6:48:40
 */
public interface AppInstanceClientRelationService {

    void batchSave(List<AppClientCostTimeStat> appClientCostTimeStatList);

    List<AppInstanceClientRelation> getAppInstanceClientRelationList(Long appId, Date date);

}
