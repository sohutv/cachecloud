package com.sohu.cache.entity;

import lombok.Data;

/**
 * Redis版本管理统计信息
 */
@Data
public class RedisVersionStat {

    /**
     * Redis版本信息
     */
    SystemResource redisVersion;

    /**
     * 已安装机器数量
     */
    int installNum;

    /**
     * 未安装机器数量
     */
    int uninstallNum;

    /**
     * 安装异常机器数量
     */
    int installExceptionNum;

    /**
     * 机器总量
     */
    int totalMachineNum;

    /**
     * 已安装比例 num%
     */
    int installRatio;

    /**
     * 应用使用数量
     */
    int appUsedNum;

    /**
     * 总应用数量
     */
    int totalAppNum;

    /**
     * 未安装机器ip
     */
    int unInstallIp;

    public RedisVersionStat() {
    }

    public RedisVersionStat(SystemResource redisVersion) {
        this.redisVersion = redisVersion;
    }
}
