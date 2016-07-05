package com.sohu.cache.entity;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * 实例的简化的统计信息
 *
 * User: lingguo
 * Date: 14-7-27 下午3:59
 */
public class InstanceStats {
    /* id */
    private long id;

    /* 实例id */
    private long instId;

    /* app id */
    private long appId;

    /* host id */
    private long hostId;

    /* ip地址 */
    private String ip;

    /* port */
    private int port;

    /* 主从，1主2从 */
    private byte role;

    /* 启用实例时设置的内存，单位：byte */
    private long maxMemory;

    /* 实例当前已用的内存，单位：byte */
    private long usedMemory;

    /*
     * 实例内存使用率
     */
    private double memUsePercent;

    /* 当前的item数 */
    private long currItems;

    /* 当前的连接数 */
    private int currConnections;

    /* 未命中数*/
    private long misses;

    /* 命中数 */
    private long hits;

    /* 开始收集时间 */
    private Timestamp createTime;

    /* 最后更新时间 */
    private Timestamp modifyTime;
    
    /**
     * 内存碎片率
     */
    private double memFragmentationRatio;
    
    /**
     * aof阻塞次数
     */
    private int aofDelayedFsync;

    private boolean isRun;

    /**
     * 实例相关全部统计指标
     */
    private Map<String,Object> infoMap;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getCurrItems() {
        return currItems;
    }

    public void setCurrItems(long currItems) {
        this.currItems = currItems;
    }

    public int getCurrConnections() {
        return currConnections;
    }

    public void setCurrConnections(int currConnections) {
        this.currConnections = currConnections;
    }


    public long getMisses() {
        return misses;
    }

    public void setMisses(long misses) {
        this.misses = misses;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(long hostId) {
        this.hostId = hostId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    public byte getRole() {
        return role;
    }

    public void setRole(byte role) {
        this.role = role;
    }

    public long getInstId() {
        return instId;
    }

    public void setInstId(long instId) {
        this.instId = instId;
    }

    public double getMemFragmentationRatio() {
        return memFragmentationRatio;
    }

    public void setMemFragmentationRatio(double memFragmentationRatio) {
        this.memFragmentationRatio = memFragmentationRatio;
    }

    public int getAofDelayedFsync() {
        return aofDelayedFsync;
    }

    public void setAofDelayedFsync(int aofDelayedFsync) {
        this.aofDelayedFsync = aofDelayedFsync;
    }

    @Override
    public String toString() {
        return "InstanceStats [id=" + id + ", instId=" + instId + ", appId=" + appId + ", hostId=" + hostId + ", ip="
                + ip + ", port=" + port + ", role=" + role + ", maxMemory=" + maxMemory + ", usedMemory=" + usedMemory
                + ", memUsePercent=" + memUsePercent + ", currItems=" + currItems + ", currConnections="
                + currConnections + ", misses=" + misses + ", hits=" + hits + ", createTime=" + createTime
                + ", modifyTime=" + modifyTime + ", memFragmentationRatio=" + memFragmentationRatio
                + ", aofDelayedFsync=" + aofDelayedFsync + ", isRun=" + isRun + ", infoMap=" + infoMap + "]";
    }

    public Map<String, Object> getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(Map<String, Object> infoMap) {
        this.infoMap = infoMap;
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean isRun) {
        this.isRun = isRun;
    }

    public double getMemUsePercent() {
        if(maxMemory<=0){
            return 0.0D;
        }
        double percent = 100 * (double) usedMemory / (maxMemory);
        DecimalFormat df = new DecimalFormat("##.##");
        return Double.parseDouble(df.format(percent));
    }
    
    /**
     * 命中率
     * @return
     */
    public String getHitPercent(){
		long totalHits = hits + misses;
		if (totalHits <= 0) {
			return "无命令执行";
		}
		double percent = 100 * (double) hits / totalHits;
		DecimalFormat df = new DecimalFormat("##.##");
		return df.format(percent) + "%";
    }
}
