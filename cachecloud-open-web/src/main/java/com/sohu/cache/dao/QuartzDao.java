package com.sohu.cache.dao;

import com.sohu.cache.entity.TriggerInfo;

import java.util.List;

/**
 * quartz相关的dao操作
 *
 * @author: lingguo
 * @time: 2014/10/13 14:44
 */
public interface QuartzDao {

    public List<TriggerInfo> getTriggersByJobGroup(String jobGroup);

    public List<TriggerInfo> getAllTriggers();

    public List<TriggerInfo> searchTriggerByNameOrGroup(String queryString);
}
