package com.sohu.cache.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
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

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

}
