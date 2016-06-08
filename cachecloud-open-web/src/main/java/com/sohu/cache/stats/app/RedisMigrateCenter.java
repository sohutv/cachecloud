package com.sohu.cache.stats.app;

import com.sohu.cache.constant.RedisMigrateEnum;
import com.sohu.cache.constant.RedisMigrateResult;

/**
 * 数据迁移
 * 
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午2:54:33
 */
public interface RedisMigrateCenter {
    
    /**
     * 检查配置
     * @param migrateMachineIp
     * @param sourceRedisMigrateEnum
     * @param sourceServers
     * @param targetAppId
     * @return
     */
    RedisMigrateResult check(String migrateMachineIp, RedisMigrateEnum sourceRedisMigrateEnum, String sourceServers, long targetAppId);
    

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
    RedisMigrateResult check(String migrateMachineIp, RedisMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            RedisMigrateEnum targetRedisMigrateEnum, String targetServers);

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
    boolean migrate(String migrateMachineIp, RedisMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            RedisMigrateEnum targetRedisMigrateEnum, String targetServers);
}
