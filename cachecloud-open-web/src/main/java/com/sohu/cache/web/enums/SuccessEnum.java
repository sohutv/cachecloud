package com.sohu.cache.web.enums;

/**
 * 成功失败状态
 * 
 * @author leifu
 * @Time 2014年10月16日
 */
public enum SuccessEnum {
    SUCCESS(1),
    FAIL(0);

    int value;

    private SuccessEnum(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
    
    public String info() {
        if (value == 1) {
            return "成功";
        } else {
            return "失败";
        }
    }
}
