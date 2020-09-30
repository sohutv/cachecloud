package com.sohu.cache.exception;

/**
 * ssh异常
 * @author leifu
 * @Date 2016-1-26
 * @Time 下午9:18:54
 */
public class SSHException extends Exception {

    private static final long serialVersionUID = -6213665149000064880L;

    public SSHException() {
        super();
    }

    public SSHException(String message) {
        super(message);
    }

    public SSHException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSHException(Throwable cause) {
        super(cause);
    }

}
