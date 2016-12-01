package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.InstanceAlert;

/**
 * 实例报警阀值Dao
 * 
 * @author leifu
 * @Date 2016年8月24日
 * @Time 上午11:55:25
 */
public interface InstanceAlertValueDao {

    List<InstanceAlert> getAllInstanceAlert();

    int saveOrUpdate(InstanceAlert instanceAlert);

    InstanceAlert getByConfigKey(@Param("configKey") String configKey);

    int updateStatus(@Param("configKey") String configKey, @Param("status") int status);

    int remove(@Param("configKey") String configKey);

    List<InstanceAlert> getByValueType(@Param("valueType") int valueType, @Param("status") int status);

}
