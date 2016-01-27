package com.sohu.cache.alert.impl;

import java.util.List;

import org.springframework.util.Assert;

import com.sohu.cache.alert.InstanceAlertService;
import com.sohu.cache.dao.InstanceFaultDao;
import com.sohu.cache.entity.InstanceFault;

/**
 * 实例报警
 * @author leifu
 * @Date 2014年12月16日
 * @Time 下午2:02:13
 */
public class InstanceAlertServiceImpl extends BaseAlertService implements InstanceAlertService {
    
    private InstanceFaultDao instanceFaultDao;

    @Override
    public List<InstanceFault> getListByInstId(int instId) {
        Assert.isTrue(instId > 0);
        return instanceFaultDao.getListByInstId(instId);
    }

    public void setInstanceFaultDao(InstanceFaultDao instanceFaultDao) {
        this.instanceFaultDao = instanceFaultDao;
    }
}
