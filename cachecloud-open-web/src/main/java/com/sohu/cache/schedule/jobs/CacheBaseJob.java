package com.sohu.cache.schedule.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * job父类，包含一个抽象函方法，将实现推迟到具体的子类
 * <p/>
 * User: lingguo
 * Date: 14-5-15
 * Time: 下午6:04
 */
public abstract class CacheBaseJob implements Job, Serializable {
    private static final long serialVersionUID = -6605766126594260961L;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final String APPLICATION_CONTEXT_KEY = "applicationContext";

    // 抽象方法，由子类实现，即具体的业务逻辑
    public abstract void action(JobExecutionContext context);

    /**
     * 统计时间
     *
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long start = System.currentTimeMillis();
        this.action(context);
        long end = System.currentTimeMillis();
        long cost = (start - end);
        if (cost > 2000) {
            logger.warn("slowJob: job: {}, trigger: {}, cost: {} ms", context.getJobDetail().getKey(),
                    context.getTrigger().getKey(), (end - start));
        } else {
            logger.info("job: {}, trigger: {}, cost: {} ms", context.getJobDetail().getKey(),
                    context.getTrigger().getKey(), (end - start));
        }
    }
}
