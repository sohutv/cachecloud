package com.sohu.cache.dao;

import com.sohu.cache.entity.DiagnosticTaskRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: rucao
 * @Date: 2020/6/9 16:49
 */
public interface DiagnosticTaskRecordDao {

    long insertDiagnosticTaskRecord(DiagnosticTaskRecord diagnosticTaskRecord);

    int updateDiagnosticStatus(@Param("id") long id, @Param("redisKey") String redisKey, @Param("status") int status, @Param("cost") long cost);

    List<DiagnosticTaskRecord> getDiagnosticTaskRecords(@Param("appId") Long appId, @Param("parentTaskId") Long parentTaskId, @Param("auditId") Long auditId, @Param("type") Integer type, @Param("status") Integer status);
}
