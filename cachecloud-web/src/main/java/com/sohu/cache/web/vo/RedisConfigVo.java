package com.sohu.cache.web.vo;

import lombok.Data;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/14 11:11
 * @Description: redis 配置参数名和值
 */
@Data
public class RedisConfigVo {

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置值
     */
    private String configValue;

}
