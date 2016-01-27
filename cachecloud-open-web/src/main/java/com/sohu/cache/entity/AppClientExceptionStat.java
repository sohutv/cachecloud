package com.sohu.cache.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 客户端异常统计
 * @author leifu
 * @Date 2015年1月20日
 * @Time 上午11:44:09
 */
public class AppClientExceptionStat {
    
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
     * 异常类
     */
    private String exceptionClass;
    
    /**
     * 异常数
     */
    private Long exceptionCount;

    /**
     * 实例ip
     */
    private String instanceHost;

    /**
     * 实例port
     */
    private Integer instancePort;
    
    /**
     * 实例id
     */
    private Integer instanceId;
    
    /**
     * 异常类型，参考ClientExceptionType.type
     */
    private Integer type;

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


    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public Long getExceptionCount() {
		return exceptionCount;
	}

	public void setExceptionCount(Long exceptionCount) {
		this.exceptionCount = exceptionCount;
	}

	public String getInstanceHost() {
        return instanceHost;
    }

    public void setInstanceHost(String instanceHost) {
        this.instanceHost = instanceHost;
    }

    public Integer getInstancePort() {
        return instancePort;
    }

    public void setInstancePort(Integer instancePort) {
        this.instancePort = instancePort;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
    
    public String getCollectTimeFormat(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = sdf.parse(String.valueOf(collectTime));
            SimpleDateFormat newSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return newSdf.format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }
    
}
