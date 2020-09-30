package com.sohu.cache.task.entity;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.task.constant.TaskQueueEnum.TaskStatusEnum;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 任务队列
 */
@Data
public class TaskQueue {

    /**
     * 自增id
     */
    private long id;

    /**
     * 进行任务的ip:port
     */
    private String executeIpPort;

    /**
     * 应用id
     */
    private long appId;

    /**
     * 类名
     */
    private String className;

    /**
     * 全局参数:json格式，内容会变
     */
    private String param;

    /**
     * 初始化参数:json格式，内容不变
     */
    private String initParam;

    /**
     * 状态，详见com.sohu.cache.task.constant.TaskQueueEnum.TaskStatusEnum
     */
    private int status;

    /**
     * 父任务id
     */
    private long parentTaskId;

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
     * 优先级
     */
    private int priority = 50;

    /**
     * 错误代码，详见 TaskQueueEnum.TaskErrorCodeEnum
     */
    private int errorCode;

    /**
     * 错误消息
     */
    private String errorMsg = "";

    /**
     * 备注
     */
    private String taskNote = "";

    /**
     * 重要信息
     */
    private String importantInfo = "";

    /**
     * 任务流
     */
    private List<TaskStepFlow> taskStepFlowList;

    public Map<String, Object> getParamMap() {
        if (StringUtils.isEmpty(param)) {
            return Collections.emptyMap();
        }
        return JSONObject.parseObject(param);
    }

    public String getPrettyParam() {
        if (StringUtils.isEmpty(param)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(param);
        return JSONObject.toJSONString(jsonObject, true);
    }

    public String getStatusDesc() {
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.getTaskStatusEnum(status);
        if (taskStatusEnum != null) {
            return taskStatusEnum.getInfo();
        }
        return "";
    }

    public String getProgress() {
        if (CollectionUtils.isEmpty(taskStepFlowList)) {
            return "";
        }
        int success = 0;
        int total = taskStepFlowList.size();
        for (TaskStepFlow taskStepFlow : taskStepFlowList) {
            if (taskStepFlow.isSkip() || taskStepFlow.isSuccess()) {
                success++;
            }
        }
        double percent = success * 100.0 / total * 1.0;
        return new DecimalFormat("#.00").format(percent) + "%";
    }

    public String getCostSeconds() {
        if (status != TaskStatusEnum.SUCCESS.getStatus()) {
            return "";
        }
        if (endTime != null && startTime != null) {
            long ms = (endTime.getTime() - startTime.getTime()) / 1000;
            return String.valueOf(ms);
        }
        return "";
    }

    public boolean isSuccess() {
        return TaskStatusEnum.SUCCESS.getStatus() == status;
    }

    public boolean isRunning() {
        return TaskStatusEnum.RUNNING.getStatus() == status;
    }

    public boolean isAbort() {
        return TaskStatusEnum.ABORT.getStatus() == status;
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
