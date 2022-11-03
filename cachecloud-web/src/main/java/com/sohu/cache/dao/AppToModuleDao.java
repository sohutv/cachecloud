package com.sohu.cache.dao;

import com.sohu.cache.entity.AppToModule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zengyizhao
 * @CreateTime: 2022/9/1 16:09
 * @Description: 应用模块对应表
 * @Version: 1.0
 */
public interface AppToModuleDao {
    
    List<AppToModule> getByAppId(@Param("appId") Long appId);

    Long save(AppToModule appToModule);

    Long saveAll(List<AppToModule> appToModuleList);

    void deleteByAppIdAndModuleVersionId(@Param("appId") Long appId, @Param("moduleVersonId") Long moduleVersionId);

}
