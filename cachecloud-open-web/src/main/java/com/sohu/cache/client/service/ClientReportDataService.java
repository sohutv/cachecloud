package com.sohu.cache.client.service;

import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * cachecloud客户端上报数据处理
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:01
 */
public interface ClientReportDataService {
    
    /**
     * 处理上报数据
     * @param clientReportBean
     */
    public boolean deal(ClientReportBean clientReportBean);
    
}
