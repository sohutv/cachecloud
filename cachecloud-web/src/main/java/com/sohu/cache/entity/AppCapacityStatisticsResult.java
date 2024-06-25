package com.sohu.cache.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zengyizhao
 * @CreateTime: 2023/6/14 16:20
 * @Description: 查询应用内存、内存审计统计信息
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppCapacityStatisticsResult {

    private long appId;

    private int versionId;

    private Long curMem;

    private Long memUsed;

    private Integer shardingMasterNum;

    private double memUsedRatio;

    public double getMemUsedRatio() {
        double ratio = 0D;
        if(curMem != null && memUsed != null && curMem != 0){
            String format = String.format("%.2f", memUsed * 1D/ curMem * 100);
            return Double.parseDouble(format);
        }
        return ratio;
    }
}
