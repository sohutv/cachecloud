package com.sohu.cache.web.vo;

import lombok.Data;

import java.util.Date;


/**
 * Created by yijunzhang on 14-10-14.
 */
@Data
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

    public Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(Date date) {
        this.date = (Date) date.clone();
    }
}
