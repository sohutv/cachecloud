package com.sohu.cache.schedule.jobs;

import com.sohu.cache.client.service.ClientReportCostDistriService;
import com.sohu.cache.client.service.ClientReportValueDistriService;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 清理天维度数据任务
 * Created by yijunzhang
 */
public class CleanupDayDimensionalityJob extends CacheBaseJob {

    private static final long serialVersionUID = 8815839394475276540L;

    private static int BATCH_SIZE = 1000;

    private static final String CLEAN_APP_HOUR_COMMAND_STATISTICS = "delete from app_hour_command_statistics where create_time < ? limit " + BATCH_SIZE;

    private static final String CLEAN_APP_MINUTE_COMMAND_STATISTICS = "delete from app_minute_command_statistics where create_time < ? limit " + BATCH_SIZE;

    private static final String CLEAN_APP_HOUR_STATISTICS = "delete from app_hour_statistics where create_time < ? limit " + BATCH_SIZE;

    private static final String CLEAN_APP_MINUTE_STATISTICS = "delete from app_minute_statistics where create_time < ? limit " + BATCH_SIZE;
    /**
     * 清除客户端耗时汇总数据
     */
    private static final String CLEAN_APP_CLIENT_MINUTE_COST_TOTAL = "delete from app_client_costtime_minute_stat_total where collect_time < ? limit " + BATCH_SIZE;

    //清除服务器统计数据
    private static final String CLEAN_SERVER_STAT_STATISTICS = "delete from server_stat where cdate < ? limit " + BATCH_SIZE;

    /**
     * 清除实例基础统计
     */
    private static final String CLEAN_INSTANCE_MINUTE_STATS = "delete from instance_minute_stats where collect_time < ? limit " + BATCH_SIZE;

    JdbcTemplate jdbcTemplate = null;

    @Override
    public void action(JobExecutionContext context) {
        if (!ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA) {
            logger.warn("whether_schedule_clean_data is false , ignored");
            return;
        }
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            jdbcTemplate = applicationContext.getBean("jdbcTemplate", JdbcTemplate.class);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            // 清除应用&命令统计数据(保存31天)
            calendar.add(Calendar.DAY_OF_MONTH, -31);
            Date time = calendar.getTime();
            long cleanCount = 0;
            cleanCount = scrollDelete(CLEAN_APP_HOUR_COMMAND_STATISTICS, time);
            logger.warn("clean_app_hour_command_statistics count={}", cleanCount);
            cleanCount = scrollDelete(CLEAN_APP_MINUTE_COMMAND_STATISTICS, time);
            logger.warn("clean_app_minute_command_statistics count={}", cleanCount);
            cleanCount = scrollDelete(CLEAN_APP_HOUR_STATISTICS, time);
            logger.warn("clean_app_hour_statistics count={}", cleanCount);
            cleanCount = scrollDelete(CLEAN_APP_MINUTE_STATISTICS, time);
            logger.warn("clean_app_minute_statistics count={}", cleanCount);

            //清除服务器统计数据
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            cleanCount = scrollDelete(CLEAN_SERVER_STAT_STATISTICS, date);
            logger.warn("clean_server_stat_total count={}", cleanCount);

            long timeFormat = NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmm00").format(calendar.getTime()));
            //清除进程级别统计数据(保存5天)
            long start = System.currentTimeMillis();
            timeFormat = NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmm").format(DateUtils.addDays(new Date(), -5)));
            cleanCount = scrollDelete(CLEAN_INSTANCE_MINUTE_STATS, timeFormat);
            logger.warn("clean_instance_minute_stats timeFormat={} count={} cost={}s", timeFormat, cleanCount, (System.currentTimeMillis() - start) / 1000);

            //注销此逻辑，其操作的表已废弃，待统一删除
            //清除客户端耗时数据(保存2天)
//            ClientReportCostDistriService clientReportCostDistriService = applicationContext.getBean(
//                    "clientReportCostDistriService", ClientReportCostDistriService.class);
//            calendar.setTime(new Date());
//            calendar.add(Calendar.DAY_OF_MONTH, -2);
//            timeFormat = NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmm00").format(calendar.getTime()));
//            cleanCount = clientReportCostDistriService.deleteBeforeCollectTime(timeFormat);
//            logger.warn("clean_app_client_costtime_minute_stat count={}", cleanCount);

            //清除客户端耗时汇总数据(保存14天)
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -14);
            timeFormat = NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmm00").format(calendar.getTime()));
            cleanCount = jdbcTemplate.update(CLEAN_APP_CLIENT_MINUTE_COST_TOTAL, timeFormat);
            logger.warn("clean_app_client_costtime_minute_stat_total count={}", cleanCount);

            //清除客户端值数据(保存2天)
            ClientReportValueDistriService clientReportValueDistriService = applicationContext.getBean(
                    "clientReportValueDistriService", ClientReportValueDistriService.class);
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -2);
            timeFormat = NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmm00").format(calendar.getTime()));
            cleanCount = clientReportValueDistriService.deleteBeforeCollectTime(timeFormat);
            logger.warn("clean_app_client_value_minute_stats count={}", cleanCount);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * 滚动删除表数据
     */
    private long scrollDelete(String sql, Object time) {
        long totalCount = 0;
        while (true) {
            int cleanCount = jdbcTemplate.update(sql, time);
            totalCount += cleanCount;
            if (cleanCount == 0) {
                break;
            }
        }
        return totalCount;
    }

}
