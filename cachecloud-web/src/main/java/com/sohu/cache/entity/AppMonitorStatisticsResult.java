package com.sohu.cache.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zengyizhao
 * @CreateTime: 2023/6/14 16:20
 * @Description: 查询应用统计信息（前端）实体类
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppMonitorStatisticsResult {

    private long appId;

    private Long connExpCount;

    private Long cmdExpCount;

    private Long slowLogCount;

    private Long latencyCount;

    private Long cmdCount;

    private String gatherTime;

}
