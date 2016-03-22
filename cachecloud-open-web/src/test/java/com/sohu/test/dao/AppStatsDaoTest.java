package com.sohu.test.dao;

import com.sohu.cache.dao.AppStatsDao;
import com.sohu.cache.entity.AppCommandStats;
import com.sohu.cache.entity.AppStats;
import com.sohu.cache.entity.TimeDimensionality;
import com.sohu.test.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by yijunzhang on 14-6-9.
 */
public class AppStatsDaoTest extends BaseTest {

    @Resource
    private AppStatsDao appStatsDao;

    private long appId = 10011L;

    private long collectTime = 201412011530L;

    @Test
    public void testMergeAppStats() {
        assertNotNull(appStatsDao);
        AppStats appStats = new AppStats();
        appStats.setAppId(appId);
        appStats.setCollectTime(collectTime);
        appStats.setConnectedClients(1);
        appStats.setCreateTime(new Date());
        appStats.setHits(10);
        appStats.setMisses(10);
        appStats.setUsedMemory(1024);
        appStats.setEvictedKeys(10);
        appStats.setExpiredKeys(10);
        appStats.setNetInputByte(0);
        appStats.setNetOutputByte(0);
        appStats.setModifyTime(new Date());
        appStatsDao.mergeMinuteAppStats(appStats);
        appStatsDao.mergeHourAppStats(appStats);
    }

    @Test
    public void testMergeCommandStatus() {
        assertNotNull(appStatsDao);
        AppCommandStats commandStats = new AppCommandStats();
        commandStats.setAppId(appId);
        commandStats.setCollectTime(collectTime);
        commandStats.setCommandName("get");
        commandStats.setCommandCount(10);
        commandStats.setModifyTime(new Date());
        appStatsDao.mergeMinuteCommandStatus(commandStats);
        appStatsDao.mergeHourCommandStatus(commandStats);
    }

    @Test
    public void testMergeHourCommandStatus() {
        String time = "2014090216";
        for (int i = 10; i <= 19; i++) {
            AppCommandStats commandStats = new AppCommandStats();
            long collectTime = Long.parseLong(time + i);
            commandStats.setAppId(appId);
            commandStats.setCollectTime(collectTime);
            commandStats.setCommandName("get");
            commandStats.setCommandCount(i + 100);
            commandStats.setModifyTime(new Date());
            appStatsDao.mergeHourCommandStatus(commandStats);
        }
    }

    @Test
    public void testMergeHourAppStats() {
        assertNotNull(appStatsDao);
        String time = "2014090216";
        for (int i = 10; i <= 19; i++) {
            long collectTime = Long.parseLong(time + i);
            AppStats appStats = new AppStats();
            appStats.setAppId(appId);
            appStats.setCollectTime(collectTime);
            appStats.setConnectedClients(1);
            appStats.setCreateTime(new Date());
            appStats.setHits(i * 20);
            appStats.setMisses(i * 10);
            appStats.setUsedMemory(i);
            appStats.setEvictedKeys(i);
            appStats.setExpiredKeys(i);
            appStats.setModifyTime(new Date());
            appStatsDao.mergeHourAppStats(appStats);
        }
    }

    @Test
    public void getAppStatsListByDate() {
        List<AppStats> list = appStatsDao.getAppStatsList(appId, new TimeDimensionality(collectTime, collectTime + 20, "yyyyMMddHHmm"));
        logger.info(list.toString());
    }

    @Test
    public void getAppCommandStatsListByDate() {
        List<AppCommandStats> list = appStatsDao.getAppCommandStatsList(appId, "get", new TimeDimensionality(collectTime, collectTime + 20, "yyyyMMddHHmm"));
        logger.info("list->" + list.toString());
        logger.info("size->" + list.size());
    }

    @Test
    public void getAppAllCommandStatsList() {
        List<AppCommandStats> list = appStatsDao.getAppAllCommandStatsList(appId, new TimeDimensionality(collectTime, collectTime + 1800, "yyyyMMddHHmm"));
        logger.info("list->" + list.toString());
        logger.info("size->" + list.size());
    }

}
