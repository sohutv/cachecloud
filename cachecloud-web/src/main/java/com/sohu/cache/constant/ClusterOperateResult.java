package com.sohu.cache.constant;


/**
 * Cluster Operate Result
 * @author leifu
 * @Date 2017年6月27日
 * @Time 上午8:43:10
 */
public class ClusterOperateResult {

    private int status;

    private String message;

    public ClusterOperateResult(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public static ClusterOperateResult success() {
        return new ClusterOperateResult(1, "");
    }

    public static ClusterOperateResult fail(String message) {
        return new ClusterOperateResult(0, message);
    }
    
    public boolean isSuccess() {
        return status == 1;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ClusterOperateResult [status=" + status + ", message=" + message + "]";
    }

}
