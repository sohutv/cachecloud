package com.sohu.cache.entity;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 客户端内收集数据map的尺寸
 * @author leifu
 * @Date 2015年7月13日
 * @Time 下午3:01:34
 */
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Date getReportTime() {
        return reportTime;
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCostMapSize() {
        return costMapSize;
    }

    public void setCostMapSize(int costMapSize) {
        this.costMapSize = costMapSize;
    }

    public int getValueMapSize() {
        return valueMapSize;
    }

    public void setValueMapSize(int valueMapSize) {
        this.valueMapSize = valueMapSize;
    }

    public int getExceptionMapSize() {
        return exceptionMapSize;
    }

    public void setExceptionMapSize(int exceptionMapSize) {
        this.exceptionMapSize = exceptionMapSize;
    }

    public int getCollectMapSize() {
        return collectMapSize;
    }

    public void setCollectMapSize(int collectMapSize) {
        this.collectMapSize = collectMapSize;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }
    
}
