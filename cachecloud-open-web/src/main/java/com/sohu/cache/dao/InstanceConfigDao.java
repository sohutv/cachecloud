package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.InstanceConfig;

/**
 * 配置模板Dao
 * 
 * @author leifu
 * @Date 2016年6月22日
 * @Time 下午5:46:37
 */
public interface InstanceConfigDao {
    
    /**
     * 获取所有配置模板
     * @return
     */
    List<InstanceConfig> getAllInstanceConfig();

    /**
     * 根据type获取配置模板列表
     * 
     * @param type
     * @return
     */
    List<InstanceConfig> getByType(@Param("type") int type);

    /**
     * 保存或者更新配置模板
     * 
     * @param instanceConfig
     * @return
     */
    int saveOrUpdate(InstanceConfig instanceConfig);

    /**
     * 根据id获取配置模板
     * 
     * @param id
     * @return
     */
    InstanceConfig getById(@Param("id") long id);

    /**
     * 根据configKey和type获取配置
     * 
     * @param configKey
     * @param type
     * @return
     */
    InstanceConfig getByConfigKeyAndType(@Param("configKey") String configKey, @Param("type") int type);

    /**
     * 更改配置状态
     * @param id
     * @param status
     * @return
     */
    int updateStatus(@Param("id") long id, @Param("status") int status);

    /**
     * 删除配置
     * @param id
     * @return
     */
    int remove(@Param("id") long id);

}
