package com.sohu.cache.schedule.jobs;

import com.sohu.cache.web.service.AppAutoCapacityService;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

/**
 * Description: 定时更新内存使用最大值
 * @author zengyizhao
 * @version 1.0
 * @date 2022/10/11
 */
public class AppCapacityMonitorJob extends CacheBaseJob {

    @Override
    public void action(JobExecutionContext context) {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            try {
                AppAutoCapacityService appAutoCapacityService = applicationContext.getBean("appAutoCapacityService", AppAutoCapacityService.class);
                appAutoCapacityService.updateAppMemUsedHistory();
            } catch (Exception e) {
                logger.error("updateAppMemUsedHistory error {}", e.getMessage());
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
