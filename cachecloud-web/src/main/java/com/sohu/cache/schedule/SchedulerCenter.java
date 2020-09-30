package com.sohu.cache.schedule;

import com.sohu.cache.entity.TriggerInfo;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.List;

/**
 * 控制job、trigger和scheduler的基类
 * User: lingguo
 */
public interface SchedulerCenter {

    public Trigger getTrigger(TriggerKey triggerKey);

    /**
     * @param triggerKey
     * @return
     */
    public boolean unscheduleJob(TriggerKey triggerKey);

    /**
     * 获取所有trigger
     *
     * @return
     */
    public List<TriggerInfo> getAllTriggers();

    /**
     * 模糊查询trigger
     *
     * @return
     */
    public List<TriggerInfo> getTriggersByNameOrGroup(String query);

    /**
     * 暂定trigger
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

}
