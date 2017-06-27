package com.sohu.cache.redis;

import java.util.List;

import com.sohu.cache.constant.ClusterOperateResult;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.web.enums.RedisOperateEnum;

/**
 * redis 部署配置
 * Created by yijunzhang on 14-7-1.
 */
public interface RedisDeployCenter {

    /**
     * 部署cluster 集群
     *
     * @param appId        应用id
     * @param clusterNodes redis实例集合
     * @param maxMemory 实例最大内存,单位MB
     * @return 实例是否部署成功
     */
    public boolean deployClusterInstance(long appId, List<RedisClusterNode> clusterNodes, int maxMemory);

    /**
     * 部署redis sentinel实例 实例组
     *
     * @param appId        应用id
     * @param masterHost       主节点地址
     * @param slaveHost        从节点地址
     * @param maxMemory    实例最大内存,单位MB
     * @param sentinelList sentinel-host列表
     * @param 
     * @return 实例是否部署成功
     */
    public boolean deploySentinelInstance(long appId, String masterHost, String slaveHost, int maxMemory, List<String> sentinelList);

    /**
     * 部署Standalone redis实例
     *
     * @param appId        应用id
     * @param host        节点地址
     * @param maxMemory    实例最大内存,单位MB
     * @return 实例是否部署成功
     */
    public boolean deployStandaloneInstance(long appId, String host, int maxMemory);

    /**
     * 修改app下所有实例的配置
     *
     * @param appId
     * @param parameter
     * @param value
     * @return
     */
    public boolean modifyAppConfig(long appId, String parameter, String value);

    /**
     * 修改实例配置
     * @param appId
     * @param host
     * @param port
     * @param parameter
     * @param value
     * @return
     */
    public boolean modifyInstanceConfig(long appId, String host, int port, String parameter, String value);

    /**
     * 为应用appId添加sentinel服务器
     *
     * @param appId
     * @param sentinelHost
     * @return
     */
    public boolean addSentinel(long appId, String sentinelHost) throws Exception;

    /**
     * 为主节点添加从节点
     *
     * @param appId
     * @param masterInstanceId
     * @param slaveHost
     * @return
     */
    public boolean addSlave(long appId, int masterInstanceId, String slaveHost) throws Exception;
    
    /**
     * 填充集群中失败的slots，添加一个master节点
     * @param appId
     * @param instanceId
     * @param masterHost
     * @return
     * @throws Exception
     */
    public RedisOperateEnum addSlotsFailMaster(long appId, int instanceId, String masterHost) throws Exception;
    
    /**
     * 创建一个redis实例
     *
     * @param appDesc
     * @param host
     * @param port
     * @param maxMemory
     * @return
     */
    public boolean createRunNode(AppDesc appDesc, String host, Integer port, int maxMemory, boolean isCluster);

    /**
     * sentinel类型应用执行Failover,主从切换
     * @param appId
     * @return
     */
    public boolean sentinelFailover(long appId) throws Exception;

    /**
     * cluster类型应用执行Failover,主从切换,只能在从节点执行
     *
     * @param appId
     * @param slaveInstanceId
     * @param failoverParam
     * @return
     */
    public boolean clusterFailover(long appId, int slaveInstanceId, String failoverParam) throws Exception;

    /**
     * 检查是否具备forget的条件
     * @param appId
     * @param forgetInstanceId
     * @return
     */
    public ClusterOperateResult checkClusterForget(Long appId, int forgetInstanceId);

    /**
     * 删除节点
     * @param appId
     * @param delNodeInstanceId
     * @return
     */
    public ClusterOperateResult delNode(Long appId, int delNodeInstanceId);



}
