package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * 错误日志统计
 *
 * @author fulei
 */
@Data
public class ErrorLogStat {

    private long id;

    private String ip;

    private int port;

    private String className;

    private long collectTime;

    private long diffErrorCount;

    private long totalErrorCount;

    private Date createTime;

    private Date updateTime;

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public Date getUpdateTime() {
        return (Date) updateTime.clone();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = (Date) updateTime.clone();
    }
}
