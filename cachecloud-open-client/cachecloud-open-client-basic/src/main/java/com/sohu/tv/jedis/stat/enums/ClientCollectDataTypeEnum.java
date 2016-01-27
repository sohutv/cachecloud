package com.sohu.tv.jedis.stat.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 上报类型
 * @author leifu
 * @Date 2015年1月13日
 * @Time 下午6:33:22
 */
public enum ClientCollectDataTypeEnum {
    /**
     * 耗时分布
     */
    COST_TIME_DISTRI_TYPE(1, "cost_time_distri_type"),
    
    /**
     * 值大小分布
     */
    VALUE_LENGTH_DISTRI_TYPE(2, "value_length_distri_type"),
    
    /**
     * 异常
     */
    EXCEPTION_TYPE(3, "exception_type");
    
    
    public static Map<Integer, ClientCollectDataTypeEnum> MAP = new HashMap<Integer, ClientCollectDataTypeEnum>();
    
    static{
        for(ClientCollectDataTypeEnum clientCollectDataTypeEnum : ClientCollectDataTypeEnum.values()){
            MAP.put(clientCollectDataTypeEnum.getValue(), clientCollectDataTypeEnum);
        }
    }
    

    private int value;
    private String info;

    private ClientCollectDataTypeEnum(int value, String info) {
        this.value = value;
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public int getValue() {
        return value;
    }
    
    public static boolean isRightType(Integer type){
        return MAP.containsKey(type);
    }
    
    public static ClientCollectDataTypeEnum get(Integer type){
        return MAP.get(type);
    }

}
