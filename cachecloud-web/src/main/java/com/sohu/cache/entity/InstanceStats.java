package com.sohu.cache.entity;

import lombok.Data;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * 实例的简化的统计信息
 *
 * User: lingguo
 */
@Data
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

    public Timestamp getCreateTime() {
        return (Timestamp) createTime.clone();
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = (Timestamp) createTime.clone();
    }

    public Timestamp getModifyTime() {
        return (Timestamp) modifyTime.clone();
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = (Timestamp) modifyTime.clone();
    }
}
