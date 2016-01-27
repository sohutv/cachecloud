package com.sohu.cache.async;

import java.util.concurrent.*;

import com.sohu.cache.client.service.impl.ClientReportDataServiceImpl;

/**
 * 异步线程池 Created by yijunzhang on 14-7-10.
 */
public class AsyncThreadPoolFactory {

    public static final ThreadPoolExecutor CLIENT_REPORT_THREAD_POOL =
            new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS,
                    new SynchronousQueue<Runnable>(), new NamedThreadFactory(
                            ClientReportDataServiceImpl.CLIENT_REPORT_POOL, true));

}
