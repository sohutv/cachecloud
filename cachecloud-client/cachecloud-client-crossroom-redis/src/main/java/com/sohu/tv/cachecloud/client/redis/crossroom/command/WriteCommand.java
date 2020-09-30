package com.sohu.tv.cachecloud.client.redis.crossroom.command;

import com.sohu.tv.cachecloud.client.redis.crossroom.enums.DataStatusEnum;
import com.sohu.tv.cachecloud.client.redis.crossroom.enums.MultiWriteResult;
import com.sohu.tv.cachecloud.client.redis.hystrix.DataComponentCommand;

import java.util.concurrent.Future;

/**
 * Created by zhangyijun on 16/4/26.
 */
public abstract class WriteCommand<T> extends BaseCommand {

    protected abstract T writeMajor();

    protected abstract T writeMinor();
    
    protected abstract String getCommandParam();

    public MultiWriteResult<T> write() {
        DataComponentCommand<T> majorCommand =
                new DataComponentCommand<T>(MAJOR_WRITE_COMMAND_KEY, MAJOR_GROUP_KEY, MAJOR_THREAD_POOL_KEY,
                        majorTimeOut, majorThreads) {
                    @Override
                    protected T run() throws Exception {
                        return writeMajor();
                    }

                    @Override
                    public T getBusinessFallback() {
                        logger.warn("major cross-room failed: {}", getCommandParam());
                        return null;
                    }
                };

        DataComponentCommand<T> minorCommand =
                new DataComponentCommand<T>(MINOR_WRITE_COMMAND_KEY, MINOR_GROUP_KEY, MINOR_THREAD_POOL_KEY,
                        minorTimeOut, minorThreads) {
                    @Override
                    protected T run() throws Exception {
                        return writeMinor();
                    }

                    @Override
                    public T getBusinessFallback() {
                        logger.warn("minor cross-room failed: {}", getCommandParam());
                        return null;
                    }
                };

        Future<T> majorFuture = majorCommand.queue();
        Future<T> minorFuture = minorCommand.queue();
        T majorResult = null;
        T minorResult = null;
        try {
            majorResult = majorFuture.get();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            minorResult = minorFuture.get();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        DataStatusEnum majorStatus = DataStatusEnum.SUCCESS;
        DataStatusEnum minorStatus = DataStatusEnum.SUCCESS;
        if (majorResult == null) {
            majorStatus = DataStatusEnum.FAIL;
        }
        if (minorResult == null) {
            minorStatus = DataStatusEnum.FAIL;
        }
        return new MultiWriteResult<T>(majorStatus, majorResult, minorStatus, minorResult);
    }

}
