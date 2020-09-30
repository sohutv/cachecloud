package com.sohu.tv.cachecloud.client.redis.crossroom.entity;

import java.util.List;

/**
 * 跨机房客户端major和monior的拓扑
 * @author leifu
 * @Date 2016年9月21日
 * @Time 上午9:57:59
 */
public class RedisCrossRoomTopology {
    
    /**
     * 主appId
     */
    private long majorAppId;
    
    /**
     * 主appId实例列表
     */
    private List<String> majorInstanceList;
    
    /**
     * 备appId
     */
    private long minorAppId;
    
    /**
     * 备appId实例列表
     */
    private List<String> minorInstanceList;

    public long getMajorAppId() {
        return majorAppId;
    }

    public void setMajorAppId(long majorAppId) {
        this.majorAppId = majorAppId;
    }

    public List<String> getMajorInstanceList() {
        return majorInstanceList;
    }

    public void setMajorInstanceList(List<String> majorInstanceList) {
        this.majorInstanceList = majorInstanceList;
    }

    public long getMinorAppId() {
        return minorAppId;
    }

    public void setMinorAppId(long minorAppId) {
        this.minorAppId = minorAppId;
    }

    public List<String> getMinorInstanceList() {
        return minorInstanceList;
    }

    public void setMinorInstanceList(List<String> minorInstanceList) {
        this.minorInstanceList = minorInstanceList;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("major appId:").append(majorAppId).append("\n");
        for (String majorInstance : majorInstanceList) {
            result.append("\t").append(majorInstance).append("\n");
        }
        result.append("minor appId:").append(minorAppId).append("\n");
        for (String minorInstance : minorInstanceList) {
            result.append("\t").append(minorInstance).append("\n");
        }
        return result.toString();
    }

    
}
