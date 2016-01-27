package com.sohu.cache.dao;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppAuditLog;


/**
 * 应用审批日志记录
 * 
 * @author leifu
 * @Time 2014年6月5日
 */
public interface AppAuditLogDao {

    public int save(AppAuditLog appAuditLog);

    /**
     * 根据类型type(参考AppAuditLogTypeEnum)获取审批日志
     * @param appAuditId
     * @param type
     * @return
     */
    public AppAuditLog getAuditByType(@Param("appAuditId") Long appAuditId, @Param("type") int type);

}
