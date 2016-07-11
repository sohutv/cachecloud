package com.sohu.cache.async;

import java.util.concurrent.*;

import com.sohu.cache.client.service.impl.ClientReportDataServiceImpl;
import com.sohu.cache.redis.impl.RedisCenterImpl;

/**
 * 异步线程池 Created by yijunzhang on 14-7-10.
 */
public class AsyncThreadPoolFactory {

    public static final ThreadPoolExecutor CLIENT_REPORT_THREAD_POOL =
            new ThreadPoolExecutor(80, 80, 0L, TimeUnit.MILLISECONDS,
                    new SynchronousQueue<Runnable>(), new NamedThreadFactory(
                            ClientReportDataServiceImpl.CLIENT_REPORT_POOL, true));
    
    
    public static final ThreadPoolExecutor REDIS_SLOWLOG_THREAD_POOL =
            new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS,
                    new SynchronousQueue<Runnable>(), new NamedThreadFactory(
                            RedisCenterImpl.REDIS_SLOWLOG_POOL, true));

}
