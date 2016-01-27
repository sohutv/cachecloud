package com.sohu.cache.jmx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by yijunzhang on 14-7-21.
 */
public class ThreadPoolStatisticsImpl implements ThreadPoolStatisticsMBean {

    private final ThreadPoolExecutor threadPoolExecutor;

    public ThreadPoolStatisticsImpl(ExecutorService executorService) {
        if (executorService != null && executorService instanceof ThreadPoolExecutor) {
            this.threadPoolExecutor = (ThreadPoolExecutor) executorService;
        } else {
            throw new IllegalArgumentException("executorService not instanceof ThreadPoolExecutor");
        }
    }

    @Override
    public double getUsedPercentage() {
        if (threadPoolExecutor == null) {
            return 0;
        }
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        long taskCount = threadPoolExecutor.getTaskCount();
        long runningCount = (taskCount - completedTaskCount);
        if (runningCount < 0L) {
            runningCount = 0L;
        }
        long maxPoolSize = threadPoolExecutor.getMaximumPoolSize();

        double run = Double.valueOf(runningCount);
        double size = Double.valueOf(maxPoolSize);
        if (run == 0d || size == 0d) {
            return 0D;
        }
        return run / size;
    }

    @Override
    public int getActiveCount() {
        if (threadPoolExecutor == null) {
            return 0;
        }
        return threadPoolExecutor.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        if (threadPoolExecutor == null) {
            return 0;
        }
        return threadPoolExecutor.getCompletedTaskCount();
    }

    @Override
    public int getCorePoolSize() {
        if (threadPoolExecutor == null) {
            return 0;
        }
        return threadPoolExecutor.getCorePoolSize();
    }

    @Override
    public int getLargestPoolSize() {
        if (threadPoolExecutor == null) {
            return 0;
        }
        return threadPoolExecutor.getLargestPoolSize();
    }

    @Override
    public int getMaximumPoolSize() {
        if (threadPoolExecutor == null) {
            return 0;
        }
        return threadPoolExecutor.getMaximumPoolSize();
    }

    @Override
    public int getPoolSize() {
        if (threadPoolExecutor == null) {
            return 0;
        }
        return threadPoolExecutor.getPoolSize();
    }

    @Override
    public long getTaskCount() {
        if (threadPoolExecutor == null) {
            return 0;
        }
        return threadPoolExecutor.getTaskCount();
    }
}
