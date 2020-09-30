package com.sohu.cache.web.enums;

/**
 * 
 * @author leifu
 * @Date 2016年1月12日
 * @Time 下午2:28:25
 */
public enum RedisOperateEnum {
   
    OP_SUCCESS(1),
    ALREADY_SUCCESS(2),
    FAIL(0);
    
    private int value;

    private RedisOperateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
    
}
