package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * 客户端内收集数据map的尺寸
 *
 * @author leifu
 */
@Data
public class AppClientDataSizeStat {

    private long id;

    /**
     * 格式yyyyMMddHHmm00
     */
    private long collectTime;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 上报时间
     */
    private Date reportTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 耗时map尺寸
     */
    private int costMapSize;

    /**
     * 值map尺寸
     */
    private int valueMapSize;

    /**
     * 异常map尺寸
     */
    private int exceptionMapSize;

    /**
     * 收集map尺寸
     */
    private int collectMapSize;

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public Date getReportTime() {
        return (Date) reportTime.clone();
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = (Date) reportTime.clone();
    }
}
