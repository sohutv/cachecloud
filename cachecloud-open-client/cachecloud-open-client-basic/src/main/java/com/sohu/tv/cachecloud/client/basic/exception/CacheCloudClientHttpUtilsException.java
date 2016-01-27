package com.sohu.tv.cachecloud.client.basic.exception;

/**
 * http工具异常类
 * @author leifu
 * @Date 2015-1-31
 * @Time 下午6:41:22
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