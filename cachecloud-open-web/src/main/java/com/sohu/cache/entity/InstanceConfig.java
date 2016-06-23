package com.sohu.cache.entity;

import java.util.Date;

/**
 * 实例配置模板
 * @author leifu
 * @Date 2016年6月22日
 * @Time 下午5:45:29
 */
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
     * 动态参数个数，例如configValue dump-%d.rdb，那么动态参数个数是1
     */
    private int dynamicParamCount;
    
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }


    public int getDynamicParamCount() {
        return dynamicParamCount;
    }

    public void setDynamicParamCount(int dynamicParamCount) {
        this.dynamicParamCount = dynamicParamCount;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "InstanceConfig [id=" + id + ", configKey=" + configKey + ", configValue=" + configValue
                + ", info=" + info + ", dynamicParamCount=" + dynamicParamCount + ", updateTime=" + updateTime
                + ", type=" + type + ", status=" + status + "]";
    }
    
    

    
}
