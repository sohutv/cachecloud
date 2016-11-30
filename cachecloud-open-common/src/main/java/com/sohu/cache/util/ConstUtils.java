package com.sohu.cache.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * cachecloud常量
 * @author leifu
 * @Date 2016年3月1日
 * @Time 下午12:54:45
 */
public class ConstUtils {
    // cache的类型区分
    public static final int CACHE_TYPE_REDIS_CLUSTER = 2;
    public static final int CACHE_REDIS_SENTINEL = 5;
    public static final int CACHE_REDIS_STANDALONE = 6;

    // 数据源名称
    public static final String REDIS = "redis";
    public static final String MACHINE = "machine";

    // redis job/trigger name/group
    public static final String REDIS_JOB_NAME = "redisJob";
    public static final String REDIS_JOB_GROUP = "redis";
    public static final String REDIS_TRIGGER_GROUP = "redis-";

    // machine job/trigger name/group
    public static final String MACHINE_JOB_NAME = "machineJob";
    public static final String MACHINE_JOB_GROUP = "machine";
    public static final String MACHINE_TRIGGER_GROUP = "machine-";

    // machine monitor job/trigger name/group
    public static final String MACHINE_MONITOR_JOB_NAME = "machineMonitorJob";
    public static final String MACHINE_MONITOR_JOB_GROUP = "machineMonitor";
    public static final String MACHINE_MONITOR_TRIGGER_GROUP = "machineMonitor-";

    
    // redis-slowlog job/trigger name/group
    public static final String REDIS_SLOWLOG_JOB_NAME = "redisSlowLogJob";
    public static final String REDIS_SLOWLOG_JOB_GROUP = "redisSlowLog";
    public static final String REDIS_SLOWLOG_TRIGGER_GROUP = "redisSlowLog-";
    
    // 创建trigger时，dataMap的数据key
    public static final String HOST_KEY = "host_key";
    public static final String PORT_KEY = "port_key";
    public static final String APP_KEY = "app_key";
    public static final String HOST_ID_KEY = "host_id_key";
    // server job/trigger name/group
    public static final String SERVER_JOB_NAME = "serverJob";
    public static final String SERVER_JOB_GROUP = "server";
    public static final String SERVER_TRIGGER_GROUP = "server-";

    //mysql收集数据的时间字段
    public static final String COLLECT_TIME = "CollectTime";

    // 触发时间
    public static final String TRIGGER_TIME_KEY = "trigger_time_key";

    // 容量转换
    public static final int _1024 = 1024;
    
    // 表示空字符串
    public static final String EMPTY = "";
    
    /**
     * 服务端版本
     */
    public static final String CACHECLOUD_VERSION = "master";

    /**
     * 机器报警阀值
     */
    public static double DEFAULT_CPU_USAGE_RATIO_THRESHOLD = 80.0;
    public static double CPU_USAGE_RATIO_THRESHOLD = DEFAULT_CPU_USAGE_RATIO_THRESHOLD;

    
    public static double DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD = 80.0;
    public static double MEMORY_USAGE_RATIO_THRESHOLD = DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD;

    public static double DEFAULT_LOAD_THRESHOLD = 8.0;
    public static double LOAD_THRESHOLD = DEFAULT_LOAD_THRESHOLD;

    /**
     * 应用客户端连接数报警阀值 
     */
    public static int DEFAULT_APP_CLIENT_CONN_THRESHOLD = 2000;
    public static int APP_CLIENT_CONN_THRESHOLD = DEFAULT_APP_CLIENT_CONN_THRESHOLD;

    /**
     * 机器统一的用户名、密码、端口
     */
    public static String DEFAULT_USERNAME = "cachecloud";
    public static String USERNAME = DEFAULT_USERNAME;

    public static String DEFAULT_PASSWORD = "cachecloud";
    public static String PASSWORD = DEFAULT_PASSWORD;

    public static int DEFAULT_SSH_PORT_DEFAULT = 22;
    public static int SSH_PORT_DEFAULT = DEFAULT_SSH_PORT_DEFAULT;


    /**
     * 管理员相关
     */
    public static String DEFAULT_SUPER_ADMIN_NAME = "admin";
    public static String SUPER_ADMIN_NAME = DEFAULT_SUPER_ADMIN_NAME;
    
    public static String DEFAULT_SUPER_ADMIN_PASS = "admin";
    public static String SUPER_ADMIN_PASS = DEFAULT_SUPER_ADMIN_PASS;
    
    public static String DEFAULT_SUPER_ADMINS="admin";
    public static String SUPER_ADMINS = DEFAULT_SUPER_ADMINS;
    
    public static List<String> SUPER_MANAGER;
    
    /**
     * ldap登陆
     */
    public static String DEFAULT_LDAP_URL = EMPTY;
    public static String LDAP_URL = DEFAULT_LDAP_URL;
    
    /**
     * 登陆邮箱后缀
     */
    public static String EMAIL_SUFFIX = "";
    
    /**
     * 是否为调试
     */
    public static boolean IS_DEBUG;
    
    /**
     * 联系人
     */
    public static String DEFAULT_CONTACT = "user1:(xx@zz.com, user1:135xxxxxxxx)<br/>user2: (user2@zz.com, user2:138xxxxxxxx)";
    public static String CONTACT = DEFAULT_CONTACT;

    
    /**
     * 文档地址
     */
    public static String DEFAULT_DOCUMENT_URL = "http://cachecloud.github.io";
    public static String DOCUMENT_URL = DEFAULT_DOCUMENT_URL;
    
    /**
     * 报警相关
     */
    public static String DEFAULT_EMAILS = "xx@sohu.com,yy@qq.com";
    public static String EMAILS = DEFAULT_EMAILS;

    public static String DEFAULT_PHONES = "13812345678,13787654321";
    public static String PHONES = DEFAULT_PHONES;

    /**
     * 邮箱报警接口
     */
    public static String DEFAULT_EMAIL_ALERT_INTERFACE = EMPTY;
    public static String EMAIL_ALERT_INTERFACE = DEFAULT_EMAIL_ALERT_INTERFACE;
    
    /**
     * 短信报警接口
     */
    public static String DEFAULT_MOBILE_ALERT_INTERFACE = EMPTY;
    public static String MOBILE_ALERT_INTERFACE = DEFAULT_MOBILE_ALERT_INTERFACE;
    
    /**
     * maven仓库地址
     */
    public static String DEFAULT_MAVEN_WAREHOUSE = "http://your_maven_house";
    public static String MAVEN_WAREHOUSE = DEFAULT_MAVEN_WAREHOUSE;
    
    
    /**
     * 客户端可用版本
     */
    public static String DEFAULT_GOOD_CLIENT_VERSIONS = "1.0-SNAPSHOT";
    public static String GOOD_CLIENT_VERSIONS = DEFAULT_GOOD_CLIENT_VERSIONS;

    /**
     * 客户端警告版本
     */
    public static String DEFAULT_WARN_CLIENT_VERSIONS = "0.1";
    public static String WARN_CLIENT_VERSIONS = DEFAULT_WARN_CLIENT_VERSIONS;
    
    
    /**
     * 客户端错误版本
     */
    public static String DEFAULT_ERROR_CLIENT_VERSIONS = "0.0";
    public static String ERROR_CLIENT_VERSIONS = DEFAULT_ERROR_CLIENT_VERSIONS;

    /**
     * redis-migrate-tool相关路径
     */
    public static String DEFAULT_REDIS_MIGRATE_TOOL_HOME = "/opt/cachecloud/redis-migrate-tool/";
    public static String REDIS_MIGRATE_TOOL_HOME = DEFAULT_REDIS_MIGRATE_TOOL_HOME;

    public static String getRedisMigrateToolCmd() {
        return REDIS_MIGRATE_TOOL_HOME + "src/redis-migrate-tool";
    }
    
    public static String getRedisMigrateToolDir() {
        return REDIS_MIGRATE_TOOL_HOME + "data/";
    }
    
    /**
     * redis-migrate-tool端口
     */
    public static int REDIS_MIGRATE_TOOL_PORT = 8888;
    
    /**
     * 1是session,2是cookie(参考UserLoginTypeEnum)
     */
    public static int DEFAULT_USER_LOGIN_TYPE = 1;
    public static int USER_LOGIN_TYPE = DEFAULT_USER_LOGIN_TYPE;
    
    /**
     * cookie登录方式所需要的域
     */
    public static String DEFAULT_COOKIE_DOMAIN = EMPTY;
    public static String COOKIE_DOMAIN = DEFAULT_COOKIE_DOMAIN;
    
    /**
     * cachecloud根目录，这个要与cachecloud-init.sh脚本中的目录一致
     */
    public static String DEFAULT_CACHECLOUD_BASE_DIR = "/opt";
    public static String CACHECLOUD_BASE_DIR = DEFAULT_CACHECLOUD_BASE_DIR;
    
    /**
     * 是否定期清理各种统计数据：(详见CleanUpStatisticsJob)
     */
    public static boolean DEFAULT_WHETHER_SCHEDULE_CLEAN_DATA = false;
    public static boolean WHETHER_SCHEDULE_CLEAN_DATA = DEFAULT_WHETHER_SCHEDULE_CLEAN_DATA;
    
    
    /**
     * appkey秘钥
     */
    public static String DEFAULT_APP_SECRET_BASE_KEY = "cachecloud-2014";
    public static String APP_SECRET_BASE_KEY = DEFAULT_APP_SECRET_BASE_KEY;
    
    
    /**
     * 机器性能统计周期(分钟)
     */
    public static int DEFAULT_MACHINE_STATS_CRON_MINUTE = 1;
    public static int MACHINE_STATS_CRON_MINUTE = DEFAULT_MACHINE_STATS_CRON_MINUTE;
    
    
    /**
     * 网站域名
     */
    public static final String CC_DOMAIN = "http://your.domain.com";
    
    /**
     * 领导邮件
     */
    public static List<String> LEADER_EMAIL_LIST = new ArrayList<String>();
    static {
    }
    
    static {
        ResourceBundle applicationResourceBundle = ResourceBundle.getBundle("application");
        IS_DEBUG = "true".equals(applicationResourceBundle.getString("isDebug"));
    }
    
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
     * 内部错误
     */
    public static final String INNER_ERROR = "cachecloud_inner_error";
    
    /**
     * 登录跳转参数
     */
    public final static String RREDIRECT_URL_PARAM = "redirectUrl";

}


