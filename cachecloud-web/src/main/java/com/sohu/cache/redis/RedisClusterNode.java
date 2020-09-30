package com.sohu.cache.redis;

/**
 * Created by yijunzhang on 14-8-25.
 */
public class RedisClusterNode {

    /**
     * 主节点地址
     */
    private String masterHost;

    /**
     * 从节点地址
     */
    private String slaveHost;

    public String getMasterHost() {
        return masterHost;
    }

    public void setMasterHost(String masterHost) {
        this.masterHost = masterHost;
    }

    public String getSlaveHost() {
        return slaveHost;
    }

    public void setSlaveHost(String slaveHost) {
        this.slaveHost = slaveHost;
    }

    public RedisClusterNode(String masterHost, String slaveHost) {
        this.masterHost = masterHost;
        this.slaveHost = slaveHost;
    }

    public RedisClusterNode() {
    }

    @Override
    public String toString() {
        return "RedisClusterNode{" +
                "masterHost='" + masterHost + '\'' +
                ", slaveHost='" + slaveHost + '\'' +
                '}';
    }
}
