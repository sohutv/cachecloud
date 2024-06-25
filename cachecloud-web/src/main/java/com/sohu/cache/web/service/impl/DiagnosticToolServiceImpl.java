package com.sohu.cache.web.service.impl;

import com.sohu.cache.constant.DiagnosticTypeEnum;
import com.sohu.cache.dao.DiagnosticTaskRecordDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.DiagnosticTaskRecord;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.DiagnosticToolService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: rucao
 * @Date: 2020/6/5 5:33 下午
 */
@Service
@Slf4j
public class DiagnosticToolServiceImpl implements DiagnosticToolService {
    @Autowired
    private AssistRedisService assistRedisService;
    @Autowired
    private DiagnosticTaskRecordDao diagnosticTaskRecordDao;
    @Autowired
    private AppService appService;
    @Autowired
    private RedisCenter redisCenter;

    @Override
    public Map<Long, List<InstanceInfo>> getAppInstancesMap(List<AppDesc> appDescList) {

        Map<Long, List<InstanceInfo>> appInstancesMap = appDescList.parallelStream().collect(Collectors.toMap(
                appDesc -> appDesc.getAppId(),
                appDesc -> appService.getAppOnlineInstanceInfo(appDesc.getAppId())));
        return appInstancesMap;
    }

    @Override
    public List<DiagnosticTaskRecord> getDiagnosticTaskRecords(Long appId, Long parentTaskId, Long auditId, Integer type, Integer status) {
        List<DiagnosticTaskRecord> diagnosticTaskRecords = new ArrayList<>();
        try {
            diagnosticTaskRecords = diagnosticTaskRecordDao.getDiagnosticTaskRecords(appId, parentTaskId, auditId, type, status);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return diagnosticTaskRecords;
    }

    @Override
    public Map<String, String> getDiagnosticDataMap(String redisKey, int type, boolean err) {
        if (type == DiagnosticTypeEnum.SLOT_ANALYSIS.getType() && err == true) {
            Map<String, String> tmp = assistRedisService.hgetAll(redisKey);
            return tmp.keySet().stream()
                    .filter(key -> tmp.get(key).contains("error"))
                    .collect(Collectors.toMap(key -> key, key -> tmp.get(key)));
        }
        return assistRedisService.hgetAll(redisKey);
    }

    @Override
    public String getHotkeyDiagnosticData(String redisKey) {
        return assistRedisService.get(redisKey);
    }

    @Override
    public List<String> getScanDiagnosticData(String redisKey) {
        return assistRedisService.lrange(redisKey, 0, -1);
    }

    @Override
    public List<String> getScanCleanDiagnosticData(String redisKey) {
        return assistRedisService.lrange(redisKey, 0, -1);
    }

    @Override
    public List<String> getSampleScanData(Long appId, String nodes, String pattern) {
        List<String> sampleScanData = new ArrayList<>();
        String host = "";
        int port = 0;
        if (StringUtil.isBlank(nodes)) {
            List<InstanceInfo> instanceInfoList = appService.getAppOnlineInstanceInfo(appId);
            for (InstanceInfo instanceInfo : instanceInfoList) {
                if ("master".equals(instanceInfo.getRoleDesc())) {
                    host = instanceInfo.getIp();
                    port = instanceInfo.getPort();
                    break;
                }
            }
        } else {
            String[] nodeArray = nodes.split(",");
            String hostPort = nodeArray.length > 0 ? nodeArray[0] : "";
            if (!StringUtil.isBlank(hostPort)) {
                host = hostPort.split(":").length > 0 ? hostPort.split(":")[0] : "";
                port = hostPort.split(":").length > 1 ? Integer.parseInt(hostPort.split(":")[1]) : 0;
            }
        }

        long startTime = System.currentTimeMillis();
        Jedis jedis = null;
        try {
            jedis = redisCenter.getAdminJedis(appId, host, port);

            long dbSize = jedis.dbSize();
            if (dbSize == 0) {
                log.info("{} {}:{} dbsize is {}", appId, host, port, dbSize);
                return sampleScanData;
            }
            log.info("{} {}:{} total key is {} ", appId, host, port, dbSize);
            // scan参数
            byte[] cursor = "0".getBytes(Charset.forName("UTF-8"));
            ScanParams scanParams = StringUtil.isBlank(pattern) ?
                    new ScanParams().count(50) :
                    new ScanParams().match(pattern).count(50);

            while (true) {
                try {
                    ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
                    cursor = scanResult.getCursorAsBytes();
                    List<byte[]> keyList = scanResult.getResult();
                    if (CollectionUtils.isNotEmpty(keyList)) {
                        sampleScanData.addAll(keyList.stream().map(byteKey -> new String(byteKey)).collect(Collectors.toList()));
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    //防止无限循环
                    if (sampleScanData.size() >= 50 || Arrays.equals("0".getBytes(Charset.forName("UTF-8")), cursor)) {
                        break;
                    }
                }
            }
            //结果存redis
            long cost = System.currentTimeMillis() - startTime;
            log.info("{} {}:{} scan key successfully, cost time is {} ms, total key is {}", appId, host, port, cost, sampleScanData.size());
            return sampleScanData;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("redis-cli -h {} -p {} admin auth error", host, port);
            log.error("scan key appId {} {}:{}  error:" + e.getMessage(), appId, host, port, e);
            return sampleScanData;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
