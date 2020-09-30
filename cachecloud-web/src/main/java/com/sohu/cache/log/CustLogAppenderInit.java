package com.sohu.cache.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CustLogAppenderInit {

    @Autowired
    private TaskFlowRecordAppender taskFlowRecordAppender;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(CustLogAppenderInit.class);

    @PostConstruct
    public void init() {
        logger.warn("custLogAppender init begin!");
        if (LoggerFactory.getILoggerFactory() instanceof LoggerContext) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            //自定义任务流appender
            taskFlowRecordAppender.setContext(loggerContext);
            taskFlowRecordAppender.start();
            rootLogger.addAppender(taskFlowRecordAppender);
            logger.warn("custLogAppender init Done!");
        } else {
            logger.error("custLogAppender init failed , LoggerFactory.getILoggerFactory()={}",
                    LoggerFactory.getILoggerFactory());
        }
    }

}
