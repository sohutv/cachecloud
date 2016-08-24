package com.sohu.cache.schedule.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.util.ConstUtils;

/**
 * 监控机器的状态信息的job
 *
 * User: lingguo
 * Date: 14-7-2
 */
public class MachineMonitorJob extends CacheBaseJob {
    @Override
    public void action(JobExecutionContext context) {
        try {
            JobDataMap dataMap = context.getMergedJobDataMap();
            String ip = dataMap.getString(ConstUtils.HOST_KEY);
            long hostId = dataMap.getLong(ConstUtils.HOST_ID_KEY);

            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            MachineCenter machineCenter = applicationContext.getBean("machineCenter", MachineCenter.class);
            machineCenter.asyncMonitorMachineStats(hostId, ip);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
