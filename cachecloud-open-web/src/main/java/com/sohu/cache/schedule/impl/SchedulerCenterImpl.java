package com.sohu.cache.schedule.impl;

import com.sohu.cache.dao.QuartzDao;
import com.sohu.cache.entity.TriggerInfo;
import com.sohu.cache.schedule.SchedulerCenter;

import org.apache.commons.lang.time.DateUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * 根据scheduler控制job、trigger和scheduler的执行和状态
 * <p/>
 * User: lingguo
 * Date: 14-5-18
 * Time: 下午10:15
 */
public class SchedulerCenterImpl implements SchedulerCenter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 注入预定义的scheduler
    private Scheduler clusterScheduler;

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
    public boolean deployJobByCron(JobKey jobKey, TriggerKey triggerKey, Map<String, Object> dataMap, String cron, boolean replace) {
        Assert.isTrue(jobKey != null);
        Assert.isTrue(triggerKey != null);
        Assert.isTrue(CronExpression.isValidExpression(cron), "invalid cron = " + cron);
        try {
            JobDetail jobDetail = clusterScheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                logger.error("JobKey {}:{} is not exist", jobKey.getName(), jobKey.getGroup());
                return false;
            }
            fireCronTrigger(triggerKey, jobDetail, cron, replace, dataMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public boolean deployJobByDelay(JobKey jobKey, TriggerKey triggerKey, Map<String, Object> dataMap, int  delaySeconds, boolean replace) {
        Assert.isTrue(jobKey != null);
        Assert.isTrue(triggerKey != null);
        Assert.isTrue(delaySeconds > 0);
        try {
            JobDetail jobDetail = clusterScheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                logger.error("JobKey {}:{} is not exist", jobKey.getName(), jobKey.getGroup());
                return false;
            }
            fireSimpleTrigger(triggerKey, jobDetail, replace, dataMap, delaySeconds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private boolean fireSimpleTrigger(TriggerKey triggerKey, JobDetail jobDetail, boolean replace, Map<String, Object> dataMap,int delaySeconds) {
        try {
            boolean isExists = clusterScheduler.checkExists(triggerKey);
            if (isExists) {
                if (replace) {
                    logger.warn("replace trigger={}:{} ", triggerKey.getName(), triggerKey.getGroup());
                    clusterScheduler.unscheduleJob(triggerKey);
                } else {
                    logger.info("exist trigger={}:{} ", triggerKey.getName(), triggerKey.getGroup());
                    return false;
                }
            }
            Date startAtDate = new Date(System.currentTimeMillis() + delaySeconds * 1000);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail)
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(1)
                            .withRepeatCount(0)
                            )
                    .startAt(startAtDate)
                    .build();
            if (dataMap != null && dataMap.size() > 0) {
                trigger.getJobDataMap().putAll(dataMap);
            }
            clusterScheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private boolean fireCronTrigger(TriggerKey triggerKey, JobDetail jobDetail, String cron, boolean replace, Map<String, Object> dataMap) {
        try {
            boolean isExists = clusterScheduler.checkExists(triggerKey);
            if (isExists) {
                if (replace) {
                    logger.warn("replace trigger={}:{} ", triggerKey.getName(), triggerKey.getGroup());
                    clusterScheduler.unscheduleJob(triggerKey);
                } else {
                    logger.info("exist trigger={}:{} ", triggerKey.getName(), triggerKey.getGroup());
                    return false;
                }
            }
            Date startDate = DateUtils.addSeconds(new Date(), 20);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .startAt(startDate)
                    .build();
            if (dataMap != null && dataMap.size() > 0) {
                trigger.getJobDataMap().putAll(dataMap);
            }
            clusterScheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
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

    public void setClusterScheduler(Scheduler clusterScheduler) {
        this.clusterScheduler = clusterScheduler;
    }

    public void setQuartzDao(QuartzDao quartzDao) {
        this.quartzDao = quartzDao;
    }
}
