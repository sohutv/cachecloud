package com.sohu.cache.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * 实例报警结果
 * @author leifu
 * @Date 2017年6月19日
 * @Time 下午10:50:23
 */
public class InstanceAlertValueResult {
    
    /**
     * 实例报警配置
     */
    private InstanceAlertConfig instanceAlertConfig;
    
    /**
     * 实例信息
     */
    private InstanceInfo instanceInfo;

    /**
     * 当前值
     */
    private String currentValue;
    
    /**
     * 应用id
     */
    private long appId;
    
    /**
     * 单位
     */
    private String unit;

    /**
     * 应用信息
     */
    private AppDesc appDesc;
    
    /**
     * 其他信息
     */
    private String otherInfo;

    public InstanceAlertValueResult() {
    }

    public InstanceAlertValueResult(InstanceAlertConfig instanceAlertConfig, InstanceInfo instanceInfo,
            String currentValue, long appId, String unit) {
        this.instanceAlertConfig = instanceAlertConfig;
        this.instanceInfo = instanceInfo;
        this.currentValue = currentValue;
        this.appId = appId;
        this.unit = unit;
    }

    public InstanceAlertConfig getInstanceAlertConfig() {
        return instanceAlertConfig;
    }

    public void setInstanceAlertConfig(InstanceAlertConfig instanceAlertConfig) {
        this.instanceAlertConfig = instanceAlertConfig;
    }

    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }

    public void setInstanceInfo(InstanceInfo instanceInfo) {
        this.instanceInfo = instanceInfo;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public AppDesc getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(AppDesc appDesc) {
        this.appDesc = appDesc;
    }

    public String getUnit() {
        return unit;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAlertMessage() {
        return String.format("实际值为%s%s,%s预设值%s%s", currentValue, unit, instanceAlertConfig.getCompareInfo(),
                instanceAlertConfig.getAlertValue(), unit);
    }
    
    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
