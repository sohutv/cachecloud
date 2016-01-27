package com.sohu.cache.jmx;

/**
 * Created by yijunzhang on 14-7-21.
 */
public interface ThreadPoolStatisticsMBean {

    /**
     * @return 获取当前线程池使用率
     */
    double getUsedPercentage();

    /**
     * @return 返回主动执行任务的近似线程数
     */
    int getActiveCount();

    /**
     * @return 返回已完成执行的近似任务总数
     */
    long getCompletedTaskCount();

    /**
     * @return 返回核心线程数
     */
    int getCorePoolSize();

    /**
     * @return 返回曾经同时位于池中的最大线程数。
     */
    int getLargestPoolSize();

    /**
     * @return 返回允许的最大线程数
     */
    int getMaximumPoolSize();

    /**
     * @return 返回池中的当前线程数。
     */
    int getPoolSize();

    /**
     * @return 返回曾计划执行的近似任务总数
     */
    long getTaskCount();

}
