package com.sohu.cache.schedule.jobs;

import com.sohu.cache.client.service.AppClientStatisticGatherService;
import com.sohu.cache.entity.TimeBetween;
import com.sohu.cache.web.util.DateUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.springframework.context.ApplicationContext;

import java.util.Date;

/**
 * Description: server端 命令调用次数统计
 * @author zengyizhao
 * @version 1.0
 * @date 2024/05/14
 */
public class GatherAppClientStatisticsServerCmdCountJob extends CacheBaseJob {
    private final static String COLLECT_TIME_FORMAT = "yyyyMMddHH";

    @Override
    public void action(JobExecutionContext context) {
        try {
            logger.warn("begin-gatherAppClientStatisticsServerCmdCountJob");
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            AppClientStatisticGatherService appClientStatisticGatherService = applicationContext.getBean("appClientStatisticGatherService", AppClientStatisticGatherService.class);

            //前5-10分钟
            TimeBetween timeBetween = fillWithDateFormat();
            long startTime = timeBetween.getStartTime();
            long endTime = timeBetween.getEndTime();

            appClientStatisticGatherService.bathAddServerCmdCount(startTime, endTime);
            logger.warn("end-gatherAppClientStatisticsServerCmdCountJob, startTime={} endTime:{}", startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private TimeBetween fillWithDateFormat() {
        Date endDate = DateUtils.addHours(new Date(), -1);
        Date startDate = endDate;
        long startTime = NumberUtils.toLong(DateUtil.formatDate(startDate, COLLECT_TIME_FORMAT));
        long endTime = startTime;
        return new TimeBetween(startTime, endTime, startDate, endDate);
    }
}
