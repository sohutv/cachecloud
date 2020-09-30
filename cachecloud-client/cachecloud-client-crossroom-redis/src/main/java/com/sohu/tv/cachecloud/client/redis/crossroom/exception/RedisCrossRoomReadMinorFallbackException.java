package com.sohu.tv.cachecloud.client.redis.crossroom.exception;

/**
 * 跨机房客户端monior-fallback抛出的异常
 * @author leifu
 * @Date 2016年9月21日
 * @Time 上午11:13:21
 */
public class RedisCrossRoomReadMinorFallbackException extends RedisCrossRoomException {

    private static final long serialVersionUID = 1084513155677145195L;

    public RedisCrossRoomReadMinorFallbackException(String message) {
		super(message);
	}

	public RedisCrossRoomReadMinorFallbackException(Throwable e) {
		super(e);
	}

	public RedisCrossRoomReadMinorFallbackException(String message, Throwable cause) {
		super(message, cause);
	}
}