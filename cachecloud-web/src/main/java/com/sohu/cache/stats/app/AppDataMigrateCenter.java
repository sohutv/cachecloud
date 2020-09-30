package com.sohu.cache.stats.app;

import com.sohu.cache.constant.RedisMigrateToolConstant;
import com.sohu.cache.entity.AppDataMigrateSearch;
import com.sohu.cache.entity.AppDataMigrateStatus;

import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/10/24
 */
public interface AppDataMigrateCenter {
    /**
     * 查看日志
     *
     * @param id
     * @param pageSize
     * @return
     */
    String showDataMigrateLog(long id, int pageSize);

    /**
     * 查看校验日志
     *
     * @param id
     * @param pageSize
     * @return
     */
    String showCheckDataLog(long id, int pageSize);

    /**
     * 查看配置
     *
     * @param id
     * @return
     */
    String showDataMigrateConf(long id);

    /**
     * 查询迁移工具的实时状态
     *
     * @param id
     * @return
     */
    Map<RedisMigrateToolConstant, Map<String, Object>> showMiragteToolProcess(long id);

    /**
     * 搜索列表
     *
     * @param appDataMigrateSearch
     * @return
     */
    List<AppDataMigrateStatus> search(AppDataMigrateSearch appDataMigrateSearch);

    int getMigrateTaskCount(AppDataMigrateSearch appDataMigrateSearch);
}
