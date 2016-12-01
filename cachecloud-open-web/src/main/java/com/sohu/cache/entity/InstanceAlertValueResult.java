package com.sohu.cache.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 实例报警结果
 * @author leifu
 * @Date 2016年9月13日
 * @Time 上午10:54:49
 */
public class InstanceAlertValueResult {
    /**
     * 监控项
     */
    private String configKey;

    /**
     * 阀值
     */
    private String alertValue;
    
    /**
     * 比较类型：小于、等于、大于、不等于
     */
    private int compareType;

    /**
     * 监控说明
     */
    private String currentValue;

    /**
     * 1固定值,2差值
     */
    private int valueType;
    
    /**
     * 应用id
     */
    private long appId;
    
    /**
     * 实例ip
     */
    private String ip;
    
    /**
     * 实例端口
     */
    private int port;
    
    /**
     * 应用信息
     */
    private AppDesc appDesc;


    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getAlertValue() {
        return alertValue;
    }

    public void setAlertValue(String alertValue) {
        this.alertValue = alertValue;
    }
    
    public String getCompareInfo() {
        CompareTypeEnum compareTypeEnum = CompareTypeEnum.getByValue(compareType);
        return compareTypeEnum == null ? "" : compareTypeEnum.getInfo();
    }
    
    public static enum CompareTypeEnum {
        SMALLER(-1, "小于"),
        EQUAL(0, "等于"),
        BIGGER(1, "大于"),
        NOT_EQUAL(2, "不等于");

        public static Map<Integer, CompareTypeEnum> MAP = new HashMap<Integer, InstanceAlertValueResult.CompareTypeEnum>();
        static {
            for (CompareTypeEnum compareTypeEnum : CompareTypeEnum.values()) {
                MAP.put(compareTypeEnum.value, compareTypeEnum);
            }
        }
        
        public static CompareTypeEnum getByValue(int compareType) {
            return MAP.get(compareType);
        }
        
        private int value;
        
        private String info;

        private CompareTypeEnum(int value, String info) {
            this.value = value;
            this.info = info;
        }

        public int getValue() {
            return value;
        }

        public String getInfo() {
            return info;
        }

    }

    public int getCompareType() {
        return compareType;
    }

    public void setCompareType(int compareType) {
        this.compareType = compareType;
    }

    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public AppDesc getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(AppDesc appDesc) {
        this.appDesc = appDesc;
    }

    public String getAlertMessage() {
        return String.format("实际值为%s,%s预设值%s", currentValue, getCompareInfo(), alertValue);
    }
    
    @Override
    public String toString() {
        return "InstanceAlertValueResult [configKey=" + configKey + ", alertValue=" + alertValue + ", compareType="
                + compareType + ", currentValue=" + currentValue + ", valueType=" + valueType + ", appId=" + appId
                + ", ip=" + ip + ", port=" + port + "]";
    }
}
