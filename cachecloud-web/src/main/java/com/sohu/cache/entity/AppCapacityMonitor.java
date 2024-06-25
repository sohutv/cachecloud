package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: zengyizhao
 * @DateTime: 2022/10/10 9:38
 * @Description: 应用容量（自动扩容、缩容审计报表）
 */
@Data
public class AppCapacityMonitor {

    /**
     * 记录id
     */
    private long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 应用主节点数
     */
    private Integer shardingMasterNum;

    /**
     * 应用初始内存
     */
    private long mem;

    /**
     * 应用当前内存
     */
    private long curMem;

    /**
     * 应用使用内存
     */
    private long memUsed;

    /**
     * 应用已使用内存（历史最大值）
     */
    private long memUsedHistory;

    /**
     * 应用分片初始内存
     */
    private long shardingMem;

    /**
     * 应用分片当前内存
     */
    private long curShardingMem;

    /**
     * 分片已使用内存（最大值）
     */
    private long shardingMemUsed;

    /**
     * 应用扩容内存使用百分比
     */
    private Integer expandMemPercent;

    /**
     * 扩容比率，单次扩容比率
     */
    private Integer expandRatio;

    /**
     * 当日总扩容比率（超出不可扩容），0：不限制
     */
    private Integer expandRatioTotal;

    /**
     * 是否可扩容: 0否，1是
     */
    private Integer isExpand;

    /**
     * 是否可缩容: 0否，1是
     */
    private Integer isReduce;

    /**
     * 缩容内存使用率最小值
     */
    private Integer reduceRatioMin;

    /**
     * 缩容内存使用率最大值
     */
    private Integer reduceRatioMax;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 扩容时间（上次扩容时间）
     */
    private Date expandTime;

    /**
     * 计划状态：0:无意义；1：待缩容；2：待扩容
     */
    private Integer scheduleStatus;

    /**
     * 计划处理时间
     */
    private Date scheduleTime;

    /**
     * 当日扩容次数限制
     */
    private int expandCount;

}
