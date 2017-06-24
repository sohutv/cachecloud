package com.sohu.cache.entity;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.ReshardStatusEnum;

import java.util.Date;

/**
 * 进度持久化
 * 
 * @author leifu
 * @Date 2017年6月24日
 * @Time 下午6:34:07
 */
public class InstanceReshardProcess {
    
    private int id;

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
     * 正在迁移的slot
     */
    private int migratingSlot;
    
    /**
     * 已完成迁移的slot数量
     */
    private int finishSlotNum;

    /**
     * 0:运行中 1:完成 2:出错
     */
    private int status;

    /**
     * 迁移开始时间
     */
    private Date startTime;

    /**
     * 迁移结束时间
     */
    private Date endTime;
    
    /**
     * 创建时间
     * @return
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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


    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getMigratingSlot() {
        return migratingSlot;
    }

    public void setMigratingSlot(int migratingSlot) {
        this.migratingSlot = migratingSlot;
    }

    public int getFinishSlotNum() {
        return finishSlotNum;
    }

    public void setFinishSlotNum(int finishSlotNum) {
        this.finishSlotNum = finishSlotNum;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getTotalSlot() {
        return endSlot - startSlot + 1;
    }
    
    public String getStatusDesc() {
        ReshardStatusEnum reshardStatusEnum = ReshardStatusEnum.getReshardStatusEnum(status);
        return reshardStatusEnum == null ? "" : reshardStatusEnum.getInfo();
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}
