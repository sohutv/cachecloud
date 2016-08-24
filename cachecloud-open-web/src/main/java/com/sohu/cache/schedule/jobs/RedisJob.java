package com.sohu.cache.schedule.jobs;

import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.ScheduleUtil;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;

/**
 * redis job类
 * <p/>
 * User: lingguo
 * Date: 14-5-13
 * Time: 下午4:11
 */
public class RedisJob extends CacheBaseJob {
    private static final long serialVersionUID = 2626836144949582163L;

    /**
     * 实现收集任务，通过RedisCenter
     *
     * @param context
     */
    @Override
    public void action(JobExecutionContext context) {
        JobDataMap dataMap = new JobDataMap();
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            RedisCenter redisCenter = (RedisCenter) applicationContext.getBean("redisCenter");
            dataMap = context.getMergedJobDataMap();
            String host = dataMap.getString(ConstUtils.HOST_KEY);
            int port = dataMap.getInt(ConstUtils.PORT_KEY);
            long appId = dataMap.getLong(ConstUtils.APP_KEY);
            Trigger trigger = context.getTrigger();
            long collectTime = ScheduleUtil.getCollectTime(trigger.getPreviousFireTime());
            redisCenter.collectRedisInfo(appId, collectTime, host, port);
        } catch (SchedulerException e) {
            logger.error("host: {}, appId: {}", dataMap.get(ConstUtils.HOST_KEY), dataMap.get(ConstUtils.APP_KEY));
            logger.error("port: {}", dataMap.get(ConstUtils.PORT_KEY));
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("host: {}, appId: {}", dataMap.get(ConstUtils.HOST_KEY), dataMap.get(ConstUtils.APP_KEY));
            logger.error("port: {}", dataMap.get(ConstUtils.PORT_KEY));
            logger.error(e.getMessage(), e);
        }
    }
}

