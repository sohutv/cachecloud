package com.sohu.cache.web.service;

import java.util.List;
import java.util.Map;

import com.sohu.cache.entity.SystemConfig;
import com.sohu.cache.web.enums.SuccessEnum;

/**
 * cachecloud配置服务
 * @author leifu
 * @Date 2016年5月23日
 * @Time 上午10:35:04
 */
public interface ConfigService {
    
    /**
     * 加载配置
     */
    public void reloadSystemConfig();

    /**
     * 更新配置
     * @param configMap
     * @return
     */
    SuccessEnum updateConfig(Map<String, String> configMap);

    /**
     * 获取配置列表
     * @param status
     * @return
     */
    List<SystemConfig> getConfigList(int status);
    
}
