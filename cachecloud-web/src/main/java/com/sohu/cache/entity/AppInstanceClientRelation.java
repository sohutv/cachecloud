package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * 应用下实例与客户端对应关系
 *
 * @author leifu
 */
@Data
public class AppInstanceClientRelation {

    /**
     * 应用id
     */
    private long appId;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 节点ip
     */
    private String instanceHost;

    /**
     * 节点端口
     */
    private int instancePort;

    /**
     * 节点端口
     */
    private long instanceId;

    /**
     * 日期
     */
    private Date day;

    public AppInstanceClientRelation(long appId, String clientIp, String instanceHost, int instancePort,
            long instanceId, Date day) {
        this.appId = appId;
        this.clientIp = clientIp;
        this.instanceHost = instanceHost;
        this.instancePort = instancePort;
        this.instanceId = instanceId;
        this.day = day;
    }

    public AppInstanceClientRelation() {
    }

    public static AppInstanceClientRelation generateFromAppClientCostTimeStat(
            AppClientCostTimeStat appClientCostTimeStat) {
        if (appClientCostTimeStat == null) {
            return null;
        } else {
            return new AppInstanceClientRelation(appClientCostTimeStat.getAppId(),
                    appClientCostTimeStat.getClientIp(), appClientCostTimeStat.getInstanceHost(), appClientCostTimeStat
                    .getInstancePort(), appClientCostTimeStat.getInstanceId(), new Date(
                    System.currentTimeMillis()));
        }
    }

    public Date getDay() {
        return (Date) day.clone();
    }

    public void setDay(Date day) {
        this.day = (Date) day.clone();
    }
}