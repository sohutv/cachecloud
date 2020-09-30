package com.sohu.tv.cachecloud.client.redis.crossroom.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhangyijun on 16/4/26.
 */
public class BaseCommand {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * hystrix参数,例如超时、线程池、关门策略、开门策略等等。
     */
    
    protected static final String MAJOR_READ_COMMAND_KEY = "major_read_command";
    protected static final String MAJOR_WRITE_COMMAND_KEY = "major_write_command";
    protected static final String MAJOR_GROUP_KEY = "major_redis_group";
    protected static final String MAJOR_THREAD_POOL_KEY = "major_redis_pool";
    public static int majorTimeOut = 1000;
    public static int majorThreads = 100;

    /**
     * hystrix参数,例如超时、线程池、关门策略、开门策略等等。
     */
    protected static final String MINOR_READ_COMMAND_KEY = "minor_read_command";
    protected static final String MINOR_WRITE_COMMAND_KEY = "minor_write_command";
    protected static final String MINOR_GROUP_KEY = "minor_redis_group";
    protected static final String MINOR_THREAD_POOL_KEY = "minor_redis_pool";
    public static int minorTimeOut = 1000;
    public static int minorThreads = 100;

}
