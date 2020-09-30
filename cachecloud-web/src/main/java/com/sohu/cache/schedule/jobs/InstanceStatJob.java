package com.sohu.cache.schedule.jobs;

import com.sohu.cache.inspect.impl.InstanceStateInspector;
import com.sohu.cache.util.EnvUtil;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Created by chenshi on 2020/5/27.
 */
public class InstanceStatJob extends CacheBaseJob{
    @Override
    public void action(JobExecutionContext context) {

        try {
            long startTime = System.currentTimeMillis();
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);

            Environment env = applicationContext.getBean(Environment.class);
            if (EnvUtil.isDev(env)) {
                logger.warn("environment is dev ignored");
                return;
            }

            InstanceStateInspector instanceStateInspector = applicationContext.getBean("instanceStateInspector", InstanceStateInspector.class);
            instanceStateInspector.inspect();
            logger.info("InstanceAlertValueJob cost time {} ms", (System.currentTimeMillis() - startTime));
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
