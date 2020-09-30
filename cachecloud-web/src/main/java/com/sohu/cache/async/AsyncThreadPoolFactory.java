package com.sohu.cache.async;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步线程池 Created by yijunzhang on 14-7-10.
 */
public class AsyncThreadPoolFactory {

    public static final String DEFAULT_ASYNC_POOL = "async-pool";
    public static final ThreadPoolExecutor DEFAULT_ASYNC_THREAD_POOL = new ThreadPoolExecutor(256, 256,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024),
                new NamedThreadFactory(DEFAULT_ASYNC_POOL, true),new CounterRejectedExecutionHandler());

    public static final String TASK_EXECUTE_POOL = "task-execute-pool";
    public static final ThreadPoolExecutor TASK_EXECUTE_THREAD_POOL =
            new ThreadPoolExecutor(200, 200, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(500), new NamedThreadFactory(TASK_EXECUTE_POOL, true), new CounterRejectedExecutionHandler());

    public static final String CLIENT_REPORT_POOL ="client-report-pool";
    public static final ThreadPoolExecutor CLIENT_REPORT_THREAD_POOL = new ThreadPoolExecutor(100,
            100, 0L, TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>(), new NamedThreadFactory(CLIENT_REPORT_POOL, true),new CounterRejectedExecutionHandler());

    public static final String REDIS_SLOWLOG_POOL = "redis-slowlog-pool";
    public static final ThreadPoolExecutor REDIS_SLOWLOG_THREAD_POOL = new ThreadPoolExecutor(30,
            30, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(256), new NamedThreadFactory(REDIS_SLOWLOG_POOL, true),new CounterRejectedExecutionHandler());

    public static final String MACHINE_POOL = "machine-ssh-pool";
    public static final ThreadPoolExecutor MACHINE_THREAD_POOL = new ThreadPoolExecutor(256,
            512, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(2048), new NamedThreadFactory(MACHINE_POOL, true));

    public static final String APP_POOL = "app-pool";
    public static final ThreadPoolExecutor APP_THREAD_POOL = new ThreadPoolExecutor(10,
            10, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(256), new NamedThreadFactory(APP_POOL, true));

    public static final String BREVITY_SCHEDULER_POOL = "brevity-scheduler-pool";
    public static final ThreadPoolExecutor BREVITY_SCHEDULER_ASYNC_THREAD_POOL = new ThreadPoolExecutor(10, 100,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024),
            new NamedThreadFactory(BREVITY_SCHEDULER_POOL, true),new CounterRejectedExecutionHandler());

    public static final String RESHARD_PROCESS_POOL = "redis-cluster-reshard";
    public static final ThreadPoolExecutor RESHARD_PROCESS_THREAD_POOL = new ThreadPoolExecutor(10, 100,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(256),
            new NamedThreadFactory(RESHARD_PROCESS_POOL, false),new CounterRejectedExecutionHandler());

}
