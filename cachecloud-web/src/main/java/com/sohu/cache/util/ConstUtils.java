package com.sohu.cache.util;

import com.sohu.cache.web.enums.SshAuthTypeEnum;

import java.util.List;

/**
 * cachecloud常量
 */
public class ConstUtils {
    public static final String NOTICE_TITLE = "【CacheCloud】状态通知";

    /**
     * 应用类型区分
     * 2: Redis Cluster
     * 6: Redis Standalone
     * 5: Redis+Sentinel
     * 7: Redis+Twemproxy
     * 8: Pika+Sentinel
     * 9: Pika+Twemproxy
     */
    public static final int CACHE_TYPE_REDIS_CLUSTER = 2;
    public static final int CACHE_REDIS_SENTINEL = 5;
    public static final int CACHE_REDIS_STANDALONE = 6;
    public static final int CACHE_REDIS_TWEMPROXY = 7;
    public static final int CACHE_PIKA_SENTINEL = 8;
    public static final int CACHE_PIKA_TWEMPROXY = 9;

    // 数据源名称
    public static final String REDIS = "redis";
    public static final String MACHINE = "machine";

    //mysql收集数据的时间字段
    public static final String COLLECT_TIME = "CollectTime";

    // 容量转换
    public static final int _1024 = 1024;

    // 表示空字符串
    public static final String EMPTY = "";

    //SSO clientId
    public static final String SSO_CLIENT = "clientId";

    /**
     * 服务端版本
     */
    public static final String CACHECLOUD_VERSION = "3.2.0";
    /**
     * 逗号
     */
    public static final String COMMA = ",";
    /**
     * 换行
     */
    public static final String NEXT_LINE = "\n";
    /**
     * 空格
     */
    public static final String SPACE = " ";
    /**
     * 冒号
     */
    public static final String COLON = ":";
    /**
     * 井号
     */
    public static final String POUND = "#";
    /**
     * 分号
     */
    public static final String SEMICOLON = ";";
    /**
     * at @
     */
    public static final String AT = "@";
    /**
     * 内部错误
     */
    public static final String INNER_ERROR = "cachecloud_inner_error";
    /**
     * 登录跳转参数
     */
    public final static String RREDIRECT_URL_PARAM = "redirectUrl";
    /**
     * redis默认启动路径
     */
    public final static String REDIS_DEFAULT_DIR = "/opt/cachecloud/redis";
    /**
     * redis安装基准目录
     */
    public static final String REDIS_INSTALL_BASE_DIR = "/opt/cachecloud";
    public static final String REDIS_COMPILE_BASE_DIR = "/opt/cachecloud/compile/";
    /**
     * pika 安装基准目录,挂载ssd
     */
    public static final String PIKA_INSTALL_BASE_DIR = "/opt/cachecloud/pika";
    /**
     * nutcracker当期版本
     */
    public static final String DEFAULT_NUTCRACKER_CUR_VERSION = "0.4.1";
    /**
     * redis migrate tool当期版本
     */
    public static final String DEFAULT_RMT_CUR_VERSION = "1.0";
    /**
     * codis当期版本
     */
    public static final String DEFAULT_CODIS_CUR_VERSION = "3.2.2";
    /**
     * pika当期版本
     */
    public static final String DEFAULT_PIKA_CUR_VERSION = "3.0.3";
    /**
     * slave相对于master节点端口增值
     */
    public static final int SLAVE_PORT_INCREASE = 1000;
    /**
     * 机器报警阀值
     */
    public static final double DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD = 85.0;
    /**
     * 应用客户端连接数报警阀值
     */
    public static final int DEFAULT_APP_CLIENT_CONN_THRESHOLD = 2000;
    /**
     * 机器统一的用户名、密码、端口
     */
    public static final String DEFAULT_USERNAME = "cachecloud";
    public static final String DEFAULT_PASSWORD = "cachecloud";
    public static final String DEFAULT_USER_LOGIN_ENCRY_KEY = "97c9d9de0a2dbd64";
    public static final String DEFAULT_USER_PASSWORD = "89750bfb62c09ba4f7dfe8b7e45ca31f";
    public static final int DEFAULT_SSH_PORT_DEFAULT = 22;
    /**
     * ssh授权方式：参考SshAuthTypeEnum
     */
    public static final int DEFAULT_SSH_AUTH_TYPE = SshAuthTypeEnum.PUBLIC_KEY.getValue();
    /**
     * public key pem
     */
    public static final String DEFAULT_PUBLIC_KEY_PEM = "/opt/ssh/id_rsa";
    public static final String DEFAULT_PUBLIC_USERNAME = "cachecloud";
    public static final String MEMCACHE_USER = "memcached_server";
    public static final String MEMCACHE_KEY_PEM = "/home/redis_server/cachecloud/cachecloud/memcache_server_key/id_rsa";
    /**
     * module info
     */
    public static final String MODULE_BASE_PATH = "/opt/cachecloud/module/";

    /**
     * 管理员相关
     */
    public static final String DEFAULT_SUPER_ADMIN_NAME = "admin";
    public static final String DEFAULT_SUPER_ADMIN_PASS = "admin";
    public static final String DEFAULT_SUPER_ADMINS = "admin";

    /**
     * redis-migrate-tool端口
     */
    public static final int DEFAULT_REDIS_MIGRATE_TOOL_PORT = 8888;
    /**
     * redis server基准端口
     */
    public static final int DEFAULT_REDIS_SERVER_BASE_PORT = 6400;
    /**
     * redis sentinel基准端口
     */
    public static final int DEFAULT_REDIS_SENTINEL_BASE_PORT = 7500;
    /**
     * 1是session,2是cookie(参考UserLoginTypeEnum)
     */
    public static final int DEFAULT_USER_LOGIN_TYPE = 2;
    /**
     * cachecloud根目录，这个要与cachecloud-init.sh脚本中的目录一致
     */
    public static final String DEFAULT_CACHECLOUD_BASE_DIR = "/opt";
    /**
     * 是否定期清理各种统计数据：(详见CleanUpStatisticsJob)
     */
    public static final boolean DEFAULT_WHETHER_SCHEDULE_CLEAN_DATA = true;
    /**
     * appkey秘钥
     */
    public static final String DEFAULT_APP_SECRET_BASE_KEY = "cachecloud-2014";
    /**
     * 机器性能统计周期(分钟)
     */
    public static final int DEFAULT_MACHINE_STATS_CRON_MINUTE = 1;
    /**
     * big key阈值
     */
    public static final int DEFAULT_STRING_MAX_LENGTH = 100 * 1024;
    public static final int DEFAULT_HASH_MAX_LENGTH = 50000;
    public static final int DEFAULT_LIST_MAX_LENGTH = 50000;
    public static final int DEFAULT_SET_MAX_LENGTH = 50000;
    public static final int DEFAULT_ZSET_MAX_LENGTH = 50000;
    public static double MEMORY_USAGE_RATIO_THRESHOLD = DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD;
    public static int APP_CLIENT_CONN_THRESHOLD = DEFAULT_APP_CLIENT_CONN_THRESHOLD;
    public static String USERNAME = DEFAULT_USERNAME;
    public static String PASSWORD = DEFAULT_PASSWORD;
    public static int SSH_PORT_DEFAULT = DEFAULT_SSH_PORT_DEFAULT;
    public static int SSH_AUTH_TYPE = DEFAULT_SSH_AUTH_TYPE;
    public static String PUBLIC_KEY_PEM = DEFAULT_PUBLIC_KEY_PEM;
    public static String PUBLIC_USERNAME = DEFAULT_PUBLIC_USERNAME;
    public static String SUPER_ADMIN_NAME = DEFAULT_SUPER_ADMIN_NAME;
    public static String SUPER_ADMIN_PASS = DEFAULT_SUPER_ADMIN_PASS;
    public static String SUPER_ADMINS = DEFAULT_SUPER_ADMINS;
    public static String USER_LOGIN_ENCRY_KEY = DEFAULT_USER_LOGIN_ENCRY_KEY;
    public static List<String> SUPER_MANAGER;
    /**
     * 联系人
     */
    public static String CONTACT;

    /**
     * redis-migrate-tool相关路径
     */
    public static String REDIS_MIGRATE_TOOL_HOME;
    /**
     * redis-shake相关路径
     */
    public static String REDIS_SHAKE_HOME;
    /**
     * redis-full-check相关
     */
    public static String REDIS_FULL_CHECK_HOME;
    public static int REDIS_MIGRATE_TOOL_PORT = DEFAULT_REDIS_MIGRATE_TOOL_PORT;
    public static int REDIS_SERVER_BASE_PORT = DEFAULT_REDIS_SERVER_BASE_PORT;
    public static int REDIS_SENTINEL_BASE_PORT = DEFAULT_REDIS_SENTINEL_BASE_PORT;
    public static int USER_LOGIN_TYPE = DEFAULT_USER_LOGIN_TYPE;
    /**
     * cookie登录方式所需要的域
     */
    public static String CACHECLOUD_BASE_DIR = DEFAULT_CACHECLOUD_BASE_DIR;
    public static boolean WHETHER_SCHEDULE_CLEAN_DATA = DEFAULT_WHETHER_SCHEDULE_CLEAN_DATA;
    public static String APP_SECRET_BASE_KEY = DEFAULT_APP_SECRET_BASE_KEY;
    public static int MACHINE_STATS_CRON_MINUTE = DEFAULT_MACHINE_STATS_CRON_MINUTE;
    /**
     * 领导邮件
     */
    public static List<String> LEADER_EMAIL_LIST;
    public static String NUTCRACKER_CUR_VERSION = DEFAULT_NUTCRACKER_CUR_VERSION;
    public static String RMT_CUR_VERSION = DEFAULT_RMT_CUR_VERSION;
    public static String CODIS_CUR_VERSION = DEFAULT_CODIS_CUR_VERSION;
    public static String PIKA_CUR_VERSION = DEFAULT_PIKA_CUR_VERSION;
    /**
     * 内部redis配置
     */
    public static volatile String CACHECLOUD_INTERNAL_REDIS_SENTINELS = "";
    public static volatile String CACHECLOUD_INTERNAL_REDIS_MASTERNAME = "";
    public static volatile String CACHECLOUD_INTERNAL_REDIS_PASSWORD = "";
    public static int STRING_MAX_LENGTH = DEFAULT_STRING_MAX_LENGTH;
    public static int HASH_MAX_LENGTH = DEFAULT_HASH_MAX_LENGTH;
    public static int LIST_MAX_LENGTH = DEFAULT_LIST_MAX_LENGTH;
    public static int SET_MAX_LENGTH = DEFAULT_SET_MAX_LENGTH;
    public static int ZSET_MAX_LENGTH = DEFAULT_ZSET_MAX_LENGTH;
    public static int SSH_CONNECTION_TIMEOUT = 5000;

    public static String getRedisMigrateToolCmd(String name) {
        return ConstUtils.getRedisToolDir(name) + "src/redis-migrate-tool";
    }

    public static String getRedisShakeStartCmd() {
        return "sh " + REDIS_SHAKE_HOME + "start.sh";
    }

    public static String getRedisShakeLinuxCmd(String name) {
        return ConstUtils.getRedisToolDir(name) + "redis-shake.linux";
    }

    public static String getRedisShakeStopCmd() {
        return "sh " + REDIS_SHAKE_HOME + "stop.sh";
    }

    public static String getRedisFullCheckDir() {
        return REDIS_FULL_CHECK_HOME;
    }

    public static String getRedisFullCheckResultDir() {
        return REDIS_FULL_CHECK_HOME + "data/";
    }

    public static String getRedisFullCheckMetricDir() {
        return REDIS_FULL_CHECK_HOME + "metric/";
    }

    public static String getRedisFullCheckLogDir() {
        return REDIS_FULL_CHECK_HOME + "logs/";
    }

    public static String getRedisFullCheckCmd() {
        return REDIS_FULL_CHECK_HOME + "redis-full-check";
    }

    public static String getRedisMigrateToolDir() {
        return REDIS_MIGRATE_TOOL_HOME + "data/";
    }

    public static String getRedisShakeDir() {
        return REDIS_SHAKE_HOME;
    }

    public static String getRedisShakePidDir(String name) {
        return ConstUtils.getRedisToolDir(name) + "pid/";
    }

    public static String getRedisShakeLogsDir(String name) {
        return ConstUtils.getRedisToolDir(name) + "logs/";
    }

    public static String getRedisShakeConfDir(String name) {
        return ConstUtils.getRedisToolDir(name) + "conf/";
    }

    public static String getRedisShakeHttpPort() {
        return "9320";
    }

    /**
     * 空闲key
     *
     * @param appId
     * @param auditId
     * @return
     */
    public static String getRedisServerIdleKey(long appId, long auditId) {
        return String.format("cc:key:idle:%s:%s", appId, auditId);
    }

    /**
     * key类型
     *
     * @param appId
     * @param auditId
     * @return
     */
    public static String getRedisServerTypeKey(long appId, long auditId) {
        return String.format("cc:key:type:%s:%s", appId, auditId);
    }

    /**
     * key ttl
     *
     * @param appId
     * @param auditId
     * @return
     */
    public static String getRedisServerTtlKey(long appId, long auditId) {
        return String.format("cc:key:ttl:%s:%s", appId, auditId);
    }

    /**
     * value size
     *
     * @param appId
     * @param auditId
     * @return
     */
    public static String getRedisServerValueSizeKey(long appId, long auditId) {
        return String.format("cc:key:valueSize:%s:%s", appId, auditId);
    }

    /**
     * 生成taskflowId
     *
     * @param taskFlowId
     * @return
     */
    public static String getTaskFlowRedisKey(String taskFlowId) {
        return "cc:taskflow:" + taskFlowId;
    }


    public static String getInstanceScanKey(long taskId, String hostPort) {
        return String.format("cc:key:scan:%s:%s", taskId, hostPort);
    }

    public static String getInstanceDelKey(long taskId, String hostPort) {
        return String.format("cc:key:del:%s:%s", taskId, hostPort);
    }

    public static String getInstanceBigKey(long taskId, String hostPort) {
        return String.format("cc:key:bigkey:%s:%s", taskId, hostPort);
    }

    public static String getInstanceIdleKey(long taskId, String hostPort) {
        return String.format("cc:key:idlekey:%s:%s", taskId, hostPort);
    }

    public static String getInstanceHotKey(long taskId, String hostPort) {
        return String.format("cc:key:hotkey:%s:%s", taskId, hostPort);
    }

    public static String getInstanceSlotAnalysis(long taskId, String hostPort) {
        return String.format("cc:key:slotAnalysis:%s:%s", taskId, hostPort);
    }

    public static String getInstanceScanClean(long taskId, String hostPort) {
        return String.format("cc:key:scanClean:%s:%s", taskId, hostPort);
    }

    public static String getScanClean(long taskId) {
        return String.format("cc:key:scanClean:%s", taskId);
    }

    public static String getRedisDir(String versionName){
        return String.format("%s/%s", REDIS_INSTALL_BASE_DIR, versionName);
    }

    public static String getRedisToolDir(String versionName){
        return String.format("%s/%s/", REDIS_INSTALL_BASE_DIR, versionName);
    }
}


