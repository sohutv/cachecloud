package com.sohu.cache.entity;

import lombok.Data;

import java.sql.Date;

/**
 * Created by rucao on 2019/12/13
 */
@Data
public class AppClientCommandStatistics {
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
     * 应用id
     */
    private long appId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 命令明文
     */
    private String command;
    /**
     * 命令累计毫秒耗时
     */
    private long cost;
    /**
     * 命令调用量
     */
    private long count;
    /**
     * 输入流量
     */
    private long bytesIn;
    /**
     * 输出流量
     */
    private long bytesOut;

}
