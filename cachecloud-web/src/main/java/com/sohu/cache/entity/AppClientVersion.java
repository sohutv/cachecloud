package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * 客户端版本
 * @author leifu
 */
@Data
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

    public Date getReportTime() {
        return (Date) reportTime.clone();
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = (Date) reportTime.clone();
    }

}
