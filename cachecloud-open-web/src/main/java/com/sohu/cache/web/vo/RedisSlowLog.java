package com.sohu.cache.web.vo;

import java.util.Date;


/**
 * Created by yijunzhang on 14-10-14.
 */
public class RedisSlowLog {

    /**
     * 慢查询id
     */
    private long id;

    /**
     * 执行时间点
     */
    private String timeStamp;

    /**
     * 慢查询执行时间(微秒)
     */
    private long executionTime;

    private String command;
    
    /**
     * 执行日期时间
     */
    private Date date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "RedisSlowLog [id=" + id + ", timeStamp=" + timeStamp + ", executionTime=" + executionTime
                + ", command=" + command + ", date=" + date + "]";
    }

}
