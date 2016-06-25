package com.sohu.cache.redis.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
        List<InstanceConfig> instanceConfigList = instanceConfigDao.getByType(ConstUtils.CACHE_REDIS_STANDALONE);
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            int status = instanceConfig.getStatus();
            if (status == 0) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if (RedisConfigEnum.MAXMEMORY.getKey().equals(configKey)) {
                configs.add(configKey + " " + String.format(configValue, maxMemory));
            } else if (RedisConfigEnum.DBFILENAME.getKey().equals(configKey) 
                    || RedisConfigEnum.APPENDFILENAME.getKey().equals(configKey) || RedisConfigEnum.PORT.getKey().equals(configKey)) {
                configs.add(configKey + " " + String.format(configValue, port));
            } else if (RedisConfigEnum.DIR.getKey().equals(configKey)) {
                configs.add(configKey + " " + MachineProtocol.DATA_DIR);
            } else if (RedisConfigEnum.AUTO_AOF_REWRITE_PERCENTAGE.getKey().equals(configKey)) {
                //随机比例 auto-aof-rewrite-percentage
                int percent = 69 + new Random().nextInt(30);
                configs.add(configKey + " " + String.format(RedisConfigEnum.AUTO_AOF_REWRITE_PERCENTAGE.getValue(), percent));
            } else {
                configs.add(configKey + " " + configValue);
            }
        }
        return configs;
    }

    @Override
    public List<String> handleSentinelConfig(String masterName, String host, int port, int sentinelPort, int quorum) {
        List<InstanceConfig> instanceConfigList = instanceConfigDao.getByType(ConstUtils.CACHE_REDIS_SENTINEL);
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            int status = instanceConfig.getStatus();
            if (status == 0) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if(RedisSentinelConfigEnum.PORT.getKey().equals(configKey)) {
                configs.add(configKey + " " + String.format(configValue, sentinelPort));
            } else if(RedisSentinelConfigEnum.MONITOR.getKey().equals(configKey)) {
                configs.add(configKey + " " + String.format(configValue, masterName, host, port, 1));
            } else if(RedisSentinelConfigEnum.DOWN_AFTER_MILLISECONDS.getKey().equals(configKey) || RedisSentinelConfigEnum.FAILOVER_TIMEOUT.getKey().equals(configKey) || RedisSentinelConfigEnum.PARALLEL_SYNCS.getKey().equals(configKey)) {
                configs.add(configKey + " " + String.format(configValue, masterName));
            } else {
                configs.add(configKey + " " + configValue);
            }
        }
        return configs;
    }

    @Override
    public List<String> handleClusterConfig(int port) {
        List<InstanceConfig> instanceConfigList = instanceConfigDao.getByType(ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            int status = instanceConfig.getStatus();
            if (status == 0) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if (RedisClusterConfigEnum.CLUSTER_CONFIG_FILE.getKey().equals(configKey)) {
                configs.add(configKey + " " + String.format(configValue, port));
            } else {
                configs.add(configKey + " " + configValue);
            }
        }
        return configs;
    }

    public void setInstanceConfigDao(InstanceConfigDao instanceConfigDao) {
        this.instanceConfigDao = instanceConfigDao;
    }

}
