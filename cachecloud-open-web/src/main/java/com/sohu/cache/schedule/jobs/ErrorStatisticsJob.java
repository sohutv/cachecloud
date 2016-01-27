package com.sohu.cache.schedule.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.sohu.cache.jmx.ErrorLoggerWatcherMBean;
import com.sohu.cache.schedule.jobs.CacheBaseJob;
import com.sohu.cache.web.component.EmailComponent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 异常统计
 * @author leifu
 * @Date 2014年10月31日
 * @Time 上午11:05:42
 */
public class ErrorStatisticsJob extends CacheBaseJob {
    private static final long serialVersionUID = 3566693097569373471L;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void action(JobExecutionContext context) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SchedulerContext schedulerContext;
        try {
            schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            EmailComponent emailComponent = applicationContext.getBean("emailComponent", EmailComponent.class);
            ErrorLoggerWatcherMBean errorLoggerWatcher = applicationContext.getBean("errorLoggerWatcher", ErrorLoggerWatcherMBean.class);
//            if (errorLoggerWatcher.getTotalErrorCount() == 0L) {
//                logger.warn("errorLoggerWatcher.totalErrorCount == 0 -o-");
//                return;
//            }
            String title = "CacheCloud异常统计， 日期:" + dateFormat.format(date) + ";服务器:" + System.getProperty("local.ip") + ";总数:" + errorLoggerWatcher.getTotalErrorCount();

            StringBuilder buffer = new StringBuilder();
            buffer.append(title + ":<br/>");
            for (Map.Entry<String, Long> entry : errorLoggerWatcher.getErrorInfos().entrySet()) {
                Long num = entry.getValue();
                if (num == 0L) {
                    continue;
                }
                String key = entry.getKey();
                buffer.append(key + "=" + num + "<br/>");
            }

            emailComponent.sendMailToAdmin(title, buffer.toString());
            //清理异常
            errorLoggerWatcher.clear();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        
    }
}
