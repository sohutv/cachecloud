package com.sohu.cache.task.entity;

import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import lombok.Data;

import java.util.Date;

/**
 * 任务步骤流
 */
@Data
public class TaskStepFlow {

    /**
     * 自增id
     */
    private long id;

    /**
     * 任务id
     */
    private long taskId;

    /**
     * 子任务id
     */
    private long childTaskId;

    /**
     * 类名
     */
    private String className;

    /**
     * 步骤名
     */
    private String stepName;

    /**
     * 序号
     */
    private int orderNo;

    /**
     * 状态, 参考：TaskFlowStatusEnum
     */
    private int status;

    /**
     * 日志
     */
    private String log = "";

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 执行ip:port
     */
    private String executeIpPort;

    /**
     * 任务流描述
     */
    private TaskStepMeta taskStepMeta;

    public boolean isSuccess() {
        return status == TaskFlowStatusEnum.SUCCESS.getStatus();
    }

    public boolean isSkip() {
        return status == TaskFlowStatusEnum.SKIP.getStatus();
    }

    public String getExecuteIpPort() {
        return executeIpPort;
    }

    public void setExecuteIpPort(String executeIpPort) {
        this.executeIpPort = executeIpPort;
    }

    public TaskStepMeta getTaskStepMeta() {
        return taskStepMeta;
    }

    public void setTaskStepMeta(TaskStepMeta taskStepMeta) {
        this.taskStepMeta = taskStepMeta;
    }

    public String getStatusDesc() {
        TaskFlowStatusEnum taskFlowStatusEnum = TaskFlowStatusEnum.getTaskFlowStatusEnum(status);
        if (taskFlowStatusEnum != null) {
            return taskFlowStatusEnum.getInfo();
        }
        return "";
    }

    public String getCostSeconds() {
        if (status != TaskFlowStatusEnum.SUCCESS.getStatus()) {
            return "";
        }
        if (endTime != null && startTime != null) {
            long ms = (endTime.getTime() - startTime.getTime()) / 1000;
            return String.valueOf(ms);
        }
        return "";
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
