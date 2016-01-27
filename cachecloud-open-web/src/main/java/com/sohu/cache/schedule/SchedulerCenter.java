package com.sohu.cache.schedule;

import com.sohu.cache.entity.TriggerInfo;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.List;
import java.util.Map;

/**
 * 控制job、trigger和scheduler的基类
 * User: lingguo
 * Date: 14-5-18
 * Time: 下午8:18
 */
public interface SchedulerCenter {

    public Trigger getTrigger(TriggerKey triggerKey);

    /**
     * @param triggerKey
     * @return
     */
    public boolean unscheduleJob(TriggerKey triggerKey);

    /**
     * 根据cron部署Job
     *
     * @param jobKey
     * @param triggerKey
     * @param dataMap
     * @param cron
     * @param replace
     * @return
     */
    public boolean deployJobByCron(JobKey jobKey, TriggerKey triggerKey, Map<String, Object> dataMap, String cron, boolean replace);

    /**
     * 根据延迟执行的Job
     *
     * @param jobKey
     * @param triggerKey
     * @param dataMap
     * @param delaySeconds
     * @param replace
     * @return
     */
    public boolean deployJobByDelay(JobKey jobKey, TriggerKey triggerKey, Map<String, Object> dataMap, int delaySeconds, boolean replace);

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
