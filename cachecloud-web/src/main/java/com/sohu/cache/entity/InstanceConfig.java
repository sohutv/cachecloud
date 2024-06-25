package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * 实例配置模板
 *
 * @author leifu
 */
@Data
public class InstanceConfig {

    private long id;

    /**
     * 配置名:为了防止与key冲突
     */
    private String configKey;

    /**
     * 配置值:为了防止与value冲突
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
     * 配置项绑定redis版本主键id
     */
    private int versionId;

    /**
     * 是否可重置：0不可，1可重置 (默认值：1)
     */
    private int refresh = 1;

    /**
     * 取值类型（0：默认值 config_value；1：从主节点拷贝），默认0
     */
    private int valueType;

    public InstanceConfig() {
    }

    public InstanceConfig(String configKey, String configValue){
        this.configKey = configKey;
        this.configValue = configValue;
    }

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

    public Date getUpdateTime() {
        return (Date) updateTime.clone();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = (Date) updateTime.clone();
    }

}
