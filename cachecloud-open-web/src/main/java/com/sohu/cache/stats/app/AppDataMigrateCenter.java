package com.sohu.cache.stats.app;

import com.sohu.cache.constant.AppDataMigrateEnum;
import com.sohu.cache.constant.AppDataMigrateResult;

/**
 * 数据迁移
 * 
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午2:54:33
 */
public interface AppDataMigrateCenter {
    
    /**
     * 检查配置
     * @param migrateMachineIp
     * @param sourceRedisMigrateEnum
     * @param sourceServers
     * @param targetAppId
     * @return
     */
    AppDataMigrateResult check(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers, long targetAppId);
    

    /**
     * 检查配置
     * 
     * @param migrateMachineIp
     * @param sourceRedisMigrateEnum
     * @param sourceServers
     * @param targetRedisMigrateEnum
     * @param targetServers
     * @return
     */
    AppDataMigrateResult check(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            AppDataMigrateEnum targetRedisMigrateEnum, String targetServers);

    /**
     * 开始迁移
     * 
     * @param migrateMachineIp
     * @param sourceRedisMigrateEnum
     * @param sourceServers
     * @param targetRedisMigrateEnum
     * @param targetServers
     * @return
     */
    boolean migrate(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            AppDataMigrateEnum targetRedisMigrateEnum, String targetServers);
    
}
