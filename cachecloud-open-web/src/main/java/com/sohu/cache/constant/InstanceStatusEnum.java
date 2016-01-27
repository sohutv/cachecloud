package com.sohu.cache.constant;

/**
 * 实例状态
 * @author leifu
 * @Date 2014年11月26日
 * @Time 下午5:05:35
 */
public enum InstanceStatusEnum {
    ERROR_STATUS(0),
    GOOD_STATUS(1),
    OFFLINE_STATUS(2);

    int status;

    InstanceStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
