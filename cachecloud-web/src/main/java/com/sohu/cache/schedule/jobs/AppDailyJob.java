package com.sohu.cache.schedule.jobs;

import com.sohu.cache.stats.app.AppDailyDataCenter;
import com.sohu.cache.util.EnvUtil;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 发送日报
 *
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
            Environment env = applicationContext.getBean(Environment.class);
            if (EnvUtil.isDev(env)) {
                logger.warn("environment is dev ignored");
                return;
            }

            try {
                AppDailyDataCenter appDailyDataCenter = applicationContext.getBean("appDailyDataCenter", AppDailyDataCenter.class);
                appDailyDataCenter.sendAppDailyEmail();
            } catch (Exception e) {
                logger.error("sendAppDailyEmail error", e.getMessage());
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }

    }
}
