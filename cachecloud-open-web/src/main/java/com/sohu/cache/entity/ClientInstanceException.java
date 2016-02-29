package com.sohu.cache.entity;

/**
 * 客户端实例异常
 * @author leifu
 * @Date 2016年2月18日
 * @Time 下午5:07:42
 */
public class ClientInstanceException {

    private long appId;

    private long instanceId;

    private String instanceHost;

    private int instancePort;

    private int exceptionCount;

    public ClientInstanceException(long appId, long instanceId, String instanceHost, int instancePort, int exceptionCount) {
        this.appId = appId;
        this.instanceId = instanceId;
        this.instanceHost = instanceHost;
        this.instancePort = instancePort;
        this.exceptionCount = exceptionCount;
    }

    public ClientInstanceException() {
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceHost() {
        return instanceHost;
    }

    public void setInstanceHost(String instanceHost) {
        this.instanceHost = instanceHost;
    }

    public int getInstancePort() {
        return instancePort;
    }

    public void setInstancePort(int instancePort) {
        this.instancePort = instancePort;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    @Override
    public String toString() {
        return "ClientInstanceExceptionVO [appId=" + appId + ", instanceId=" + instanceId + ", instanceHost=" + instanceHost
                + ", instancePort=" + instancePort + ", exceptionCount=" + exceptionCount + "]";
    }

}
