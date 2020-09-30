package com.sohu.cache.constant;

/**
 * Created by yijunzhang on 2018-12-26
 */
public class OperateResult {
    private boolean isSuccess;

    private String message;

    private OperateResult(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public static OperateResult success() {
        return new OperateResult(true, "");
    }

    public static OperateResult fail(String message) {
        return new OperateResult(false, message);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OperateResult [isSuccess=" + isSuccess + ", message=" + message + "]";
    }

}
