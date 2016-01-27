package com.sohu.cache.jmx;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.google.common.util.concurrent.AtomicLongMap;
import com.sohu.cache.log.statistic.ErrorStatisticsAppender;

import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by yijunzhang on 14-2-19.
 */
public class ErrorLoggerWatcher implements ErrorLoggerWatcherMBean {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 初始化logback 自定义appender
     */
    public void init() {
        logger.warn("ErrorStatisticsAppender init begin!");
        if (LoggerFactory.getILoggerFactory() instanceof LoggerContext) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            ErrorStatisticsAppender errorStatisticsAppender = new ErrorStatisticsAppender();
            errorStatisticsAppender.setContext(loggerContext);
            errorStatisticsAppender.start();
            rootLogger.addAppender(errorStatisticsAppender);
            logger.warn("ErrorStatisticsAppender init Done!");
        } else {
            logger.error("ErrorStatisticsAppender init failed , LoggerFactory.getILoggerFactory()={}", LoggerFactory.getILoggerFactory());
        }

    }

    @Override
    public long getTotalErrorCount() {
        AtomicLongMap<String> atomicLongMap = ErrorStatisticsAppender.ERROR_NAME_VALUE_MAP;
        return atomicLongMap.sum();
    }

    @Override
    public Map<String, Long> getErrorInfos() {
        Map<String, Long> longMap = ErrorStatisticsAppender.ERROR_NAME_VALUE_MAP.asMap();
        Map<String, Long> resultMap = new LinkedHashMap<String, Long>();
        //排序
        SortedMap<Long, List<String>> sortedMap = new TreeMap<Long, List<String>>();
        for (Map.Entry<String, Long> entry : longMap.entrySet()) {
            String key = entry.getKey();
            Long num = entry.getValue();
            if (num == 0L) {
                continue;
            }
            if (sortedMap.containsKey(num)) {
                sortedMap.get(num).add(key);
            } else {
                List<String> keys = new ArrayList<String>();
                keys.add(key);
                sortedMap.put(num, keys);
            }
        }
        List<Long> keys = new ArrayList<Long>(sortedMap.keySet());
        Collections.reverse(keys);

        for (Long num : keys) {
            for (String key : sortedMap.get(num)) {
                resultMap.put(key, num);
            }
        }
        return resultMap;
    }

    @Override
    public void clear() {
        ErrorStatisticsAppender.ERROR_NAME_VALUE_MAP.clear();
    }
}
