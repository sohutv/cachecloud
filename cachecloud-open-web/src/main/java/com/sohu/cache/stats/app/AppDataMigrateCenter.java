package com.sohu.cache.stats.app;

import java.util.List;
import java.util.Map;

import com.sohu.cache.constant.AppDataMigrateEnum;
import com.sohu.cache.constant.AppDataMigrateResult;
import com.sohu.cache.constant.CommandResult;
import com.sohu.cache.constant.RedisMigrateToolConstant;
import com.sohu.cache.entity.AppDataMigrateSearch;
import com.sohu.cache.entity.AppDataMigrateStatus;

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
            AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, String redisSourcePass, String redisTargetPass);

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
    boolean migrate(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, long sourceAppId, long targetAppId, String redisSourcePass, String targetSourcePass, long userId);


    /**
     * 搜索列表
     * @param appDataMigrateSearch
     * @return
     */
	List<AppDataMigrateStatus> search(AppDataMigrateSearch appDataMigrateSearch);

    /**
     * 查看日志
     * @param id
     * @param pageSize
     * @return
     */
    String showDataMigrateLog(long id, int pageSize);
    
    /**
     * 查看配置
     * @param id
     * @return
     */
    String showDataMigrateConf(long id);

    /**
     * 查询迁移工具的实时状态
     * @param id
     * @return
     */
    Map<RedisMigrateToolConstant, Map<String, Object>> showMiragteToolProcess(long id);


    /**
     * 比较源和目标的样本数据
     * @param id
     * @param nums
     * @return
     */
    CommandResult sampleCheckData(long id, int nums);


    /**
     * 关闭迁移
     * @param id
     * @return
     */
    AppDataMigrateResult stopMigrate(long id);

    
}
