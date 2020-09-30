package com.sohu.cache.task.entity;

import lombok.Data;

/**
 * @author fulei
 * @date 2018年6月26日
 */
@Data
public class RedisSentinelNode {

    private long taskId;

    private String ip;

    private int port;

    public RedisSentinelNode() {
    }

    public RedisSentinelNode(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

}
