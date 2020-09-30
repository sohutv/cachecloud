package com.sohu.cache.inspect;

import com.sohu.cache.schedule.jobs.CacheBaseJob;
import com.sohu.cache.util.EnvUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Created by yijunzhang
 */
public class InspectorJob extends CacheBaseJob {
    private static final long serialVersionUID = -4277329946053271489L;

    @Override
    public void action(JobExecutionContext context) {
        try {
            long start = System.currentTimeMillis();
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);

            Environment env = applicationContext.getBean(Environment.class);
            if (EnvUtil.isDev(env)) {
                logger.warn("environment is dev ignored");
                return;
            }
            // 应用相关
            InspectHandler inspectHandler;
            JobDataMap jobDataMap = context.getMergedJobDataMap();
            String inspectorType = MapUtils.getString(jobDataMap, "inspectorType");
            if (StringUtils.isBlank(inspectorType)) {
                logger.error("=====================InspectorJob:inspectorType is null=====================");
                return;
            } else if (inspectorType.equals("host")) {
                inspectHandler = applicationContext.getBean("hostInspectHandler", InspectHandler.class);
            } else if (inspectorType.equals("app")) {
                inspectHandler = applicationContext.getBean("appInspectHandler", InspectHandler.class);
            } else {
                logger.error("=====================InspectorJob:inspectorType not match:{}=====================", inspectorType);
                return;
            }
            inspectHandler.handle();
            long end = System.currentTimeMillis();
            logger.info("=====================InspectorJob {} Done! cost={} ms=====================",
                    inspectHandler.getClass().getSimpleName(), (end - start));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
