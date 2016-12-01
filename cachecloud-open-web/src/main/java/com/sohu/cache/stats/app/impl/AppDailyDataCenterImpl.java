package com.sohu.cache.stats.app.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.sohu.cache.dao.AppClientExceptionStatDao;
import com.sohu.cache.dao.AppClientValueStatDao;
import com.sohu.cache.dao.AppStatsDao;
import com.sohu.cache.dao.InstanceSlowLogDao;
import com.sohu.cache.entity.AppClientValueDistriSimple;
import com.sohu.cache.entity.AppDailyData;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.stats.app.AppDailyDataCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.component.EmailComponent;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.util.VelocityUtils;
import com.sohu.cache.web.vo.AppDetailVO;

/**
 * 应用日报
 * @author leifu
 * @Date 2016年8月10日
 * @Time 下午5:17:02
 */
public class AppDailyDataCenterImpl implements AppDailyDataCenter {

    private Logger logger = LoggerFactory.getLogger(AppDailyDataCenterImpl.class);

    private EmailComponent emailComponent;

    private AppStatsCenter appStatsCenter;

    private VelocityEngine velocityEngine;

    private InstanceSlowLogDao instanceSlowLogDao;

    private AppClientExceptionStatDao appClientExceptionStatDao;

    private AppStatsDao appStatsDao;

    private AppClientValueStatDao appClientValueStatDao;
    
    private AppService appService;

    private final static int STAT_ERROR = 0;
    
    @Override
    public int sendAppDailyEmail() {
        Date endDate = new Date();
        Date startDate = DateUtils.addDays(endDate, -1);
        int successCount = 0;
        List<AppDesc> appDescList = appService.getAllAppDesc();
        for (AppDesc appDesc : appDescList) {
            try {
                boolean result = sendAppDailyEmail(appDesc.getAppId(), startDate, endDate);
                if (result) {
                    successCount++;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return successCount;
    }
    
    @Override
    public boolean sendAppDailyEmail(long appId, Date startDate, Date endDate) {
        try {
            AppDailyData appDailyData = generateAppDaily(appId, startDate, endDate);
            if (appDailyData == null) {
                return false;
            }
            AppDetailVO appDetailVO = appDailyData.getAppDetailVO();
            noticeAppDaily(startDate, appDetailVO, appDailyData);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public AppDailyData generateAppDaily(long appId, Date startDate, Date endDate) {
        Assert.isTrue(appId > 0L);
        AppDetailVO appDetailVO = appStatsCenter.getAppDetail(appId);
        if (appDetailVO == null) {
            logger.error("appId={} not exist", appId);
            return null;
        }
        AppDesc appDesc = appDetailVO.getAppDesc();
        if (appDesc.isOffline()) {
            return null;
        }
        if (appDesc.isTest()) {
            return null;
        }
        AppDailyData appDailyData = new AppDailyData();
        appDailyData.setStartDate(startDate);
        appDailyData.setEndDate(endDate);
        
        // 应用详情
        appDailyData.setAppDetailVO(appDetailVO);

        // 慢查询
        int slowLogCount = getSlowLogCount(appId, startDate, endDate);
        appDailyData.setSlowLogCount(slowLogCount);

        // 客户端异常数
        int clientExceptionCount = getClientExceptionCount(appId, startDate, endDate);
        appDailyData.setClientExceptionCount(clientExceptionCount);

        // 客户端值分布
        Map<String, Long> valueSizeDistributeCountMap = getAppClientValueSizeDistributeCountMap(appId, startDate, endDate);
        appDailyData.setValueSizeDistributeCountMap(valueSizeDistributeCountMap);

        
        // 应用相关统计
        Map<String, Object> appMinuteStatMap = getAppMinuteStat(appId, startDate, endDate);
        appDailyData.setMaxMinuteClientCount(MapUtils.getIntValue(appMinuteStatMap, "maxClientCount"));
        appDailyData.setAvgMinuteClientCount(MapUtils.getIntValue(appMinuteStatMap, "avgClientCount"));
        appDailyData.setMaxMinuteCommandCount(MapUtils.getIntValue(appMinuteStatMap, "maxCommandCount"));
        appDailyData.setAvgMinuteCommandCount(MapUtils.getIntValue(appMinuteStatMap, "avgCommandCount"));
        appDailyData.setMaxMinuteHitRatio(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "maxHitRatio") * 100.0));
        appDailyData.setMinMinuteHitRatio(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "minHitRatio") * 100.0));
        appDailyData.setAvgHitRatio(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "avgHitRatio") * 100.0));
        appDailyData.setAvgUsedMemory(MapUtils.getLongValue(appMinuteStatMap, "avgUsedMemory") / 1024 / 1024);
        appDailyData.setMaxUsedMemory(MapUtils.getLongValue(appMinuteStatMap, "maxUsedMemory") / 1024 / 1024);
        appDailyData.setExpiredKeysCount(MapUtils.getIntValue(appMinuteStatMap, "expiredKeys"));
        appDailyData.setEvictedKeysCount(MapUtils.getIntValue(appMinuteStatMap, "evictedKeys"));
        appDailyData.setAvgMinuteNetOutputByte(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "avgNetInputByte") / 1024.0 / 1024.0));
        appDailyData.setMaxMinuteNetOutputByte(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "maxNetInputByte") / 1024.0 / 1024.0));
        appDailyData.setAvgMinuteNetInputByte(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "avgNetOutputByte") / 1024.0 / 1024.0));
        appDailyData.setMaxMinuteNetInputByte(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "maxNetOutputByte") / 1024.0 / 1024.0));
        appDailyData.setAvgObjectSize(MapUtils.getIntValue(appMinuteStatMap, "avgObjectSize"));
        appDailyData.setMaxObjectSize(MapUtils.getIntValue(appMinuteStatMap, "maxObjectSize"));

        return appDailyData;
    }
    
    
    /**
     * 保留两位
     * @param num
     * @return
     */
    private double remainNumberTwoPoint(double num) {
        DecimalFormat df = new DecimalFormat("0.00");
        return NumberUtils.toDouble(df.format(num));
    }
    

    private Map<String, Long> getAppClientValueSizeDistributeCountMap(long appId, Date startDate, Date endDate) {
        try {
            String COLLECT_TIME_FORMAT = "yyyyMMddHHmmss";
            long startTime = NumberUtils.toLong(DateUtil.formatDate(startDate, COLLECT_TIME_FORMAT));
            long endTime = NumberUtils.toLong(DateUtil.formatDate(endDate, COLLECT_TIME_FORMAT));
            List<AppClientValueDistriSimple> appClientValueDistriSimpleList = appClientValueStatDao.getAppValueDistriList(appId, startTime, endTime);
            Map<String, Long> valueSizeInfoCountMap = new TreeMap<String, Long>();
            for (AppClientValueDistriSimple appClientValueDistriSimple : appClientValueDistriSimpleList) {
                valueSizeInfoCountMap.put(appClientValueDistriSimple.getDistributeDesc(),
                        appClientValueDistriSimple.getCount());
            }
            return valueSizeInfoCountMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 获取客户端连接数统计
     * 
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    private Map<String, Object> getAppMinuteStat(long appId, Date startDate, Date endDate) {
        try {
            String COLLECT_TIME_FORMAT = "yyyyMMddHHmm";
            long startTime = NumberUtils.toLong(DateUtil.formatDate(startDate, COLLECT_TIME_FORMAT));
            long endTime = NumberUtils.toLong(DateUtil.formatDate(endDate, COLLECT_TIME_FORMAT));
            return appStatsDao.getAppMinuteStat(appId, startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 客户端异常数
     * 
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    private int getClientExceptionCount(long appId, Date startDate, Date endDate) {
        try {
            String COLLECT_TIME_FORMAT = "yyyyMMddHHmmss";
            long startTime = NumberUtils.toLong(DateUtil.formatDate(startDate, COLLECT_TIME_FORMAT));
            long endTime = NumberUtils.toLong(DateUtil.formatDate(endDate, COLLECT_TIME_FORMAT));
            return appClientExceptionStatDao.getAppExceptionCount(appId, startTime, endTime, -1, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return STAT_ERROR;
        }
    }

    /**
     * 获取应用在指定日期内慢查询次数
     * 
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    private int getSlowLogCount(long appId, Date startDate, Date endDate) {
        try {
            return instanceSlowLogDao.getAppSlowLogCount(appId, startDate, endDate);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return STAT_ERROR;
        }
    }

    /**
     * 日报通知
     * @param startDate
     * @param appDetailVO
     * @param appDailyData
     */
    public void noticeAppDaily(Date startDate, AppDetailVO appDetailVO, AppDailyData appDailyData) {
        List<String> ccEmailList = getCCEmailList(appDetailVO.getAppDesc());
        String startDateFormat = DateUtil.formatYYYYMMdd(startDate);
        String title = String.format("【CacheCloud】%s日报(appId=%s)", startDateFormat, appDetailVO.getAppDesc().getAppId());
        String mailContent = VelocityUtils.createText(velocityEngine, appDetailVO.getAppDesc(), null, appDailyData, null, "appDaily.vm","UTF-8");
        emailComponent.sendMail(title, mailContent, appDetailVO.getEmailList(), ccEmailList);
    }
    
    /**
     * A级以上抄送管理员，S级抄送领导
     * @param appDesc
     * @return
     */
    private List<String> getCCEmailList(AppDesc appDesc) {
        Set<String> ccEmailSet = new LinkedHashSet<String>();
        //A级
        if (appDesc.isVeryImportant()) {
            for (String email : emailComponent.getAdminEmail().split(ConstUtils.COMMA)) {
                ccEmailSet.add(email);
            }
        }
        //S级
        if (appDesc.isSuperImportant()) {
            ccEmailSet.addAll(ConstUtils.LEADER_EMAIL_LIST);
        }
        return new ArrayList<String>(ccEmailSet);
    }
    
    public void setInstanceSlowLogDao(InstanceSlowLogDao instanceSlowLogDao) {
        this.instanceSlowLogDao = instanceSlowLogDao;
    }

    public void setEmailComponent(EmailComponent emailComponent) {
        this.emailComponent = emailComponent;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setAppClientExceptionStatDao(AppClientExceptionStatDao appClientExceptionStatDao) {
        this.appClientExceptionStatDao = appClientExceptionStatDao;
    }

    public void setAppStatsDao(AppStatsDao appStatsDao) {
        this.appStatsDao = appStatsDao;
    }

    public void setAppClientValueStatDao(AppClientValueStatDao appClientValueStatDao) {
        this.appClientValueStatDao = appClientValueStatDao;
    }

    public void setAppStatsCenter(AppStatsCenter appStatsCenter) {
        this.appStatsCenter = appStatsCenter;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

}
