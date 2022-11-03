package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: zengyizhao
 * @CreateTime: 2022/9/2 09:09
 * @Description: 模块配置表
 * @Version: 1.0
 */
@Data
public class RedisModuleConfig {

    private long id;

    /**
     * 配置名
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置说明
     */
    private String info;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * Redis类型(参考ConstUtil)
     */
    private int type;

    /**
     * 状态，1有效0无效
     */
    private int status;

    /**
     * 配置项绑定module version版本主键id
     */
    private int versionId;

    /**
     * 是否可重置：0：不可；1：可重置
     */
    private int refresh;

    /**
     * module info 表id
     */
    private int moduleId;

    /**
     * 配置类型，0：加载和运行配置；1：加载时配置；2：运行时配置
     */
    private int configType;


    public String getStatusDesc() {
        if (1 == status) {
            return "有效";
        } else if (0 == status) {
            return "无效";
        } else {
            return "";
        }
    }

    public boolean isEffective() {
        if (1 == getStatus()) {
            return true;
        }
        return false;
    }

}
