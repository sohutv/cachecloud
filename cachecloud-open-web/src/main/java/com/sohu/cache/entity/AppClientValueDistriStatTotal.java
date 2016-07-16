package com.sohu.cache.entity;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 客户端值分布统计
 * @author leifu
 * @Date 2015年1月20日
 * @Time 上午11:44:09
 */
public class AppClientValueDistriStatTotal {
    
    /**
     * 应用id
     */
    private long appId;

    /**
     * 格式yyyyMMddHHmm00
     */
    private long collectTime;

    /**
     * 创建时间
     */
    private Date updateTime;

    /**
     * 命令
     */
    private String command;

    /**
     * 值分布类型
     */
    private int distributeType;

    /**
     * 调用次数
     */
    private int count;

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


    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }


    public int getDistributeType() {
        return distributeType;
    }

    public void setDistributeType(int distributeType) {
        this.distributeType = distributeType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }
    
}
