package com.sohu.cache.entity;

import java.util.Date;

/**
 * @author leifu
 * @Date 2017年5月6日
 * @Time 上午11:19:50
 */
public class TimeBetween {

    private long startTime;
    
    private long endTime;
    
    private Date startDate;
    
    private Date endDate;

    public TimeBetween() {
    }

    public TimeBetween(long startTime, long endTime, Date startDate, Date endDate) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "TimeBetween [startTime=" + startTime + ", endTime=" + endTime + ", startDate=" + startDate
                + ", endDate=" + endDate + "]";
    }
    
}
