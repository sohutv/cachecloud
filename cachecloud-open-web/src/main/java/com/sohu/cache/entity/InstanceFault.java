package com.sohu.cache.entity;

import java.io.Serializable;
import java.util.Date;

public class InstanceFault implements Serializable {

    private static final long serialVersionUID = 8141174905675892249L;

    private int id;

    private int appId;

    private int instId;

    private String ip;

    private int port;

    private int status;

    private int type;

    private Date createTime;

    private String reason;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public int getInstId() {
        return instId;
    }

    public void setInstId(int instId) {
        this.instId = instId;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTypeDesc() {
        InstanceInfo info = new InstanceInfo();
        info.setType(type);
        return info.getTypeDesc();
    }

    public String getStatusDesc() {
        InstanceInfo info = new InstanceInfo();
        info.setStatus(this.status);
        return info.getStatusDesc();
    }

}
