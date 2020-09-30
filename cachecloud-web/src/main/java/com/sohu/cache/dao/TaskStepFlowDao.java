package com.sohu.cache.dao;

import com.sohu.cache.task.entity.TaskStepFlow;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface TaskStepFlowDao {

    /**
     * @param taskStepFlow
     */
    void save(TaskStepFlow taskStepFlow);

    /**
     * @param taskId
     * @param className
     * @param stepName
     * @return
     */
    TaskStepFlow getByTaskClassStep(@Param("taskId") long taskId, @Param("className") String className,
            @Param("stepName") String stepName);

    /**
     * 更新状态
     *
     * @param id
     * @param status
     */
    void updateStatus(@Param("id") long id, @Param("status") int status);

    /**
     * 更新日志
     *
     * @param id
     * @param log
     */
    void updateLog(@Param("id") long id, @Param("log") String log);

    /**
     * 更新开始时间
     *
     * @param id
     * @param startTime
     */
    void updateStartTime(@Param("id") long id, @Param("startTime") Date startTime);

    /**
     * 更新结束时间
     *
     * @param id
     * @param endTime
     */
    void updateEndTime(@Param("id") long id, @Param("endTime") Date endTime);

    /**
     * 获取任务的流列表
     *
     * @param taskId
     * @return
     */
    List<TaskStepFlow> getTaskStepFlowList(@Param("taskId") long taskId);

    /**
     * @param id
     * @param childTaskId
     */
    void updateChildTaskId(@Param("id") long id, @Param("childTaskId") long childTaskId);

    /**
     * @param taskId
     * @param executeIpPort
     */
    void updateExecuteIpPort(@Param("taskId") long taskId, @Param("executeIpPort") String executeIpPort);
}
