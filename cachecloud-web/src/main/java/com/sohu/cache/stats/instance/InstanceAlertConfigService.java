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
     * 根据类型获取实例报警配置列表
     * @param type 报警类型
     * @param appType 应用类型
     * @return
     */
    List<InstanceAlertConfig> getByTypeAndAppType(int type, int appType);

    /**
     * 保存
     * @param instanceAlertConfig 报警实例
     * @return
     */
    int save(InstanceAlertConfig instanceAlertConfig);

    /**
     * 保存
     * @param instanceAlertConfigList 报警实例列表
     * @return
     */
    int batchSave(List<InstanceAlertConfig> instanceAlertConfigList);

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
     * 根据alertConfig 获取全局配置，并根据比较类型进行筛选出紧急程度
     * @param alertConfig
     * @param compareType
     * @param appType
     * @return
     */
    InstanceAlertConfig getGlobalAlertConfigByCondition(String alertConfig, int compareType, Integer appType);

    /**
     * 更新alertValue和checkCycle
     * @param id
     * @param alertValue
     * @param checkCycle
     * @param compareType
     * @param importantLevel
     */
    void update(long id, String alertValue, int checkCycle, int compareType, int importantLevel);

    /**
     * 根据alertConfig和compareType更新所有报警配置的紧急程度
     * @param alertConfig
     * @param compareType
     * @param importantLevel
     * @param appType
     */
    void updateImportantLevel(String alertConfig, int compareType, int importantLevel, Integer appType);

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
