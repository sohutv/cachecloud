package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: rucao
 * @Date: 2020/5/7 3:12 下午
 */
@Data
public class InstanceLatencyHistory {
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
     * 创建时间
     */
    private Date createTime;
    /**
     * 延迟事件名称
     */
    private String event;
    /**
     * 事件出现延迟毛刺日期时间
     */
    private Date executeDate;
    /**
     * 延迟（单位：毫秒）
     */
    private long executionCost;

    public InstanceLatencyHistory(long instanceId, long appId, String ip, int port, String event, Date executeDate, long executionCost) {
        this.instanceId = instanceId;
        this.appId = appId;
        this.ip = ip;
        this.port = port;
        this.event = event;
        this.executeDate = executeDate;
        this.executionCost = executionCost;
    }
}
