package com.sohu.cache.redis.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.dao.InstanceConfigDao;
import com.sohu.cache.entity.InstanceConfig;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.RedisConfigTemplateService;
import com.sohu.cache.redis.enums.RedisClusterConfigEnum;
import com.sohu.cache.redis.enums.RedisConfigEnum;
import com.sohu.cache.redis.enums.RedisSentinelConfigEnum;
import com.sohu.cache.util.ConstUtils;

/**
 * redis配置模板服务
 * 
 * @author leifu
 * @Date 2016年6月23日
 * @Time 下午2:08:03
 */
public class RedisConfigTemplateServiceImpl implements RedisConfigTemplateService {

    private Logger logger = LoggerFactory.getLogger(RedisConfigTemplateServiceImpl.class);
    
    private final static String SPECIAL_EMPTY_STR = "\"\"";

    private InstanceConfigDao instanceConfigDao;
    
    @Override
    public List<InstanceConfig> getAllInstanceConfig() {
        try {
            return instanceConfigDao.getAllInstanceConfig();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<InstanceConfig> getByType(int type) {
        try {
            return instanceConfigDao.getByType(type);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public int saveOrUpdate(InstanceConfig instanceConfig) {
        return instanceConfigDao.saveOrUpdate(instanceConfig);
    }

    @Override
    public InstanceConfig getById(long id) {
        try {
            return instanceConfigDao.getById(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public InstanceConfig getByConfigKeyAndType(String configKey, int type) {
        try {
            return instanceConfigDao.getByConfigKeyAndType(configKey, type);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public int remove(long id) {
        return instanceConfigDao.remove(id);
    }

    @Override
    public int updateStatus(long id, int status) {
        return instanceConfigDao.updateStatus(id, status);
    }


    @Override
    public List<String> handleCommonConfig(int port, int maxMemory) {
        List<InstanceConfig> instanceConfigList = getByType(ConstUtils.CACHE_REDIS_STANDALONE);
        if (CollectionUtils.isEmpty(instanceConfigList)) {
            return Collections.emptyList();
        }
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            // 无效配置过滤
            if (!instanceConfig.isEffective()) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if (StringUtils.isBlank(configValue)) {
                configValue = SPECIAL_EMPTY_STR;
            }
            if (RedisConfigEnum.MAXMEMORY.getKey().equals(configKey)) {
                configValue = String.format(configValue, maxMemory);
            } else if (RedisConfigEnum.DBFILENAME.getKey().equals(configKey) 
                    || RedisConfigEnum.APPENDFILENAME.getKey().equals(configKey) || RedisConfigEnum.PORT.getKey().equals(configKey)) {
                configValue = String.format(configValue, port);
            } else if (RedisConfigEnum.DIR.getKey().equals(configKey)) {
                configValue = MachineProtocol.DATA_DIR;
            } else if (RedisConfigEnum.AUTO_AOF_REWRITE_PERCENTAGE.getKey().equals(configKey)) {
                //随机比例 auto-aof-rewrite-percentage
                int percent = 69 + new Random().nextInt(30);
                configValue = String.format(configValue, percent);
            }
            configs.add(combineConfigKeyValue(configKey, configValue));
        }
        return configs;
    }

    @Override
    public List<String> handleSentinelConfig(String masterName, String host, int port, int sentinelPort) {
        List<InstanceConfig> instanceConfigList = instanceConfigDao.getByType(ConstUtils.CACHE_REDIS_SENTINEL);
        if (CollectionUtils.isEmpty(instanceConfigList)) {
            return Collections.emptyList();
        }
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            if (!instanceConfig.isEffective()) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if (StringUtils.isBlank(configValue)) {
                configValue = SPECIAL_EMPTY_STR;
            }
            if (RedisSentinelConfigEnum.PORT.getKey().equals(configKey)) {
                configValue = String.format(configValue, sentinelPort);
            } else if(RedisSentinelConfigEnum.MONITOR.getKey().equals(configKey)) {
                configValue = String.format(configValue, masterName, host, port);
            } else if(RedisSentinelConfigEnum.DOWN_AFTER_MILLISECONDS.getKey().equals(configKey) || RedisSentinelConfigEnum.FAILOVER_TIMEOUT.getKey().equals(configKey) || RedisSentinelConfigEnum.PARALLEL_SYNCS.getKey().equals(configKey)) {
                configValue = String.format(configValue, masterName);
            } else if (RedisConfigEnum.DIR.getKey().equals(configKey)) {
                configValue = MachineProtocol.DATA_DIR;
            } 
            configs.add(combineConfigKeyValue(configKey, configValue));
        }
        return configs;
    }

    @Override
    public List<String> handleClusterConfig(int port) {
        List<InstanceConfig> instanceConfigList = getByType(ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        if (CollectionUtils.isEmpty(instanceConfigList)) {
            return Collections.emptyList();
        }
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            if (!instanceConfig.isEffective()) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if (StringUtils.isBlank(configValue)) {
                configValue = SPECIAL_EMPTY_STR;
            }
            if (RedisClusterConfigEnum.CLUSTER_CONFIG_FILE.getKey().equals(configKey)) {
                configValue = String.format(configValue, port);
            }
            configs.add(combineConfigKeyValue(configKey, configValue));

        }
        return configs;
    }
    
    @Override
    public List<String> handleCommonDefaultConfig(int port, int maxMemory) {
        List<String> configs = new ArrayList<String>();
        for (RedisConfigEnum config : RedisConfigEnum.values()) {
            if (RedisConfigEnum.MAXMEMORY.equals(config)) {
                configs.add(config.getKey() + " " + String.format(config.getValue(), maxMemory));
            } else if (RedisConfigEnum.DBFILENAME.equals(config) ||
                    RedisConfigEnum.APPENDFILENAME.equals(config) || RedisConfigEnum.PORT.equals(config)) {
                configs.add(config.getKey() + " " + String.format(config.getValue(), port));
            } else if (RedisConfigEnum.DIR.equals(config)) {
                configs.add(config.getKey() + " " + MachineProtocol.DATA_DIR);
            } else if (RedisConfigEnum.AUTO_AOF_REWRITE_PERCENTAGE.equals(config)) {
                //随机比例 auto-aof-rewrite-percentage
                int percent = 69 + new Random().nextInt(30);
                configs.add(config.getKey() + " " + String.format(RedisConfigEnum.AUTO_AOF_REWRITE_PERCENTAGE.getValue(), percent));
            } else {
                configs.add(config.getKey() + " " + config.getValue());
            }
        }
        return configs;
    }

    @Override
    public List<String> handleSentinelDefaultConfig(String masterName, String host, int port, int sentinelPort) {
        List<String> configs = new ArrayList<String>();
        configs.add(RedisSentinelConfigEnum.PORT.getKey() + " " + String.format(RedisSentinelConfigEnum.PORT.getValue(), sentinelPort));
        configs.add(RedisSentinelConfigEnum.DIR.getKey() + " " + RedisSentinelConfigEnum.DIR.getValue());
        configs.add(RedisSentinelConfigEnum.MONITOR.getKey() + " " + String.format(RedisSentinelConfigEnum.MONITOR.getValue(), masterName, host, port, 1));
        configs.add(RedisSentinelConfigEnum.DOWN_AFTER_MILLISECONDS.getKey() + " " + String
                .format(RedisSentinelConfigEnum.DOWN_AFTER_MILLISECONDS.getValue(), masterName));
        configs.add(RedisSentinelConfigEnum.FAILOVER_TIMEOUT.getKey() + " " + String
                .format(RedisSentinelConfigEnum.FAILOVER_TIMEOUT.getValue(), masterName));
        configs.add(RedisSentinelConfigEnum.PARALLEL_SYNCS.getKey() + " " + String
                .format(RedisSentinelConfigEnum.PARALLEL_SYNCS.getValue(), masterName));
        return configs;
    }

    @Override
    public List<String> handleClusterDefaultConfig(int port) {
        List<String> configs = new ArrayList<String>();
        for (RedisClusterConfigEnum config : RedisClusterConfigEnum.values()) {
            if (config.equals(RedisClusterConfigEnum.CLUSTER_CONFIG_FILE)) {
                configs.add(RedisClusterConfigEnum.CLUSTER_CONFIG_FILE.getKey() + " "
                        + String.format(RedisClusterConfigEnum.CLUSTER_CONFIG_FILE.getValue(), port));
            } else {
                configs.add(config.getKey() + " "
                        + config.getValue());
            }
        }
        return configs;
    }

    /**
     * 组合
     * @param configKey
     * @param configValue
     * @return
     */
    private String combineConfigKeyValue(String configKey, String configValue) {
        return configKey + ConstUtils.SPACE + configValue;
    }

    public void setInstanceConfigDao(InstanceConfigDao instanceConfigDao) {
        this.instanceConfigDao = instanceConfigDao;
    }


}
