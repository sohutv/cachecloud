package com.sohu.cache.task;

import com.sohu.cache.constant.OperateResult;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.task.constant.PikaNode;
import com.sohu.cache.task.constant.TaskQueueEnum.TaskStatusEnum;
import com.sohu.cache.task.entity.*;

import java.util.List;

/**
 * 任务相关
 */
public interface TaskService {

    /**
     * 执行任务
     *
     * @param taskId
     * @return
     */
    TaskStatusEnum executeTask(long taskId);

    /**
     * @param taskId
     * @return
     */
    List<TaskStepFlow> getTaskStepFlowList(long taskId);

    /**
     * @param taskId
     * @return
     */
    TaskQueue getTaskQueueById(long taskId);

    /**
     * 任务流描述信息
     *
     * @param className
     * @return
     */
    List<TaskStepMeta> getTaskStepMetaList(String className);

    /**
     * 获取当前任务流
     *
     * @param taskId
     * @return
     */
    TaskStepFlow getCurrentTaskStepFlow(long taskId);

    /**
     * 添加pika实例安装任务
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     */
    long addPikaInstallTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加redis server实例安装任务
     *
     * @param appId
     * @param host
     * @param port
     * @param maxMemory
     * @param version
     * @param parentTaskId
     */
    long addRedisServerInstallTask(long appId, String host, int port, int maxMemory, String version, Boolean isCluster, long parentTaskId);

    /**
     * 添加redis server实例stop任务
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     */
    long addRedisServerStopTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加memcache实例stop任务
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     */
    long addMemcacheStopTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加memcache集群下线
     *
     * @param appId
     * @param auditId
     * @param parentTaskId
     */
    long addMemcacheClusterOfflineTask(long appId, long auditId, long parentTaskId);

    /**
     * 添加pika实例stop任务
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     */
    long addPikaStopTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加redis server实例start任务
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     */
    long addRedisServerStartTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加redis sentinel实例安装任务
     *
     * @param appId
     * @param host
     * @param port
     * @param masterRedisServerNodes
     * @param quorum
     * @param dbVersion
     * @param parentTaskId
     * @return
     */
    long addRedisSentinelInstallTask(long appId, String host, int port,
                                     List<RedisServerNode> masterRedisServerNodes, int quorum, String dbVersion, long parentTaskId);

    /**
     * 添加redis sentinel实例stop任务
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     * @return
     */
    long addRedisSentinelStopTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加redis port实例安装任务
     *
     * @param appId
     * @param redisPortHost
     * @param redisPortHttpPort
     * @param sourceHost
     * @param sourcePort
     * @param targetHost
     * @param targetPort
     * @param parentTaskId
     * @return
     */
//    long addRedisPortInstallTask(long appId, String redisPortHost, int redisPortHttpPort, String sourceHost,
//                                 int sourcePort, String targetHost, int targetPort, long parentTaskId);

    /**
     * 添加redis migrate tool任务
     *
     * @param migrateToolHost
     * @param migrateToolPort
     * @param sourceAppId
     * @param targetAppId
     * @param sourceRedisServerNodes
     * @param parentTaskId
     * @return
     */
    long addRedisMigrateToolInstallTask(String migrateToolHost, int migrateToolPort, long sourceAppId, long targetAppId,
                                        List<RedisServerNode> sourceRedisServerNodes, long parentTaskId);

    /**
     * 添加nutcracker实例
     *
     * @param appId
     * @param host
     * @param port
     * @param masterRedisServerNodes
     * @param parentTaskId
     */
    long addNutCrackerInstallTask(long appId, String host, int port,
                                  List<RedisServerNode> masterRedisServerNodes, long parentTaskId);

    /**
     * 添加codis proxy实例
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     */
    long addCodisProxyInstallTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加codis dashboard实例
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     */
    long addCodisDashboardTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加nutcracker实例stop任务
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     */
    long addNutCrackerStopTask(long appId, String host, int port, long parentTaskId);

    /**
     * 添加清理redisserver数据任务
     *
     * @param appId
     * @param host
     * @param port
     * @param parentTaskId
     * @return
     */
    long addRedisServerFlushDataTask(long appId, String host, int port, long parentTaskId);

    /**
     * 应用刷新配置
     *
     * @param appId
     * @param appIsNew
     * @param parentTaskId
     * @return
     */
    long addAppConfigFlushZkTask(long appId, Boolean appIsNew, long parentTaskId);

    /**
     * 添加twemproxy应用
     *
     * @param appId
     * @param auditId
     * @param maxMemory
     * @param redisServerMachineList
     * @param redisSentinelMachineList
     * @param nutCrackerMachineList
     * @param masterPerMachine
     * @param sentinelPerMachine
     * @param nutCrackerPerMachine
     * @param isNeedFlushZkConfig      是否要刷新zk配置
     * @param version
     * @param parentTaskId
     * @return
     */
    long addTwemproxyAppTask(long appId, long auditId, int maxMemory, List<String> redisServerMachineList,
                             List<String> redisSentinelMachineList, List<String> nutCrackerMachineList, int masterPerMachine,
                             int sentinelPerMachine, int nutCrackerPerMachine, Boolean isNeedFlushZkConfig, String version,
                             long parentTaskId);

    /**
     * redis standalone
     *
     * @param appId                  应用id
     * @param appAuditId             审核id
     * @param maxMemory              实例内存
     * @param redisServerMachineList 部署节点
     * @param masterPerMachine       部署实例数
     * @param dbVersion              redis版本
     * @param parentTaskId           父任务id
     * @return
     */
    long addRedisStandaloneAppTask(long appId, long appAuditId, int maxMemory, List<String> redisServerMachineList,
                                   int masterPerMachine, String dbVersion, long parentTaskId);

    /**
     * redis cluster集群
     *
     * @param appId                  应用id
     * @param appAuditId             审核id
     * @param maxMemory              实例内存
     * @param redisServerMachineList 部署节点
     * @param masterPerMachine       部署实例数
     * @param dbVersion              redis版本
     * @param parentTaskId           父任务id
     * @return
     */
    long addRedisClusterAppTask(long appId, long appAuditId, int maxMemory, List<String> redisServerMachineList,
                                int masterPerMachine, String dbVersion, long parentTaskId);

    /**
     * redis sentinel集群
     *
     * @param appId
     * @param appAuditId
     * @param maxMemory
     * @param redisServerMachineList
     * @param redisSentinelMachineList
     * @param masterPerMachine
     * @param sentinelPerMachine
     * @param dbVersion
     * @param parentTaskId
     * @return
     */
    long addRedisSentinelAppTask(long appId, long appAuditId, int maxMemory, List<String> redisServerMachineList,
                                 List<String> redisSentinelMachineList, int masterPerMachine, int sentinelPerMachine, String dbVersion,
                                 long parentTaskId);

    /**
     * 添加twemproxy pika
     *
     * @param appId
     * @param auditId
     * @param maxMemory
     * @param pikaMachineList
     * @param redisSentinelMachineList
     * @param nutCrackerMachineList
     * @param masterPerMachine
     * @param sentinelPerMachine
     * @param nutCrackerPerMachine
     * @param isNeedFlushZkConfig      是否要刷新zk配置
     * @param parentTaskId
     * @return
     */
    long addTwemproxyPikaTask(long appId, long auditId, int maxMemory, List<String> pikaMachineList,
                              List<String> redisSentinelMachineList, List<String> nutCrackerMachineList, int masterPerMachine,
                              int sentinelPerMachine, int nutCrackerPerMachine, Boolean isNeedFlushZkConfig, long parentTaskId);

    /**
     * pika sentinel
     *
     * @param appId
     * @param appAuditId
     * @param maxMemory
     * @param pikaMachineList
     * @param redisSentinelMachineList
     * @param masterPerMachine
     * @param sentinelPerMachine
     * @param parentTaskId
     * @return
     */
    long addPikaSentinelAppTask(long appId, long appAuditId, int maxMemory, List<String> pikaMachineList,
                                List<String> redisSentinelMachineList, int masterPerMachine, int sentinelPerMachine, long parentTaskId);

    /**
     * twemproxy redis下线
     *
     * @param appId
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addTwemproxyOfflineTask(long appId, long auditId, long parentTaskId);

    /**
     * redis sentinel app下线
     *
     * @param appId
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addRedisSentinelAppOfflineTask(long appId, long auditId, long parentTaskId);

    /**
     * pika sentinel app下线
     *
     * @param appId
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addPikaSentinelAppOfflineTask(long appId, long auditId, long parentTaskId);

    /**
     * twemproxy pika下线
     *
     * @param appId
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addTwemproxyPikaOfflineTask(long appId, long auditId, long parentTaskId);

    /**
     * twemproxy -> twemproxy
     *
     * @param sourceAppId
     * @param targetAppId
     * @param appAuditId
     * @param isScaleOut
     * @param parentTaskId
     * @return
     */
    long addTwemproxyToTwemproxyTask(long sourceAppId, long targetAppId, long appAuditId, boolean isScaleOut,
                                     long parentTaskId);

    /**
     * twemproxy -> twemproxy(v2)
     *
     * @param sourceAppId
     * @param targetAppId
     * @param appAuditId
     * @param isScaleOut
     * @param onlyMigrate
     * @param parentTaskId
     * @return
     */
    long addTwemproxyToTwemproxyTaskV2(long sourceAppId, long targetAppId, long appAuditId, boolean isScaleOut,
                                       boolean onlyMigrate, long parentTaskId);

    /**
     * 备库重搭
     *
     * @param appId
     * @param masterHost
     * @param masterPort
     * @param slaveMachineHost
     * @param parentTaskId
     * @return
     */
    long addSlaveRedisServerRebuildTask(long appId, String masterHost, int masterPort,
                                        String slaveMachineHost, long parentTaskId);

    /**
     * pika备库重搭
     *
     * @param appId
     * @param masterHost
     * @param masterPort
     * @param slaveMachineHost
     * @param parentTaskId
     * @return
     */
    long addSlavePikaRebuildTask(long appId, String masterHost, int masterPort,
                                 String slaveMachineHost, long parentTaskId);

    /**
     * sentinel failover
     *
     * @param appId
     * @param masterHost
     * @param masterPort
     * @param parentTaskId
     * @return
     */
    long addSlaveRedisSentinelFailoverTask(long appId, String masterHost, int masterPort, long parentTaskId);

    /**
     * 实例idle key分析任务
     *
     * @param appId
     * @param auditId
     * @param host
     * @param port
     * @param parentTaskId
     * @return
     */
    long addRedisServerIdleKeyAnalysisTask(long appId, long auditId, String host, int port, long parentTaskId);

    /**
     * 实例idle key分析任务
     *
     * @param appId
     * @param auditId
     * @param host
     * @param port
     * @param parentTaskId
     * @return
     */
    long addRedisServerKeyTypeAnalysisTask(long appId, long auditId, String host, int port, long parentTaskId);

    /**
     * 实例key ttl分析任务
     *
     * @param appId
     * @param auditId
     * @param host
     * @param port
     * @param parentTaskId
     * @return
     */
    long addRedisServerKeyTtlAnalysisTask(long appId, long auditId, String host, int port, long parentTaskId);

    /**
     * 实例key value size分析任务
     *
     * @param appId
     * @param auditId
     * @param host
     * @param port
     * @param parentTaskId
     * @return
     */
    long addRedisServerKeyValueAnalysisTask(long appId, long auditId, String host, int port, long parentTaskId);

    /**
     * 实例big key分析任务
     *
     * @param appId
     * @param auditId
     * @param host
     * @param port
     * @param parentTaskId
     * @return
     */
    long addRedisServerBigKeyAnalysisTask(long appId, long auditId, String host, int port, long parentTaskId);

    /**
     * twemproxy key分析任务
     *
     * @param appId
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addAppKeyAnalysisTask(long appId, long auditId, long parentTaskId);

    /**
     * twemproxy flushall任务
     *
     * @param appId
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addTwemproxyFlushAllDataTask(long appId, long auditId, long parentTaskId);

    /**
     * twemproxy pika flushall任务
     *
     * @param appId
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addTwemproxyPikaFlushAllDataTask(long appId, long auditId, long parentTaskId);

    /**
     * @param host
     * @param port
     * @param sourceAppId
     * @param targetAppId
     * @param parentTaskId
     * @return
     */
    long addRemoveRedisMigrateToolTask(String host, int port, long sourceAppId, long targetAppId, long parentTaskId);

    /**
     * 添加nut cracker扩容任务
     *
     * @param appId
     * @param nutCrackerMachineList
     * @param nutCrackerPerMachine
     * @param parentTaskId
     * @return
     */
    long addNutCrackerScaleOutTask(long appId, List<String> nutCrackerMachineList, int nutCrackerPerMachine,
                                   long parentTaskId);

    /**
     * 添加nut cracker批量下线任务
     *
     * @param appId
     * @param nutCrackerNodes
     * @param parentTaskId
     * @return
     */
    long addNutCrackerListOfflineTask(long appId, List<NutCrackerNode> nutCrackerNodes, long parentTaskId);

    /**
     * 添加redis sentinel批量下线任务
     *
     * @param appId
     * @param redisSentinelNodes
     * @param parentTaskId
     * @return
     */
    long addRedisSentinelListOfflineTask(long appId, List<RedisSentinelNode> redisSentinelNodes, long parentTaskId);

    /**
     * 添加redis slave server批量下线任务
     *
     * @param appId
     * @param redisServerNodes
     * @param parentTaskId
     * @return
     */
    long addRedisSlaveServerOfflineTask(long appId, List<RedisServerNode> redisServerNodes, long parentTaskId);

    /**
     * 添加pika slave批量下线任务
     *
     * @param appId
     * @param pikaNodes
     * @param parentTaskId
     * @return
     */
    long addPikaSlaveOfflineTask(long appId, List<PikaNode> pikaNodes, long parentTaskId);

    /**
     * 添加redis sentinel添加任务
     *
     * @param appId
     * @param redisSentinelMachineList
     * @param dbVersion
     * @param parentTaskId
     * @return
     */
    long addRedisSentinelAddTask(long appId, List<String> redisSentinelMachineList, String dbVersion,
                                 long parentTaskId);

    /**
     * 为故障机器添加批量failover
     *
     * @param appId
     * @param host
     * @param parentTaskId
     * @return
     */
    long addTwemproxyFaultMachineFailoverTask(long appId, String host, long parentTaskId);

    /**
     * 为应用故障机器做备库重搭
     *
     * @param appId
     * @param host
     * @param parentTaskId
     * @return
     */
    long addMachineSlaveRebuildTask(long appId, String host, long parentTaskId);

    long addAppTopologyExamTask(boolean auto, int examType, long appId, long parentTaskId);

    long addMachineExamTask(List<String> ipList, Integer useType, long parentTaskId);

    long addOffLineAppTask(long appId, Long auditId, long parentTaskId, AppUser userInfo);


    /**
     * scan key
     *
     * @param appId
     * @param auditId
     * @param pattern
     * @param size
     * @param parentTaskId
     * @return
     */
    long addAppScanKeyTask(long appId, long auditId, String nodes, String pattern, int size, long parentTaskId);

    long addInstanceScanKeyTask(long appId, long auditId, String host, int port, String pattern, int size, long parentTaskId);


    /**
     * delete key
     *
     * @param appId
     * @param nodes
     * @param pattern
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addAppDelKeyTask(long appId, String nodes, String pattern, long auditId, long parentTaskId);

    long addInstanceDelKeyTask(long appId, String host, int port, String pattern, long auditId, long parentTaskId);

    /**
     * bigkey
     *
     * @param appId
     * @param nodes
     * @param fromBytes
     * @param toBytes
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addAppBigKeyTask(long appId, String nodes, long fromBytes, long toBytes, int size, long auditId, long parentTaskId);

    long addInstanceBigKeyTask(long appId, String host, int port, long fromBytes, long toBytes, int size, long auditId, long parentTaskId);

    /**
     * idle key
     *
     * @param appId
     * @param nodes
     * @param idleTime
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addAppIdleKeyTask(long appId, String nodes, long idleTime, int size, long auditId, long parentTaskId);

    long addInstanceIdleKeyTask(long appId, String host, int port, long idleTime, int size, long auditId, long parentTaskId);


    /**
     * hot key
     *
     * @param appId
     * @param nodes
     * @param monitorCount
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addAppHotKeyTask(long appId, String nodes, String command, long auditId, long parentTaskId);

    long addInstanceHotKeyTask(long appId, String host, int port, String command, long auditId, long parentTaskId);


    /**
     * slotAnalysis
     *
     * @param appId
     * @param nodes
     * @param auditId
     * @param parentTaskId
     * @return
     */
    long addAppSlotAnalysisTask(long appId, String nodes, long auditId, long parentTaskId);

    long addInstanceSlotAnalysisTask(long appId, String host, int port, long auditId, long parentTaskId);


    /**
     * 更新childTaskId
     *
     * @param taskStepFlowId
     * @param masterChildTaskId
     */
    void updateTaskStepFlowChildTaskId(long taskStepFlowId, long masterChildTaskId);

    /**
     * 任务状态
     *
     * @param taskStatusEnum
     */
    List<TaskQueue> getTaskQueueList(TaskStatusEnum taskStatusEnum);

    /**
     * 更新任务状态
     *
     * @param taskId
     * @param taskStatusEnum
     */
    void updateTaskQueueStatus(long taskId, TaskStatusEnum taskStatusEnum);

    /**
     * @param taskSearch
     * @return
     */
    int getTaskQueueCount(TaskSearch taskSearch);

    /**
     * @param taskSearch
     * @return
     */
    List<TaskQueue> getTaskQueueList(TaskSearch taskSearch);

    /**
     * @param searchTaskId
     * @return
     */
    List<TaskQueue> getTaskQueueTreeByTaskId(long searchTaskId);

    /**
     * 修改任务参数
     *
     * @param taskId
     * @param param
     */
    OperateResult updateParam(long taskId, String param);

    /**
     * 执行新任务
     */
    void executeNewTask();

    /**
     * @param taskFlowId
     * @param status
     * @return
     */
    OperateResult updateTaskFlowStatus(long taskFlowId, int status);

    /**
     * @param appId
     * @param className
     * @return
     */
    List<TaskQueue> getByAppAndClass(long appId, String className);

    /**
     * 添加机器同步数据任务task
     */
    long addMachineSyncTask(String sourceIp, String targetIp, String containerIp, String important_Info, long parentTaskId);

    long addResourceCompileTask(Integer resourceId, Integer repositoryId, String containerIp, AppUser userInfo);

}
