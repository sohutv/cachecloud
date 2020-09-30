package com.sohu.cache.schedule.jobs;

import com.sohu.cache.util.ConstUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by rucao on 2020/1/8
 */
public class CleanupDayAppClientStatJob extends CacheBaseJob {
    private static final long serialVersionUID = 8815839394475276540L;
    /**
     * 清除命令统计&异常统计
     */
    private static final String CLEAN_APP_CLIENT_COMMAND_MINUTE_STATISTICS = "delete from app_client_command_minute_statistics where current_min < ?";
    private static final String CLEAN_APP_CLIENT_EXCEPTION_MINUTE_STATISTICS = "delete from app_client_exception_minute_statistics where current_min < ?";
    private static final String CLEAN_APP_CLIENT_LATENCY_COMMAND = "delete from app_client_latency_command where create_time < ?";
    private static final String CLEAN_APP_CLIENT_STATISTIC_GATHER = "delete from app_client_statistic_gather where gather_time < ?";
    private static final String CLEAN_INSTANCE_LATENCY_HISTORY = "delete from instance_latency_history where execute_date < ?";

    @Override
    public void action(JobExecutionContext context) {
        if (!ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA) {
            logger.warn("whether_schedule_clean_data is false , ignored");
            return;
        }
        try {
            logger.warn("begin-CleanupDayAppClientStatJob");
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            JdbcTemplate jdbcTemplate = applicationContext.getBean("jdbcTemplate", JdbcTemplate.class);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -14);
            long timeFormat = NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmm00").format(calendar.getTime()));
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

            /**
             * 清除命令统计&异常统计（保存14天）
             */
            int cleanCount = jdbcTemplate.update(CLEAN_APP_CLIENT_COMMAND_MINUTE_STATISTICS, timeFormat);
            logger.warn("clean_app_client_command_minute_statistics count={}", cleanCount);
            cleanCount = jdbcTemplate.update(CLEAN_APP_CLIENT_EXCEPTION_MINUTE_STATISTICS, timeFormat);
            logger.warn("clean_app_client_exception_minute_statistics count={}", cleanCount);
            cleanCount = jdbcTemplate.update(CLEAN_APP_CLIENT_LATENCY_COMMAND, calendar.getTime());
            logger.warn("clean_app_client_latency_command count={}", cleanCount);
            cleanCount = jdbcTemplate.update(CLEAN_APP_CLIENT_STATISTIC_GATHER, date);
            logger.warn("clean_app_client_statistic_gather count={}", cleanCount);
            cleanCount = jdbcTemplate.update(CLEAN_INSTANCE_LATENCY_HISTORY, timeFormat);
            logger.warn("clean_instance_latency_history count={}", cleanCount);

            logger.warn("end-CleanupDayAppClientStatJob");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
