package com.sohu.cache.exception;

/**
 * 参数异常
 * 
 * @author leifu
 * @Date 2016-1-26
 * @Time 下午9:21:03
 */
public class IllegalParamException extends Exception {
    
    private static final long serialVersionUID = -1148039976867829902L;

    public IllegalParamException() {
        super();
    }

    public IllegalParamException(String message) {
        super(message);
    }

    public IllegalParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalParamException(Throwable cause) {
        super(cause);
    }
}
