package com.sohu.cache.entity;

import lombok.Data;

import java.sql.Date;

/**
 * Created by rucao on 2019/12/29
 */
@Data
public class AppClientStatisticGather {
    private long id;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 应用id
     */
    private long appId;
    /**
     * 格式yyyy-MM-dd 收集时间
     */
    private String gatherTime;
    /**
     * 累计命令调用次数
     */
    private long cmdCount;
    /**
     * 平均命令调用耗时
     */
    private double avgCmdCost;
    /**
     * 累计连接异常事件次数
     */
    private int connExpCount;
    /**
     * 平均连接异常事件耗时
     */
    private double avgConnExpCost;
    /**
     * 累计命令超时事件次数
     */
    private int cmdExpCount;
    /**
     * 平均命令超时事件耗时
     */
    private double avgCmdExpCost;
    /**
     * 应用实例数
     */
    private int instanceCount;
    /**
     * 平均碎片率
     */
    private double avgMemFragRatio;
    /**
     * 内存使用率
     */
    private double memUsedRatio;
    /**
     * 异常数（旧，待下线）
     */
    private int exceptionCount;
    /**
     * 慢查询次数
     */
    private int slowLogCount;
    /**
     * 延迟事件统计
     */
    private int latencyCount;

    /**
     * 客户端连接数
     */

    /**
     * 存储对象数
     */
    private long objectSize;
    /**
     * 应用客户端连接数
     */
    private long connectedClients;

    /**
     * 内存占用 byte
     */
    private long usedMemory;

    /**
     * 物理内存占用 byte
     */
    private long usedMemoryRss;

    /**
     * 进程系统态消耗(单位:秒)
     */
    private long maxCpuSys;

    /**
     * 进程用户态消耗(单位:秒)
     */
    private long maxCpuUser;

    /**
     * 拓扑诊断结果，0：正常，1：异常
     */
    private int topologyExamResult;

}
