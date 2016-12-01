package com.sohu.cache.schedule.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import com.sohu.cache.schedule.jobs.CacheBaseJob;
import com.sohu.cache.stats.instance.InstanceAlertValueService;

/**
 * 实例分钟报警
 * @author leifu
 * @Date 2016年9月13日
 * @Time 下午3:53:04
 */
public class InstanceAlertValueJob extends CacheBaseJob {

    private static final long serialVersionUID = 1035952011763660681L;

    @Override
    public void action(JobExecutionContext context) {
        try {
            long startTime = System.currentTimeMillis();
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            InstanceAlertValueService instanceAlertValueService = applicationContext.getBean("instanceAlertValueService", InstanceAlertValueService.class);
            int instanceSize = instanceAlertValueService.monitorLastMinuteAllInstanceInfo();
            logger.warn("InstanceAlertValueJob monitor {} instance, costtime {} ms", instanceSize, (System.currentTimeMillis() - startTime));
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        
    }
}
