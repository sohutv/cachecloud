package com.sohu.cache.dao;

import com.sohu.cache.entity.RedisModuleConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zengyizhao
 * @CreateTime: 2022/9/1 16:09
 * @Description: 模块配置表
 * @Version: 1.0
 */
public interface RedisModuleConfigDao {
    
    /**
     * 获取模块各版本所有配置
     * @param ModuleId
     * @return
     */
    List<RedisModuleConfig> getModuleConfigByModuleId(@Param("moduleId") int ModuleId);

    /**
     * 获取模块对应版本的所有配置
     * @param versionId redis版本主键id
     * @return redis版本所有配置项
     */
    List<RedisModuleConfig> getModuleConfigByVersionId(@Param("versionId") int versionId);

    /**
     * 保存或者更新配置模板
     * 
     * @param moduleConfig
     * @return
     */
    int saveOrUpdate(RedisModuleConfig moduleConfig);

    /**
     * 根据id获取配置模板
     * 
     * @param id
     * @return
     */
    RedisModuleConfig getById(@Param("id") long id);

    /**
     * 根据条件获取配置
     * 
     * @param moduleConfig
     * @return
     */
    List<RedisModuleConfig> getByCondition(RedisModuleConfig moduleConfig);

    /**
     * 更改配置状态
     * @param id
     * @param status
     * @return
     */
    int updateStatus(@Param("id") long id, @Param("status") int status);

}
