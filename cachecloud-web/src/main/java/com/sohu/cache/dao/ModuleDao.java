package com.sohu.cache.dao;

import com.sohu.cache.entity.ModuleInfo;
import com.sohu.cache.entity.ModuleVersion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ModuleDao {

    public List<ModuleInfo> getAllModules();

    public ModuleInfo getModule(@Param("moduleName") String moduleName);

    public ModuleVersion getVersion(@Param("versionId") int versionId);

    public List<ModuleVersion> getAllVersions(@Param("moduleId") int moduleId);

    public void delModule(@Param("moduleId") int moduleId);

    public void saveOrUpdate(ModuleInfo moduleInfo);

    public void saveOrUpdateVersion(ModuleVersion moduleVersion);
}
