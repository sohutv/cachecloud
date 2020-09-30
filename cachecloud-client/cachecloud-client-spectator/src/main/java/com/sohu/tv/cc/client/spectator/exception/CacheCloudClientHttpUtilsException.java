package com.sohu.tv.cc.client.spectator.exception;

/**
 * http工具异常类
 * @author leifu
 */
public class CacheCloudClientHttpUtilsException extends RuntimeException {

	private static final long serialVersionUID = 1087328658524130263L;

	public CacheCloudClientHttpUtilsException(String message) {
		super(message);
	}

	public CacheCloudClientHttpUtilsException(Throwable e) {
		super(e);
	}

	public CacheCloudClientHttpUtilsException(String message, Throwable cause) {
		super(message, cause);
	}
}