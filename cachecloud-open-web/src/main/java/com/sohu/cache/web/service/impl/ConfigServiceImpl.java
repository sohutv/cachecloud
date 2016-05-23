package com.sohu.cache.web.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.dao.ConfigDao;
import com.sohu.cache.entity.SystemConfig;
import com.sohu.cache.util.ConstUtils;
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

    public void init() {
        reloadSystemConfig();
    }

    /**
     * 加载配置
     */
    public void reloadSystemConfig() {
        logger.warn("===========ConfigServiceImpl reload config start============");
        // 加载配置
        Map<String, String> configMap = getConfigMap();
        // 赋值配置
        ConstUtils.CONTACT = MapUtils.getString(configMap, "cachecloud.contact", ConstUtils.DEFAULT_CONTACT);
        ConstUtils.DOCUMENT_URL = MapUtils.getString(configMap, "cachecloud.documentUrl",
                ConstUtils.DEFAULT_DOCUMENT_URL);
        ConstUtils.EMAILS = MapUtils.getString(configMap, "cachecloud.owner.email", ConstUtils.DEFAULT_EMAILS);
        ConstUtils.PHONES = MapUtils.getString(configMap, "cachecloud.owner.phone", ConstUtils.DEFAULT_PHONES);
        ConstUtils.MAVEN_WAREHOUSE = MapUtils.getString(configMap, "cachecloud.mavenWareHouse",
                ConstUtils.DEFAULT_MAVEN_WAREHOUSE);
        ConstUtils.SUPER_ADMINS = MapUtils.getString(configMap, "cachecloud.superAdmin",
                ConstUtils.DEFAULT_SUPER_ADMINS);
        ConstUtils.SUPER_MANAGER = Arrays.asList(ConstUtils.SUPER_ADMINS.split(","));
        ConstUtils.USERNAME = MapUtils.getString(configMap, "shell.auth.simple.user.name", ConstUtils.DEFAULT_USERNAME);
        ConstUtils.PASSWORD = MapUtils.getString(configMap, "shell.auth.simple.user.password",
                ConstUtils.DEFAULT_PASSWORD);
        ConstUtils.SSH_PORT_DEFAULT = Integer.parseInt(MapUtils.getString(configMap, "cachecloud.machine.ssh.port",
                String.valueOf(ConstUtils.DEFAULT_SSH_PORT_DEFAULT)));
        ConstUtils.SUPER_ADMIN_NAME = MapUtils.getString(configMap, "cachecloud.admin.user.name",
                ConstUtils.DEFAULT_SUPER_ADMIN_NAME);
        ConstUtils.SUPER_ADMIN_PASS = MapUtils.getString(configMap, "cachecloud.admin.user.password",
                ConstUtils.DEFAULT_SUPER_ADMIN_PASS);
        ConstUtils.CPU_USAGE_RATIO_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.cpu.alert.ratio",
                ConstUtils.DEFAULT_CPU_USAGE_RATIO_THRESHOLD);
        ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.mem.alert.ratio",
                ConstUtils.DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD);
        ConstUtils.LOAD_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.load.alert.ratio",
                ConstUtils.DEFAULT_LOAD_THRESHOLD);

        logger.warn("===========ConfigServiceImpl reload config end============");
    }

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

    /**
     * 获取所有配置的key-value
     * 
     * @return
     */
    private Map<String, String> getConfigMap() {
        Map<String, String> configMap = new LinkedHashMap<String, String>();
        List<SystemConfig> systemConfigList = getConfigList(1);
        for (SystemConfig systemConfig : systemConfigList) {
            configMap.put(systemConfig.getConfigKey(), systemConfig.getConfigValue());
        }
        return configMap;
    }

    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

}
