package com.sohu.test.stats.app;

import com.sohu.cache.dao.AppStatsDao;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.constant.AppTopology;
import com.sohu.cache.entity.AppCommandStats;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppStats;
import com.sohu.cache.util.ScheduleUtil;
import com.sohu.test.BaseTest;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by lingguo on 14-6-26.
 */
public class AppCenterTest extends BaseTest {

    @Resource
    private AppStatsCenter appStatsCenter;

    @Test
    public void testGetAppStatsListByMinuteTime() {
        Assert.assertNotNull(appStatsCenter);

        long appId = 999;
        long endTime = ScheduleUtil.getCollectTime(new Date());
        long beginTime = ScheduleUtil.getLastCollectTime(endTime);

        List<AppStats> appStatsList = appStatsCenter.getAppStatsListByMinuteTime(appId, beginTime, endTime);
        for (AppStats appStats: appStatsList) {
            logger.info("appStats: {}", appStats);
        }
    }

    @Test
    public void testGetAppCommandStatsListByMinuteTime() {
        Assert.assertNotNull(appStatsCenter);

        long appId = 999;
        String beginStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        long begin = Integer.parseInt(beginStr + "0000");
        long end = Integer.parseInt(beginStr + "2359");
        List<AppCommandStats> topAppCommandStatsListByDate = appStatsCenter.getTop5AppCommandStatsList(appId, begin, end);
        for (AppCommandStats appCommandStats : topAppCommandStatsListByDate) {
            logger.info("appCommandStats: {}", appCommandStats.toString());
        }
    }

    @Test
    public void testQueryAppTopology() {
        Assert.assertNotNull(appStatsCenter);

        long appId = 999;
        Map<AppTopology, Object> appTopologyMap = appStatsCenter.queryAppTopology(appId);
        for (AppTopology appTopology: appTopologyMap.keySet()) {
            logger.info("key: {}, value: {}", appTopology.getValue(), appTopologyMap.get(appTopology));
        }
    }
}
