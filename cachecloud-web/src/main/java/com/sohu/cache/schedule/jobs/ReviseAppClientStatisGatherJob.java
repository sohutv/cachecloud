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
 * Created by rucao on 2020/3/27
 */
public class ReviseAppClientStatisGatherJob extends CacheBaseJob {

    private static final long serialVersionUID = -5968147536403452672L;
    private final static String COLLECT_TIME_FORMAT = "yyyyMMdd000000";

    @Override
    public void action(JobExecutionContext context) {
        try {
            logger.warn("begin-reviseAppClientStatisGatherJob");
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            AppClientStatisticGatherService appClientStatisticGatherService = applicationContext.getBean("appClientStatisticGatherService", AppClientStatisticGatherService.class);

            //前一天
            TimeBetween timeBetween = fillWithDateFormat();
            long startTime = timeBetween.getStartTime();
            long endTime = timeBetween.getEndTime();
            appClientStatisticGatherService.bathSave(startTime, endTime);
            logger.warn("end-reviseAppClientStatisGatherJob, startTime={} endTime:{}", startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private TimeBetween fillWithDateFormat() {
        Date endDate = new Date();
        Date startDate = DateUtils.addDays(endDate, -1);
        long startTime = NumberUtils.toLong(DateUtil.formatDate(startDate, COLLECT_TIME_FORMAT));
        long endTime = NumberUtils.toLong(DateUtil.formatDate(endDate, COLLECT_TIME_FORMAT));
        return new TimeBetween(startTime, endTime, startDate, endDate);
    }
}
