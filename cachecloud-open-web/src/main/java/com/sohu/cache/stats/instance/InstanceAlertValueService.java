package com.sohu.cache.stats.instance;

import java.util.List;

import com.sohu.cache.entity.InstanceAlert;

/**
 * 实例报警阀值
 * 
 * @author leifu
 * @Date 2016年8月24日
 * @Time 上午11:46:12
 */
public interface InstanceAlertValueService {

    /**
     * 获取所有实例报警阀值
     * 
     * @return
     */
    List<InstanceAlert> getAllInstanceAlert();

    /**
     * 保存或者更新实例报警阀值
     * 
     * @param instanceAlert
     * @return
     */
    int saveOrUpdate(InstanceAlert instanceAlert);

    /**
     * 根据id获取实例报警阀值
     * 
     * @param configKey
     * @return
     */
    InstanceAlert getByConfigKey(String configKey);

    /**
     * 更改实例报警阀值
     * 
     * @param configKey
     * @param status
     * @return
     */
    int updateStatus(String configKey, int status);

    /**
     * 删除实例报警阀值
     * 
     * @param configKey
     */
    int remove(String configKey);
    
    
    /**
     * 监控所有Redis上一分钟状态
     */
    int monitorLastMinuteAllInstanceInfo();

}
