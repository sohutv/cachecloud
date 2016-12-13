package com.sohu.cache.redis;

import com.sohu.cache.constant.RedisConstant;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceSlotModel;
import com.sohu.cache.entity.InstanceSlowLog;
import com.sohu.cache.web.vo.RedisSlowLog;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * redis相关操作接口
 * Created by yijunzhang on 14-6-10.
 */
public interface RedisCenter {

    /**
     * 部署redis数据收集任务(幂等操作)
     *
     * @param appId
     * @param host
     * @param port
     * @return
     */
    public boolean deployRedisCollection(long appId, String host, int port);

    /**
     * 取消部署redis收集任务
     *
     * @param appId
     * @param host
     * @param port
     * @return
     */
    public boolean unDeployRedisCollection(long appId, String host, int port);
    
    /**
     * 部署redis收集慢查询日志
     *
     * @param appId
     * @param host
     * @param port
     * @return
     */
    public boolean deployRedisSlowLogCollection(long appId, String host, int port);

    /**
     * 取消部署redis收集慢查询日志
     *
     * @param appId
     * @param host
     * @param port
     * @return
     */
    public boolean unDeployRedisSlowLogCollection(long appId, String host, int port);

    /**
     * 收集redis统计信息
     *
     * @param appId
     * @param host
     * @param port
     * @return
     */
    public Map<RedisConstant, Map<String, Object>> collectRedisInfo(long appId, long collectTime, String host,
            int port);

    /**
     * 收集redis统计信息
     *
     * @param host
     * @param port
     * @return
     */
    public Map<RedisConstant, Map<String, Object>> getInfoStats(String host, int port);

    /**
     * 根据ip和port判断redis实例当前是主还是从
     *
     * @param ip   ip
     * @param port port
     * @return 主返回true，从返回false；
     */
    public Boolean isMaster(String ip, int port);

    /**
     * 获取从节点的主节点地址
     *
     * @param ip
     * @param port
     * @return
     */
    public HostAndPort getMaster(String ip, int port);

    /**
     * 判断实例是否运行(带密码)
     *
     * @param ip
     * @param port
     * @param password
     * @return
     */
    public boolean isRun(String ip, int port, String password);
    
    /**
     * 判断实例是否运行
     *
     * @param ip
     * @param port
     * @return
     */
    public boolean isRun(String ip, int port);
    
    /**
     * 下线指定实例
     *
     * @param ip
     * @param port
     * @return
     */
    public boolean shutdown(String ip, int port);

    /**
     * 执行redis命令返回结果
     *
     * @param appDesc
     * @param command
     * @return
     */
    public String executeCommand(AppDesc appDesc, String command);

    /**
     * 实例执行redis命令
     *
     * @param appId
     * @param host
     * @param port
     * @param command
     * @return
     */
    public String executeCommand(long appId, String host, int port, String command);

    /**
     * 获取jedisSentinelPool实例,必须是sentinel类型应用
     *
     * @param appDesc
     * @return
     */
    public JedisSentinelPool getJedisSentinelPool(AppDesc appDesc);

    /**
     * 获取redis实例配置信息
     *
     * @param instanceId
     * @return
     */
    public Map<String, String> getRedisConfigList(int instanceId);

    /**
     * 获取redis实例慢查询
     *
     * @param instanceId
     * @return
     */
    public List<RedisSlowLog> getRedisSlowLogs(int instanceId, int maxCount);

    /**
     * 获取client连接信息
     *
     * @param instanceId
     * @return
     */
    public List<String> getClientList(int instanceId);

    /**
     * 配置重写
     *
     * @return
     */
    public boolean configRewrite(final String host, final int port);

    /**
     * 获取maxmemory配置
     *
     * @param host
     * @param port
     * @return
     */
    public Long getRedisMaxMemory(String host, int port);

    /**
     * 清理app数据
     *
     * @param appDesc
     * @param appUser
     * @return
     */
    public boolean cleanAppData(AppDesc appDesc, AppUser appUser);

    /**
     * 判断是否为孤立节点
     * @param host
     * @param port
     * @return
     */
    public boolean isSingleClusterNode(String host, int port);

    
    /**
     * 获取集群中失联的slots
     * @param appId
     * @return
     */
    public Map<String,String> getClusterLossSlots(long appId);
    
    /**
     * 获取集群中失联的slots
     * @param host
     * @param port
     * @return
     */
    public List<Integer> getClusterLossSlots(String host, int port);

    /**
     * 获取集群中失联的slots
     * @param healthyHost
     * @param healthyPort
     * @param lossSlotsHost
     * @param lossSlotsPort
     * @return
     */
    public List<Integer> getInstanceSlots(String healthyHost, int healthyPort, String lossSlotsHost, int lossSlotsPort);

    /**
     * 从一个应用中获取一个健康的实例
     * @param appId
     * @return
     */
    public InstanceInfo getHealthyInstanceInfo(long appId);
    
    /**
     * 收集redis慢查询日志
     *
     * @param appId
     * @param host
     * @param port
     * @return
     */
    public List<InstanceSlowLog> collectRedisSlowLog(long appId, long collectTime, String host,
            int port);
    
    /**
     * 按照appid获取慢查询日志
     * @param appId
     * @return
     */
    public List<InstanceSlowLog> getInstanceSlowLogByAppId(long appId);

    /**
     * 按照appid获取慢查询日志
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<InstanceSlowLog> getInstanceSlowLogByAppId(long appId, Date startDate, Date endDate);

    /**
     * 按照appid获取慢查询日志数关系
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<String, Long> getInstanceSlowLogCountMapByAppId(Long appId, Date startDate, Date endDate);
    
    /**
     * 判断当前节点是否是sentinel节点
     * @param ip
     * @param port
     * @return
     */
    public boolean isSentinelNode(String ip, int port);
    
    /**
     * 获取集群的slots分布
     * @param appId
     * @return
     */
    Map<String, InstanceSlotModel> getClusterSlotsMap(long appId);

    /**
     * 获取Redis版本
     * @param ip
     * @param port
     * @return
     */
    public String getRedisVersion(String ip, int port);
    
    
    /**
     * 获取nodeId
     * @param ip
     * @param port
     * @return
     */
    public String getNodeId(String ip, int port);
    
}
