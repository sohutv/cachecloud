package com.sohu.cache.task.entity;

import com.sohu.cache.task.constant.InstanceRoleEnum;
import com.sohu.cache.task.constant.PikaNode;
import lombok.Data;

/**
 * @author fulei
 * @date 2018年6月26日
 * @time 下午5:52:49
 */
@Data
public class RedisServerNode {

    private long taskId;

    private String ip;

    private int port;

    private int role;

    private int maxmemory;

    private String masterHost;

    private int masterPort;

    private String masterName;

    public RedisServerNode() {
    }

    public RedisServerNode(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public RedisServerNode(String ip, int port, String masterName) {
        this.ip = ip;
        this.port = port;
        this.masterName = masterName;
    }

    public RedisServerNode(String ip, int port, int maxmemory) {
        this.ip = ip;
        this.port = port;
        this.maxmemory = maxmemory;
    }

    public RedisServerNode(String ip, int port, int role, int maxmemory, String masterHost, int masterPort) {
        this.ip = ip;
        this.port = port;
        this.role = role;
        this.maxmemory = maxmemory;
        this.masterHost = masterHost;
        this.masterPort = masterPort;
    }

    public String genHostAndPort() {
        return masterHost + ":" + masterPort;
    }

    public boolean isMaster() {
        return role == InstanceRoleEnum.MASTER.getRole();
    }

    public boolean isSlave() {
        return role == InstanceRoleEnum.SLAVE.getRole();
    }


    @Override
    public String toString() {
        return "RedisServerNode [taskId=" + taskId + ", ip=" + ip + ", port=" + port + ", role=" + role + ", maxmemory="
                + maxmemory + ", masterHost=" + masterHost + ", masterPort=" + masterPort + ", masterName=" + masterName
                + "]";
    }

    public String getUniqKey() {
        return ip + "-" + port + "-" + masterName;
    }

    public static RedisServerNode transferFromPika(PikaNode pikaNode) {
        RedisServerNode redisServerNode = new RedisServerNode();
        redisServerNode.setIp(pikaNode.getIp());
        redisServerNode.setMasterHost(pikaNode.getMasterHost());
        redisServerNode.setMasterName(pikaNode.getMasterName());
        redisServerNode.setMasterPort(pikaNode.getMasterPort());
        redisServerNode.setPort(pikaNode.getPort());
        redisServerNode.setRole(pikaNode.getRole());
        redisServerNode.setTaskId(pikaNode.getTaskId());
        return redisServerNode;
    }
}
