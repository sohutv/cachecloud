package com.sohu.cache.web.service;

import com.sohu.cache.entity.ModuleInfo;
import com.sohu.cache.entity.ModuleVersion;
import com.sohu.cache.web.enums.SuccessEnum;

import java.util.List;

/**
 * Created by chenshi on 2021/4/28.
 */
public interface ModuleService {

    public List<ModuleInfo> getAllModules();

    public List<ModuleInfo> getAllModuleVersions();

    public ModuleInfo getModuleVersions(String moduleName);

    public ModuleVersion getModuleVersionById(int versionId);

    public SuccessEnum deleteModule(int moduleId);

    public SuccessEnum saveOrUpdateModule(ModuleInfo moduleInfo);

    public SuccessEnum saveOrUpdateVersion(ModuleVersion moduleVersion);

}
