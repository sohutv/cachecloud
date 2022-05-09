package com.sohu.cache.web.vo;

import lombok.Data;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/29 11:11
 * @Description: 应用redis 配置校验
 */
@Data
public class AppRedisCommandCheckVo {

    /**
     * 宿主机ip
     */
    private String machineIps;

    /**
     * pod ip
     */
    private String podIp;

    /**
     * 命令
     */
    private String command;

    /**
     * 检测方式
     */
    private Integer checkType;

    /**
     * info检测
     * info一级指标项
     */
    private String infoIndicate;

    /**
     * info检测
     * 配置项
     */
    private String indicateName;


    /**
     * info检测
     * 最大重试次数
     */
    private Integer maxTry = 40;

    /**
     * 比较值
     */
    private String expectValue;

    /**
     * log检测
     * 允许的log距离当前时间的范围，以分钟计（如10分钟以内）
     */
    private Integer minuteInternal = 10;

    public Integer getMaxTry(){
        if(maxTry == null){
            return 40;
        }
        return maxTry;
    }

    public Integer getMinuteInternal(){
        if(minuteInternal == null){
            return 10;
        }
        return minuteInternal;
    }

}
