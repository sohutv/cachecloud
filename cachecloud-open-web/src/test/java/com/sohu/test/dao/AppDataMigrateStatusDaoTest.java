package com.sohu.test.dao;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.sohu.cache.dao.AppDataMigrateStatusDao;
import com.sohu.cache.entity.AppDataMigrateStatus;
import com.sohu.test.BaseTest;

/**
 * 迁移状态dao测试
 * 
 * @author leifu
 * @Date 2016-6-9
 * @Time 下午5:38:51
 */
public class AppDataMigrateStatusDaoTest extends BaseTest {

    @Resource
    private AppDataMigrateStatusDao appDataMigrateStatusDao;

    @Test
    public void testSave() {
        AppDataMigrateStatus appDataMigrateStatus = new AppDataMigrateStatus();
        appDataMigrateStatus.setMigrateEndTime(new Date());
        appDataMigrateStatus.setMigrateMachineIp("10.10.53.159");
        appDataMigrateStatus.setMigrateStartTime(new Date());
        appDataMigrateStatus.setMigrateStatus(1);
        appDataMigrateStatus.setMigrateUserId(10244);
        appDataMigrateStatus.setSourceAppId(10023);
        appDataMigrateStatus.setSourceMigrateType(1);
        appDataMigrateStatus.setSourceServers("10.10.53.159:6379");
        appDataMigrateStatus.setTargetAppId(0);
        appDataMigrateStatus.setTargetMigrateType(2);
        appDataMigrateStatus.setTargetServers("10.10.52.136:6385");
        appDataMigrateStatusDao.save(appDataMigrateStatus);
    }

    @Test
    public void testSearch() {
        List<AppDataMigrateStatus> list = appDataMigrateStatusDao.search(null);
        System.out.println("list.size: " + list.size());
        logger.info("==============testSearch start==============");
        for (AppDataMigrateStatus appDataMigrateStatus : list) {
            logger.info(appDataMigrateStatus.toString());
        }
        logger.info("==============testSearch end==============");
    }

}
