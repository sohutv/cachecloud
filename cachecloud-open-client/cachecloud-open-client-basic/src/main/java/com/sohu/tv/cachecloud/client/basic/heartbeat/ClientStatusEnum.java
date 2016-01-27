package com.sohu.tv.cachecloud.client.basic.heartbeat;

/**
 * 检查客户端的版本是否ok的枚举
 *
 * @author: lingguo
 * @time: 2014/10/22 10:13
 */
public enum ClientStatusEnum {
    GOOD(1),
    WARN(0),
    ERROR(-1);

    int status;

    ClientStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
