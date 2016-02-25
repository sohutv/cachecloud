package com.sohu.cache.schedule;

import com.sohu.cache.entity.TriggerInfo;

import org.quartz.TriggerKey;

import java.util.List;

/**
 * trigger管理接口
 *
 * @author: lingguo
 * @time: 2014/10/13 14:02
 */
public interface TriggerCenter {

    /**
     * 增加一个新的trigger
     *
     * @param jobGroup  trigger所属的job分组：redis/machine/machineMonitor
     * @param ip
     * @param port
     * @return
     */
    public boolean addTrigger(String jobGroup, String ip, int port);
    /**
     * 暂停trigger
     *
     * @param triggerKey
     * @return
     */
    public boolean pauseTrigger(TriggerKey triggerKey);

    /**
     * 恢复trigger
     *
     * @param triggerKey
     * @return
     */
    public boolean resumeTrigger(TriggerKey triggerKey);

    /**
     * 删除trigger(从db中删除了)
     *
     * @param triggerKey
     * @return
     */
    public boolean removeTrigger(TriggerKey triggerKey);

    /**
     * 查询某一job类型下的所有trigger
     *
     * @param jobGroup job类型：redis/machine/machineMonitor
     * @return
     */
    public List<TriggerInfo> getTriggersByJobGroup(String jobGroup);

    /**
     * 返回所有的trigger
     *
     * @return
     */
    public List<TriggerInfo> getAllTriggers();

    /**
     * 查询trigger，模糊匹配trigger name或trigger group
     *
     * @param queryString   trigger name或trigger group的关键字
     * @return
     */
    public List<TriggerInfo> searchTriggerByNameOrGroup(String queryString);
}
