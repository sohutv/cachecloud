package com.sohu.cache.dao;

import com.sohu.cache.entity.AppAudit;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yijunzhang on 14-10-20.
 */
public interface AppAuditDao {

    /**
     * 新增审核记录
     *
     * @param appAudit
     */
    public void insertAppAudit(AppAudit appAudit);

    /**
     * 查询所有等待审批的记录
     */
    public List<AppAudit> selectWaitAppAudits(@Param("status") Integer status, @Param("type") Integer type, @Param("auditId") Long auditId, @Param("userId") Long userId, @Param("operateId") Long operateId);

    /**
     * 按id查询
     *
     * @param id
     * @return
     */
    public AppAudit getAppAudit(@Param("id") long id);

    /**
     * 更新审核状态
     */
    void updateAppAudit(@Param("id") long id, @Param("status") int status);

    void updateAppAuditUser(@Param("id") long id, @Param("status") int status, @Param("operateId") Long operateId);

    void updateAppAuditOperateUser(@Param("id") long id, @Param("operateId") Long operateId);

    /**
     * 更新驳回理由
     */
    public void updateRefuseReason(@Param("id") long id, @Param("refuseReason") String refuseReason);

    /**
     * 通过appId获取所有审批记录
     */
    public List<AppAudit> getAppAuditByAppId(@Param("appId") Long appId);

    /**
     * 通过appId获取所有审批记录
     */
    public List<AppAudit> getAppAuditByCondition(@Param("appId") Long appId, @Param("type") Integer type);

    /**
     * 通过appId，type, 和时间范围获取所有审批记录
     */
    public List<AppAudit> getAppAuditByTypeAndTimeRange(@Param("appId") Long appId, @Param("type") Integer type, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * @param id
     * @param taskId
     */
    public void updateTaskId(@Param("id") long id, @Param("taskId") long taskId);

    List<Map<String, Object>> getStatisticGroupByStatus(@Param("userId") Long userId, @Param("operateId") Long operateId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<Map<String, Object>> getStatisticGroupByType(@Param("userId") Long userId, @Param("operateId") Long operateId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

}
