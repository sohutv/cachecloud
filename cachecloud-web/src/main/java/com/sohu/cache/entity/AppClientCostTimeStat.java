package com.sohu.cache.entity;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 客户端耗时统计
 * @author leifu
 */
@Data
public class AppClientCostTimeStat {
    
    private long id;

    /**
     * 应用id
     */
    private long appId;

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
     * 命令
     */
    private String command;

    /**
     * 调用次数
     */
    private int count;

    /**
     * 实例ip
     */
    private String instanceHost;

    /**
     * 实例port
     */
    private int instancePort;
    
    /**
     * 实例id
     */
    private long instanceId;
    
    /**
     * 中位值
     */
    private int median;

    /**
     * 平均值
     */
    private double mean;

    /**
     * 90%最大值
     */
    private int ninetyPercentMax;

    /**
     * 99%最大值
     */
    private int ninetyNinePercentMax;

    /**
     * 100%最大值
     */
    private int hundredMax;

    public Date getReportTime() {
        return (Date) reportTime.clone();
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = (Date) reportTime.clone();
    }

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public Long getCollectTimeStamp() throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date;
        try {
            date = sdf.parse(String.valueOf(this.collectTime));
            return date.getTime();
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getTimeStamp() throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = sdf.parse(String.valueOf(this.collectTime));
        return date.getTime();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientIp == null) ? 0 : clientIp.hashCode());
        result = prime * result + (int) (instanceId ^ (instanceId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AppClientCostTimeStat other = (AppClientCostTimeStat) obj;
        if (clientIp == null) {
            if (other.clientIp != null)
                return false;
        } else if (!clientIp.equals(other.clientIp))
            return false;
        if (instanceId != other.instanceId)
            return false;
        return true;
    }

}
