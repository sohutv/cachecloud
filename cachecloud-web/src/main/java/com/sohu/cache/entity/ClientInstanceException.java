package com.sohu.cache.entity;

import lombok.Data;

/**
 * 客户端实例异常
 *
 * @author leifu
 */
@Data
public class ClientInstanceException {

    private long appId;

    private long instanceId;

    private String instanceHost;

    private int instancePort;

    private int exceptionCount;

    public ClientInstanceException(long appId, long instanceId, String instanceHost, int instancePort,
            int exceptionCount) {
        this.appId = appId;
        this.instanceId = instanceId;
        this.instanceHost = instanceHost;
        this.instancePort = instancePort;
        this.exceptionCount = exceptionCount;
    }

    public ClientInstanceException() {
    }

}
