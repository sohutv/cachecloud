package com.sohu.cache.client.service;

import java.util.List;

import com.sohu.cache.entity.AppClientVersion;

/**
 * 客户端版本信息
 * @author leifu
 * @Date 2015年2月2日
 * @Time 上午10:19:59
 */
public interface ClientVersionService {

    /**
     * 保存客户端版本信息
     * @param appId
     * @param appClientIp
     * @param clientVersion
     */
    void saveOrUpdateClientVersion(long appId, String appClientIp, String clientVersion);
    
    /**
     * 获取应用的所有客户端版本信息
     * @param appId
     * @return
     */
    List<AppClientVersion> getAppAllClientVersion(long appId);
    
    /**
     * 获取应用的所有客户端版本信息(过滤掉版本网段 10.7 10.2 10.1)
     * @param appId
     * @return
     */
    List<AppClientVersion> getAppAllServerClientVersion(long appId);

    /**
     * 获取所有客户端版本
     * @return
     */
    List<AppClientVersion> getAll(long appId);

}
