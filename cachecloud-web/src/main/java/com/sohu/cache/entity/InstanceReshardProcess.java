package com.sohu.cache.entity;

import com.sohu.cache.constant.ReshardStatusEnum;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 进度持久化
 *
 * @author leifu
 */
@Data
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
     * 源实例
     */
    private InstanceInfo sourceInstanceInfo;

    /**
     * 目标实例id
     */
    private int targetInstanceId;

    /**
     * 目标实例
     */
    private InstanceInfo targetInstanceInfo;

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
     * 0是,1否
     */
    private int isPipeline;

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
     *
     * @return
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private final static String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    public String getStartTimeFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
        return simpleDateFormat.format(startTime);
    }

    public String getEndTimeFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
        return simpleDateFormat.format(endTime);
    }

    public String getCreateTimeFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
        return simpleDateFormat.format(createTime);
    }

    public String getUpdateTimeFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
        return simpleDateFormat.format(updateTime);
    }

    public int getTotalSlot() {
        return endSlot - startSlot + 1;
    }

    public String getStatusDesc() {
        ReshardStatusEnum reshardStatusEnum = ReshardStatusEnum.getReshardStatusEnum(status);
        return reshardStatusEnum == null ? "" : reshardStatusEnum.getInfo();
    }

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

    public Date getStartTime() {
        return (Date) startTime.clone();
    }

    public void setStartTime(Date startTime) {
        this.startTime = (Date) startTime.clone();
    }

    public Date getEndTime() {
        return (Date) endTime.clone();
    }

    public void setEndTime(Date endTime) {
        this.endTime = (Date) endTime.clone();
    }
}
