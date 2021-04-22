package com.sohu.cache.dao;


import com.sohu.cache.entity.AppImport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: rucao
 * @Date: 2021/1/7 下午6:03
 */
public interface AppImportDao {
    AppImport get(@Param("id") Long id);

    int save(AppImport appImport);

    int update(AppImport appImport);

    List<AppImport> getAppImports(@Param("status") int status);
}
