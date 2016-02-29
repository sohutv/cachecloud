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
 * redis慢查询job
 * 
 * @author leifu
 * @Date 2016年2月22日
 * @Time 上午9:38:49
 */
public class RedisSlowLogJob extends CacheBaseJob {
    private static final long serialVersionUID = 2626836144949582163L;

    @Override
    public void action(JobExecutionContext context) {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            RedisCenter redisCenter = (RedisCenter) applicationContext.getBean("redisCenter");
            JobDataMap dataMap = context.getMergedJobDataMap();
            String host = dataMap.getString(ConstUtils.HOST_KEY);
            int port = dataMap.getInt(ConstUtils.PORT_KEY);
            long appId = dataMap.getLong(ConstUtils.APP_KEY);
            Trigger trigger = context.getTrigger();
            long collectTime = ScheduleUtil.getCollectTime(trigger.getPreviousFireTime());
            redisCenter.collectRedisSlowLog(appId, collectTime, host, port);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
