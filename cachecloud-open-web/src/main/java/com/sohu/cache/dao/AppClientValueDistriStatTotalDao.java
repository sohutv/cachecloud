package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppClientValueDistriSimple;
import com.sohu.cache.entity.AppClientValueDistriStatTotal;

/**
 * 
 * @author leifu
 * @Date 2016年5月5日
 * @Time 上午10:30:40
 */
public interface AppClientValueDistriStatTotalDao {

    /**
     * 按照时间区间查询客户端值分布
     * @param appId
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppClientValueDistriSimple> getAppValueDistriList(@Param("appId") long appId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    /**
     * 批量保存
     * @param appClientValueDistriStatList
     */
    void batchSave(@Param("appClientValueDistriStatTotalList") List<AppClientValueDistriStatTotal> appClientValueDistriStatTotalList);

}
