package com.sohu.cache.stats.instance.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.dao.InstanceAlertValueDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceStatsDao;
import com.sohu.cache.entity.InstanceAlert;
import com.sohu.cache.entity.InstanceAlert.StatusEnum;
import com.sohu.cache.entity.InstanceAlert.ValueTypeEnum;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceAlertValueResult.CompareTypeEnum;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.StandardStats;
import com.sohu.cache.stats.instance.InstanceAlertValueService;
import com.sohu.cache.util.JsonUtil;
import com.sohu.cache.web.component.EmailComponent;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.VelocityUtils;

/**
 * 实例报警阀值
 * 
 * @author leifu
 * @Date 2016年8月24日
 * @Time 上午11:46:12
 */
public class InstanceAlertValueServiceImpl implements InstanceAlertValueService {

    private Logger logger = LoggerFactory.getLogger(InstanceAlertValueServiceImpl.class);

    private InstanceAlertValueDao instanceAlertValueDao;

    private InstanceStatsDao instanceStatsDao;
    
    private InstanceDao instanceDao;
    
    private EmailComponent emailComponent;
    
    private VelocityEngine velocityEngine;
    
    private AppService appService;

    @Override
    public List<InstanceAlert> getAllInstanceAlert() {
        try {
            return instanceAlertValueDao.getAllInstanceAlert();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public int saveOrUpdate(InstanceAlert instanceAlert) {
        return instanceAlertValueDao.saveOrUpdate(instanceAlert);
    }

    @Override
    public InstanceAlert getByConfigKey(String configKey) {
        try {
            return instanceAlertValueDao.getByConfigKey(configKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int updateStatus(String configKey, int status) {
        return instanceAlertValueDao.updateStatus(configKey, status);
    }

    @Override
    public int remove(String configKey) {
        return instanceAlertValueDao.remove(configKey);
    }

    @Override
    public int monitorLastMinuteAllInstanceInfo() {
        List<InstanceAlertValueResult> resultList = new ArrayList<InstanceAlertValueResult>();
        // 固定值
        List<InstanceAlert> staticInstanceAlertList = instanceAlertValueDao.getByValueType(ValueTypeEnum.STATIC.getValue(), StatusEnum.YES.getValue());
        // 差值
        List<InstanceAlert> diffInstanceAlertList = instanceAlertValueDao.getByValueType(ValueTypeEnum.DIFF.getValue(), StatusEnum.YES.getValue());

        // 取上1分钟Redis实例统计信息
        Date date = new Date();
        Date beginTime = DateUtils.addMinutes(date, -2);
        Date endTime = DateUtils.addMinutes(date, -1);
        // 上一分钟Redis实例信息统计
        long start = System.currentTimeMillis();
        List<StandardStats> standardStatsList = instanceStatsDao.getStandardStatsByCreateTime(beginTime, endTime, "redis");
        long cost = System.currentTimeMillis() - start;
        if (cost > 2000) {
            logger.warn("getStandardStatsByCreateTime {} to {} costtime is {} ms", beginTime, endTime, cost);
        }
        // 遍历所有Redis实例统计信息，和预设阀值对比报警
        for (StandardStats standardStats : standardStatsList) {
            try {
                if (standardStats == null) {
                    continue;
                }
                // 固定值
                if (CollectionUtils.isNotEmpty(staticInstanceAlertList)) {
                    List<InstanceAlertValueResult> staticInstanceAlertResultList = checkStaticInstanceInfoAlert(standardStats, staticInstanceAlertList);
                    resultList.addAll(staticInstanceAlertResultList);
                }

                // 差值
                if (CollectionUtils.isNotEmpty(diffInstanceAlertList)) {
                    List<InstanceAlertValueResult> diffInstanceAlertResultList = checkDiffInstanceInfoAlert(standardStats, diffInstanceAlertList);
                    if (CollectionUtils.isNotEmpty(diffInstanceAlertResultList)) {
                        resultList.addAll(diffInstanceAlertResultList);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        // 最终报警
        sendInstanceAlertEmail(beginTime, endTime, resultList);
            
        return standardStatsList.size();
    }

    /**
     * 发送邮件
     * @param instanceAlertValueResultList
     */
    private void sendInstanceAlertEmail(Date beginTime, Date endTime, List<InstanceAlertValueResult> instanceAlertValueResultList) {
        if (CollectionUtils.isEmpty(instanceAlertValueResultList)) {
            return;
        }
        Collections.sort(instanceAlertValueResultList, new Comparator<InstanceAlertValueResult>() {

            @Override
            public int compare(InstanceAlertValueResult o1, InstanceAlertValueResult o2) {
                return (int) (o1.getAppId() - o2.getAppId());
            }
        });
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String emailTitle = String.format("Redis实例分钟报警(%s~%s)", sdf.format(beginTime), sdf.format(endTime));
        String emailContent = VelocityUtils.createText(velocityEngine, null, null, null, instanceAlertValueResultList, "instanceAlert.vm","UTF-8");
        emailComponent.sendMailToAdmin(emailTitle, emailContent.toString());
        
    }

    /**
     * 检测每个实例统计信息
     * 
     * @param standardStats
     * @param instanceAlertList
     * @return
     */
    public List<InstanceAlertValueResult> checkStaticInstanceInfoAlert(StandardStats standardStats,
            List<InstanceAlert> instanceAlertList) {
        List<InstanceAlertValueResult> resultList = new ArrayList<InstanceAlertValueResult>();
        // 标准Redis info统计信息
        String jsonInfo = standardStats.getInfoJson();
        if (StringUtils.isBlank(jsonInfo)) {
            return null;
        }
        // 转换成Map
        Map<String, Object> infoMap = JsonUtil.fromJson(jsonInfo, Map.class);
        if (MapUtils.isEmpty(infoMap)) {
            return null;
        }
        // 转换成Map<String, Map<String,Object>>
        for (Entry<String, Object> entry : infoMap.entrySet()) {
            Object object = entry.getValue();
            if (!(object instanceof Map)) {
                continue;
            }
            Map<String, Object> sectionInfoMap = (Map<String, Object>) object;
            for (Entry<String, Object> sectionInfoEntry : sectionInfoMap.entrySet()) {
                String infoKey = sectionInfoEntry.getKey();
                Object infoValue = sectionInfoEntry.getValue();
                InstanceAlertValueResult instanceAlertValueResult = generateInstanceValueResult(instanceAlertList, infoKey,
                        infoValue, standardStats.getIp(), standardStats.getPort());
                if (instanceAlertValueResult != null) {
                    resultList.add(instanceAlertValueResult);
                }
            }
        }
        return resultList;
    }

    /**
     * 检测每个实例统计信息
     * 
     * @param standardStats
     * @param instanceAlertList
     * @return
     */
    public List<InstanceAlertValueResult> checkDiffInstanceInfoAlert(StandardStats standardStats,
            List<InstanceAlert> instanceAlertList) {
        List<InstanceAlertValueResult> resultList = new ArrayList<InstanceAlertValueResult>();
        // 标准Redis info差值
        String jsonInfo = standardStats.getDiffJson();
        if (StringUtils.isBlank(jsonInfo)) {
            logger.error("id={}'s standardStats is empty", standardStats.getId());
            return null;
        }
        // 转换成Map
        Map<String, Object> infoMap = JsonUtil.fromJson(jsonInfo, Map.class);
        if (MapUtils.isEmpty(infoMap)) {
            return null;
        }
        // 转换成Map<String, Map<String,Object>>
        for (Entry<String, Object> entry : infoMap.entrySet()) {
            String infoKey = entry.getKey();
            Object infoValue = entry.getValue();
            InstanceAlertValueResult instanceAlertValueResult = generateInstanceValueResult(instanceAlertList, infoKey, infoValue,
                    standardStats.getIp(), standardStats.getPort());
            if (instanceAlertValueResult != null) {
                resultList.add(instanceAlertValueResult);
            }
        }
        return resultList;
    }

    /**
     * 比较info的没有一个属性
     * 
     * @param instanceAlertList
     * @param infoKey
     * @param infoValue
     * @return
     */
    private InstanceAlertValueResult generateInstanceValueResult(List<InstanceAlert> instanceAlertList, String infoKey,
            Object infoValue, String ip, int port) {
        for (InstanceAlert instanceAlert : instanceAlertList) {
            String alertConfigKey = instanceAlert.getConfigKey();
            if (StringUtils.isBlank(infoKey) || StringUtils.isBlank(alertConfigKey)
                    || !infoKey.equals(alertConfigKey)) {
                continue;
            }
            String alertInfoValue = instanceAlert.getAlertValue();
            // 比较类型 1和-1是大于和小于
            int compareType = instanceAlert.getCompareType();
            if (compareType == CompareTypeEnum.SMALLER.getValue() || compareType == CompareTypeEnum.BIGGER.getValue()) {
                double infoValueDouble = NumberUtils.toDouble(infoValue.toString());
                double alertInfoValueDouble = NumberUtils.toDouble(alertInfoValue);
                if ((compareType == -1 && infoValueDouble < alertInfoValueDouble)
                        || (compareType == 1 && infoValueDouble > alertInfoValueDouble)) {
                    return generateByInstanceStat(instanceAlert, infoKey, infoValue, ip, port);
                }
            }
            // 比较类型为0，表示等于
            else if (compareType == CompareTypeEnum.EQUAL.getValue() && infoValue.toString().equals(alertInfoValue)) {
                return generateByInstanceStat(instanceAlert, infoKey, infoValue, ip, port);
            }
            // 比较类型为2，表示不等于
            else if (compareType == CompareTypeEnum.NOT_EQUAL.getValue() && !infoValue.toString().equals(alertInfoValue)) {
                return generateByInstanceStat(instanceAlert, infoKey, infoValue, ip, port);
            }
        }
        return null;
    }

    /**
     * 生成实例报警结果
     * 
     * @param instanceAlert
     * @param infoKey
     * @param infoValue
     * @param ip
     * @param port
     * @return
     */
    private InstanceAlertValueResult generateByInstanceStat(InstanceAlert instanceAlert, String infoKey,
            Object infoValue, String ip, int port) {
        //根据infoKey决定单位等
        String alertValue = instanceAlert.getAlertValue();
        String infoValueStr = infoValue.toString();
        //网络输入输出流量、客户端最大输入buffer、aof文件当前尺寸
        if ("total_net_output_bytes".equals(infoKey) || "total_net_input_bytes".equals(infoKey)
                || "client_biggest_input_buf".equals(infoKey) || "aof_current_size".equals(infoKey)) {
            // 以MB为单位显示
            alertValue = changeByteToMB(alertValue);
            infoValueStr = changeByteToMB(infoValueStr);
        }
        InstanceAlertValueResult instanceAlertValueResult = new InstanceAlertValueResult();
        instanceAlertValueResult.setAlertValue(alertValue);
        instanceAlertValueResult.setCompareType(instanceAlert.getCompareType());
        instanceAlertValueResult.setValueType(instanceAlert.getValueType());
        instanceAlertValueResult.setConfigKey(infoKey);
        instanceAlertValueResult.setCurrentValue(infoValueStr);
        instanceAlertValueResult.setIp(ip);
        instanceAlertValueResult.setPort(port);
        InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(ip, port);
        if (instanceInfo != null) {
            long appId = instanceInfo.getAppId();
            instanceAlertValueResult.setAppId(appId);
            instanceAlertValueResult.setAppDesc(appService.getByAppId(appId));
        }
        return instanceAlertValueResult;
    }

    private String changeByteToMB(String value) {
        return NumberUtils.toLong(value) / 1000 / 1000 + "MB";
    }

    public void setInstanceStatsDao(InstanceStatsDao instanceStatsDao) {
        this.instanceStatsDao = instanceStatsDao;
    }

    public void setInstanceAlertValueDao(InstanceAlertValueDao instanceAlertValueDao) {
        this.instanceAlertValueDao = instanceAlertValueDao;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setEmailComponent(EmailComponent emailComponent) {
        this.emailComponent = emailComponent;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

}
