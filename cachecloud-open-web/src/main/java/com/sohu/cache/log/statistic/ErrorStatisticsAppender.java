package com.sohu.cache.log.statistic;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.google.common.util.concurrent.AtomicLongMap;
/**
 * 自定义Appender，用于统计异常名和数量键值对
 * @author leifu
 * @Time 2014年10月17日
 */
public class ErrorStatisticsAppender extends AppenderBase<ILoggingEvent> {
	/**
	 * guava的AtomicLongMap
	 */
    public static final AtomicLongMap<String> ERROR_NAME_VALUE_MAP = AtomicLongMap.create();
    @Override
    protected void append(ILoggingEvent event) {
        if (event == null) {
            return;
        }
        if (event.getLevel() == Level.ERROR || event.getLevel() == Level.WARN) {
            IThrowableProxy throwableProxy = event.getThrowableProxy();
            if (throwableProxy != null) {
            	//接口名
                String errorClassName = throwableProxy.getClassName();
                if (errorClassName != null && !"".equals(errorClassName.trim())) {
                	//写入AtomicLongMap并计数
                    ERROR_NAME_VALUE_MAP.getAndIncrement(errorClassName);
                }
            }
        }
    }
}
