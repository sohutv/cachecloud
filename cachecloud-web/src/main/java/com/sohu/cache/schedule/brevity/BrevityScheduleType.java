package com.sohu.cache.schedule.brevity;

import java.util.HashMap;
import java.util.Map;

/**
 * 短频任务类型
 * Created by yijunzhang
 */
public enum BrevityScheduleType {

    REDIS_INFO(10, 1, "redis基本信息采集"),
    REDIS_SLOWLOG(11, 20, "redis慢查询采集"),
    REDIS_LATENCY(12, 1, "redis延迟事件信息采集"),
    MACHINE_INFO(50, 1, "机器信息采集"),
    MACHINE_MONITOR(51, 20, "机器监控报警任务"),
    MACHINE_NMON(52, 5, "机器nmon数据采集");

    private static Map<Integer, BrevityScheduleType> MAP = new HashMap();

    static {
        for (BrevityScheduleType scheduleType : BrevityScheduleType.values()) {
            MAP.put(scheduleType.type, scheduleType);
        }
    }

    BrevityScheduleType(int type, int minutes, String info) {
        this.type = type;
        this.minutes = minutes;
        this.info = info;
    }

    //类型
    private int type;

    //分钟分段
    private int minutes;

    //任务说明
    private String info;

    public static BrevityScheduleType typeOf(int type) {
        return MAP.get(type);
    }

    public int getType() {
        return type;
    }

    public int getMinutes() {
        return minutes;
    }


    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "BrevityScheduleType{" +
                "type=" + type +
                ", minutes=" + minutes +
                ", info='" + info + '\'' +
                '}';
    }}
