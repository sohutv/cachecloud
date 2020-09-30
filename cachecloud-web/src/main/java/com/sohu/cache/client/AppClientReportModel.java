package com.sohu.cache.client;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/12/16
 */
@Data
public class AppClientReportModel {
    /**
     * appId
     */
    private long appId;
    /**
     * 客户端版本
     */
    private String clientVersion;
    /**
     * 客户端ip
     */
    private String clientIp;
    /**
     * 客户端连接池配置信息
     */
    private Map<String, Object> config;
    /**
     * 上报数据时间
     */
    private long currentMin;
    /**
     * 统计耗时
     */
    private long cost;
    /**
     * 上报异常数据
     */
    private List<Map<String, Object>> exceptionModels;

    /**
     * 上报命令数据
     */
    private List<Map<String, Object>> commandStatsModels;

    /**
     * 其他信息
     */
    private Map<String, Object> otherInfo;

}
