package com.sohu.cache.web.service.impl;

import com.sohu.cache.dao.ModuleDao;
import com.sohu.cache.entity.ModuleInfo;
import com.sohu.cache.entity.ModuleVersion;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.ModuleService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by chenshi on 2021/4/28.
 */
@Service
public class ModuleServiceImpl implements ModuleService {

    private Logger logger = LoggerFactory.getLogger(ModuleServiceImpl.class);

    @Autowired
    ModuleDao moduleDao;

    @Override
    public List<ModuleInfo> getAllModules() {

        return moduleDao.getAllModules();
    }

    public List<ModuleInfo> getAllModuleVersions(){

        List<ModuleInfo> allModules = moduleDao.getAllModules();
        if(!CollectionUtils.isEmpty(allModules)){
            for(ModuleInfo moduleInfo : allModules){
                List<ModuleVersion> allVersions = moduleDao.getAllVersions(moduleInfo.getId());
                if(!CollectionUtils.isEmpty(allVersions)){
                    moduleInfo.setVersions(allVersions);
                }
            }
        }
        return allModules;
    }

    public ModuleInfo getModuleVersions(String moduleName){

        ModuleInfo moduleInfo = moduleDao.getModule(moduleName);
        if(null != moduleInfo){
            List<ModuleVersion> allVersions = moduleDao.getAllVersions(moduleInfo.getId());
            if(!CollectionUtils.isEmpty(allVersions)){
                moduleInfo.setVersions(allVersions);
            }
        }
        return moduleInfo;
    }

    public ModuleVersion getModuleVersionById(int versionId){

        return moduleDao.getVersion(versionId);
    }

    public SuccessEnum deleteModule(int moduleId){

        try {
            moduleDao.delModule(moduleId);
        } catch (Exception e) {
            logger.error("key {} value {} update faily" + e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
        return SuccessEnum.SUCCESS;
    }

    @Override
    public SuccessEnum saveOrUpdateModule(ModuleInfo moduleInfo) {
        try {
            moduleDao.saveOrUpdate(moduleInfo);
        } catch (Exception e) {
            logger.error("key {} value {} update faily" + e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
        return SuccessEnum.SUCCESS;
    }

    public SuccessEnum saveOrUpdateVersion(ModuleVersion moduleVersion){
        try {
            moduleDao.saveOrUpdateVersion(moduleVersion);
        } catch (Exception e) {
            logger.error("key {} value {} update faily" + e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
        return SuccessEnum.SUCCESS;
    }

}
