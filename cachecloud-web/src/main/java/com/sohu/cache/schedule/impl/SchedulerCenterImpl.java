package com.sohu.cache.schedule.impl;

import com.sohu.cache.dao.QuartzDao;
import com.sohu.cache.entity.TriggerInfo;
import com.sohu.cache.schedule.SchedulerCenter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 根据scheduler控制job、trigger和scheduler的执行和状态
 * <p/>
 * User: lingguo
 */
@Service("schedulerCenter")
public class SchedulerCenterImpl implements SchedulerCenter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 注入预定义的scheduler
    @Autowired
    private Scheduler clusterScheduler;
    @Autowired
    private QuartzDao quartzDao;

    /**
     * 删除trigger
     *
     * @param triggerKey
     * @return
     */
    @Override
    public boolean unscheduleJob(TriggerKey triggerKey) {
        boolean opResult = true;
        try {
            opResult = clusterScheduler.checkExists(triggerKey);
            if (opResult) {
                opResult = clusterScheduler.unscheduleJob(triggerKey);
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            opResult = false;
        }
        return opResult;
    }

    @Override
    public Trigger getTrigger(TriggerKey triggerKey) {
        Trigger trigger = null;
        try {
            trigger = clusterScheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        return trigger;
    }

    @Override
    public List<TriggerInfo> getAllTriggers() {
        return quartzDao.getAllTriggers();
    }

    @Override
    public List<TriggerInfo> getTriggersByNameOrGroup(String query) {
        return quartzDao.searchTriggerByNameOrGroup(query);
    }

    @Override
    public boolean pauseTrigger(TriggerKey triggerKey) {
        try {
            boolean exists = clusterScheduler.checkExists(triggerKey);
            if (exists) {
                clusterScheduler.pauseTrigger(triggerKey);
                return true;
            }
            logger.error("triggerKey={} not exists", triggerKey);
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean resumeTrigger(TriggerKey triggerKey) {
        try {
            boolean exists = clusterScheduler.checkExists(triggerKey);
            if (exists) {
                clusterScheduler.resumeTrigger(triggerKey);
                return true;
            }
            logger.error("triggerKey={} not exists", triggerKey);
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
