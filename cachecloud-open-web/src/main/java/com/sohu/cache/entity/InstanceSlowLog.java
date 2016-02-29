package com.sohu.cache.entity;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

/**
 * 实例慢查询日志
 * @author leifu
 * @Date 2016年2月22日
 * @Time 上午11:57:02
 */
public class InstanceSlowLog {
    
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
     * ip地址
     */
    private String ip;

    /**
     * port
     */
    private int port;

    /**
     * 慢查询日志id
     */
    private long slowLogId;

    /**
     * 耗时
     */
    private int costTime;
    
    /**
     * 命令
     */
    private String command;

    /**
     * 记录创建时间
     */
    private Timestamp createTime;

    /**
     * 慢查询发生时间
     */
    private Timestamp executeTime;

    public long getId() {
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

    public long getSlowLogId() {
        return slowLogId;
    }

    public void setSlowLogId(long slowLogId) {
        this.slowLogId = slowLogId;
    }

    public int getCostTime() {
        return costTime;
    }

    public void setCostTime(int costTime) {
        this.costTime = costTime;
    }

    public String getCommand() {
        int maxLength = 30;
        if (StringUtils.isNotBlank(command) && command.length() > maxLength) {
            return command.substring(0, maxLength) + "...";
        }
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Timestamp executeTime) {
        this.executeTime = executeTime;
    }

    @Override
    public String toString() {
        return "InstanceSlowLog [id=" + id + ", instanceId=" + instanceId + ", appId=" + appId + ", ip=" + ip + ", port="
                + port + ", slowLogId=" + slowLogId + ", costTime=" + costTime + ", command=" + command
                + ", createTime=" + createTime + ", executeTime=" + executeTime + "]";
    }

    
}
