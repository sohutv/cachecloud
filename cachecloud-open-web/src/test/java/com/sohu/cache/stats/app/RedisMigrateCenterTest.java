package com.sohu.cache.stats.app;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;

import com.sohu.cache.constant.RedisMigrateEnum;
import com.sohu.cache.constant.RedisMigrateResult;
import com.sohu.cache.stats.app.impl.RedisMigrateCenterImpl;
import com.sohu.test.BaseTest;

/**
 * 测试迁移数据
 * 
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午8:53:19
 */
public class RedisMigrateCenterTest extends BaseTest {

    @Resource(name = "redisMigrateCenter")
    private RedisMigrateCenterImpl redisMigrateCenter;

    @Test
    public void testCheckMigrateMachine() {
        // 1.机器是否在列表里(已测)
        // 2.是否正确安装redis-migrate-tool(已测)
        // 3.是否存在正在执行的redis-migrate-tool(已测)

        String migrateMachineIp = "10.10.53.158";
        RedisMigrateEnum sourceRedisMigrateEnum = RedisMigrateEnum.REDIS_NODE;
        String sourceServers = "10.10.53.159:6379";

        RedisMigrateEnum targetRedisMigrateEnum = RedisMigrateEnum.REDIS_CLUSTER_NODE;
        String targetServers = "10.10.53.159:6380";

        RedisMigrateResult redisMigrateResult = redisMigrateCenter.check(migrateMachineIp, sourceRedisMigrateEnum,
                sourceServers, targetRedisMigrateEnum, targetServers);

        logger.info("===============testCheck start=================");
        logger.info(redisMigrateResult.toString());
        logger.info("===============testCheck end=================");
    }

    @Test
    public void testCheckServers() {
        // 1. 实例列表格式问题(已测)
        // 2.1 rdb文件是否存在(已测)
        // 2.2 redis节点是否存活

        String migrateMachineIp = "10.10.53.159";

        // RedisMigrateEnum sourceRedisMigrateEnum =
        // RedisMigrateEnum.REDIS_NODE;
        // String sourceServers = "10.10.53.159:6388";

        RedisMigrateEnum sourceRedisMigrateEnum = RedisMigrateEnum.RDB_FILE;
        String sourceServers = "/opt/soft/redis/data/dump-6380.rdb";

        RedisMigrateEnum targetRedisMigrateEnum = RedisMigrateEnum.REDIS_CLUSTER_NODE;
        String targetServers = "10.10.53.159:6380";

        RedisMigrateResult redisMigrateResult = redisMigrateCenter.check(migrateMachineIp, sourceRedisMigrateEnum,
                sourceServers, targetRedisMigrateEnum, targetServers);

        logger.info("===============testCheck start=================");
        logger.info(redisMigrateResult.toString());
        logger.info("===============testCheck end=================");
    }

    private String getConfigContent() {
        RedisMigrateEnum sourceRedisMigrateEnum = RedisMigrateEnum.REDIS_NODE;
        String sourceServers = "10.10.53.159:6379";

        RedisMigrateEnum targetRedisMigrateEnum = RedisMigrateEnum.REDIS_CLUSTER_NODE;
        String targetServers = "10.10.53.159:6380";

        String configConent = redisMigrateCenter.generateConfig(sourceRedisMigrateEnum, sourceServers,
                targetRedisMigrateEnum, targetServers);
        return configConent;
    }

    @Test
    public void testConfigFile() {
        String configConent = getConfigContent();
        logger.info("===============testCheck start=================");
        logger.info(configConent);
        logger.info("===============testCheck end=================");
    }

    @Test
    public void testCreateRemoteFile() {
        String fileName = "rmt-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".conf";
        String configConent = getConfigContent();
        String migrateMachineIp = "10.10.53.159";
        redisMigrateCenter.createRemoteFile(migrateMachineIp, fileName, configConent);
    }

    @Test
    public void testMigrateNode() {
        String migrateMachineIp = "10.10.53.159";
        RedisMigrateEnum sourceRedisMigrateEnum = RedisMigrateEnum.REDIS_NODE;
        String sourceServers = "10.10.53.159:6379";
        RedisMigrateEnum targetRedisMigrateEnum = RedisMigrateEnum.REDIS_CLUSTER_NODE;
        String targetServers = "10.10.53.159:6380";

        boolean isMigrate = redisMigrateCenter.migrate(migrateMachineIp, sourceRedisMigrateEnum, sourceServers,
                targetRedisMigrateEnum,
                targetServers);
        logger.warn("============testMigrate start=============");
        logger.warn("isMigrate:{}", isMigrate);
        logger.warn("============testMigrate start=============");
    }
    
    @Test
    public void testMigrateRDB() {
        String migrateMachineIp = "10.10.53.159";
        RedisMigrateEnum sourceRedisMigrateEnum = RedisMigrateEnum.RDB_FILE;
        String sourceServers = "/opt/soft/redis/data/dump-6379.rdb.back";
        RedisMigrateEnum targetRedisMigrateEnum = RedisMigrateEnum.REDIS_NODE;
        String targetServers = "10.10.53.159:6380";

        boolean isMigrate = redisMigrateCenter.migrate(migrateMachineIp, sourceRedisMigrateEnum, sourceServers,
                targetRedisMigrateEnum,
                targetServers);
        logger.warn("============testMigrate start=============");
        logger.warn("isMigrate:{}", isMigrate);
        logger.warn("============testMigrate start=============");
    }

}
