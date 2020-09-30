package com.sohu.cache.web.service;

import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.DiagnosticTaskRecord;
import com.sohu.cache.entity.InstanceInfo;

import java.util.List;
import java.util.Map;

/**
 * @Author: rucao
 * @Date: 2020/6/5 5:33 下午
 */
public interface DiagnosticToolService {
    Map<Long, List<InstanceInfo>> getAppInstancesMap(List<AppDesc> appDescList);

    List<DiagnosticTaskRecord> getDiagnosticTaskRecords(Long appId, Long parentTaskId, Long auditId, Integer type, Integer status);

    List<String> getScanDiagnosticData(String redisKey);

    Map<String, String> getDiagnosticDataMap(String redisKey,int type,boolean err);

    String getHotkeyDiagnosticData(String redisKey);

    List<String> getSampleScanData(Long appId, String nodes, String pattern);
}
