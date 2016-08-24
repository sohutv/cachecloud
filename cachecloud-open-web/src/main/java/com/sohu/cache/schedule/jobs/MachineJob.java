package com.sohu.cache.schedule.jobs;

import java.util.Date;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.ScheduleUtil;

/**
 * 基于机器的job
 *
 * Created by yijunzhang on 14-6-5.
 */
public class MachineJob extends CacheBaseJob {

    @Override
    public void action(JobExecutionContext context) {
        try {
            JobDataMap dataMap =  context.getMergedJobDataMap();
            Date now = new Date();
            dataMap.put(ConstUtils.TRIGGER_TIME_KEY, now);
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext)schedulerContext.get(APPLICATION_CONTEXT_KEY);
            MachineCenter machineCenter = applicationContext.getBean("machineCenter", MachineCenter.class);
            String ip = dataMap.getString(ConstUtils.HOST_KEY);
            long hostId = dataMap.getLong(ConstUtils.HOST_ID_KEY);
            machineCenter.asyncCollectMachineInfo(hostId, ScheduleUtil.getCollectTime(new Date()), ip);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
