package com.sohu.cache.task.entity;

import lombok.Data;

import java.util.Date;

/**
 * 任务步骤元数据
 */
@Data
public class TaskStepMeta {

    /**
     * 自增id
     */
    private long id;

    /**
     * 类名
     */
    private String className;

    /**
     * 步骤名
     */
    private String stepName;

    /**
     * 步骤描述
     */
    private String stepDesc;

    /**
     * 运维建议
     */
    private String opsDevice;

    /**
     * 超时时间
     */
    private int timeout;

    /**
     * 顺序
     */
    private int orderNo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public Date getUpdateTime() {
        return (Date) updateTime.clone();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = (Date) updateTime.clone();
    }

}
