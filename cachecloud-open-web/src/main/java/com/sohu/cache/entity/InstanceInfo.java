package com.sohu.cache.entity;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 实例信息
 * User: lingguo
 * Date: 14-5-29
 * Time: 下午9:38
 */
public class InstanceInfo implements Serializable {
    private static final long serialVersionUID = -903896025243493024L;

    /**
     * 实例id
     */
    private int id;

    /**
     * 用于表示主从，如果是主，该值为0，如果是从，则值为主的id
     */
    private int parentId;

    /**
     * 应用id
     */
    private long appId;

    /**
     * host id
     */
    private long hostId;

    /**
     * ip
     */
    private String ip;

    /**
     * 端口
     */
    private int port;

    /**
     * 是否启用 0:节点异常,1:正常启用,2:节点下线
     */
    private int status;

    /**
     * 开启的内存
     */
    private int mem;

    /**
     * 连接数
     */
    private int conn;

    /**
     * 启动命令 或者 redis-sentinel的masterName
     */
    private String cmd;

    /**
     * 类型：2. redis-cluster, 5. redis-sentinel 6.redis-standalone
     */
    private int type;

    private String typeDesc;

    private int masterInstanceId;

    private String masterHost;

    private int masterPort;

    private String roleDesc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(long hostId) {
        this.hostId = hostId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getMem() {
        return mem;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public int getConn() {
        return conn;
    }

    public void setConn(int conn) {
        this.conn = conn;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeDesc() {
        if (type <= 0) {
            return "";
        } else if (type == 2) {
            return "redis-cluster";
        } else if (type == 5) {
            return "redis-sentinel";
        } else if (type == 6) {
            return "redis-standalone";
        }
        return "";
    }

    @Override
    public String toString() {
        return "InstanceInfo{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", appId=" + appId +
                ", hostId=" + hostId +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", status=" + status +
                ", mem=" + mem +
                ", conn=" + conn +
                ", cmd='" + cmd + '\'' +
                ", type=" + type +
                '}';
    }

    public int getMasterInstanceId() {
        return masterInstanceId;
    }

    public void setMasterInstanceId(int masterInstanceId) {
        this.masterInstanceId = masterInstanceId;
    }

    public String getMasterHost() {
        return masterHost;
    }

    public void setMasterHost(String masterHost) {
        this.masterHost = masterHost;
    }

    public int getMasterPort() {
        return masterPort;
    }

    public void setMasterPort(int masterPort) {
        this.masterPort = masterPort;
    }

    public String getStatusDesc() {
        //是否启用 0:节点异常,1:正常启用,2:节点下线
        if (status == 0) {
            return "心跳停止";
        } else if (status == 1) {
            return "运行中";
        } else {
            return "已下线";
        }
    }

    /**
     * 判断当前节点是否下线
     *
     * @return
     */
    public boolean isOffline() {
        if (status == 2) {
            return true;
        }
        return false;
    }

    public void setRoleDesc(Boolean isMaster) {
        if (isMaster == null) {
            roleDesc = "未知";
        } else if (isMaster) {
            roleDesc = "master";
        } else {
            roleDesc = "slave";
        }
    }

    public String getRoleDesc() {
        if (type == 5) {
            return "sentinel";
        } else {
            return roleDesc;
        }
    }

}
