package com.sohu.cache.stats.app;

import com.sohu.cache.constant.AppDataMigrateEnum;
import com.sohu.cache.constant.AppDataMigrateResult;
import com.sohu.cache.constant.CommandResult;
import com.sohu.cache.entity.AppDataMigrateStatus;
import com.sohu.cache.entity.SystemResource;

/**
 * 数据迁移工具 redis-shake
 * Created by rucao on 2019/10/23
 */
public interface RedisShakeCenter {
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
     * @param sourceAppId
     * @param targetAppId
     * @param redisSourcePass
     * @param redisTargetPass
     * @param userId
     * @return
     */
    AppDataMigrateStatus migrate(String migrateMachineIp, int source_rdb_parallel, int parallel,
                                 AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
                                 AppDataMigrateEnum targetRedisMigrateEnum, String targetServers,
                                 long sourceAppId, long targetAppId,
                                 String redisSourcePass, String redisTargetPass,
                                 String redisSourceVersion, String redisTargetVersion,
                                 long userId, SystemResource resource);

    /**
     * 关闭迁移
     *
     * @param id
     * @return
     */
    AppDataMigrateResult stopMigrate(long id);

    /**
     * 比较源和目标的样本数据
     *
     * @param id
     * @param batchcount
     * @return
     */
    CommandResult checkData(long id, int batchcount, int comparemode);

    /**
     * @param appId
     * @return
     */
    String getAppInstanceListForRedisShake(long appId);

    /**
     * 显示迁移进度
     *
     * @param id
     * @return
     */
    String showProcess(long id);

}
