package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 实例状态
 * @author leifu
 * @Date 2014年11月26日
 * @Time 下午5:05:35
 */
public enum InstanceStatusEnum {
    ERROR_STATUS(0, "心跳停止"),
    GOOD_STATUS(1, "运行中"),
    OFFLINE_STATUS(2, "已下线");

    private int status;
    
    private String info;
    
    private static Map<Integer, InstanceStatusEnum> MAP = new HashMap<Integer, InstanceStatusEnum>();
    static {
        for(InstanceStatusEnum instanceStatusEnum : InstanceStatusEnum.values()) {
            MAP.put(instanceStatusEnum.getStatus(), instanceStatusEnum);
        }
    }
    
    public static InstanceStatusEnum getByStatus(int status) {
        return MAP.get(status);
    }
    
    private InstanceStatusEnum(int status, String info) {
        this.status = status;
        this.info = info;
    }

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }
    
    
}
