package com.sohu.cache.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.InstanceAlertConfig;

/**
 * 实例报警配置Dao
 * @author leifu
 * @Date 2017年5月19日
 * @Time 上午11:56:56
 */
public interface InstanceAlertConfigDao {
    
    int save(InstanceAlertConfig instanceAlertConfig);

    List<InstanceAlertConfig> getAll();
    
    List<InstanceAlertConfig> getByType(@Param("type") int type);
    
    List<InstanceAlertConfig> getByAlertConfig(@Param("alertConfig") String alertConfig);

    InstanceAlertConfig get(@Param("id") int id);

    int remove(@Param("id") int id);
    
    void update(@Param("id") long id, @Param("alertValue") String alertValue, @Param("checkCycle") int checkCycle);
    
    void updateLastCheckTime(@Param("id") long id, @Param("lastCheckTime") Date lastCheckTime);
    
}
