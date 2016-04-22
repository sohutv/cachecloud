package com.sohu.tv.cache.client.common.jmx;

import java.util.Map;


/**
 * 监控cachecloud数据收集
 * @author leifu
 * @Date 2015年1月25日
 * @Time 上午10:30:13
 */
public interface CachecloudDataWatcherMBean {

    public Map<String, Map<Integer,Long>> getCostTimeMap();
    
    public Map<String, Long> getCostTimeGroupByMinute();
    
    public Map<String, Map<String,Long>> getCostTimeGroupByMinuteAndCommand();

    public Map<String, Map<String,Long>> getExceptionMap();
    
    public Map<String, Map<String,Long>> getValueLengthMap();
    
    /**
     * 收集数据本身的耗时
     * @return
     */
    public Map<String, Map<Long,Long>> getCollectionCostTimeMap();

}
