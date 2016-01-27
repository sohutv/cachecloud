package com.sohu.cache.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 客户端耗时统计
 * @author leifu
 * @Date 2015年1月20日
 * @Time 上午11:44:09
 */
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
    

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
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

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getInstanceHost() {
        return instanceHost;
    }

    public void setInstanceHost(String instanceHost) {
        this.instanceHost = instanceHost;
    }

    public int getInstancePort() {
        return instancePort;
    }

    public void setInstancePort(int instancePort) {
        this.instancePort = instancePort;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }
    
    public int getMedian() {
        return median;
    }

    public void setMedian(int median) {
        this.median = median;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public int getNinetyPercentMax() {
        return ninetyPercentMax;
    }

    public void setNinetyPercentMax(int ninetyPercentMax) {
        this.ninetyPercentMax = ninetyPercentMax;
    }

    public int getNinetyNinePercentMax() {
        return ninetyNinePercentMax;
    }

    public void setNinetyNinePercentMax(int ninetyNinePercentMax) {
        this.ninetyNinePercentMax = ninetyNinePercentMax;
    }

    public int getHundredMax() {
        return hundredMax;
    }

    public void setHundredMax(int hundredMax) {
        this.hundredMax = hundredMax;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }
    
}
