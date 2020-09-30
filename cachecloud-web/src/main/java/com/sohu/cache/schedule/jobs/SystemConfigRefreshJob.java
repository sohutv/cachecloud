package com.sohu.cache.schedule.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import com.sohu.cache.schedule.jobs.CacheBaseJob;
import com.sohu.cache.web.service.ConfigService;

/**
 * 刷新系统配置
 * @author leifu
 * @Date 2016年6月30日
 * @Time 下午5:30:42
 */
public class SystemConfigRefreshJob extends CacheBaseJob {

    private static final long serialVersionUID = 7751425759758902400L;

    @Override
    public void action(JobExecutionContext context) {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            ConfigService configService = applicationContext.getBean("configService", ConfigService.class);
            configService.reloadSystemConfig();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        
    }
}
