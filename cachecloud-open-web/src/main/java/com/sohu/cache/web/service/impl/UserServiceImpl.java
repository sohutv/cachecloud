package com.sohu.cache.web.service.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.dao.AppToUserDao;
import com.sohu.cache.dao.AppUserDao;
import com.sohu.cache.entity.AppToUser;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.UserService;

/**
 * 用户管理实现
 * @author leifu
 * @Date 2014年10月27日
 * @Time 上午9:57:43
 */
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 用户dao
     */
    private AppUserDao appUserDao;

    /**
     * 用户应用关系dao
     */
    private AppToUserDao appToUserDao;

    @Override
    public AppUser get(Long userId) {
        return appUserDao.get(userId);
    }

    @Override
    public List<AppUser> getUserList(String chName) {
        return appUserDao.getUserList(chName);
    }

    @Override
    public List<AppUser> getByAppId(Long appId) {
        if (appId == null || appId < 0) {
            return Collections.emptyList();
        }
        List<AppUser> resultList = new ArrayList<AppUser>();
        List<AppToUser> appToUsers = appToUserDao.getByAppId(appId);
        if (appToUsers != null && appToUsers.size() > 0) {
            for (AppToUser appToUser : appToUsers) {
                Long userId = appToUser.getUserId();
                if (userId == null) {
                    continue;
                }
                AppUser user = appUserDao.get(userId);
                if (user == null) {
                    continue;
                }
                resultList.add(user);
            }
        }
        return resultList;
    }

    @Override
    public AppUser getByName(String name) {
        try {
            return appUserDao.getByName(name);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SuccessEnum save(AppUser appUser) {
        try {
            appUserDao.save(appUser);
            return SuccessEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
    }

    @Override
    public SuccessEnum update(AppUser appUser) {
        try {
            appUserDao.update(appUser);
            return SuccessEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
    }

    @Override
    public SuccessEnum delete(Long userId) {
        try {
            appUserDao.delete(userId);
            return SuccessEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
    }

    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    public void setAppToUserDao(AppToUserDao appToUserDao) {
        this.appToUserDao = appToUserDao;
    }
}
