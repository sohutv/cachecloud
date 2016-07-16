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

import com.sohu.cache.constant.UserLoginTypeEnum;
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
        logger.info("===========ConfigServiceImpl reload config start============");
        // 加载配置
        Map<String, String> configMap = getConfigMap();

        // 文案相关
        ConstUtils.CONTACT = MapUtils.getString(configMap, "cachecloud.contact", ConstUtils.DEFAULT_CONTACT);
        logger.info("{}: {}", "ConstUtils.CONTACT", ConstUtils.CONTACT);
        
        ConstUtils.DOCUMENT_URL = MapUtils.getString(configMap, "cachecloud.documentUrl",
                ConstUtils.DEFAULT_DOCUMENT_URL);
        logger.info("{}: {}", "ConstUtils.DOCUMENT_URL", ConstUtils.DOCUMENT_URL);

        
        ConstUtils.MAVEN_WAREHOUSE = MapUtils.getString(configMap, "cachecloud.mavenWareHouse",
                ConstUtils.DEFAULT_MAVEN_WAREHOUSE);
        logger.info("{}: {}", "ConstUtils.MAVEN_WAREHOUSE", ConstUtils.MAVEN_WAREHOUSE);


        // 报警相关配置
        ConstUtils.EMAILS = MapUtils.getString(configMap, "cachecloud.owner.email", ConstUtils.DEFAULT_EMAILS);
        logger.info("{}: {}", "ConstUtils.EMAILS", ConstUtils.EMAILS);

        ConstUtils.PHONES = MapUtils.getString(configMap, "cachecloud.owner.phone", ConstUtils.DEFAULT_PHONES);
        logger.info("{}: {}", "ConstUtils.PHONES", ConstUtils.PHONES);


        // ssh相关配置
        ConstUtils.USERNAME = MapUtils.getString(configMap, "cachecloud.machine.ssh.name", ConstUtils.DEFAULT_USERNAME);
        logger.info("{}: {}", "ConstUtils.USERNAME", ConstUtils.USERNAME);

        
        ConstUtils.PASSWORD = MapUtils.getString(configMap, "cachecloud.machine.ssh.password",
                ConstUtils.DEFAULT_PASSWORD);
        logger.info("{}: {}", "ConstUtils.PASSWORD", ConstUtils.PASSWORD);

        
        ConstUtils.SSH_PORT_DEFAULT = Integer.parseInt(MapUtils.getString(configMap, "cachecloud.machine.ssh.port",
                String.valueOf(ConstUtils.DEFAULT_SSH_PORT_DEFAULT)));
        logger.info("{}: {}", "ConstUtils.SSH_PORT_DEFAULT", ConstUtils.SSH_PORT_DEFAULT);


        // 管理员相关配置
        ConstUtils.SUPER_ADMIN_NAME = MapUtils.getString(configMap, "cachecloud.admin.user.name",
                ConstUtils.DEFAULT_SUPER_ADMIN_NAME);
        logger.info("{}: {}", "ConstUtils.SUPER_ADMIN_NAME", ConstUtils.SUPER_ADMIN_NAME);

        
        ConstUtils.SUPER_ADMIN_PASS = MapUtils.getString(configMap, "cachecloud.admin.user.password",
                ConstUtils.DEFAULT_SUPER_ADMIN_PASS);
        logger.info("{}: {}", "ConstUtils.SUPER_ADMIN_PASS", ConstUtils.SUPER_ADMIN_PASS);

        
        ConstUtils.SUPER_ADMINS = MapUtils.getString(configMap, "cachecloud.superAdmin",
                ConstUtils.DEFAULT_SUPER_ADMINS);
        logger.info("{}: {}", "ConstUtils.SUPER_ADMINS", ConstUtils.SUPER_ADMINS);

        
        ConstUtils.SUPER_MANAGER = Arrays.asList(ConstUtils.SUPER_ADMINS.split(","));
        logger.info("{}: {}", "ConstUtils.SUPER_MANAGER", ConstUtils.SUPER_MANAGER);


        // 机器报警阀值
        ConstUtils.CPU_USAGE_RATIO_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.cpu.alert.ratio",
                ConstUtils.DEFAULT_CPU_USAGE_RATIO_THRESHOLD);
        logger.info("{}: {}", "ConstUtils.CPU_USAGE_RATIO_THRESHOLD", ConstUtils.CPU_USAGE_RATIO_THRESHOLD);

        ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.mem.alert.ratio",
                ConstUtils.DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD);
        logger.info("{}: {}", "ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD", ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD);
        
        ConstUtils.LOAD_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.load.alert.ratio",
                ConstUtils.DEFAULT_LOAD_THRESHOLD);
        logger.info("{}: {}", "ConstUtils.LOAD_THRESHOLD", ConstUtils.LOAD_THRESHOLD);

        

        // 客户端版本
        ConstUtils.GOOD_CLIENT_VERSIONS = MapUtils.getString(configMap, "cachecloud.good.client",
                ConstUtils.DEFAULT_GOOD_CLIENT_VERSIONS);
        logger.info("{}: {}", "ConstUtils.GOOD_CLIENT_VERSIONS", ConstUtils.GOOD_CLIENT_VERSIONS);
        
        ConstUtils.WARN_CLIENT_VERSIONS = MapUtils.getString(configMap, "cachecloud.warn.client",
                ConstUtils.DEFAULT_WARN_CLIENT_VERSIONS);
        logger.info("{}: {}", "ConstUtils.WARN_CLIENT_VERSIONS", ConstUtils.WARN_CLIENT_VERSIONS);

        ConstUtils.ERROR_CLIENT_VERSIONS = MapUtils.getString(configMap, "cachecloud.error.client",
                ConstUtils.DEFAULT_ERROR_CLIENT_VERSIONS);
        logger.info("{}: {}", "ConstUtils.ERROR_CLIENT_VERSIONS", ConstUtils.ERROR_CLIENT_VERSIONS);

        //redis-migrate-tool安装路径
        ConstUtils.REDIS_MIGRATE_TOOL_HOME = MapUtils.getString(configMap, "redis.migrate.tool.home",
                ConstUtils.DEFAULT_REDIS_MIGRATE_TOOL_HOME);
        logger.info("{}: {}", "ConstUtils.REDIS_MIGRATE_TOOL_HOME", ConstUtils.REDIS_MIGRATE_TOOL_HOME);
        
        //用户登录状态方式
        ConstUtils.USER_LOGIN_TYPE = MapUtils.getIntValue(configMap, "cachecloud.user.login.type", ConstUtils.DEFAULT_USER_LOGIN_TYPE);
        UserLoginTypeEnum userLoginTypeEnum = UserLoginTypeEnum.getLoginTypeEnum(ConstUtils.USER_LOGIN_TYPE);
        logger.info("{}: {}, {}", "ConstUtils.USER_LOGIN_TYPE", userLoginTypeEnum.getType(), userLoginTypeEnum.getDesc());
        
        //cookie登录方式所需要的domain
        ConstUtils.COOKIE_DOMAIN = MapUtils.getString(configMap, "cachecloud.cookie.domain", ConstUtils.DEFAULT_COOKIE_DOMAIN);
        logger.info("{}: {}", "ConstUtils.COOKIE_DOMAIN", ConstUtils.COOKIE_DOMAIN);
        
        //cachecloud根目录
        ConstUtils.CACHECLOUD_BASE_DIR = MapUtils.getString(configMap, "cachecloud.base.dir", ConstUtils.DEFAULT_CACHECLOUD_BASE_DIR);
        logger.info("{}: {}", "ConstUtils.CACHECLOUD_BASE_DIR", ConstUtils.CACHECLOUD_BASE_DIR);
        
        //应用客户端连接报警阀值
        ConstUtils.APP_CLIENT_CONN_THRESHOLD = MapUtils.getIntValue(configMap, "cachecloud.app.client.conn.threshold", ConstUtils.DEFAULT_APP_CLIENT_CONN_THRESHOLD);
        logger.info("{}: {}", "ConstUtils.APP_CLIENT_CONN_THRESHOLD", ConstUtils.APP_CLIENT_CONN_THRESHOLD);
        
        //邮件报警接口
        ConstUtils.EMAIL_ALERT_INTERFACE = MapUtils.getString(configMap, "cachecloud.email.alert.interface", ConstUtils.DEFAULT_EMAIL_ALERT_INTERFACE);
        logger.info("{}: {}", "ConstUtils.EMAIL_ALERT_INTERFACE", ConstUtils.EMAIL_ALERT_INTERFACE);
        
        //短信报警接口
        ConstUtils.MOBILE_ALERT_INTERFACE = MapUtils.getString(configMap, "cachecloud.mobile.alert.interface", ConstUtils.DEFAULT_MOBILE_ALERT_INTERFACE);
        logger.info("{}: {}", "ConstUtils.MOBILE_ALERT_INTERFACE", ConstUtils.MOBILE_ALERT_INTERFACE);
        
        //LDAP登录地址
        ConstUtils.LDAP_URL = MapUtils.getString(configMap, "cachecloud.ldap.url", ConstUtils.DEFAULT_LDAP_URL);
        logger.info("{}: {}", "ConstUtils.LDAP_URL", ConstUtils.LDAP_URL);
        
        //是否定期清理各种统计数据(详见CleanUpStatisticsJob)
        ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA = MapUtils.getBooleanValue(configMap, "cachecloud.whether.schedule.clean.data", ConstUtils.DEFAULT_WHETHER_SCHEDULE_CLEAN_DATA);
        logger.info("{}: {}", "ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA", ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA);
        
        // app secret key
        ConstUtils.APP_SECRET_BASE_KEY = MapUtils.getString(configMap, "cachecloud.app.secret.base.key", ConstUtils.DEFAULT_APP_SECRET_BASE_KEY);
        logger.info("{}: {}", "ConstUtils.APP_SECRET_KEY", ConstUtils.APP_SECRET_BASE_KEY);
        
        // 机器性能统计周期(分钟)
        ConstUtils.MACHINE_STATS_CRON_MINUTE = MapUtils.getIntValue(configMap, "cachecloud.machine.stats.cron.minute", ConstUtils.DEFAULT_MACHINE_STATS_CRON_MINUTE);
        logger.info("{}: {}", "ConstUtils.MACHINE_STATS_CRON_MINUTE", ConstUtils.MACHINE_STATS_CRON_MINUTE);
        
        
        logger.info("===========ConfigServiceImpl reload config end============");
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
