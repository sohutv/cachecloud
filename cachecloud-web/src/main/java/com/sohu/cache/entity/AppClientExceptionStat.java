package com.sohu.cache.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

/**
 * 客户端异常统计
 * @author leifu
 */
@Data
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

}
