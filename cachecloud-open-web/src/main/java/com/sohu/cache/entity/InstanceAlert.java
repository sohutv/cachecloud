package com.sohu.cache.entity;

import java.util.HashMap;
import java.util.Map;


/**
 * 实例报警阀值
 * 
 * @author leifu
 * @Date 2016年5月23日
 * @Time 上午11:18:11
 */
public class InstanceAlert {
    /**
     * 监控项
     */
    private String configKey;

    /**
     * 阀值
     */
    private String alertValue;
    
    /**
     * 比较类型：小于-1、等于0、大于1、不等于2 
     */
    private int compareType;

    /**
     * 监控说明
     */
    private String info;

    /**
     * 状态,1有效0无效
     */
    private int status;

    /**
     * 顺序
     */
    private int orderId;
    
    /**
     * 1固定值,2差值
     */
    private int valueType;

    public InstanceAlert() {
        super();
    }

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

    public int getCompareType() {
        return compareType;
    }

    public void setCompareType(int compareType) {
        this.compareType = compareType;
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

    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }
    
    public static enum StatusEnum {
        YES(1, "有效"),
        NO(0, "无效");

        private int value;
        
        private String info;

        private StatusEnum(int value, String info) {
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
    
    public static enum ValueTypeEnum {
        STATIC(1, "固定值"),
        DIFF(2, "差值");

        private int value;
        
        private String info;

        private ValueTypeEnum(int value, String info) {
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

    @Override
    public String toString() {
        return "InstanceAlert [configKey=" + configKey + ", alertValue=" + alertValue + ", compareType=" + compareType
                + ", info=" + info + ", status=" + status + ", orderId=" + orderId + ", valueType=" + valueType + "]";
    }


}
