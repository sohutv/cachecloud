package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author fulei
 * @date 2018年8月11日
 * @time 上午11:16:22
 */
@Data
public class ThreadPoolStat {

    private long id;

    /**
     * 进程ip
     */
    private String ip;

    /**
     * 进程port
     */
    private int port;

    private String threadPoolName;

    private long collectTime;

    private Date collectDate;

    private int maximumPoolSize;

    private int corePoolSize;

    private int activeCount;

    private long diffTaskCount;

    private long taskCount;

    private long completedTaskCount;

    private int queueSize;

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

    public Date getCollectDate() {
        return (Date) collectDate.clone();
    }

    public void setCollectDate(Date collectDate) {
        this.collectDate = (Date) collectDate.clone();
    }
}
