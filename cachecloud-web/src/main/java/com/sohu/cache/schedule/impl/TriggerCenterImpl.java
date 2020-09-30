package com.sohu.cache.schedule.impl;

import com.sohu.cache.dao.QuartzDao;
import com.sohu.cache.entity.TriggerInfo;
import com.sohu.cache.schedule.TriggerCenter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * trigger管理接口的实现
 *
 * @author: lingguo
 * @time: 2014/10/13 14:03
 */
@Service("triggerCenter")
public class TriggerCenterImpl implements TriggerCenter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Scheduler clusterScheduler;
    @Autowired
    private QuartzDao quartzDao;

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
}
