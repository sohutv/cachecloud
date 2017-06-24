package com.sohu.cache.redis;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * 进度持久化
 * 
 * @author leifu
 * @Date 2017年6月24日
 * @Time 下午6:34:07
 */
public class ReshardProcessV2 {
    
    private long id;

    /**
     * 应用id
     */
    private long appId;

    /**
     * 审批id
     */
    private long auditId;

    /**
     * 源实例id
     */
    private int sourceInstanceId;

    /**
     * 目标实例id
     */
    private int targetInstanceId;

    /**
     * 开始slot
     */
    private int startSlot;

    /**
     * 结束slot
     */
    private int endSlot;

    /**
     * 已完成迁移的slot数量
     */
    private int finishReshardSlot;

    /**
     * 0:运行中 1:完成 2:出错
     */
    private int status;

    /**
     * 开始时间
     */
    private Date beginTime;

    /**
     * 结束时间
     */
    private Date endTime;

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

    public long getAuditId() {
        return auditId;
    }

    public void setAuditId(long auditId) {
        this.auditId = auditId;
    }

    public int getSourceInstanceId() {
        return sourceInstanceId;
    }

    public void setSourceInstanceId(int sourceInstanceId) {
        this.sourceInstanceId = sourceInstanceId;
    }

    public int getTargetInstanceId() {
        return targetInstanceId;
    }

    public void setTargetInstanceId(int targetInstanceId) {
        this.targetInstanceId = targetInstanceId;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(int startSlot) {
        this.startSlot = startSlot;
    }

    public int getEndSlot() {
        return endSlot;
    }

    public void setEndSlot(int endSlot) {
        this.endSlot = endSlot;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getFinishReshardSlot() {
        return finishReshardSlot;
    }

    public void setFinishReshardSlot(int finishReshardSlot) {
        this.finishReshardSlot = finishReshardSlot;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public int getTotalSlot() {
        return endSlot - startSlot;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}
