package com.sohu.cache.dao;

import com.sohu.cache.task.entity.TaskQueue;
import com.sohu.cache.task.entity.TaskSearch;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface TaskQueueDao {
    /**
     * @param taskQueue
     */
    void save(TaskQueue taskQueue);

    /**
     * @param taskId
     * @return
     */
    TaskQueue getById(@Param("taskId") long taskId);

    /**
     * 更新任务状态
     *
     * @param taskId
     * @param status
     */
    void updateStatus(@Param("taskId") long taskId, @Param("status") int status);

    /**
     * 更新任务参数
     *
     * @param taskId
     * @param param
     */
    void updateParam(@Param("taskId") long taskId, @Param("param") String param);

    /**
     * 根据status获取任务列表
     *
     * @param status
     * @return
     */
    List<TaskQueue> getTaskQueueListByStatus(@Param("status") int status);

    /**
     * 更新开始时间
     *
     * @param taskId
     * @param startTime
     */
    void updateStartTime(@Param("taskId") long taskId, @Param("startTime") Date startTime);

    /**
     * 更新结束时间
     *
     * @param taskId
     * @param endTime
     */
    void updateEndTime(@Param("taskId") long taskId, @Param("endTime") Date endTime);

    /**
     * @param taskSearch
     * @return
     */
    int getTaskQueueCount(TaskSearch taskSearch);

    /**
     * @param taskSearch
     * @return
     */
    List<TaskQueue> getTaskQueueList(TaskSearch taskSearch);

    /**
     * @param taskId
     * @return
     */
    List<TaskQueue> getChildTaskQueueList(@Param("taskId") long taskId);

    /**
     * @param status
     * @return
     */
    int getStatusCount(int status);

    /**
     * @param taskId
     * @param executeIpPort
     */
    void updateExecuteIpPort(@Param("taskId") long taskId, @Param("executeIpPort") String executeIpPort);

    /**
     * @param appId
     * @param className
     * @return
     */
    List<TaskQueue> getByAppAndClass(@Param("appId") long appId, @Param("className") String className);

}
