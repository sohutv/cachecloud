package com.sohu.cache.web.vo;

import lombok.Data;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/14 11:11
 * @Description: 应用redis 配置校验
 */
@Data
public class AppRedisConfigCheckVo {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * redis版本
     */
    private Integer versionId;

    /**
     * 配置项
     */
    private String configName;

    /**
     * 比较类型
     */
    private int compareType;

    /**
     * 比较值
     */
    private String expectValue;

}
