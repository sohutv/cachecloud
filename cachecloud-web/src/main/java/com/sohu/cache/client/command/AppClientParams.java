package com.sohu.cache.client.command;

import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;

import java.util.List;

/**
 * Created by zhangyijun on 2017/8/7.
 */
public class AppClientParams {

    private final long appId;

    // 调用级别cache,加速初始化.
    private AppDesc cacheAppDesc;
    private List<InstanceInfo> cacheInstanceInfos;
    private String cacheMaxVersion;

    private final int type;

    private final String appClientIp;

    private final String clientVersion;

    public AppClientParams(long appId, int type, String appClientIp, String clientVersion) {
        this.appId = appId;
        this.type = type;
        this.appClientIp = appClientIp;
        this.clientVersion = clientVersion;
    }

    public long getAppId() {
        return appId;
    }

    public int getType() {
        return type;
    }

    public String getAppClientIp() {
        return appClientIp;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public AppDesc getCacheAppDesc() {
        return cacheAppDesc;
    }

    public void setCacheAppDesc(AppDesc cacheAppDesc) {
        this.cacheAppDesc = cacheAppDesc;
    }

    public List<InstanceInfo> getCacheInstanceInfos() {
        return cacheInstanceInfos;
    }

    public void setCacheInstanceInfos(List<InstanceInfo> cacheInstanceInfos) {
        this.cacheInstanceInfos = cacheInstanceInfos;
    }

    public String getCacheMaxVersion() {
        return cacheMaxVersion;
    }

    public void setCacheMaxVersion(String cacheMaxVersion) {
        this.cacheMaxVersion = cacheMaxVersion;
    }
}
