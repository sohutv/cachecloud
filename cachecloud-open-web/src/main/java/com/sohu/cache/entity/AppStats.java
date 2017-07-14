package com.sohu.cache.entity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

/**
 * Created by yijunzhang on 14-6-9.
 */
public class AppStats {

    /**
     * 应用id
     */
    private long appId;

    /**
     * 收集时间:格式yyyyMMddHHmm/yyyyMMdd/yyyyMMddHH
     */
    private long collectTime;

    /**
     * 命中数量
     */
    private long hits;

    /**
     * 未命中数量
     */
    private long misses;

    /**
     * 命令执行次数
     */
    private long commandCount;

    /**
     * 内存占用
     */
    private long usedMemory;

    /**
     * 过期key数量
     */
    private long expiredKeys;

    /**
     * 驱逐key数量
     */
    private long evictedKeys;

    /**
     * 网络输入字节
     */
    private long netInputByte;

    /**
     * 网络输出字节
     */
    private long netOutputByte;

    /**
     * 客户端连接数
     */
    private int connectedClients;

    /**
     * 存储对象数
     */
    private long objectSize;

    /**
     * 累加的实例数
     */
    private int accumulation;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 命令统计集合
     */
    private List<AppCommandStats> commandStatsList;

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public long getHits() {
        return hits;
    }
    
    /**
     * 命中率
     * @return
     */
    public long getHitPercent() {
        long total = hits + misses;
        if (total == 0) {
            return 0;
        } else {
            NumberFormat formatter = new DecimalFormat("0");
            return NumberUtils.toLong(formatter.format(hits * 100.0 / total));
        }
    }
    
    public void setHits(long hits) {
        this.hits = hits;
    }

    public long getMisses() {
        return misses;
    }

    public void setMisses(long misses) {
        this.misses = misses;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getExpiredKeys() {
        return expiredKeys;
    }

    public void setExpiredKeys(long expiredKeys) {
        this.expiredKeys = expiredKeys;
    }

    public long getEvictedKeys() {
        return evictedKeys;
    }

    public void setEvictedKeys(long evictedKeys) {
        this.evictedKeys = evictedKeys;
    }

    public int getConnectedClients() {
        return connectedClients;
    }

    public void setConnectedClients(int connectedClients) {
        this.connectedClients = connectedClients;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public String toString() {
        return "AppStats{" +
                "appId=" + appId +
                ", collectTime=" + collectTime +
                ", hits=" + hits +
                ", misses=" + misses +
                ", usedMemory=" + usedMemory +
                ", expiredKeys=" + expiredKeys +
                ", evictedKeys=" + evictedKeys +
                ", connectedClients=" + connectedClients +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", objectSize=" + objectSize +
                ", accumulation=" + accumulation +
                '}';
    }

    public long getCommandCount() {
        return commandCount;
    }

    public void setCommandCount(long commandCount) {
        this.commandCount = commandCount;
    }

    public void setObjectSize(long objectSize) {
        this.objectSize = objectSize;
    }

    public long getObjectSize() {
        return objectSize;
    }

    public int getAccumulation() {
        return accumulation;
    }

    public void setAccumulation(int accumulation) {
        this.accumulation = accumulation;
    }

    public List<AppCommandStats> getCommandStatsList() {
        return commandStatsList;
    }

    public void setCommandStatsList(List<AppCommandStats> commandStatsList) {
        this.commandStatsList = commandStatsList;
    }

    public long getNetInputByte() {
        return netInputByte;
    }

    public void setNetInputByte(long netInputByte) {
        this.netInputByte = netInputByte;
    }

    public long getNetOutputByte() {
        return netOutputByte;
    }

    public void setNetOutputByte(long netOutputByte) {
        this.netOutputByte = netOutputByte;
    }
}
