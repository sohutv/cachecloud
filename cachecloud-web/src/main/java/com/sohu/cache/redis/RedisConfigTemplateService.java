package com.sohu.cache.redis;

import com.sohu.cache.entity.InstanceConfig;
import com.sohu.cache.entity.SystemResource;

import java.util.List;
import java.util.Map;

/**
 * redis配置模板服务
 * @author leifu
 * @Date 2016年6月23日
 * @Time 下午2:08:03
 */
public interface RedisConfigTemplateService {

    /**
     * 获取所有配置模板列表
     * @return
     */
    List<InstanceConfig> getAllInstanceConfig();

    /**
     * 根据type获取配置模板列表
     * 
     * @param type
     * @return
     */
    List<InstanceConfig> getByType(int type);

    /**
     * 根据type,versionId获取配置模板列表
     *
     * @param type
     * @return
     */
    List<InstanceConfig> getByVesionAndType(int type,int versionId);

    /**
     * 根据versionId获取模板所有配置
     *
     * @param versionId
     * @return 版本对应所有有效配置项
     */
    List<InstanceConfig> getByVesion(int versionId);

    /**
     * 保存或者更新配置模板
     * 
     * @param instanceConfig
     * @return
     */
    int saveOrUpdate(InstanceConfig instanceConfig);

    /**
     * 根据id获取配置模板
     * 
     * @param id
     * @return
     */
    InstanceConfig getById(long id);

    /**
     * 根据configKey和type获取配置
     * 
     * @param configKey
     * @param type
     * @return
     */
    InstanceConfig getByConfigKeyAndType(String configKey, int type);

    /**
     * 更改配置状态
     * @param id
     * @param status
     * @return
     */
    int updateStatus(long id, int status);
    
    /**
     * 删除配置
     * @param id
     */
    int remove(long id);
    
    /**
     * 普通节点配置
     * @param port
     * @param maxMemory
     * @param versionId Redis版本
     * @return 普通配置列表
     */
    List<String> handleCommonConfig(String host, int port, int maxMemory, int versionId);

    /**
     * sentinel节点配置(兼容k8s)
     * @param masterName
     * @param host  master节点ip
     * @param port  master节点port
     * @param sentinelHost sentinel host
     * @param sentinelPort sentinel port
     * @param versionId
     * @return
     */
    List<String> handleSentinelConfig(String masterName, String host, int port,String sentinelHost, int sentinelPort, int versionId);

    /**
     * cluster节点配置
     * @param port
     * @param versionId Redis版本
     * @return
     */
    List<String> handleClusterConfig(int port, int versionId);
    
    
    /**
     * 普通节点默认配置
     * @param port
     * @param maxMemory
     * @return
     */
    List<String> handleCommonDefaultConfig(int port, int maxMemory);

    /**
     * sentinel节点默认配置
     * @param masterName
     * @param host
     * @param port
     * @param sentinelPort
     * @return
     */
    List<String> handleSentinelDefaultConfig(String masterName, String host, int port, int sentinelPort);
    
    /**
     * cluster节点默认配置
     * @param port
     * @return
     */
    List<String> handleClusterDefaultConfig(int port);


    /**
     * 通过redis名称查询是否重复
     */
    public SystemResource getRedisVersionByName(String versionName);

    /**
     * 202007
     */
    public String copyRedisConfig(int versionCopyId,SystemResource resource);

    /**
     * 更新机器安装redis版本情况
     * @return SuccessEnum.SUCCESS SuccessEnum.FAIL
     */
    public String updateMachineInstallRedis(String host);

    public Boolean checkAndInstallRedisResource(String host,SystemResource redisResource);

    public Boolean checkAndInstallRedisTool(String host,SystemResource redisResource);

    /**
     * slave更新为新版本配置
     */
    public Map<String,Object> slaveUpdateConfig(long appId,Integer upgradeVersionId,String upgradeVersionName);

    /**
     * master-slave failover
     */
    public Map<String,Object> slaveFailover(long appid);

    /**
     * 检查slave psync是否成功
     */
    public Boolean slaveIsPsync(long appId,String ip,int port);
}
