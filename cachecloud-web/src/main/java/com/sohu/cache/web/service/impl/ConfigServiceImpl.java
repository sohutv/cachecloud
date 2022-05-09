package com.sohu.cache.web.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sohu.cache.alert.utils.AlertUtils;
import com.sohu.cache.util.StringUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.dao.ConfigDao;
import com.sohu.cache.entity.SystemConfig;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author leifu
 * @Date 2016年5月23日
 * @Time 上午10:35:26
 */
@Service("configService")
public class ConfigServiceImpl implements ConfigService {

    private Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
    @Autowired
    private ConfigDao configDao;


    @PostConstruct
    public void init() {
        reloadSystemConfig();
    }

    /**
     * 加载配置
     */
    public void reloadSystemConfig() {
        logger.info("ConfigServiceImpl reload config start");
        // 加载配置
        Map<String, String> configMap = getConfigMap();

        // 文案相关
        ConstUtils.CONTACT = MapUtils.getString(configMap, "cachecloud.contact");
        logger.debug("{}: {}", "ConstUtils.CONTACT", ConstUtils.CONTACT);

        // 报警相关配置
        AlertUtils.EMAILS = MapUtils.getString(configMap, "cachecloud.owner.email");
        logger.debug("{}: {}", "ConstUtils.EMAILS", AlertUtils.EMAILS);

        AlertUtils.PHONES = MapUtils.getString(configMap, "cachecloud.owner.phone");
        logger.debug("{}: {}", "ConstUtils.PHONES", AlertUtils.PHONES);

        AlertUtils.WECHAT = MapUtils.getString(configMap, "cachecloud.owner.weChat");
        logger.debug("{}: {}", "ConstUtils.WECHAT", AlertUtils.WECHAT);

        // ssh相关配置
        ConstUtils.USERNAME = MapUtils.getString(configMap, "cachecloud.machine.ssh.name", ConstUtils.DEFAULT_USERNAME);
        logger.debug("{}: {}", "ConstUtils.USERNAME", ConstUtils.USERNAME);

        ConstUtils.PASSWORD = MapUtils.getString(configMap, "cachecloud.machine.ssh.password",
                ConstUtils.DEFAULT_PASSWORD);
        logger.debug("{}: {}", "ConstUtils.PASSWORD", ConstUtils.PASSWORD);

        ConstUtils.SSH_PORT_DEFAULT = Integer.parseInt(MapUtils.getString(configMap, "cachecloud.machine.ssh.port",
                String.valueOf(ConstUtils.DEFAULT_SSH_PORT_DEFAULT)));
        logger.debug("{}: {}", "ConstUtils.SSH_PORT_DEFAULT", ConstUtils.SSH_PORT_DEFAULT);

        //ssh授权方式
        ConstUtils.SSH_AUTH_TYPE = MapUtils.getIntValue(configMap, "cachecloud.ssh.auth.type", ConstUtils.DEFAULT_SSH_AUTH_TYPE);
        logger.debug("{}: {}", "ConstUtils.SSH_AUTH", ConstUtils.SSH_AUTH_TYPE);

        //public key pem
        ConstUtils.PUBLIC_KEY_PEM = MapUtils.getString(configMap, "cachecloud.public.key.pem", ConstUtils.DEFAULT_PUBLIC_KEY_PEM);
        logger.debug("{}: {}", "ConstUtils.PUBLIC_KEY_PEM", ConstUtils.PUBLIC_KEY_PEM);

        ConstUtils.PUBLIC_KEY_PEM = MapUtils.getString(configMap, "cachecloud.public.key.pem", ConstUtils.DEFAULT_PUBLIC_KEY_PEM);
        logger.debug("{}: {}", "ConstUtils.PUBLIC_KEY_PEM", ConstUtils.PUBLIC_KEY_PEM);

        ConstUtils.PUBLIC_USERNAME = MapUtils.getString(configMap, "cachecloud.public.user.name", ConstUtils.DEFAULT_PUBLIC_USERNAME);
        logger.debug("{}: {}", "ConstUtils.PUBLIC_USERNAME", ConstUtils.PUBLIC_USERNAME);

        // 管理员相关配置
        ConstUtils.SUPER_ADMIN_NAME = MapUtils.getString(configMap, "cachecloud.admin.user.name",
                ConstUtils.DEFAULT_SUPER_ADMIN_NAME);
        logger.debug("{}: {}", "ConstUtils.SUPER_ADMIN_NAME", ConstUtils.SUPER_ADMIN_NAME);

        ConstUtils.SUPER_ADMIN_PASS = MapUtils.getString(configMap, "cachecloud.admin.user.password",
                ConstUtils.DEFAULT_SUPER_ADMIN_PASS);
        logger.debug("{}: {}", "ConstUtils.SUPER_ADMIN_PASS", ConstUtils.SUPER_ADMIN_PASS);

        ConstUtils.SUPER_ADMINS = MapUtils.getString(configMap, "cachecloud.superAdmin",
                ConstUtils.DEFAULT_SUPER_ADMINS);
        logger.debug("{}: {}", "ConstUtils.SUPER_ADMINS", ConstUtils.SUPER_ADMINS);

        ConstUtils.SUPER_MANAGER = Arrays.asList(ConstUtils.SUPER_ADMINS.split(","));
        logger.debug("{}: {}", "ConstUtils.SUPER_MANAGER", ConstUtils.SUPER_MANAGER);

        String leaderEmailListStr = MapUtils.getString(configMap, "cachecloud.leader.email");
        if (!StringUtil.isBlank(leaderEmailListStr)) {
            ConstUtils.LEADER_EMAIL_LIST = Arrays.asList(leaderEmailListStr.split(","));
        }

        // 机器报警阀值
        ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD = MapUtils.getDoubleValue(configMap, "machine.mem.alert.ratio",
                ConstUtils.DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD);
        logger.debug("{}: {}", "ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD", ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD);

        //cachecloud根目录
        ConstUtils.CACHECLOUD_BASE_DIR = MapUtils.getString(configMap, "cachecloud.base.dir", ConstUtils.DEFAULT_CACHECLOUD_BASE_DIR);
        logger.debug("{}: {}", "ConstUtils.CACHECLOUD_BASE_DIR", ConstUtils.CACHECLOUD_BASE_DIR);

        //应用客户端连接报警阀值
        ConstUtils.APP_CLIENT_CONN_THRESHOLD = MapUtils.getIntValue(configMap, "cachecloud.app.client.conn.threshold", ConstUtils.DEFAULT_APP_CLIENT_CONN_THRESHOLD);
        logger.debug("{}: {}", "ConstUtils.APP_CLIENT_CONN_THRESHOLD", ConstUtils.APP_CLIENT_CONN_THRESHOLD);

        //邮件报警接口
        AlertUtils.EMAIL_ALERT_INTERFACE = MapUtils.getString(configMap, "cachecloud.email.alert.interface");
        logger.debug("{}: {}", "ConstUtils.EMAIL_ALERT_INTERFACE", AlertUtils.EMAIL_ALERT_INTERFACE);

        //短信报警接口
        AlertUtils.MOBILE_ALERT_INTERFACE = MapUtils.getString(configMap, "cachecloud.mobile.alert.interface");
        logger.debug("{}: {}", "ConstUtils.MOBILE_ALERT_INTERFACE", AlertUtils.MOBILE_ALERT_INTERFACE);

        //微信报警接口
        AlertUtils.WECHAT_ALERT_INTERFACE = MapUtils.getString(configMap, "cachecloud.weChat.alert.interface");
        logger.debug("{}: {}", "ConstUtils.MOBILE_ALERT_INTERFACE", AlertUtils.MOBILE_ALERT_INTERFACE);

        //是否定期清理各种统计数据(详见CleanUpStatisticsJob)
        ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA = MapUtils.getBooleanValue(configMap, "cachecloud.whether.schedule.clean.data", ConstUtils.DEFAULT_WHETHER_SCHEDULE_CLEAN_DATA);
        logger.debug("{}: {}", "ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA", ConstUtils.WHETHER_SCHEDULE_CLEAN_DATA);

        // 机器性能统计周期(分钟)
        ConstUtils.MACHINE_STATS_CRON_MINUTE = MapUtils.getIntValue(configMap, "cachecloud.machine.stats.cron.minute", ConstUtils.DEFAULT_MACHINE_STATS_CRON_MINUTE);
        logger.debug("{}: {}", "ConstUtils.MACHINE_STATS_CRON_MINUTE", ConstUtils.MACHINE_STATS_CRON_MINUTE);

        logger.info("ConfigServiceImpl reload config end");
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
