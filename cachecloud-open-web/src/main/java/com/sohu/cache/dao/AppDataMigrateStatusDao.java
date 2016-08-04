package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppDataMigrateSearch;
import com.sohu.cache.entity.AppDataMigrateStatus;

/**
 * 迁移状态Dao
 * 
 * @author leifu
 * @Date 2016-6-9
 * @Time 下午5:25:53
 */
public interface AppDataMigrateStatusDao {

    int save(AppDataMigrateStatus appDataMigrateStatus);

    int getMigrateMachineStatCount(@Param("migrateMachineIp") String migrateMachineIp, @Param("status") int status);

    AppDataMigrateStatus get(@Param("id") long id);

    int updateStatus(@Param("id") long id, @Param("status") int status);

    List<AppDataMigrateStatus> search(@Param("appDataMigrateSearch") AppDataMigrateSearch appDataMigrateSearch);

}
