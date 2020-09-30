package com.sohu.tv.cachecloud.client.redis.crossroom.stat;

import com.sohu.tv.cachecloud.client.redis.crossroom.RedisCrossRoomClient;
import com.sohu.tv.cachecloud.client.redis.crossroom.enums.HystrixStatCountTypeEnum;
import com.sohu.tv.cachecloud.client.redis.crossroom.util.NamedThreadFactory;
import com.sohu.tv.cc.client.spectator.util.AtomicLongMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Redis跨机房客户端监控
 * @author leifu
 * @Date 2016年9月19日
 * @Time 下午5:15:27
 */
public class RedisCrossRoomClientStatusChecker {

    private final Logger logger = LoggerFactory.getLogger(RedisCrossRoomClientStatusChecker.class);
    
    /**
     * 客户端
     */
    private RedisCrossRoomClient redisCrossRoomClient;
    
    /**
     * 每分钟检测一次
     */
    private final ScheduledExecutorService redisCrossRoomStatusScheduledExecutor = Executors.newScheduledThreadPool(3,
            new NamedThreadFactory("redisCrossRoomStatusScheduledExecutor", true));
    private ScheduledFuture<?> redisCrossRoomStatusScheduleFuture;
    private final int delay = 5;
    private final int fixCycle = 60;

    public RedisCrossRoomClientStatusChecker(RedisCrossRoomClient redisCrossRoomClient) {
        this.redisCrossRoomClient = redisCrossRoomClient;
    }

    /**
     * 初始化
     */
    public void start() {
        Runnable redisCrossRoomClientStatusCheckerThread = new Runnable() {
            @Override
            public void run() {
                try {
                    //检测错误率
                    checkAndDetectWhetherSwitch();
                } catch (Throwable e) {
                    logger.error("redisCrossRoomClientStatusChecker thread message is" + e.getMessage(), e);
                }
            }
        };
        // 启动定时任务
        redisCrossRoomStatusScheduleFuture = redisCrossRoomStatusScheduledExecutor.scheduleWithFixedDelay(
                redisCrossRoomClientStatusCheckerThread, delay, fixCycle, TimeUnit.SECONDS);
    }
    
    /**
     * 监测决定是否切换
     */
    private void checkAndDetectWhetherSwitch() {
        int checkMinutes = redisCrossRoomClient.getAlarmSwitchMinutes();
        try {
            //获取最近分钟数据
            Map<String, AtomicLongMap<Integer>> recentMinutesStatusDataMap = RedisCrossRoomClientStatusCollector.getRecentMinuteData(checkMinutes);
            boolean isMajorHealthy = isMajorHealthy(recentMinutesStatusDataMap);
            if (!isMajorHealthy) {
                redisCrossRoomClient.autoSwitchMajorMinor();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
	}

    /**
     * 计算错误率
     * @param recentMinutesStatusDataList
     * @return
     */
    private boolean isMajorHealthy(Map<String, AtomicLongMap<Integer>> recentMinutesStatusDataMap) {
        if (recentMinutesStatusDataMap == null) {
            return true;
        }
        //有些统计暂时没用上，后期调整策略时用。
        long allCount = 0;
        long runCount = 0;
        long fallbackAllCount = 0;
        long fallbackRunCount = 0;
        long fallbackFallbackCount = 0;
        for (AtomicLongMap<Integer> recentMinutesStatusData : recentMinutesStatusDataMap.values()) {
            if (recentMinutesStatusData == null) {
                continue;
            }
            allCount += recentMinutesStatusData.get(HystrixStatCountTypeEnum.ALL.getValue());
            runCount += recentMinutesStatusData.get(HystrixStatCountTypeEnum.RUN.getValue());
            fallbackAllCount += recentMinutesStatusData.get(HystrixStatCountTypeEnum.FALLBACK_ALL.getValue());
            fallbackRunCount += recentMinutesStatusData.get(HystrixStatCountTypeEnum.FALLBACK_RUN.getValue());
            fallbackFallbackCount += recentMinutesStatusData.get(HystrixStatCountTypeEnum.FALLBACK_FALLBACK.getValue());
        }
        //没有调用
        if (allCount <= 0 || fallbackAllCount <= 0){
            return true;
        }
        //错误率超过预设值阀值
        int switchMinCount = redisCrossRoomClient.getSwitchMinCount();
        double checkErrorPercentage = redisCrossRoomClient.getAlarmSwitchErrorPercentage();
        double recentMinuteErrorPercentage = fallbackAllCount * 1.0 / allCount;
        //错误率和至少的调用次数
        if (recentMinuteErrorPercentage > checkErrorPercentage && allCount > switchMinCount) {
            logger.warn("recentMinuteErrorPercentage is {}, allCount is {}", recentMinuteErrorPercentage, allCount);
            return false;
        }
        return true;
    }

    /**
     * 关闭
     */
    public void close() {
        try {
            redisCrossRoomStatusScheduleFuture.cancel(true);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }
    
    public RedisCrossRoomClient getRedisCrossRoomClient() {
        return redisCrossRoomClient;
    }


}
