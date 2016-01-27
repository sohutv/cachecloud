package com.sohu.cache.jmx;

import java.util.Map;

/**
 * Created by yijunzhang on 14-2-18.
 */
public interface ErrorLoggerWatcherMBean {

    public long getTotalErrorCount();

    public Map<String, Long> getErrorInfos();

    public void clear();

}
