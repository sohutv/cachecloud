package com.sohu.cache.web.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.dao.ConfigDao;
import com.sohu.cache.entity.SystemConfig;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.ConfigService;

/**
 * @author leifu
 * @Date 2016年5月23日
 * @Time 上午10:35:26
 */
public class ConfigServiceImpl implements ConfigService {

    private Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);

    private ConfigDao configDao;

    @Override
    public SuccessEnum updateConfig(Map<String, String> configMap) {
        for (Entry<String, String> entry : configMap.entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue();
            try {
                configDao.update(configKey, configValue);
            } catch (Exception e) {
                logger.error("key {} value {} update faily" + e.getMessage(), configKey, configValue, e);
                return SuccessEnum.FAIL;
            }
        }
        return SuccessEnum.SUCCESS;
    }

    @Override
    public List<SystemConfig> getConfigList(int status) {
        try {
            return configDao.getConfigList(status);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

}
