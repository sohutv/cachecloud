package com.sohu.cache.client.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.ClientVersionService;
import com.sohu.cache.dao.AppClientVersionDao;
import com.sohu.cache.entity.AppClientVersion;

/**
 * 客户端版本信息
 * 
 * @author leifu
 * @Date 2015年2月2日
 * @Time 上午10:19:59
 */
public class ClientVersionServiceImpl implements ClientVersionService {

    private final Logger logger = LoggerFactory.getLogger(ClientVersionServiceImpl.class);

    private AppClientVersionDao appClientVersionDao;

    /**
     * 办公网段ip
     */
    private static Set<String> OFFICE_IP = new HashSet<String>();
    static {
        OFFICE_IP.add("10.1");
        OFFICE_IP.add("10.2");
        OFFICE_IP.add("10.7");
    }

    @Override
    public void saveOrUpdateClientVersion(long appId, String appClientIp, String clientVersion) {
        try {
            AppClientVersion appClientVersion = new AppClientVersion();
            appClientVersion.setAppId(appId);
            appClientVersion.setClientIp(appClientIp);
            appClientVersion.setClientVersion(clientVersion);
            appClientVersion.setReportTime(new Date());
            appClientVersionDao.saveOrUpdateClientVersion(appClientVersion);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public List<AppClientVersion> getAppAllClientVersion(long appId) {
        try {
            return appClientVersionDao.getAppAllClientVersion(appId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<AppClientVersion> getAppAllServerClientVersion(long appId) {
        List<AppClientVersion> appClientVersionList = getAppAllClientVersion(appId);
        if (CollectionUtils.isEmpty(appClientVersionList)) {
            return Collections.emptyList();
        }
        List<AppClientVersion> appClientVersionServerList = new ArrayList<AppClientVersion>();
        for (AppClientVersion appClientVersion : appClientVersionList) {
            String clientIp = appClientVersion.getClientIp();
            String[] items = clientIp.split(".");
            //过滤办公网段ip
            if (!OFFICE_IP.contains(items[0] + "." + items[1])) {
                appClientVersionServerList.add(appClientVersion);
            }
        }
        return appClientVersionServerList;
    }
    
    @Override
    public List<AppClientVersion> getAll(long appId) {
        try {
            return appClientVersionDao.getAll(appId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void setAppClientVersionDao(AppClientVersionDao appClientVersionDao) {
        this.appClientVersionDao = appClientVersionDao;
    }

    

}
