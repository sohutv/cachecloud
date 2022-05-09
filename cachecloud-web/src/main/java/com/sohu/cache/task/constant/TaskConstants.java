package com.sohu.cache.task.constant;

import com.sohu.cache.task.tasks.MachineSyncTask;
import com.sohu.cache.task.tasks.RedisClusterAppDeployTask;
import com.sohu.cache.task.tasks.RedisSentinelAppDeployTask;
import com.sohu.cache.task.tasks.RedisStandaloneAppDeployTask;
import com.sohu.cache.task.tasks.daily.MachineExamTask;
import com.sohu.cache.task.tasks.daily.TopologyExamTask;
import com.sohu.cache.task.tasks.install.RedisSentinelInstallTask;
import com.sohu.cache.task.tasks.install.RedisServerInstallTask;
import com.sohu.cache.task.tasks.resource.PackCompileTask;

/**
 * 任务相关常量
 *
 * @author fulei
 */
public class TaskConstants {

    /**
     * init方法
     */
    public final static String INIT_METHOD_KEY = "init";

    /**
     * 任务id
     */
    public final static String TASK_ID_KEY = "taskId";

    /**
     * 任务流id
     */
    public final static String TASK_STEP_FLOW_ID = "taskStepFlowId";

    /**
     * 各种类型实例列表
     */
    //单个实例
    public final static String REDIS_SERVER_NODE_KEY = "redisServerNode";
    public final static String REDIS_SERVER_NODES_KEY = "redisServerNodes";
    public final static String REDIS_SENTINEL_NODES_KEY = "redisSentinelNodes";
    public final static String NUT_CRACKER_NODES_KEY = "nutCrackerNodes";
    public final static String REDIS_PORT_NODES_KEY = "redisPortNodes";
    public final static String REDIS_MIGRATE_TOOL_NODES_KEY = "redisMigrateToolNodes";
    public final static String PIKA_NODES_KEY = "pikaNodes";
    public final static String PIKA_NODE_KEY = "pikaNode";
    public final static String MEMCACHE_NODES_KEY = "memcacheNodes";


    /**
     * for codis
     */
    public final static String CODIS_SERVER_NODES_KEY = "codisServerNodes";
    public final static String CODIS_PROXY_NODES_KEY = "codisProxyNodes";
    public final static String CODIS_DASHBOARD_NODES_KEY = "codisDashboardNodes";

    /**
     * 主节点列表
     */
    public final static String MASTER_REDIS_SERVER_NODES = "masterRedisServerNodes";

    /**
     * 实例信息
     */
    public final static String APPID_KEY = "appId";
    public final static String AUDIT_ID_KEY = "auditId";
    public final static String HOST_KEY = "host";
    public final static String PORT_KEY = "port";
    public final static String VERSION_KEY = "db_version";
    public final static String MODULE_KEY = "moduleVersions";
    public final static String IS_CLUSTER_KEY = "is_cluster";

    public final static String MASTER_HOST_KEY = "master_host";
    public final static String MASTER_PORT_KEY = "master_port";
    public final static String SLAVE_MACHINE_KEY = "slave_machine";
    public final static String SLAVE_MACHINE_LIST_KEY = "slave_machine_list";
    public final static String MACHINE_IP_LIST_KEY = "machineIpList";
    public final static String USE_TYPE_KEY = "useType";
    public final static String EXAM_TYPE_KEY = "examType";
    public final static String USER_INFO_KEY = "user_info_key";

    /**
     * redis-port相关
     */
    public final static String HTTP_PORT_KEY = "http_port";
    public final static String SOURCE_HOST_KEY = "source_host";
    public final static String SOURCE_PORT_KEY = "source_port";
    public final static String SOURCE_PASSWORD_KEY = "source_password";
    public final static String TARGET_HOST_KEY = "target_host";
    public final static String TARGET_PORT_KEY = "target_port";
    public final static String TARGET_PASSWORD_KEY = "target_password";

    /**
     * 机器相关
     */
    public final static String CONTAINER_IP = "container_ip";

    /**
     * 资源信息
     */
    public final static String RESOURCE_ID = "resource_id";
    public final static String REPOSITORY_ID = "repository_id";

    /**
     * flush config
     */
    public final static String APP_IS_NEW_KEY = "appIsNew";

    /**
     * twemproxy
     */
    public final static String REDIS_SERVER_MACHINE_LIST_KEY = "redisServerMachineList";
    public final static String REDIS_SENTINEL_MACHINE_LIST_KEY = "redisSentinelMachineList";
    public final static String NUT_CRACKER_MACHINE_LIST_KEY = "nutCrackerMachineList";
    public final static String MASTER_PER_MACHINE_KEY = "masterPerMachine";
    public final static String SENTINEL_PER_MACHINE_KEY = "sentinelPerMachine";
    public final static String NUT_CRACKER_PER_MACHINE_KEY = "nutCrackerPerMachine";

    /**
     * pika
     */
    public final static String PIKA_MACHINE_LIST_KEY = "pikaMachineList";

    /**
     * for codis
     */
    public final static String CODIS_SERVER_MACHINE_LIST_KEY = "codisServerMachineList";
    public final static String CODIS_PRORY_MACHINE_LIST_KEY = "codisProxyMachineList";
    public final static String CODIS_DASHBOARD_MACHINE_LIST_KEY = "codisDashboardMachineList";
    public final static String CODIS_PROXY_PER_MACHINE_KEY = "codisProxyPerMachine";


    /**
     * redis sentinel quorum
     */
    public final static String REDIS_SENTINEL_QUORUM_KEY = "quorum";

    /**
     * redis server maxmemory
     */
    public final static String REDIS_SERVER_MAX_MEMORY_KEY = "maxMemory";

    /**
     * flush zk config taskId
     */
    public final static String FLUSH_ZK_CONFIG_TASKID_KEY = "flushZkConfigTaskId";


    /**
     * offline slave taskId
     */
    public final static String OFFLINE_SLAVE_TASKID_KEY = "offlineSlaveTaskId";

    /**
     * 是否需要刷新zk
     */
    public static final String IS_NEED_FLUSH_ZK_CONFIG_KEY = "isNeedFlushZkConfig";


    /**
     * stopRmtTaskId
     */
    public final static String STOP_RMT_TASKID_KEY = "stopRmtTaskId";

    /**
     * offlineSourceAppTaskId
     */
    public final static String OFFLINE_SOURCE_APP_TASKID_KEY = "offlineSourceAppTaskId";

    /**
     * 扩容
     */
    public final static String IS_SCALE_OUT_KEY = "isScaleOut";//是否是扩容
    public final static String IS_ONLY_MIGRATE_KEY = "isOnlyMigrate";//是否仅是迁移
    public final static String SOURCE_APP_ID_KEY = "sourceAppId";
    public final static String TARGET_APP_ID_KEY = "targetAppId";


    /**
     * redis server安装超时时间
     */
    public final static int REDIS_SERVER_INSTALL_TIMEOUT = 600;


    /**
     * pika安装超时时间
     */
    public final static int PIKA_INSTALL_TIMEOUT = 600;

    /**
     * codis server安装超时时间
     */
    public final static int CODIS_SERVER_INSTALL_TIMEOUT = 300;

    /**
     * redis server下线超时时间
     */
    public final static int REDIS_SERVER_OFFLINE_TIMEOUT = 300;

    /**
     * memcache下线超时时间
     */
    public final static int MEMCACHE_OFFLINE_TIMEOUT = 300;

    /**
     * pika下线超时时间
     */
    public final static int PIKA_OFFLINE_TIMEOUT = 300;

    /**
     * redis sentinel安装超时时间
     */
    public final static int REDIS_SENTINEL_INSTALL_TIMEOUT = 600;

    /**
     * redis sentinel下线超时时间
     */
    public final static int REDIS_SENTINEL_OFFLINE_TIMEOUT = 300;

    /**
     * nutcracker安装超时时间
     */
    public final static int NUT_CRACKER_INSTALL_TIMEOUT = 600;

    /**
     * codis proxy安装超时时间
     */
    public final static int CODIS_PROXY_INSTALL_TIMEOUT = 300;

    /**
     * codis dashboard安装超时时间
     */
    public final static int CODIS_DASHBOARD_INSTALL_TIMEOUT = 300;

    /**
     * nutcracker下线装超时时间
     */
    public final static int NUT_CRACKER_OFFLINE_TIMEOUT = 300;

    /**
     * redis port安装超时时间
     */
    public final static int REDIS_PORT_INSTALL_TIMEOUT = 300;

    /**
     * 刷zk配置超时
     */
    public final static int FLUSH_ZK_CONFIG_TIMEOUT = 300;

    /**
     * 下线slave超时
     */
    public final static int OFFLINE_SLAVE_TIMEOUT = 600;

    /**
     * rmt remove超时
     */
    public final static int RMT_REMOVE_TIMEOUT = 300;

    /**
     * rmt start超时
     */
    public final static int RMT_INSTALL_TIMEOUT = 300;


    /**
     * 应用下线超时
     */
    public final static int APP_OFFLINE_TIMEOUT = 600;

    /**
     * rmt同步超时时间
     */
    public final static int RMT_SYNC_TIMEOUT = 3600 * 4;

    /**
     * 等待proxy重启完成时间
     */
    public final static int NUTCRACKER_ALL_RESTART_TIMEOUT = 600;

    /**
     * redis server idle key分析超时时间
     */
    public final static int REDIS_SERVER_IDLE_KEY_ANALYSIS_TIMEOUT = 1800;

    /**
     * redis server key type分析超时时间
     */
    public final static int REDIS_SERVER_KEY_TYPE_ANALYSIS_TIMEOUT = 1800;

    /**
     * redis server key ttl分析超时时间
     */
    public final static int REDIS_SERVER_KEY_TTL_ANALYSIS_TIMEOUT = 1800;

    /**
     * redis server key value size分析超时时间
     */
    public final static int REDIS_SERVER_KEY_VALUE_SIZE_ANALYSIS_TIMEOUT = 1800;

    /**
     * redis server big key分析超时时间
     */
    public final static int REDIS_SERVER_BIG_KEY_ANALYSIS_TIMEOUT = 1800;

    /**
     * single rmt max used memory
     */
    public final static int SINGLE_RMT_USED_MEMORY_GB = 400;


    /**
     * max rmt count
     */
    public final static int MAX_RMT_COUNT = 3;


    /**
     * redis server big key分析超时时间
     */
    public final static int REDIS_SERVER_DIAGNOSTIC_TIMEOUT = 1800;

//	/**
//	 * pika实例安装class name
//	 */
//	public final static String PIKA_INSTANCE_INSTALL_CLASS = PikaInstanceInstallTask.class.getSimpleName();
//
//	/**
//	 * twemproxy pika实例安装class name
//	 */
//	public final static String TWEMPROXY_PIKA_OFFLINE_CLASS = TwemproxyPikaOfflineTask.class.getSimpleName();
//
//
    /**
     * redis server实例安装class name
     */
    public final static String REDIS_SERVER_INSTANCE_INSTALL_CLASS = RedisServerInstallTask.class.getSimpleName();

    /**
     * redis sentinel实例安装class name
     */
    public final static String REDIS_SENTINEL_INSTANCE_INSTALL_CLASS = RedisSentinelInstallTask.class.getSimpleName();

//	/**
//	 * redis port实例安装class name
//	 */
//	public final static String REDIS_PORT_INSTANCE_INSTALL_CLASS = RedisPortInstallTask.class.getSimpleName();
//
//	/**
//	 * redis migrate tool实例安装class name
//	 */
//	public final static String REDIS_MIGRATE_TOOL_INSTANCE_INSTALL_CLASS = RedisMigrateToolInstallTask.class.getSimpleName();
//
//	/**
//	 * nutcracker实例安装class name
//	 */
//	public final static String NUT_CRACKER_INSTANCE_INSTALL_CLASS = NutCrackerInstallTask.class.getSimpleName();
//
    /**
     * redis sentinel应用安装class name
     */
    public final static String REDIS_SENTINEL_APP_DEPLOY_CLASS = RedisSentinelAppDeployTask.class.getSimpleName();

    /**
     * redis cluster应用安装class name
     */
    public final static String REDIS_CLUSTER_APP_DEPLOY_CLASS = RedisClusterAppDeployTask.class.getSimpleName();

    /**
     * redis standalone应用安装class name
     */
    public final static String REDIS_STANDALONE_APP_DEPLOY_CLASS = RedisStandaloneAppDeployTask.class.getSimpleName();

//
//	/**
//	 * redis sentinel应用安装class name
//	 */
//	public final static String PIKA_SENTINEL_APP_INSTALL_CLASS = PikaSentinelInstallTask.class.getSimpleName();
//
//	/**
//	 * twemproxy应用安装class name
//	 */
//	public final static String TWEM_PROXY_APP_INSTALL_CLASS = TwemproxyAppInstallTask.class.getSimpleName();
//
//	/**
//	 * twemproxy pika应用安装class name
//	 */
//	public final static String TWEM_PROXY_PIKA_INSTALL_CLASS = TwemproxyPikaInstallTask.class.getSimpleName();
//
//	/**
//	 * nutcracker扩容
//	 */
//	public final static String NUT_CRACKER_SCALE_OUT_CLASS = NutCrackerScaleOutTask.class.getSimpleName();
//
//	/**
//	 * nutcracker下线
//	 */
//	public final static String NUT_CRACKER_LIST_OFFLINE_CLASS = NutCrackerListOfflineTask.class.getSimpleName();
//
//	/**
//	 * sentinel下线
//	 */
//	public final static String REDIS_SENTINEL_LIST_OFFLINE_CLASS = AppRedisSentinelOfflineTask.class.getSimpleName();
//
//	/**
//	 * redis slave server下线
//	 */
//	public final static String REDIS_SLAVE_SERVER_OFFLINE_CLASS = RedisSlaveServerOfflineTask.class.getSimpleName();
//
//	/**
//	 * pika slave下线
//	 */
//	public final static String PIKA_SLAVE_OFFLINE_CLASS = PikaSlaveOfflineTask.class.getSimpleName();
//
//	/**
//	 * 添加redis sentinel
//	 */
//	public final static String REDIS_SENTINEL_ADD_CLASS = RedisSentinelAddTask.class.getSimpleName();
//
//	/**
//	 * 应用刷新配置
//	 */
//	public final static String APP_CONFIG_FLUSH_ZK_CLASS = AppConfigFlushZkTask.class.getSimpleName();
//
//	/**
//	 * twemproxy -> twemproxy
//	 */
//	public final static String TWEMPROXY_TO_TWEMPROXY_CLASS = TwemproxyToTwemproxyTask.class.getSimpleName();
//
//	/**
//	 * twemproxy -> twemproxy(v2)
//	 */
//	public final static String TWEMPROXY_TO_TWEMPROXY_V2_CLASS = TwemproxyToTwemproxyTaskV2.class.getSimpleName();
//
//	/**
//	 * 删除redis migrate tool
//	 */
//	public final static String REDIS_MIGRATE_TOOL_REMOVE_CLASS = RedisMigrateToolRemoveTask.class.getSimpleName();
//
//
//	/**
//	 * flush redis server data
//	 */
//	public final static String REDIS_SERVER_FLUSH_CLASS = RedisServerFlushDataTask.class.getSimpleName();
//
//	/**
//	 * analysis redis server idle key
//	 */
//	public final static String REDIS_SERVER_IDLE_KEY_ANALYSIS_CLASS = RedisServerIdleKeyAnalysisTask.class.getSimpleName();
//
//	/**
//	 * analysis redis server key type
//	 */
//	public final static String REDIS_SERVER_KEY_TYPE_ANALYSIS_CLASS = RedisServerKeyTypeAnalysisTask.class.getSimpleName();
//
//	/**
//	 * analysis redis server key ttl
//	 */
//	public final static String REDIS_SERVER_KEY_TTL_ANALYSIS_CLASS = RedisServerKeyTtlAnalysisTask.class.getSimpleName();
//
//	/**
//	 * analysis redis server key value size
//	 */
//	public final static String REDIS_SERVER_KEY_VALUE_SIZE_ANALYSIS_CLASS = RedisServerKeyValueAnalysisTask.class.getSimpleName();
//
//
//
//	/**
//	 * analysis twemproxy key
//	 */
//	public final static String TWEMPROXY_KEY_ANALYSIS_CLASS = TwemproxyKeyAnalysisTask.class.getSimpleName();
//
//	/**
//	 * twemproxy flushall data
//	 */
//	public final static String TWEMPROXY_FLUSHALL_DATA_CLASS = TwemproxyFlushDataTask.class.getSimpleName();
//
//	/**
//	 * twemproxy pika flushall data
//	 */
//	public final static String TWEMPROXY_PIKA_FLUSHALL_DATA_CLASS = TwemproxyPikaFlushDataTask.class.getSimpleName();
//
//	/**
//	 * stop redis server
//	 */
//	public final static String REDIS_SERVER_STOP_CLASS = RedisServerStopTask.class.getSimpleName();
//
//	/**
//	 * stop memcache
//	 */
//	public final static String MEMCACHE_STOP_CLASS = MemcacheStopTask.class.getSimpleName();
//
//	/**
//	 * offline memcache
//	 */
//	public final static String MEMCACHE_CLUSTER_OFFLINE_CLASS = MemcacheClusterOfflineTask.class.getSimpleName();
//
//	/**
//	 * stop redis server
//	 */
//	public final static String REDIS_SERVER_START_CLASS = RedisServerStartTask.class.getSimpleName();
//
//	/**
//	 * stop pika
//	 */
//	public final static String PIKA_STOP_CLASS = PikaStopTask.class.getSimpleName();
//
//	/**
//	 * stop redis sentinel
//	 */
//	public final static String REDIS_SENTINEL_STOP_CLASS = RedisSentinelStopTask.class.getSimpleName();
//
//	/**
//	 * stop nutcracker
//	 */
//	public final static String NUT_CRACKER_STOP_CLASS = NutCrackerStopTask.class.getSimpleName();
//
//	/**
//	 * 下线twemproxy
//	 */
//	public final static String TWEMPROXY_OFFLINE_CLASS = TwemproxyOfflineTask.class.getSimpleName();
//
//	/**
//	 * 下线redis sentinel app
//	 */
//	public final static String REDIS_SENTINEL_APP_OFFLINE_CLASS = RedisSentinelAppOfflineTask.class.getSimpleName();
//
//	/**
//	 * 下线pika sentinel app
//	 */
//	public final static String PIKA_SENTINEL_APP_OFFLINE_CLASS = PikaSentinelAppOfflineTask.class.getSimpleName();
//
//	/**
//	 * 备库重搭redis
//	 */
//	public final static String REDIS_SLAVE_SERVER_REBUILD_CLASS = RedisSlaveServerRebuildTask.class.getSimpleName();
//
//	/**
//	 * 备库重搭pika
//	 */
//	public final static String PIKA_SLAVE_REBUILD_CLASS = PikaSlaveRebuildTask.class.getSimpleName();
//
//	/**
//	 * sentinel failover
//	 */
//	public final static String REDIS_SENTINEL_FAILOVER_CLASS = RedisSentinelFailoverTask.class.getSimpleName();
//
//	/**
//	 * machine slave rebuild
//	 */
//	public final static String MACHINE_SLAVE_REBUILD_CLASS = MachineSlaveRebuildTask.class.getSimpleName();
//
//	/**
//	 * twemproxy fault machine failover
//	 */
//	public final static String TWEMPROXY_FAULT_MACHINE_FAILOVER_CLASS = TwemproxyFaultMachineFailoverTask.class.getSimpleName();
//
//
//	/**
//	 * codis server实例安装class name
//	 */
//	public final static String CODIS_SERVER_INSTANCE_INSTALL_CLASS = CodisServerInstallTask.class.getSimpleName();
//
//	/**
//	 * codis proxy实例安装class name
//	 */
//	public final static String CODIS_PROXY_INSTANCE_INSTALL_CLASS = CodisProxyInstallTask.class.getSimpleName();
//
//
//	/**
//	 * codis dashboard实例安装class name
//	 */
//	public final static String CODIS_DASHBOARD_INSTANCE_INSTALL_CLASS = CodisDashboardInstallTask.class.getSimpleName();

    /**
     * 应用拓扑故障检查class name
     */
    public final static String TOPOLOGY_EXAM_CLASS = TopologyExamTask.class.getSimpleName();

    /**
     * 机器使用情况检查 class name
     */
    public final static String MACHINE_EXAM_CLASS = MachineExamTask.class.getSimpleName();
    /**
     * 机器同步数据任务 className
     */
    public final static String MACHINE_SYNC_CLASS = MachineSyncTask.class.getSimpleName();

    public final static String PACK_COMPILE_TASK = PackCompileTask.class.getSimpleName();

    /**
     * 最大内存限制(MB)
     */
    public final static int MAX_MEMORY_LIMIT = 1024 * 10;

    /**
     * 一次分配每个机器最大rediserver实例数
     */
    public final static int MAX_MASTER_PER_MACHINE = 50;

    /**
     * 一份分配每个机器最大nutcracker数
     */
    public final static int MAX_NUT_CRACK_PER_MACHINE = 50;

    /**
     * 一份分配每个机器最大codis proxy数
     */
    public final static int MAX_CODIS_PROXY_PER_MACHINE = 15;

}
