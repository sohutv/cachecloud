package com.sohu.cache.web.vo;

import com.sohu.cache.entity.InstanceInfo;
import lombok.Data;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/14 11:11
 * @Description: 应用redis 配置校验
 */
@Data
public class InstanceRedisConfigCheckResult {

    /**
     * 校验结果
     */
    private boolean success;

    /**
     * 配置项
     */
    private String configName;

    /**
     * 配置预期值
     */
    private String expectValue;

    /**
     * 实际值
     */
    private String realValue;

    /**
     * 实例信息
     */
    private InstanceInfo instanceInfo;

}
