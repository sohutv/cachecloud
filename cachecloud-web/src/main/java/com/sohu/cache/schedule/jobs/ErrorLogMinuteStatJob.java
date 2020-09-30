package com.sohu.cache.schedule.jobs;

import com.sohu.cache.dao.ErrorLogStatDao;
import com.sohu.cache.entity.ErrorLogStat;
import com.sohu.cache.log.ErrorStatisticsAppender;
import com.sohu.cache.web.util.IpUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ErrorLogMinuteStatJob extends CacheBaseJob {

    private static final long serialVersionUID = -7340448181862790770L;

    @Override
    public void action(JobExecutionContext context) {
        long startTime = System.currentTimeMillis();
        Date collectDate = new Date();
        logger.warn("ErrorLogMinuteStatJob start");
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);

            ErrorLogStatDao errorLogStatDao = applicationContext.getBean("errorLogStatDao", ErrorLogStatDao.class);
            IpUtil ipUtil = applicationContext.getBean("ipUtil", IpUtil.class);

            int port = ipUtil.getLocalPort();
            String ip = ipUtil.getLocalIP();

            Map<String, Long> errorLogMap = ErrorStatisticsAppender.ERROR_NAME_VALUE_MAP.asMap();

            for (Map.Entry<String, Long> entry : errorLogMap.entrySet()) {
                String className = entry.getKey();
                Long currentErrorCount = entry.getValue();
                if (currentErrorCount == null) {
                    continue;
                }
                Long lastErrorTotalCount = errorLogStatDao.getLastErrorCount(ip, port, className);
                long diffErrorCount;
                if (lastErrorTotalCount == null || currentErrorCount - lastErrorTotalCount < 0) {
                    diffErrorCount = currentErrorCount;
                } else {
                    diffErrorCount = currentErrorCount - lastErrorTotalCount;
                }
                if (diffErrorCount == 0) {
                    continue;
                }
                ErrorLogStat errorLogStat = new ErrorLogStat();
                errorLogStat.setIp(ip);
                errorLogStat.setPort(port);
                errorLogStat.setClassName(className);
                errorLogStat.setCollectTime(getCollectTime(collectDate));
                errorLogStat.setTotalErrorCount(currentErrorCount);
                errorLogStat.setDiffErrorCount(diffErrorCount);
                Date now = new Date();
                errorLogStat.setCreateTime(now);
                errorLogStat.setUpdateTime(now);

                errorLogStatDao.save(errorLogStat);
            }

        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        logger.warn("ErrorLogMinuteStatJob end, cost time {} ms", (System.currentTimeMillis() - startTime));
    }

    private long getCollectTime(Date collectDate) {
        return NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmmss").format(collectDate));
    }
}
