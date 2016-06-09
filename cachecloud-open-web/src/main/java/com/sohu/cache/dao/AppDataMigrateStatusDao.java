package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppDataMigrateStatus;

/**
 * 迁移状态Dao
 * @author leifu
 * @Date 2016-6-9
 * @Time 下午5:25:53
 */
public interface AppDataMigrateStatusDao {

    int save(AppDataMigrateStatus appDataMigrateStatus);
    
    List<AppDataMigrateStatus> search(@Param("migrateStatus") Integer migrateStatus);
}
