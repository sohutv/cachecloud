package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppClientValueDistriSimple;
import com.sohu.cache.entity.AppClientValueDistriStatTotal;

/**
 * 
 * @author leifu
 * @Date 2016年5月9日
 * @Time 下午5:25:52
 */
public interface AppClientValueStatDao {

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
    
    
    /**
     * 保存
     * @param appClientValueDistriStatTotal
     */
    int save(AppClientValueDistriStatTotal appClientValueDistriStatTotal);

    /**
     * 删除指定收集时间前的数据
     * @param collectTime
     * @return
     */
    int deleteBeforeCollectTime(@Param("collectTime") long collectTime);

}
