package com.sohu.cache.alert.impl;

import java.util.List;

import org.springframework.util.Assert;

import com.sohu.cache.alert.AppAlertService;
import com.sohu.cache.dao.InstanceFaultDao;
import com.sohu.cache.entity.InstanceFault;

/**
 * 应用报警实现
 * 
 * @author leifu
 * @Date 2014年12月17日
 * @Time 上午9:51:21
 */
public class AppAlertServiceImpl extends BaseAlertService implements AppAlertService {

    private InstanceFaultDao instanceFaultDao;

    @Override
    public List<InstanceFault> getListByAppId(long appId) {
        Assert.isTrue(appId > 0);
        return instanceFaultDao.getListByAppId(appId);
    }

    public void setInstanceFaultDao(InstanceFaultDao instanceFaultDao) {
        this.instanceFaultDao = instanceFaultDao;
    }
}
