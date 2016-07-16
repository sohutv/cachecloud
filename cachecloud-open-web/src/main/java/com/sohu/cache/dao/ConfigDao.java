package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.SystemConfig;

/**
 * 配置修改dao
 * 
 * @author leifu
 * @Date 2016年5月23日
 * @Time 下午12:51:45
 */
public interface ConfigDao {

    /**
     * 更新配置对key-value
     * 
     * @param configKey
     * @param configValue
     */
    public void update(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 获取配置列表
     * 
     * @param status
     * @return
     */
    public List<SystemConfig> getConfigList(@Param("status") int status);

}
