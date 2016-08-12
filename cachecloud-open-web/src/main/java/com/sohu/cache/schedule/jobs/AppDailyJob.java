package com.sohu.cache.schedule.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import com.sohu.cache.schedule.jobs.CacheBaseJob;
import com.sohu.cache.stats.app.AppDailyDataCenter;

/**
 * 发送日报
 * @author leifu
 * @Date 2016年8月12日
 * @Time 上午11:25:09
 */
public class AppDailyJob extends CacheBaseJob {

    private static final long serialVersionUID = 7751425759758902400L;

    @Override
    public void action(JobExecutionContext context) {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            AppDailyDataCenter appDailyDataCenter = applicationContext.getBean("appDailyDataCenter", AppDailyDataCenter.class);
            appDailyDataCenter.sendAppDailyEmail();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        
    }
}
