package com.sohu.cache.entity;

import lombok.Data;

/**
 * 系统配置
 *
 * @author leifu
 */
@Data
public class SystemConfig {

    private String configKey;

    private String configValue;

    private String info;

    private int status;

    private int orderId;

}
