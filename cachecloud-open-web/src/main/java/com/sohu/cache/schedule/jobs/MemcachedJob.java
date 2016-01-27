package com.sohu.cache.schedule.jobs;

import com.sohu.cache.memcached.MemcachedCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.ScheduleUtil;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;

/**
 * memcached job类
 *
 * User: lingguo
 * Date: 14-5-13
 * Time: 下午4:10
 */
public class MemcachedJob extends CacheBaseJob {
    private static final long serialVersionUID = -9212639022810995401L;

    /**
     * 执行收集任务，通过IMemcachedCenter
     * @param context
     */
    @Override
    public void action(JobExecutionContext context) {

        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext)schedulerContext.get(APPLICATION_CONTEXT_KEY);
            MemcachedCenter memcachedCenter = (MemcachedCenter)applicationContext.getBean("memcachedCenter");
            JobDataMap dataMap = context.getMergedJobDataMap();
            String host = dataMap.getString(ConstUtils.HOST_KEY);
            int port = dataMap.getInt(ConstUtils.PORT_KEY);
            long appId = dataMap.getLong(ConstUtils.APP_KEY);
            Trigger trigger = context.getTrigger();
            long collectTime = ScheduleUtil.getCollectTime(trigger.getPreviousFireTime());
            memcachedCenter.collectMemcachedInfo(appId, collectTime, host, port);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
