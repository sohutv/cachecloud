package com.sohu.cache.stats.machine.impl;

import com.sohu.cache.stats.machine.MachineStatsCenter;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.dao.MachineStatsDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.MachineInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 机器信息和统计查询
 * @author leifu
 * @Date 2014年06月27日
 * @Time 下午2:07:03
 */
public class MachineStatsCenterImpl implements MachineStatsCenter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MachineDao machineDao;
    private MachineStatsDao machineStatsDao;

    /**
     * 通过ip查询机器的配置等信息
     *
     * @param ip
     * @return
     */
    @Override
    public MachineInfo getMachineInfoByIp(final String ip) {
        Assert.hasLength(ip, "ip is null.");

        MachineInfo machineInfo = null;
        try {
            machineInfo = machineDao.getMachineInfoByIp(ip);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return machineInfo;
    }

    /**
     * 查询该机器上的所有实例的信息
     *
     * @param ip
     * @return
     */
    @Override
    public List<InstanceInfo> getInstInfoOfMachine(final String ip) {
        Assert.hasLength(ip, "ip is null.");

        List<InstanceInfo> instanceInfoList = null;
        try {
            instanceInfoList = machineStatsDao.getInstInfoOfMachine(ip);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instanceInfoList;
    }

    public void setMachineDao(MachineDao machineDao) {
        this.machineDao = machineDao;
    }

    public void setMachineStatsDao(MachineStatsDao machineStatsDao) {
        this.machineStatsDao = machineStatsDao;
    }

}
