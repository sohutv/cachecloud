package com.sohu.cache.schedule.impl;

import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.dao.QuartzDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.TriggerInfo;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.schedule.TriggerCenter;
import com.sohu.cache.util.ConstUtils;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;

/**
 * trigger管理接口的实现
 *
 * @author: lingguo
 * @time: 2014/10/13 14:03
 */
public class TriggerCenterImpl implements TriggerCenter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Scheduler clusterScheduler;
    private QuartzDao quartzDao;
    private RedisCenter redisCenter;
    private MachineCenter machineCenter;
    private InstanceDao instanceDao;
    private MachineDao machineDao;

    /**
     * 增加一个新trigger
     *
     * @param jobGroup  trigger所属的job分组：redis/machine/machineMonitor
     * @param ip
     * @param port
     * @return
     */
    @Override
    public boolean addTrigger(String jobGroup, String ip, int port) {
        Assert.hasText(jobGroup, "jobGroup is invalid: " + jobGroup);
        Assert.hasText(ip, "ip is invalid, ip: " + ip);
        Assert.isTrue(port > 0, "port is invalid, port: " + port);

        boolean opResult = false;
        if (jobGroup.equals(ConstUtils.REDIS_JOB_GROUP)) {
            InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(ip, port);
            opResult = redisCenter.deployRedisCollection(instanceInfo.getAppId(), ip, port);
        } else if (jobGroup.equals(ConstUtils.REDIS_SLOWLOG_JOB_GROUP)) {
            InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(ip, port);
            opResult = redisCenter.deployRedisSlowLogCollection(instanceInfo.getAppId(), ip, port);
        } else if (jobGroup.equals(ConstUtils.MACHINE_JOB_GROUP)) {
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(ip);
            opResult = machineCenter.deployMachineCollection(machineInfo.getId(), ip);
        } else if (jobGroup.equals(ConstUtils.MACHINE_MONITOR_JOB_GROUP)) {
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(ip);
            opResult = machineCenter.deployMachineMonitor(machineInfo.getId(), ip);
        } else if (jobGroup.equals(ConstUtils.SERVER_TRIGGER_GROUP)) {
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(ip);
            opResult = machineCenter.deployServerCollection(machineInfo.getId(), ip);
        }
        return opResult;
    }

    /**
     * 暂停trigger
     *
     * @param triggerKey
     * @return 操作成功返回true，否则返回false；
     */
    @Override
    public boolean pauseTrigger(TriggerKey triggerKey) {
        boolean opResult = true;
        try {
            clusterScheduler.pauseTrigger(triggerKey);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            opResult = false;
        }
        return opResult;
    }

    /**
     * 恢复暂停的trigger
     *
     * @param triggerKey
     */
    @Override
    public boolean resumeTrigger(TriggerKey triggerKey) {
        boolean opResult = true;
        try {
            clusterScheduler.resumeTrigger(triggerKey);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            opResult = false;
        }
        return opResult;
    }

    /**
     * 删除一个trigger
     *
     * @param triggerKey
     * @return
     */
    @Override
    public boolean removeTrigger(TriggerKey triggerKey) {
        boolean opResult = true;
        try {
            clusterScheduler.unscheduleJob(triggerKey);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            opResult = false;
        }
        return opResult;
    }

    /**
     * 查询特定job类型下的所有trigger
     *
     * @param jobGroup job类型：redis/machine/machineMonitor
     * @return
     */
    @Override
    public List<TriggerInfo> getTriggersByJobGroup(String jobGroup) {
        List<TriggerInfo> triggersOfGroup = null;
        try {
            triggersOfGroup = quartzDao.getTriggersByJobGroup(jobGroup);
        } catch (Exception e) {
            logger.error("jobGroup: {}", jobGroup, e);
        }
        return triggersOfGroup;
    }

    /**
     * 返回所有的trigger
     *
     * @return
     */
    @Override
    public List<TriggerInfo> getAllTriggers() {
        List<TriggerInfo> allTriggers = null;
        try {
            allTriggers = quartzDao.getAllTriggers();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return allTriggers;
    }

    /**
     * 查询trigger，模糊匹配trigger name或trigger group
     *
     * @param queryString   trigger name或trigger group的关键字
     * @return
     */
    @Override
    public List<TriggerInfo> searchTriggerByNameOrGroup(String queryString) {
        List<TriggerInfo> matchTriggers = null;
        try {
            matchTriggers = quartzDao.searchTriggerByNameOrGroup(queryString);
        } catch (Exception e) {
            logger.error("queryString: {}", queryString, e);
        }
        return matchTriggers;
    }

    public void setClusterScheduler(Scheduler scheduler) {
        this.clusterScheduler = scheduler;
    }

    public void setQuartzDao(QuartzDao quartzDao) {
        this.quartzDao = quartzDao;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setMachineDao(MachineDao machineDao) {
        this.machineDao = machineDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }
}
