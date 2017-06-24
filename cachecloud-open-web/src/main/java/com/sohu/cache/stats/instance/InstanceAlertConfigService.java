package com.sohu.cache.stats.instance;

import java.util.Date;
import java.util.List;

import com.sohu.cache.entity.InstanceAlertConfig;

/**
 * 实例报警阀值配置
 * @author leifu
 * @Date 2017年5月19日
 * @Time 下午2:12:29
 */
public interface InstanceAlertConfigService {

    /**
     * 获取所有实例报警配置列表
     * @return
     */
    List<InstanceAlertConfig> getAll();
    
    /**
     * 根据类型获取实例报警配置列表
     * @param type
     * @return
     */
    List<InstanceAlertConfig> getByType(int type);

    /**
     * 保存
     * @param instanceAlert
     * @return
     */
    int save(InstanceAlertConfig instanceAlertConfig);

    /**
     * 根据id获取
     * @param id
     * @return
     */
    InstanceAlertConfig get(int id);

    /**
     * 根据id删除
     * @param id
     * @return
     */
    int remove(int id);
    
    /**
     * 更新alertValue和checkCycle
     * @param id
     * @param alertValue
     * @param checkCycle
     */
    void update(long id, String alertValue, int checkCycle);
    
    /**
     * 更新配置的最后检测时间
     * @param id
     * @param lastCheckTime
     */
    void updateLastCheckTime(long id, Date lastCheckTime);
    
    /**
     * 监控所有Redis上一分钟状态
     */
    void monitorLastMinuteAllInstanceInfo();

}
