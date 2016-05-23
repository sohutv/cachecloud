package com.sohu.cache.web.service.impl;

import java.util.Arrays;
import java.util.Collections;
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

        // 文案相关
        ConstUtils.CONTACT = MapUtils.getString(configMap, "cachecloud.contact", ConstUtils.DEFAULT_CONTACT);
        logger.warn("{}: {}", "ConstUtils.CONTACT", ConstUtils.CONTACT);
        
        ConstUtils.DOCUMENT_URL = MapUtils.getString(configMap, "cachecloud.documentUrl",
                ConstUtils.DEFAULT_DOCUMENT_URL);
        logger.warn("{}: {}", "ConstUtils.DOCUMENT_URL", ConstUtils.DOCUMENT_URL);

        
        ConstUtils.MAVEN_WAREHOUSE = MapUtils.getString(configMap, "cachecloud.mavenWareHouse",
                ConstUtils.DEFAULT_MAVEN_WAREHOUSE);
        logger.warn("{}: {}", "ConstUtils.MAVEN_WAREHOUSE", ConstUtils.MAVEN_WAREHOUSE);


        // 报警相关配置
        ConstUtils.EMAILS = MapUtils.getString(configMap, "cachecloud.owner.email", ConstUtils.DEFAULT_EMAILS);
        logger.warn("{}: {}", "ConstUtils.EMAILS", ConstUtils.EMAILS);

        ConstUtils.PHONES = MapUtils.getString(configMap, "cachecloud.owner.phone", ConstUtils.DEFAULT_PHONES);
        logger.warn("{}: {}", "ConstUtils.PHONES", ConstUtils.PHONES);


        // ssh相关配置
        ConstUtils.USERNAME = MapUtils.getString(configMap, "cachecloud.machine.ssh.name", ConstUtils.DEFAULT_USERNAME);
        logger.warn("{}: {}", "ConstUtils.USERNAME", ConstUtils.USERNAME);

        
        ConstUtils.PASSWORD = MapUtils.getString(configMap, "cachecloud.machine.ssh.password",
                ConstUtils.DEFAULT_PASSWORD);
        logger.warn("{}: {}", "ConstUtils.PASSWORD", ConstUtils.PASSWORD);

        
        ConstUtils.SSH_PORT_DEFAULT = Integer.parseInt(MapUtils.getString(configMap, "cachecloud.machine.ssh.port",
                String.valueOf(ConstUtils.DEFAULT_SSH_PORT_DEFAULT)));
        logger.warn("{}: {}", "ConstUtils.SSH_PORT_DEFAULT", ConstUtils.SSH_PORT_DEFAULT);


        // 管理员相关配置
        ConstUtils.SUPER_ADMIN_NAME = MapUtils.getString(configMap, "cachecloud.admin.user.name",
                ConstUtils.DEFAULT_SUPER_ADMIN_NAME);
        logger.warn("{}: {}", "ConstUtils.SUPER_ADMIN_NAME", ConstUtils.SUPER_ADMIN_NAME);

        
        ConstUtils.SUPER_ADMIN_PASS = MapUtils.getString(configMap, "cachecloud.admin.user.password",
                ConstUtils.DEFAULT_SUPER_ADMIN_PASS);
        logger.warn("{}: {}", "ConstUtils.SUPER_ADMIN_PASS", ConstUtils.SUPER_ADMIN_PASS);

        
        ConstUtils.SUPER_ADMINS = MapUtils.getString(configMap, "cachecloud.superAdmin",
                ConstUtils.DEFAULT_SUPER_ADMINS);
        logger.warn("{}: {}", "ConstUtils.SUPER_ADMINS", ConstUtils.SUPER_ADMINS);

        
        ConstUtils.SUPER_MANAGER = Arrays.asList(ConstUtils.SUPER_ADMINS.split(","));
        logger.warn("{}: {}", "ConstUtils.SUPER_MANAGER", ConstUtils.SUPER_MANAGER);


        // 机器报警阀值
        ConstUtils.CPU_USAGE_RATIO_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.cpu.alert.ratio",
                ConstUtils.DEFAULT_CPU_USAGE_RATIO_THRESHOLD);
        logger.warn("{}: {}", "ConstUtils.CPU_USAGE_RATIO_THRESHOLD", ConstUtils.CPU_USAGE_RATIO_THRESHOLD);

        ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.mem.alert.ratio",
                ConstUtils.DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD);
        logger.warn("{}: {}", "ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD", ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD);
        
        ConstUtils.LOAD_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.load.alert.ratio",
                ConstUtils.DEFAULT_LOAD_THRESHOLD);
        logger.warn("{}: {}", "ConstUtils.LOAD_THRESHOLD", ConstUtils.LOAD_THRESHOLD);

        

        // 客户端版本
        ConstUtils.GOOD_CLIENT_VERSIONS = MapUtils.getString(configMap, "cachecloud.good.client",
                ConstUtils.DEFAULT_GOOD_CLIENT_VERSIONS);
        logger.warn("{}: {}", "ConstUtils.GOOD_CLIENT_VERSIONS", ConstUtils.GOOD_CLIENT_VERSIONS);
        
        ConstUtils.WARN_CLIENT_VERSIONS = MapUtils.getString(configMap, "cachecloud.warn.client",
                ConstUtils.DEFAULT_WARN_CLIENT_VERSIONS);
        logger.warn("{}: {}", "ConstUtils.WARN_CLIENT_VERSIONS", ConstUtils.WARN_CLIENT_VERSIONS);

        ConstUtils.ERROR_CLIENT_VERSIONS = MapUtils.getString(configMap, "cachecloud.error.client",
                ConstUtils.DEFAULT_ERROR_CLIENT_VERSIONS);
        logger.warn("{}: {}", "ConstUtils.ERROR_CLIENT_VERSIONS", ConstUtils.ERROR_CLIENT_VERSIONS);


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
