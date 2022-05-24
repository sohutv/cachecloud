package com.sohu.cache.web.service.impl;

import com.sohu.cache.constant.AppUserAlertEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.AppToUserDao;
import com.sohu.cache.dao.AppUserDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppToUser;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理实现
 *
 * @author leifu
 * @Date 2014年10月27日
 * @Time 上午9:57:43
 */
@Service
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 用户dao
     */
    @Autowired
    private AppUserDao appUserDao;

    /**
     * 用户应用关系dao
     */
    @Autowired
    private AppToUserDao appToUserDao;
    @Resource
    private AppDao appDao;

    @Override
    public AppUser get(Long userId) {
        return appUserDao.get(userId);
    }

    @Override
    public List<AppUser> getUserList(String chName) {
        return appUserDao.getUserList(chName);
    }

    @Override
    public List<AppUser> getAllUser() {
        return appUserDao.getAllUser();
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
    public List<AppUser> getAlertByAppId(Long appId) {
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
                if (user == null || user.getIsAlert() == AppUserAlertEnum.NO.value()) {
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

    @Override
    public SuccessEnum resetPwd(Long userId) {
        try {
            appUserDao.updatePwd(userId, ConstUtils.DEFAULT_USER_PASSWORD);
            return SuccessEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
    }

    @Override
    public SuccessEnum updatePwd(Long userId, String password) {
        try {
            appUserDao.updatePwd(userId, password);
            return SuccessEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
    }

    @Override
    public String getOfficerName(Long appId) {
        if (appId == null || appId < 0) {
            return "";
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return "";
        } else {
            return getOfficerName(appDesc.getOfficer());
        }

    }

    public String getOfficerName(String officer) {
        String officerName = "";
        if (StringUtils.isNotEmpty(officer)) {
            List<AppUser> officerList = Arrays.stream(officer.split(","))
                    .filter(userId -> get(NumberUtils.toLong(userId)) != null)
                    .map(userId -> get(NumberUtils.toLong(userId))).collect(Collectors.toList());
            List<String> userStrList = officerList.stream().map(user -> user.getChName() + "(" + user.getName() + ")").collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userStrList)) {
                officerName = StringUtils.join(userStrList, ",");
            }
        }
        return officerName;
    }

    /**
     * 获取某个应用下的所有负责人
     * @param officer
     * @return
     */
    public List<AppUser> getOfficerUserByUserIds(String officer){
        List<AppUser> officerList = new ArrayList<>();
        if (StringUtils.isNotEmpty(officer)) {
            officerList = Arrays.stream(officer.split(","))
                    .map(userId -> get(NumberUtils.toLong(userId))).filter(appUser -> appUser != null).collect(Collectors.toList());
        }
        return officerList;
    }

}
