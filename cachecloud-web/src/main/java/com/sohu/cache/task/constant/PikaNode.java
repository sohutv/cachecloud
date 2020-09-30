package com.sohu.cache.task.constant;

import lombok.Data;

/**
 * @author fulei
 * @date 2018年6月26日
 */
@Data
public class PikaNode {

    private long taskId;

    private String ip;

    private int port;

    private int role;

    private String masterHost;

    private int masterPort;

    private String masterName;

    public PikaNode() {
    }

    public PikaNode(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public PikaNode(String ip, int port, String masterName) {
        this.ip = ip;
        this.port = port;
        this.masterName = masterName;
    }

    public PikaNode(String ip, int port, int role, String masterHost, int masterPort) {
        this.ip = ip;
        this.port = port;
        this.role = role;
        this.masterHost = masterHost;
        this.masterPort = masterPort;
    }

    public String genHostAndPort() {
        return masterHost + ":" + masterPort;
    }

    public String getUniqKey() {
        return ip + "-" + port + "-" + masterName;
    }
}
