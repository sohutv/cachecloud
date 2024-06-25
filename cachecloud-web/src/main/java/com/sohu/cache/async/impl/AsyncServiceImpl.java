package com.sohu.cache.async.impl;

import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * Created by yijunzhang on 14-6-18.
 */
@Service
public class AsyncServiceImpl implements AsyncService {
    private final static String DEFAULT_THREAD_POOL = "default_thread_pool";
    public final ConcurrentMap<String, ExecutorService> threadPoolMap = new ConcurrentSkipListMap<String, ExecutorService>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExecutorService defaultThreadPool = AsyncThreadPoolFactory.DEFAULT_ASYNC_THREAD_POOL;

    public AsyncServiceImpl() {
        threadPoolMap.put(DEFAULT_THREAD_POOL, defaultThreadPool);
    }

    @Override
    public boolean submitFuture(KeyCallable<?> callable) {
        return submitFuture(DEFAULT_THREAD_POOL, callable);
    }

    @Override
    public Future<?> submitFutureWithRst(KeyCallable<?> callable) {
        return submitFuture((Callable)callable);
    }

    @Override
    public boolean submitFuture(String threadPoolKey, KeyCallable<?> callable) {
        try {
            ExecutorService executorService = threadPoolMap.get(threadPoolKey);
            if (executorService == null) {
                logger.warn("threadPoolKey={} not found , used defaultThreadPool", threadPoolKey);
                executorService = defaultThreadPool;
            }
            Future future = executorService.submit(callable);
            return true;
        } catch (Exception e) {
            logger.error(callable.getKey(), e);
            return false;
        }
    }

    @Override
    public Future<?> submitFutureWithRst(String threadPoolKey, KeyCallable<?> callable) {
        try {
            ExecutorService executorService = threadPoolMap.get(threadPoolKey);
            if (executorService == null) {
                logger.warn("threadPoolKey={} not found , used defaultThreadPool", threadPoolKey);
                executorService = defaultThreadPool;
            }
            Future<?> future = executorService.submit(callable);
            return future;
        } catch (Exception e) {
            logger.error(callable.getKey(), e);
            return null;
        }
    }

    @Override
    public Future<?> submitFuture(Callable<?> callable) {
        try {
            Future<?> future = defaultThreadPool.submit(callable);
            return future;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void assemblePool(String threadPoolKey, ThreadPoolExecutor threadPool) {
        ExecutorService executorService = threadPoolMap.putIfAbsent(threadPoolKey, threadPool);
        if (executorService != null) {
            logger.warn("{} is assembled", threadPoolKey);
        }
    }

    @PreDestroy
    public void destory() {
        for (ExecutorService executorService : threadPoolMap.values()) {
            if (!executorService.isShutdown()) {
                executorService.shutdown();
            }
        }
        threadPoolMap.clear();
    }

}
