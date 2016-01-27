package com.sohu.cache.web.service.impl;

import java.util.List;

import com.sohu.cache.dao.MemFaultDao;
import com.sohu.cache.entity.InstanceFault;
import com.sohu.cache.web.service.MemFaultService;

/**
 * 故障服务
 * @author leifu
 * @Date 2015-6-6
 * @Time 下午10:03:59
 */
public class MemFaultServiceImpl implements MemFaultService {

    /**
     * 故障Dao
     */
    private MemFaultDao memFaultDao;

    @Override
    public List<InstanceFault> getFaultList() {
        return memFaultDao.getMemFaultList();
    }

    public void setMemFaultDao(MemFaultDao memFaultDao) {
        this.memFaultDao = memFaultDao;
    }

}
