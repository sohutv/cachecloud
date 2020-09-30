package com.sohu.cache.schedule.jobs;

import com.sohu.cache.util.ConstUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 清理分钟维度数据任务
 * Created by yijunzhang
 */
public class CleanupMinuteDimensionalityJob extends CacheBaseJob {

    private static final long serialVersionUID = 8815839394475276540L;

    /**
     * 实例基准数据，主要用于报警
     */
    private static final String CLEAN_STANDARD_STATISTICS = "delete from standard_statistics where collect_time < ?";

    @Override
    public void action(JobExecutionContext context) {
        if (!ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA) {
            logger.warn("whether_schedule_clean_data is false , ignored");
            return;
        }
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            JdbcTemplate jdbcTemplate = applicationContext.getBean("jdbcTemplate", JdbcTemplate.class);

            //清除进程级别统计数据(保存最近10分钟)
            long start = System.currentTimeMillis();
            Date date = DateUtils.addMinutes(new Date(), -10);
            long timeFormat = NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmm").format(date));
            int cleanCount = jdbcTemplate.update(CLEAN_STANDARD_STATISTICS, timeFormat);
            logger.warn("clean_standard_statistics timeFormat={} count={} cost:{} ms", timeFormat, cleanCount, (System.currentTimeMillis() - start));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
