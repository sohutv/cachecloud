package com.sohu.cache.task.entity;

import com.sohu.cache.task.constant.RedisDataStructureTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.text.DecimalFormat;
import java.util.Date;

@Data
@EqualsAndHashCode
public class InstanceBigKey {

    private long id;

    /**
     * 实例id
     */
    private long instanceId;

    /**
     * app id
     */
    private long appId;

    /**
     * 工单id
     */
    private long auditId;

    /**
     * ip地址
     */
    private String ip;

    /**
     * port
     */
    private int port;

    /**
     * 1主2从
     */
    private int role;

    /**
     * bigkey
     */
    private String bigKey;

    /**
     * 类型
     */
    private String type;

    /**
     * 长度
     */
    private long length;

    /**
     * 创建时间
     */
    private Date createTime;


    public String getLengthFormat() {
        if (RedisDataStructureTypeEnum.string.getValue().equals(type)) {
            return new DecimalFormat("#,###").format(length);
        } else {
            return length + "个元素";
        }
    }

    /*public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getAuditId() {
        return auditId;
    }

    public void setAuditId(long auditId) {
        this.auditId = auditId;
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

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getBigKey() {
        return bigKey;
    }

    public void setBigKey(String bigKey) {
        this.bigKey = bigKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }*/

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }
}
