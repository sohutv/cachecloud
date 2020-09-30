package com.sohu.cache.task.entity;

import lombok.Data;

/**
 * @author fulei
 */
@Data
public class NutCrackerNode {

    private long taskId;

    private String ip;

    private int port;

    public NutCrackerNode() {
    }

    public NutCrackerNode(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

}
