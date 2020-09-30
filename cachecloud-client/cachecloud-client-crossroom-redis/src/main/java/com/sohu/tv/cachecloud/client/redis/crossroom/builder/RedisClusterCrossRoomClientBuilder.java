package com.sohu.tv.cachecloud.client.redis.crossroom.builder;

import com.sohu.tv.cachecloud.client.redis.crossroom.RedisCrossRoomClient;
import com.sohu.tv.cachecloud.client.redis.crossroom.command.BaseCommand;
import com.sohu.tv.cachecloud.client.redis.crossroom.impl.RedisClusterCrossRoomClientImpl;
import com.sohu.tv.cachecloud.client.redis.crossroom.notify.RedisCrossRoomAutoSwitchNotifier;

import redis.clients.jedis.PipelineCluster;

/**
 * redis跨机房客户端builder
 * 
 * @author leifu
 * @Date 2016年4月26日
 * @Time 下午5:01:41
 */
public class RedisClusterCrossRoomClientBuilder {

    /**
     * 主
     */
    private PipelineCluster majorPipelineCluster;

    /**
     * 备
     */
    private PipelineCluster minorPipelineCluster;

    /**
     * 主appid
     */
    private long majorAppId;

    /**
     * 备appid
     */
    private long minorAppId;

    /**
     * 通知
     */
    private RedisCrossRoomAutoSwitchNotifier autoSwitchNotifier;

    /**
     * 自动switch检测有效分钟，默认5分钟
     */
    private int alarmSwitchMinutes = 5;

    /**
     * 自动switch检测错误率，默认错误率50%
     */
    private double alarmSwitchPercentage = 0.5;

    /**
     * 至少调用xx次，才进行switch
     */
    private int switchMinCount = 200;
    
    /**
     * 默认不切换major和minor
     */
    public boolean switchEnabled = false;

    public RedisCrossRoomClient build() {
        return new RedisClusterCrossRoomClientImpl(majorAppId, majorPipelineCluster, minorAppId, minorPipelineCluster,
                autoSwitchNotifier, alarmSwitchMinutes, alarmSwitchPercentage, switchMinCount, switchEnabled);
    }

    public RedisClusterCrossRoomClientBuilder(long majorAppId, PipelineCluster majorPipelineCluster,
            long minorAppId, PipelineCluster minorPipelineCluster) {
        this.majorAppId = majorAppId;
        this.majorPipelineCluster = majorPipelineCluster;
        this.minorAppId = minorAppId;
        this.minorPipelineCluster = minorPipelineCluster;
    }

    public RedisClusterCrossRoomClientBuilder setAutoSwitchNotifier(RedisCrossRoomAutoSwitchNotifier autoSwitchNotifier) {
        this.autoSwitchNotifier = autoSwitchNotifier;
        return this;
    }

    public RedisClusterCrossRoomClientBuilder setAlarmSwitchMinutes(int alarmSwitchMinutes) {
        this.alarmSwitchMinutes = alarmSwitchMinutes;
        return this;
    }

    public RedisClusterCrossRoomClientBuilder setAlarmSwitchPercentage(double alarmSwitchPercentage) {
        this.alarmSwitchPercentage = alarmSwitchPercentage;
        return this;
    }
    
    public RedisClusterCrossRoomClientBuilder setSwitchMinCount(int switchMinCount) {
        this.switchMinCount = switchMinCount;
        return this;
    }

    public RedisClusterCrossRoomClientBuilder setSwitchEnabled(boolean switchEnabled) {
        this.switchEnabled = switchEnabled;
        return this;
    }
    
    public RedisClusterCrossRoomClientBuilder setMajorTimeOut(int majorTimeOut) {
        BaseCommand.majorTimeOut = majorTimeOut;
        return this;
    }

    public RedisClusterCrossRoomClientBuilder setMajorThreads(int majorThreads) {
        BaseCommand.majorThreads = majorThreads;
        return this;
    }

    public RedisClusterCrossRoomClientBuilder setMinorTimeOut(int minorTimeOut) {
        BaseCommand.minorTimeOut = minorTimeOut;
        return this;
    }

    public RedisClusterCrossRoomClientBuilder setMinorThreads(int minorThreads) {
        BaseCommand.minorThreads = minorThreads;
        return this;
    }


}
