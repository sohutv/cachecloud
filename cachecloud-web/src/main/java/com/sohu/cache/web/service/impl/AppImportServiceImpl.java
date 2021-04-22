package com.sohu.cache.web.service.impl;

import com.sohu.cache.dao.AppImportDao;
import com.sohu.cache.entity.AppImport;
import com.sohu.cache.web.service.AppImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: rucao
 * @Date: 2021/1/8 上午11:43
 */
@Slf4j
@Service
public class AppImportServiceImpl implements AppImportService {
    @Autowired
    private AppImportDao appImportDao;

    @Override
    public List<AppImport> getImportAppList(Integer status) {
        List<AppImport> appImportList = new ArrayList<>();
        try {
            appImportList = appImportDao.getAppImports(status);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return appImportList;
    }

    @Override
    public int save(AppImport appImport) {
        try {
            return appImportDao.save(appImport);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public int update(AppImport appImport) {
        try {
            return appImportDao.update(appImport);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public AppImport appImport(Long id) {
        AppImport appImport = get(id);
        if (appImport == null) {

        }

        return null;
    }

    @Override
    public AppImport get(Long id) {
        try {
            return appImportDao.get(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
