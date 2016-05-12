package com.sohu.cache.client.service;

import com.sohu.cache.entity.InstanceInfo;

/**
 * 
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:28
 */
public interface ClientReportInstanceService {
    
    /**
     * 根据host:port获取instance信息(缓存，不要求一致性)
     * @param host
     * @param port
     * @return
     */
    InstanceInfo getInstanceInfoByHostPort(String host, int port);
}
