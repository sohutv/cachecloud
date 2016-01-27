package com.sohu.cache.entity;

import java.util.Date;

/**
 * 客户端版本
 * @author leifu
 * @Date 2015年2月2日
 * @Time 下午3:22:52
 */
public class AppClientVersion {
    
    /**
     * 自增id
     */
    private long id;
    
    /**
     * 应用id
     */
    private long appId;
    
    /**
     * 客户端ip
     */
    private String clientIp;
    
    /**
     * 客户端版本
     */
    private String clientVersion;
    
    /**
     * 上报时间
     */
    private Date reportTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Date getReportTime() {
        return reportTime;
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    @Override
    public String toString() {
        return "AppClientVersion [id=" + id + ", appId=" + appId + ", clientIp=" + clientIp + ", clientVersion="
                + clientVersion + ", reportTime=" + reportTime + "]";
    }
    
    
    
}
