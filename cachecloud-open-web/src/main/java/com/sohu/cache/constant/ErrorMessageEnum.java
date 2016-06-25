package com.sohu.cache.constant;

/**
 * 系统错误提示
 * @author leifu
 * @Date 2016-6-25
 * @Time 下午2:50:07
 */
public enum ErrorMessageEnum {

    INNER_ERROR_MSG(1, "系统异常，请观察系统日志!"),
    PARAM_ERROR_MSG(2, "参数错误!")
    ;
    
    private int id;
    
    private String message;

    private ErrorMessageEnum(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
    
    
}
