package com.sohu.cache.stats.app;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;

import com.sohu.cache.constant.AppDataMigrateEnum;
import com.sohu.cache.constant.AppDataMigrateResult;
import com.sohu.cache.stats.app.impl.AppDataMigrateCenterImpl;
import com.sohu.cache.util.ConstUtils;
import com.sohu.test.BaseTest;

/**
 * 测试迁移数据
 * 
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午8:53:19
 */
public class AppDataMigrateCenterTest extends BaseTest {

    @Resource(name = "appDataMigrateCenter")
    private AppDataMigrateCenterImpl appDataMigrateCenter;

    @Test
    public void testCheckMigrateMachine() {
        // 1.机器是否在列表里(已测)
        // 2.是否正确安装redis-migrate-tool(已测)
        // 3.是否存在正在执行的redis-migrate-tool(已测)

        String migrateMachineIp = "127.0.0.1";
        AppDataMigrateEnum sourceRedisMigrateEnum = AppDataMigrateEnum.REDIS_NODE;
        String sourceServers = "127.0.0.1:6379";

        AppDataMigrateEnum targetRedisMigrateEnum = AppDataMigrateEnum.REDIS_CLUSTER_NODE;
        String targetServers = "127.0.0.1:6380";

        AppDataMigrateResult redisMigrateResult = appDataMigrateCenter.check(migrateMachineIp, sourceRedisMigrateEnum,
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

        String migrateMachineIp = "127.0.0.1";

        // RedisMigrateEnum sourceRedisMigrateEnum =
        // RedisMigrateEnum.REDIS_NODE;
        // String sourceServers = "127.0.0.1:6388";

        AppDataMigrateEnum sourceRedisMigrateEnum = AppDataMigrateEnum.RDB_FILE;
        String sourceServers = "/opt/soft/redis/data/dump-6380.rdb";

        AppDataMigrateEnum targetRedisMigrateEnum = AppDataMigrateEnum.REDIS_CLUSTER_NODE;
        String targetServers = "127.0.0.1:6380";

        AppDataMigrateResult redisMigrateResult = appDataMigrateCenter.check(migrateMachineIp, sourceRedisMigrateEnum,
                sourceServers, targetRedisMigrateEnum, targetServers);

        logger.info("===============testCheck start=================");
        logger.info(redisMigrateResult.toString());
        logger.info("===============testCheck end=================");
    }

    private String getConfigContent() {
        AppDataMigrateEnum sourceRedisMigrateEnum = AppDataMigrateEnum.REDIS_NODE;
        String sourceServers = "127.0.0.1:6379";

        AppDataMigrateEnum targetRedisMigrateEnum = AppDataMigrateEnum.REDIS_CLUSTER_NODE;
        String targetServers = "127.0.0.1:6380";

        int port = ConstUtils.REDIS_MIGRATE_TOOL_PORT;
        String configConent = appDataMigrateCenter.generateConfig(port, sourceRedisMigrateEnum, sourceServers,
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
        String migrateMachineIp = "127.0.0.1";
        appDataMigrateCenter.createRemoteFile(migrateMachineIp, fileName, configConent);
    }

    @Test
    public void testMigrateNode() {
        String migrateMachineIp = "127.0.0.1";
        AppDataMigrateEnum sourceRedisMigrateEnum = AppDataMigrateEnum.REDIS_NODE;
        String sourceServers = "127.0.0.1:6379";
        AppDataMigrateEnum targetRedisMigrateEnum = AppDataMigrateEnum.REDIS_CLUSTER_NODE;
        String targetServers = "127.0.0.1:6380";

        boolean isMigrate = appDataMigrateCenter.migrate(migrateMachineIp, sourceRedisMigrateEnum, sourceServers,
                targetRedisMigrateEnum,
                targetServers, 10000, 20000, 30000);
        logger.warn("============testMigrate start=============");
        logger.warn("isMigrate:{}", isMigrate);
        logger.warn("============testMigrate start=============");
    }
    
    @Test
    public void testMigrateRDB() {
        String migrateMachineIp = "127.0.0.1";
        AppDataMigrateEnum sourceRedisMigrateEnum = AppDataMigrateEnum.RDB_FILE;
        String sourceServers = "/opt/soft/redis/data/dump-6379.rdb.back";
        AppDataMigrateEnum targetRedisMigrateEnum = AppDataMigrateEnum.REDIS_NODE;
        String targetServers = "127.0.0.1:6380";

        boolean isMigrate = appDataMigrateCenter.migrate(migrateMachineIp, sourceRedisMigrateEnum, sourceServers,
                targetRedisMigrateEnum,
                targetServers, 10000, 20000, 30000);
        logger.warn("============testMigrate start=============");
        logger.warn("isMigrate:{}", isMigrate);
        logger.warn("============testMigrate start=============");
    }

}
