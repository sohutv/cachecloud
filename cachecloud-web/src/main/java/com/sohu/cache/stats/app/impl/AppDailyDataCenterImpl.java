package com.sohu.cache.stats.app.impl;

import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.alert.utils.AlertUtils;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.AppDailyData;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.stats.app.AppDailyDataCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.util.FreemakerUtils;
import com.sohu.cache.web.vo.AppDetailVO;
import freemarker.template.Configuration;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * 应用日报
 *
 * @author leifu
 * @Date 2016年8月10日
 * @Time 下午5:17:02
 */
@Service("appDailyDataCenter")
public class AppDailyDataCenterImpl implements AppDailyDataCenter {

    private final static int STAT_ERROR = 0;
    @Resource
    UserService userService;
    private Logger logger = LoggerFactory.getLogger(AppDailyDataCenterImpl.class);
    @Autowired
    private EmailComponent emailComponent;
    @Autowired
    private AppStatsCenter appStatsCenter;
    @Autowired
    private Configuration configuration;
    @Autowired
    private InstanceSlowLogDao instanceSlowLogDao;
    @Autowired
    private AppClientExceptionStatDao appClientExceptionStatDao;
    @Autowired
    private AppStatsDao appStatsDao;
    @Autowired
    private AppDailyDao appDailyDao;
    @Autowired
    private AppService appService;

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
            fillAppDailyData(appDailyData);
            //保存每天的日报，后期查询和分析
            appDailyDao.save(appDailyData);
            AppDetailVO appDetailVO = appDailyData.getAppDetailVO();
            noticeAppDaily(startDate, appDetailVO, appDailyData);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 填充信息
     *
     * @param appDailyData
     */
    private void fillAppDailyData(AppDailyData appDailyData) {
        appDailyData.setAppId(appDailyData.getAppDetailVO().getAppDesc().getAppId());
        appDailyData.setDate(appDailyData.getStartDate());
        Map<String, Long> valueSizeDistributeCountMap = appDailyData.getValueSizeDistributeCountMap();
        //@TODO 暂时不计数
        long bigKeyTimes = 0;
        StringBuffer bigKeyInfo = new StringBuffer();
        for (Entry<String, Long> entry : valueSizeDistributeCountMap.entrySet()) {
            String key = entry.getKey();
            long times = entry.getValue();
            bigKeyInfo.append(key + ":" + times + "\n");
        }
        appDailyData.setBigKeyInfo(bigKeyInfo.toString());
        appDailyData.setBigKeyTimes(bigKeyTimes);
    }

    public AppDailyData generateAppDaily(long appId, Date startDate, Date endDate) {
        Assert.isTrue(appId > 0L);
        AppDetailVO appDetailVO = appStatsCenter.getAppDetail(appId);
        if (appDetailVO == null) {
            logger.error("appId={} not exist", appId);
            return null;
        }
        AppDesc appDesc = appDetailVO.getAppDesc();
        appDesc.setOfficer(userService.getOfficerName(appDesc.getOfficer()));
        if (appDesc.isOffline()) {
            return null;
        }
        if (appDesc.isTestOk()) {
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

        String searchDate = DateUtil.formatDate(startDate, "yyyy-MM-dd");
        Map<Long, Map<String, Object>> appClientGatherStatMap = appService.getAppClientStatGather(appId, searchDate);
        Map<String, Object> appClientGatherStat = MapUtils.getMap(appClientGatherStatMap, appId);
        if (MapUtils.isNotEmpty(appClientGatherStat)) {
            appDailyData.setClientCmdCount(MapUtils.getLongValue(appClientGatherStat, "cmd_count", -0l));
            appDailyData.setClientAvgCmdCost(MapUtils.getDoubleValue(appClientGatherStat, "avg_cmd_cost", -0));
            appDailyData.setClientConnExpCount(MapUtils.getLongValue(appClientGatherStat, "conn_exp_count", -0l));
            appDailyData.setClientAvgConnExpCost(MapUtils.getDoubleValue(appClientGatherStat, "avg_conn_exp_cost", -0));
            appDailyData.setClientCmdExpCount(MapUtils.getLongValue(appClientGatherStat, "cmd_exp_count", -0l));
            appDailyData.setClientAvgCmdExpCost(MapUtils.getDoubleValue(appClientGatherStat, "avg_cmd_exp_cost", -0));
            appDailyData.setLatencyCount(MapUtils.getLongValue(appClientGatherStat, "latency_count", -0l));
        }

        // 客户端值分布
        Map<String, Long> valueSizeDistributeCountMap = new HashMap<>();
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
        appDailyData.setAvgUsedDisk(MapUtils.getLongValue(appMinuteStatMap, "avgUsedDisk") / 1024 / 1024);
        appDailyData.setMaxUsedDisk(MapUtils.getLongValue(appMinuteStatMap, "maxUsedDisk") / 1024 / 1024);
        appDailyData.setExpiredKeysCount(MapUtils.getIntValue(appMinuteStatMap, "expiredKeys"));
        appDailyData.setEvictedKeysCount(MapUtils.getIntValue(appMinuteStatMap, "evictedKeys"));
        appDailyData.setAvgMinuteNetOutputByte(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "avgNetOutputByte") / 1024.0 / 1024.0));
        appDailyData.setMaxMinuteNetOutputByte(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "maxNetOutputByte") / 1024.0 / 1024.0));
        appDailyData.setAvgMinuteNetInputByte(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "avgNetInputByte") / 1024.0 / 1024.0));
        appDailyData.setMaxMinuteNetInputByte(remainNumberTwoPoint(MapUtils.getDoubleValue(appMinuteStatMap, "maxNetInputByte") / 1024.0 / 1024.0));
        appDailyData.setAvgObjectSize(MapUtils.getIntValue(appMinuteStatMap, "avgObjectSize"));
        appDailyData.setMaxObjectSize(MapUtils.getIntValue(appMinuteStatMap, "maxObjectSize"));

        return appDailyData;
    }

    /**
     * 保留两位
     *
     * @param num
     * @return
     */
    private double remainNumberTwoPoint(double num) {
        DecimalFormat df = new DecimalFormat("0.00");
        return NumberUtils.toDouble(df.format(num));
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
     *
     * @param startDate
     * @param appDetailVO
     * @param appDailyData
     */
    public void noticeAppDaily(Date startDate, AppDetailVO appDetailVO, AppDailyData appDailyData) {
        List<String> ccEmailList = getCCEmailList(appDetailVO.getAppDesc());
        String startDateFormat = DateUtil.formatYYYYMMdd(startDate);
        String title = String.format("【CacheCloud】%s日报(appId=%s)", startDateFormat, appDetailVO.getAppDesc().getAppId());
        AppDesc appDesc = appDetailVO.getAppDesc();
        Map<String, Object> context = new HashMap<>();
        context.put("appDesc", appDesc);
        context.put("appDailyData", appDailyData);
        String mailContent = FreemakerUtils.createText("appDaily.ftl", configuration, context);
        // 发送日报
        emailComponent.sendDailyMail(title, mailContent, appDetailVO.getEmailList(), ccEmailList);
    }


    public void noteAppTopologyDaily(Date startDate, List<Map> topologyExamResult) {
        String startDateFormat = DateUtil.formatYYYYMMdd(startDate);
        String title = String.format("【CacheCloud】应用拓扑结构检查结果%s日报", startDateFormat);
        Map<String, Object> context = new HashMap<>();
        context.put("examResult", topologyExamResult);
        String mailContent = FreemakerUtils.createText("topologyExam.ftl", configuration, context);
        emailComponent.sendDailyMail(title, mailContent, Arrays.asList(AlertUtils.EMAILS.split(",")), null);
    }

    /**
     * A级以上抄送管理员，S级抄送领导
     *
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

    @Override
    public AppDailyData getAppDailyData(long appId, Date date) {
        try {
            return appDailyDao.getAppDaily(appId, new SimpleDateFormat("yyyy-MM-dd").format(date));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
