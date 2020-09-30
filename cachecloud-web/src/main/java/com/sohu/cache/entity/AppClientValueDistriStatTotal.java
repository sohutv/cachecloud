package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * 客户端值分布统计
 *
 * @author leifu
 */
@Data
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

    public Date getUpdateTime() {
        return (Date) updateTime.clone();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = (Date) updateTime.clone();
    }

}
