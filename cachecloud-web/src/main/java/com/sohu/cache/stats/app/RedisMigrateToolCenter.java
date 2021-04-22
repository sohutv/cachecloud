package com.sohu.cache.stats.app;

import com.sohu.cache.constant.AppDataMigrateEnum;
import com.sohu.cache.constant.AppDataMigrateResult;
import com.sohu.cache.constant.CommandResult;
import com.sohu.cache.entity.AppDataMigrateStatus;
import com.sohu.cache.entity.SystemResource;

/**
 * 数据迁移
 *
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午2:54:33
 */
public interface RedisMigrateToolCenter {

    /**
     * 检查配置
     *
     * @param migrateMachineIp
     * @param sourceRedisMigrateEnum
     * @param sourceServers
     * @param targetRedisMigrateEnum
     * @param targetServers
     * @param redisSourcePass
     * @param redisTargetPass
     * @return
     */
    AppDataMigrateResult check(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
                               AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, String redisSourcePass, String redisTargetPass,SystemResource resource);

    /**
     * 开始迁移
     *
     * @param migrateMachineIp
     * @param sourceRedisMigrateEnum
     * @param sourceServers
     * @param targetRedisMigrateEnum
     * @param targetServers
     * @param redisSourcePass
     * @param targetSourcePass
     * @return
     */
    AppDataMigrateStatus migrate(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
                                 AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, long sourceAppId, long targetAppId, String redisSourcePass, String targetSourcePass, long userId, SystemResource resource);

    /**
     * 比较源和目标的样本数据
     *
     * @param id
     * @param nums
     * @return
     */
    CommandResult sampleCheckData(long id, int nums);

    /**
     * 关闭迁移
     *
     * @param id
     * @return
     */
    AppDataMigrateResult stopMigrate(long id);

    /**
     * @param appId
     * @return
     */
    String getAppInstanceListForRedisMigrateTool(long appId);

}