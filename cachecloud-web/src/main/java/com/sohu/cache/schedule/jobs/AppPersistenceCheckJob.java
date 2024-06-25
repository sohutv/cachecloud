package com.sohu.cache.schedule.jobs;

import com.sohu.cache.stats.app.AppPersistenceCheckCenter;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

/**
 * Description: 应用持久化配置检查修正
 * @author zengyizhao
 * @version 1.0
 * @date 2022/11/3
 */
public class AppPersistenceCheckJob extends CacheBaseJob {

    private static final long serialVersionUID = 7751425759758902400L;

    @Override
    public void action(JobExecutionContext context) {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            try {
                AppPersistenceCheckCenter appPersistenceCheckCenter = applicationContext.getBean("appPersistenceCheckCenter", AppPersistenceCheckCenter.class);
                appPersistenceCheckCenter.checkAndFixAppPersistence();
            } catch (Exception e) {
                logger.error("checkAndFixAppPersistence error", e.getMessage());
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }

    }
}
