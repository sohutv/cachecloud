package com.sohu.cache.web.vo;

import com.sohu.cache.entity.InstanceInfo;
import lombok.Data;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/14 11:11
 * @Description: 应用redis 命令校验结果
 */
@Data
public class InstanceRedisCommandCheckResult {

    /**
     * 校验结果
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 实例信息
     */
    private InstanceInfo instanceInfo;

}
