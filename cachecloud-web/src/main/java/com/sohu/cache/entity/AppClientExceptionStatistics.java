package com.sohu.cache.entity;

import lombok.Data;

import java.sql.Date;

/**
 * Created by rucao on 2019/12/13
 */
@Data
public class AppClientExceptionStatistics {
    private long id;
    /**
     * 格式yyyyMMddHHmm00
     */
    private long currentMin;
    /**
     * 客户端ip
     */
    private String clientIp;
    /**
     * 客户端连接池配置信息
     */
    private String redisPoolConfig;

    /**
     * 0：连接失败；1：命令调用超时
     */
    private int type;
    /**
     * 应用id
     */
    private long appId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 节点信息host:port
     */
    private String node;
    /**
     * type=4(连接失败):累计连接失败次数
     * type=5(命令调用超时):累计超时次数
     */
    private long count;
    /**
     * type=4(连接失败):累计连接失败毫秒耗时
     * type=5(命令调用超时):累计超时毫秒耗时
     */
    private long cost;
    /**
     * type=5(命令调用超时):统计命令topN id,逗号分隔
     */
    private String latencyCommands;

}
