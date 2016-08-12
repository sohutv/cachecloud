package com.sohu.cache.dao;

import com.sohu.cache.entity.InstanceSlowLog;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 实例慢查询dao
 * 
 * @author leifu
 * @Date 2016年2月22日
 * @Time 下午1:48:43
 */
public interface InstanceSlowLogDao {

    /**
     * 批量报错实例慢查询
     * @param instanceSlowLogList
     */
    int batchSave(@Param("instanceSlowLogList") List<InstanceSlowLog> instanceSlowLogList);

    /**
     * 按照应用id获取慢查询列表
     * @param appId
     * @return
     */
    List<InstanceSlowLog> getByAppId(@Param("appId") long appId);

    /**
     * 搜索慢查询日志
     * @param appId
     * @param startTime
     * @param endTime
     * @param limit
     * @return
     */
    List<InstanceSlowLog> search(@Param("appId") long appId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    List<Map<String, Object>> getInstanceSlowLogCountMapByAppId(@Param("appId") long appId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    
    /**
     * 获取指定日期慢查询个数
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    int getAppSlowLogCount(@Param("appId") long appId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    
}
