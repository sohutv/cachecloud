package com.sohu.cache.entity;

/**
 * 系统配置
 * 
 * @author leifu
 * @Date 2016年5月23日
 * @Time 上午11:18:11
 */
public class SystemConfig {

    private String configKey;

    private String configValue;

    private String info;

    private int status;
    
    private int orderId;


    public SystemConfig() {
        super();
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "SystemConfig [configKey=" + configKey + ", configValue=" + configValue + ", info=" + info + ", status="
                + status + ", orderId=" + orderId + "]";
    }



}
