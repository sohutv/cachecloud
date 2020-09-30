package com.sohu.cache.schedule.brevity;

import com.sohu.cache.schedule.jobs.CacheBaseJob;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.springframework.context.ApplicationContext;

/**
 * Created by yijunzhang
 */
public class BrevitySchedulerJob extends CacheBaseJob {
    private static final long serialVersionUID = 2626836144949582163L;

    /**
     * 维护短频任务
     *
     * @param context
     */
    @Override
    public void action(JobExecutionContext context) {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            BrevityScheduler brevityScheduler = applicationContext.getBean(BrevityScheduler.class);
            brevityScheduler.maintainTasks();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}