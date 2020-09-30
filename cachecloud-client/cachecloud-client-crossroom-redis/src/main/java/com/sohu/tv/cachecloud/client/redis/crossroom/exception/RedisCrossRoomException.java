package com.sohu.tv.cachecloud.client.redis.crossroom.exception;

/**
 * 跨机房客户端异常
 * @author leifu
 * @Date 2016年9月21日
 * @Time 上午11:13:21
 */
public class RedisCrossRoomException extends RuntimeException {

    private static final long serialVersionUID = 4520820416130473522L;

    public RedisCrossRoomException(String message) {
		super(message);
	}

	public RedisCrossRoomException(Throwable e) {
		super(e);
	}

	public RedisCrossRoomException(String message, Throwable cause) {
		super(message, cause);
	}
}