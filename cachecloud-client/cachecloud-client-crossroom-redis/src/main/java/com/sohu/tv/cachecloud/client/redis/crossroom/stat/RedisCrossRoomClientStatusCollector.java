package com.sohu.tv.cachecloud.client.redis.crossroom.stat;

import com.sohu.tv.cachecloud.client.redis.crossroom.enums.HystrixStatCountTypeEnum;
import com.sohu.tv.cachecloud.client.redis.crossroom.mark.RedisCrossRoomAutoSwitchInterface;
import com.sohu.tv.cachecloud.client.redis.crossroom.util.DateUtil;
import com.sohu.tv.cachecloud.client.redis.crossroom.util.NamedThreadFactory;
import com.sohu.tv.cc.client.spectator.util.AtomicLongMap;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 跨机房客户端数据收集
 * 
 * @author leifu
 * @Date 2016年9月19日
 * @Time 下午4:54:34
 */
public class RedisCrossRoomClientStatusCollector {

    private final static Logger logger = LoggerFactory.getLogger(RedisCrossRoomClientStatusCollector.class);

    /**
     * key是分钟时间戳 value是每种HystrixStatCountTypeEnum类型的计数
     */
    private static ConcurrentHashMap<String, AtomicLongMap<Integer>> hystrixAllTypeCountMap = new ConcurrentHashMap<String, AtomicLongMap<Integer>>();

    /**
     * 数据定时清理
     */
    private final static ScheduledExecutorService crossRoomStatusCleanScheduledExecutor = Executors.newScheduledThreadPool(2,
            new NamedThreadFactory("crossRoomStatusCleanScheduledExecutor", true));
    private static ScheduledFuture<?> crossRoomStatusCleanScheduleFuture;
    private final static int delay = 10;
    private final static int fixCycle = 60;

    static {
        init();
    }

    public static void init() {
        Runnable crossRoomStatusCleanThread = new Runnable() {
            @Override
            public void run() {
                try {
                    // 清理10分钟前数据
                    Date date = DateUtil.addMinutes(new Date(), -10);
                    String targetMinute = DateUtil.getCrossRoomStatusDateFormat().format(date);
                    cleanCrossRoomStatus(targetMinute);
                } catch (Throwable e) {
                    logger.error("crossRoomStatusCleanData thread message is" + e.getMessage(), e);
                }
            }
        };
        // 启动定时任务
        crossRoomStatusCleanScheduleFuture = crossRoomStatusCleanScheduledExecutor
                .scheduleWithFixedDelay(crossRoomStatusCleanThread, delay, fixCycle,
                        TimeUnit.SECONDS);
    }

    /**
     * 关闭
     */
    public static void close() {
        try {
            crossRoomStatusCleanScheduleFuture.cancel(true);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }
    
    /**
     * 清理数分钟前的数据
     * @param targetMinute
     */
    public static void cleanCrossRoomStatus(String targetMinute) {
        try {
            if (targetMinute == "" || "".equals(targetMinute)) {
                return;
            }
            long targetMinuteLong = NumberUtils.toLong(targetMinute);
            if (targetMinuteLong == 0) {
                return;
            }
            for (String tempMinuteStr : hystrixAllTypeCountMap.keySet()) {
                long tempMinuteLong = NumberUtils.toLong(tempMinuteStr);
                if (tempMinuteLong < targetMinuteLong) {
                    hystrixAllTypeCountMap.remove(tempMinuteStr);
                }
            }
        } catch (Exception e) {
            logger.error("cleanCrossRoomStatus: " + e.getMessage(), e);
        }
    }
    
    public static void collectCrossRoomStatus(HystrixStatCountTypeEnum hystrixStatCountTypeEnum) {
        if (!RedisCrossRoomAutoSwitchInterface.AUTO_SWITCH_ENABLED.get()) {
            return;
        }
        int type = hystrixStatCountTypeEnum.getValue();
        try {
            // 当前分钟
            String currentMinute = DateUtil.getCrossRoomStatusDateFormat().format(new Date());
            // 按照分钟计数
            if (hystrixAllTypeCountMap.containsKey(currentMinute)) {
                AtomicLongMap<Integer> stat = hystrixAllTypeCountMap.get(currentMinute);
                stat.getAndIncrement(type);
            } else {
                AtomicLongMap<Integer> stat = AtomicLongMap.create();
                stat.getAndIncrement(type);
                AtomicLongMap<Integer> currentStat = hystrixAllTypeCountMap.putIfAbsent(currentMinute, stat);
                if (currentStat != null) {
                    currentStat.getAndIncrement(type);
                }
            }
        } catch (Exception e) {
            logger.error("collect readcommand stat error: " + e.getMessage(), e);
        }
    }

    /**
     * 获取近beforeMinutes分钟的数据
     * @param beforeMinutes
     * @return
     */
    public static Map<String, AtomicLongMap<Integer>> getRecentMinuteData(int beforeMinutes) {
        Map<String, AtomicLongMap<Integer>> resultMap = new LinkedHashMap<String, AtomicLongMap<Integer>>();
        String currentMinute = DateUtil.getCrossRoomStatusDateFormat().format(new Date());
        for (int i = 1; i < beforeMinutes; i++) {
            String minute = getLastMinute(currentMinute, i);
            AtomicLongMap<Integer> minuteData = hystrixAllTypeCountMap.get(minute);
            if (minuteData != null) {
                resultMap.put(minute, minuteData);
            }
        }
        return resultMap;
    }
    
    private static String getLastMinute(String currentMinute, int minutes) {
        try {
            SimpleDateFormat sdf = DateUtil.getCrossRoomStatusDateFormat();
            Date currentDate = sdf.parse(currentMinute);
            Date lastMinute = DateUtil.addMinutes(currentDate, (-1 * minutes));
            return sdf.format(lastMinute);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static ConcurrentHashMap<String, AtomicLongMap<Integer>> getHystrixAllTypeCountMap() {
        return hystrixAllTypeCountMap;
    }
    
}
