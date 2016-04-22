package com.sohu.tv.jedis.stat.enums;

/**
 * 错误类型
 * 
 * @author leifu
 * @Date 2015年2月4日
 * @Time 下午4:54:02
 */
public enum ClientExceptionType {
    /**
     * redis产生的错误
     */
    REDIS_TYPE(1),
    /**
     * 客户端产生的错误(比如上报，定时线程抛出的异常)
     */
    CLIENT_EXCEPTION_TYPE(2),
    
    /**
     * redis-cluster异常
     */
    REDIS_CLUSTER(3);

    private int type;

    private ClientExceptionType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}