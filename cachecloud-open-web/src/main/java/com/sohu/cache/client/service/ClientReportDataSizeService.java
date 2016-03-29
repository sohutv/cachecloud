package com.sohu.cache.client.service;

import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * 记录客户端统计map的尺寸
 * 
 * @author leifu
 * @Date 2015年7月13日
 * @Time 下午3:00:40
 */
public interface ClientReportDataSizeService {

    void save(ClientReportBean clientReportBean);

}
