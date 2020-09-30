package com.sohu.cache.client.service;

import com.sohu.cache.client.command.AppClientParams;

import java.util.Map;

/**
 * 应用级别客户端查询服务
 * Created by zhangyijun on 2017/8/4.
 */
public interface AppClientService {

    /**
     * 根据appId查询客户端信息
     *
     * @param appClientParams
     * @return
     */
    Map<String, Object> getAppClientInfo(AppClientParams appClientParams);

}
