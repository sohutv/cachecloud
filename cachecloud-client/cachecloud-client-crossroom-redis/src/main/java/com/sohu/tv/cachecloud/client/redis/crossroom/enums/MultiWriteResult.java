package com.sohu.tv.cachecloud.client.redis.crossroom.enums;


/**
 * Created by zhangyijun on 16/4/26.
 */
public class MultiWriteResult<T> {

    private final DataStatusEnum majorStatus;
    private final T majorResult;

    private final DataStatusEnum minorStatus;
    private final T minorResult;


    public MultiWriteResult(DataStatusEnum majorStatus, T majorResult, DataStatusEnum minorStatus, T minorResult) {
        this.majorStatus = majorStatus;
        this.majorResult = majorResult;
        this.minorStatus = minorStatus;
        this.minorResult = minorResult;
    }

    public T getMajorResult() {
        return majorResult;
    }

    public T getMinorResult() {
        return minorResult;
    }

    public DataStatusEnum getMajorStatus() {
        return majorStatus;
    }

    public DataStatusEnum getMinorStatus() {
        return minorStatus;
    }

    @Override
    public String toString() {
        return "MultiWriteResult [majorStatus=" + majorStatus + ", majorResult=" + majorResult + ", minorStatus="
                + minorStatus + ", minorResult=" + minorResult + "]";
    }
    
    
}
