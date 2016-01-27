package com.sohu.cache.schedule.jobs;

import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.util.ConstUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.springframework.context.ApplicationContext;

/**
 * Created by yijunzhang on 14-12-16.
 */
public class RebuildMasterSlaveJob extends CacheBaseJob {

    private static final long serialVersionUID = 2693867111598545345L;

    /**
     * 整理redisJob
     * @param context
     */
    @Override
    public void action(JobExecutionContext context) {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            JobDataMap dataMap = context.getMergedJobDataMap();
            Long appId = dataMap.getLong(ConstUtils.APP_KEY);
            if (appId == null) {
                throw new RuntimeException("appId is null");
            }
            RedisDeployCenter redisDeployCenter = applicationContext.getBean("redisDeployCenter", RedisDeployCenter.class);
            boolean isOk = redisDeployCenter.rebuildMasterSlave(appId);
            logger.warn("appId={} rebuildMasterSlave={}", appId, isOk);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
