package com.sohu.cache.web.service.impl;

import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.dao.AppCapacityMonitorDao;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.entity.AppCapacityMonitor;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.service.AppAutoCapacityService;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.UserService;
import freemarker.template.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

/**
 * 容量监控
 * @author zengyizhao
 * @Date 2022年10月9日
 */
@Slf4j
@Service("appAutoCapacityService")
public class AppAutoCapacityServiceImpl implements AppAutoCapacityService {

    @Autowired
    private AppCapacityMonitorDao appCapacityMonitorDao;

    /**
     * 邮箱报警
     */
    @Autowired
    private EmailComponent emailComponent;

    @Autowired
    private AppDao appDao;

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppStatsCenter appStatsCenter;

    @Autowired
    private Configuration configuration;

    @Async("asyncExecutor")
    @Override
    public void checkAndExpandCapacity(AppDesc appDesc, int appMemUsePercent, Map<InstanceInfo, InstanceStats> instanceStatsMap) {
        try{
            if(appDesc != null){
                return;
            }
            Collection<InstanceStats> stats = instanceStatsMap.values();
            stats = stats.stream().filter(instanceStats -> instanceStats != null).collect(Collectors.toList());
            Long shardingMasterCount = stats.stream().filter(instanceStats -> instanceStats.getRole() == 1).count();
            int shardingMasterNum = Long.valueOf(shardingMasterCount == null ? 0L : shardingMasterCount).intValue();
            //保存当前内存使用
            List<Map.Entry<InstanceInfo, InstanceStats>> collect = instanceStatsMap.entrySet().stream().filter(entry -> entry.getValue() != null && entry.getValue().getRole() == 1).collect(Collectors.toList());
            long totalMem = collect.stream().mapToLong(entry -> entry.getValue().getMaxMemory()).sum();
            long appUsedMem = collect.stream().mapToLong(entry -> entry.getValue().getUsedMemory()).sum();
            long shardingUsedMem = 0;
            OptionalLong shardingUsedMaxMem = collect.stream().mapToLong(entry -> entry.getValue().getUsedMemory()).max();
            if(shardingUsedMaxMem.isPresent()){
                shardingUsedMem = shardingUsedMaxMem.getAsLong();
            }
            OptionalLong shardingMaxMem = stats.stream().filter(instanceStats -> instanceStats.getRole() == 1).mapToLong(stat -> stat.getMaxMemory()).max();
            long shardingMem = 0;
            if(shardingMaxMem.isPresent()){
                shardingMem = shardingMaxMem.getAsLong();
            }
            AppCapacityMonitor appCapacityMonitor = appCapacityMonitorDao.getAppCapacityMonitorByAppId(appDesc.getAppId());
            //无历史信息
            if(appCapacityMonitor == null){
                //添加记录并不进行下方check
                this.saveAppCapacityMonitor(appDesc, shardingMasterNum, totalMem, appUsedMem, shardingMem, shardingUsedMem);
                return;
            }
            //更新记录
            this.updateAppCapacityMonitor(appCapacityMonitor.getId(), appUsedMem, totalMem, shardingUsedMem, shardingMem);
            //判断是否减小过容量，更新缩容记录及时间
            if(totalMem < appCapacityMonitor.getCurMem()){
                this.cleanAppCapacityReduceSchedule(appCapacityMonitor.getId());
            }
        } catch(Exception exp){
            log.error("checkAndExpandCapacity app:{} error:{} ", appDesc, exp.getMessage());
        }
    }

    private void saveAppCapacityMonitor(AppDesc appDesc, int shardingMasterNum, long mem, long appMemUsed, long shardingMem, long shardingUsedMem) {
        ExpandConfig expandConfig = this.getExpandConfig(mem);
        AppCapacityMonitor appCapacityMonitor = new AppCapacityMonitor();
        appCapacityMonitor.setAppId(appDesc.getAppId());
        appCapacityMonitor.setShardingMasterNum(shardingMasterNum);
        appCapacityMonitor.setMem(mem);
        appCapacityMonitor.setCurMem(mem);
        appCapacityMonitor.setMemUsed(appMemUsed);
        appCapacityMonitor.setShardingMem(shardingMem);
        appCapacityMonitor.setCurShardingMem(shardingMem);
        appCapacityMonitor.setShardingMemUsed(shardingUsedMem);
        appCapacityMonitor.setExpandMemPercent(expandConfig.getExpandMemPercent());
        appCapacityMonitor.setExpandRatio(expandConfig.getExpandRatio());
        appCapacityMonitor.setExpandRatioTotal(expandConfig.getExpandRatioTotal());
        appCapacityMonitor.setExpandCount(expandConfig.getExpandCount());
        appCapacityMonitor.setIsExpand(1);
        appCapacityMonitor.setIsReduce(1);
        appCapacityMonitor.setUpdateTime(new Date());
        appCapacityMonitorDao.save(appCapacityMonitor);
    }

    private void updateAppCapacityMonitor(long id, long appMemUsed, long curMem, long shardingUsedMem, long curShardingMem) {
        ExpandConfig expandConfig = this.getExpandConfig(curMem);
        AppCapacityMonitor appCapacityMonitor = new AppCapacityMonitor();
        appCapacityMonitor.setId(id);
        appCapacityMonitor.setMemUsed(appMemUsed);
        appCapacityMonitor.setCurMem(curMem);
        appCapacityMonitor.setShardingMemUsed(shardingUsedMem);
        appCapacityMonitor.setCurShardingMem(curShardingMem);
        appCapacityMonitor.setExpandMemPercent(expandConfig.getExpandMemPercent());
        appCapacityMonitor.setExpandRatio(expandConfig.getExpandRatio());
        appCapacityMonitor.setExpandRatioTotal(expandConfig.getExpandRatioTotal());
        appCapacityMonitor.setExpandCount(expandConfig.getExpandCount());
        appCapacityMonitorDao.update(appCapacityMonitor);
    }

    private void cleanAppCapacityReduceSchedule(long id) {
        AppCapacityMonitor appCapacityMonitor = new AppCapacityMonitor();
        appCapacityMonitor.setId(id);
        appCapacityMonitor.setScheduleStatus(0);
        appCapacityMonitorDao.updateAppCapacityReduceSchedule(appCapacityMonitor);
    }

    @Override
    public void updateAppCapacityMonitor(AppCapacityMonitor appCapacityMonitor) {
        appCapacityMonitorDao.update(appCapacityMonitor);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExpandConfig{

        //初始内存下限G
        private long memSmall;

        //初始内存上限G
        private long memGreat;

        //当日扩容次数限制
        private int expandCount;

        //扩容内存使用率百分比
        private int expandMemPercent;

        //扩容比率
        private int expandRatio;

        //总扩容比率
        private int expandRatioTotal;

        public ExpandConfig(String[] params){
            if(params != null && params.length == 6){
                this.memSmall = Long.valueOf(params[0]);
                this.memGreat = Long.valueOf(params[1]);
                this.expandCount = Integer.valueOf(params[2]);
                this.expandMemPercent = Integer.valueOf(params[3]);
                this.expandRatio = Integer.valueOf(params[4]);
                this.expandRatioTotal = Integer.valueOf(params[5]);
            }
        }
    }

    private ExpandConfig getExpandConfig(long mem){
        String redisExpandConfig = ConstUtils.REDIS_EXPAND_CAPACITY_CONFIG;
        List<ExpandConfig> defaultExpandConfigList = new ArrayList<>();
        if(StringUtils.isNotBlank(redisExpandConfig)){
            String[] expandConfigs = redisExpandConfig.split(";");
            List<String> list = Arrays.asList(expandConfigs);
            list.stream().forEach(expandConfig -> defaultExpandConfigList.add(new ExpandConfig(expandConfig.split(","))));
        }
        ExpandConfig meetExpandConfig = null;
        Optional<ExpandConfig> expandConfigOptional = defaultExpandConfigList.stream().filter(expandConfig -> (expandConfig.getMemSmall() * 1024 * 1024 * 1024 < mem && expandConfig.getMemGreat() * 1024 * 1024 * 1024 >= mem)).findFirst();
        if(expandConfigOptional.isPresent()){
            meetExpandConfig = expandConfigOptional.get();
        }
        return meetExpandConfig;
    }

    @Override
    public void updateAppMemUsedHistory() {
        List<AppDesc> appDescList = appDao.getOnlineApps();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd0000");
        Date date = new Date();
        Date beginDate = DateUtils.addDays(date, 0 - ConstUtils.REDIS_MEM_USED_MAX_DAYS);
        String beginTime = sdf.format(beginDate);
        String endTime = sdf.format(date);
        appDescList.stream().forEach(appDesc -> {
            Long usedMemoryMax = appStatsCenter.getUsedMemoryMaxByTimeBetween(appDesc.getAppId(), Long.valueOf(beginTime), Long.valueOf(endTime));
            if(usedMemoryMax != null && usedMemoryMax > 0){
                appCapacityMonitorDao.updateAppUsedMemHistory(appDesc.getAppId(), usedMemoryMax);
            }
        });
    }

}
