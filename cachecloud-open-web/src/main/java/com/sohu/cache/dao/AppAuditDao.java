package com.sohu.cache.dao;

import com.sohu.cache.entity.AppAudit;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by yijunzhang on 14-10-20.
 */
public interface AppAuditDao {

    /**
     * 新增审核记录
     * @param appAudit
     */
    public void insertAppAudit(AppAudit appAudit);

    /**
     * 查询所有等待审批的记录
     */
    public List<AppAudit> selectWaitAppAudits(@Param("status") Integer status, @Param("type") Integer type);

    /**
     * 按id查询
     * @param id
     * @return
     */
    public AppAudit getAppAudit(@Param("id") long id);

    /**
     * 更新审核状态
     */
    public void updateAppAudit(@Param("id") long id, @Param("status") int status);
    
    /**
     * 更新驳回理由
     */
    public void updateRefuseReason(@Param("id") long id, @Param("refuseReason") String refuseReason);

    /**
     * 通过appId获取所有审批记录
     */
    public List<AppAudit> getAppAuditByAppId(@Param("appId")Long appId);

}
