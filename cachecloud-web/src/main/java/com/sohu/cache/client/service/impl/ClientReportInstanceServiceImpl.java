package com.sohu.cache.client.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.ClientReportInstanceService;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.InstanceInfo;

/**
 * @author leifu
 * @Date 2016年5月5日
 * @Time 上午11:05:35
 */
public class ClientReportInstanceServiceImpl implements ClientReportInstanceService {

    private Logger logger = LoggerFactory.getLogger(ClientReportInstanceServiceImpl.class);

    /**
     * 不要求一致性的本地缓存(hostport<=>instanceInfo)
     */
    private final static ConcurrentHashMap<String, InstanceInfo> hostPortInstanceMap = new ConcurrentHashMap<String, InstanceInfo>();

    private InstanceDao instanceDao;

    @Override
    public InstanceInfo getInstanceInfoByHostPort(String host, int port) {
        String hostPort = host + ":" + port;
        try {
            InstanceInfo instanceInfo = hostPortInstanceMap.get(hostPort);
            if (instanceInfo == null) {
                instanceInfo = instanceDao.getInstByIpAndPort(host, port);
                if (instanceInfo != null) {
                    hostPortInstanceMap.putIfAbsent(hostPort, instanceInfo);
                }
            }
            return instanceInfo;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

}
