package com.sohu.cache.dao;

import com.sohu.cache.entity.AppClientDataSizeStat;

/**
 * 客户端内收集数据map的尺寸查询
 * @author leifu
 * @Date 2015年7月13日
 * @Time 下午3:26:48
 */
public interface AppClientReportDataSizeDao {
    
    /**
     * 保存-客户端收集map的尺寸信息
     * @param appClientDataSizeStat
     */
    void save(AppClientDataSizeStat appClientDataSizeStat);
}
