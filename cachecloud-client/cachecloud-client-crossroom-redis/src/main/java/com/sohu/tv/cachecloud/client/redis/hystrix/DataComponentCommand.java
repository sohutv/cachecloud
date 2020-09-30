package com.sohu.tv.cachecloud.client.redis.hystrix;

import com.netflix.hystrix.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yijunzhang on 14-11-3.
 */
public abstract class DataComponentCommand<T> extends HystrixCommand<T> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public final static int DEFAULT_TIMEOUT = 500;

    public final static int DEFAULT_POOL_SIZE = 20;

    private final String commandKey;

    private final String groupKey;

    private final String poolKey;

    public DataComponentCommand(String commondKey, String groupKey, String poolKey) {
        this(commondKey, groupKey, poolKey, DEFAULT_TIMEOUT, DEFAULT_POOL_SIZE);
    }

    public DataComponentCommand(String commonKey, String groupKey, String poolKey, int timeout) {
        this(commonKey, groupKey, poolKey, timeout, DEFAULT_POOL_SIZE);
    }

    public DataComponentCommand(String commandKey, String groupKey, String poolKey, int timeout,
            int poolSize) {
        this(commandKey, groupKey, poolKey, timeout, poolSize, true);
    }

    /**
     * 创建Command对象
     *
     * @param commandKey 命令描述
     * @param groupKey 命令组名(一般对应Service/远程资源,在不使用poolKey情况下,用来定位线程池)
     * @param poolKey 线程池名(根据名称定位线程池)
     * @param timeout 请求超时时间(单位毫秒)
     * @param poolSize 线程池大小(默认:20个)
     * @param isInterruptThreadOnTimeout 超时后是否中断线程(默认：true)
     */
    public DataComponentCommand(String commandKey, String groupKey, String poolKey, int timeout,
            int poolSize, boolean isInterruptThreadOnTimeout) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(poolKey))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationThreadTimeoutInMilliseconds(timeout)
                        .withExecutionIsolationThreadInterruptOnTimeout(isInterruptThreadOnTimeout))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(poolSize)));
        this.commandKey = commandKey;
        this.groupKey = poolKey;
        this.poolKey = poolKey;
    }

    @Override
    protected T getFallback() {
        //抛出异常
        if (this.isFailedExecution()) {
            Throwable throwable = this.getFailedExecutionException();
            logger.error(throwable.getMessage(), throwable);
        }
        //判断是否为调用超时
        if (this.isResponseTimedOut()) {
            long time = this.getExecutionTimeInMilliseconds();
            logger.warn("commandKey={} groupKey={} poolKey={} timeout cost={} ms", commandKey, groupKey, poolKey, time);
        }
        return getBusinessFallback();
    }
    
    public T getBusinessFallback() {
        return null;
    }
    
}
