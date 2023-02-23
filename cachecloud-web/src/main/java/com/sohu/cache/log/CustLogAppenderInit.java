package com.sohu.cache.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CustLogAppenderInit implements ApplicationRunner{

    @Autowired
    private TaskFlowRecordAppender taskFlowRecordAppender;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(CustLogAppenderInit.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
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
