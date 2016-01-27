package com.sohu.test.dao;

import com.sohu.cache.dao.QuartzDao;
import com.sohu.cache.entity.TriggerInfo;
import com.sohu.cache.util.ConstUtils;
import com.sohu.test.BaseTest;

import org.junit.Test;
import org.quartz.Trigger;

import java.util.List;

import javax.annotation.Resource;

/**
 * @author: lingguo
 * @time: 2014/10/13 16:02
 */
public class QuartzDaoTest extends BaseTest {

    @Resource
    private QuartzDao quartzDao;

    @Test
    public void testGetTriggersByJobGroup() {
        List<TriggerInfo> triggers = quartzDao.getTriggersByJobGroup(ConstUtils.REDIS_JOB_GROUP);
        for (TriggerInfo info: triggers) {
            logger.info("info: {}", info);
        }
    }

    @Test
    public void testGetAllTriggers() {
        List<TriggerInfo> triggers = quartzDao.getAllTriggers();
        for (TriggerInfo info: triggers) {
            logger.info("{}", info);
        }
    }

    @Test
    public void testSearchTriggerByNameOrGroup() {
        String queryString = "10078";
        List<TriggerInfo> triggers = quartzDao.searchTriggerByNameOrGroup(queryString);
        for (TriggerInfo info: triggers) {
            logger.info("info: {}, {}", info, queryString);
        }
    }



}
