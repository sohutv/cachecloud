package com.sohu.cache.client.service.impl;

import com.sohu.cache.client.service.ClientVersionService;
import com.sohu.cache.dao.AppClientVersionDao;
import com.sohu.cache.entity.AppClientVersion;
import com.sohu.cache.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

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

    public String getAppMaxClientVersion(long appId) {
        try {
            return appClientVersionDao.getAppMaxClientVersion(appId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getAllMaxClientVersion() {
        try {
            return appClientVersionDao.getAllMaxClientVersion();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
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
