package com.sohu.tv.cachecloud.client.redis.crossroom.command;

import com.sohu.tv.cachecloud.client.redis.crossroom.enums.HystrixStatCountTypeEnum;
import com.sohu.tv.cachecloud.client.redis.crossroom.exception.RedisCrossRoomReadMinorFallbackException;
import com.sohu.tv.cachecloud.client.redis.crossroom.stat.RedisCrossRoomClientStatusCollector;
import com.sohu.tv.cachecloud.client.redis.crossroom.stat.RedisCrossRoomHystrixStat;
import com.sohu.tv.cachecloud.client.redis.hystrix.DataComponentCommand;

/**
 * Created by zhangyijun on 16/4/26.
 */
public abstract class ReadCommand<T> extends BaseCommand {

    protected abstract T readMajor();

    protected abstract T readMinor();

    public T read() {
        // 1.收集总数
        RedisCrossRoomClientStatusCollector.collectCrossRoomStatus(HystrixStatCountTypeEnum.ALL);
        
        DataComponentCommand<T> majorCommand =
                new DataComponentCommand<T>(MAJOR_READ_COMMAND_KEY, MAJOR_GROUP_KEY, MAJOR_THREAD_POOL_KEY,
                        majorTimeOut, majorThreads) {
                    @Override
                    protected T run() throws Exception {
                        // 2.收集run总数
                        RedisCrossRoomClientStatusCollector.collectCrossRoomStatus(HystrixStatCountTypeEnum.RUN);
                        
                        RedisCrossRoomHystrixStat.counter(MAJOR_READ_COMMAND_KEY);
                        return readMajor();
                    }

                    @Override
                    public T getBusinessFallback() {
                        // 3.收集fallback总数
                        RedisCrossRoomClientStatusCollector.collectCrossRoomStatus(HystrixStatCountTypeEnum.FALLBACK_ALL);
                        
                        RedisCrossRoomHystrixStat.counterFallBack(MAJOR_READ_COMMAND_KEY);
                        return new DataComponentCommand<T>(MINOR_READ_COMMAND_KEY, MINOR_GROUP_KEY, MINOR_THREAD_POOL_KEY,
                                minorTimeOut, minorThreads) {
                            @Override
                            protected T run() throws Exception {
                                // 4.收集fallback-run总数
                                RedisCrossRoomClientStatusCollector.collectCrossRoomStatus(HystrixStatCountTypeEnum.FALLBACK_RUN);
                                
                                RedisCrossRoomHystrixStat.counter(MINOR_READ_COMMAND_KEY);
                                return readMinor();
                            }

                            @Override
                            public T getBusinessFallback() throws RedisCrossRoomReadMinorFallbackException {
                                // 5.收集fallback-fallback总数
                                RedisCrossRoomClientStatusCollector.collectCrossRoomStatus(HystrixStatCountTypeEnum.FALLBACK_FALLBACK);
                                
                                RedisCrossRoomHystrixStat.counterFallBack(MINOR_READ_COMMAND_KEY);
                                
                                throw new RedisCrossRoomReadMinorFallbackException("MinorFallbackException");
                            }
                        }.execute();
                    }
                };
        return majorCommand.execute();
    }

}
