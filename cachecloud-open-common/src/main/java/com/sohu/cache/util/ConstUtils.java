package com.sohu.cache.util;

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

    //mysql收集数据的时间字段
    public static final String COLLECT_TIME = "CollectTime";

    // 触发时间
    public static final String TRIGGER_TIME_KEY = "trigger_time_key";

    // 容量转换
    public static final int _1024 = 1024;

    /**
     * 机器报警阀值
     */
    public static double CPU_USAGE_RATIO_THRESHOLD;
    public static double DEFAULT_CPU_USAGE_RATIO_THRESHOLD = 80.0;

    
    public static double MEMORY_USAGE_RATIO_THRESHOLD;
    public static double DEFAULT_MEMORY_USAGE_RATIO_THRESHOLD = 80.0;

    
    public static double LOAD_THRESHOLD;
    public static double DEFAULT_LOAD_THRESHOLD = 8.0;


    /**
     * 机器统一的用户名、密码、端口
     */
    public static String USERNAME;
    public static String DEFAULT_USERNAME = "cachecloud";

    public static String PASSWORD;
    public static String DEFAULT_PASSWORD = "cachecloud";

    public static int SSH_PORT_DEFAULT;
    public static int DEFAULT_SSH_PORT_DEFAULT = 22;


    /**
     * 管理员相关
     */
    public static String SUPER_ADMIN_NAME;
    public static String DEFAULT_SUPER_ADMIN_NAME = "admin";
    
    public static String SUPER_ADMIN_PASS;
    public static String DEFAULT_SUPER_ADMIN_PASS = "admin";
    
    public static String SUPER_ADMINS;
    public static String DEFAULT_SUPER_ADMINS="admin";
    
    public static List<String> SUPER_MANAGER;
    
    /**
     * 是否为调试
     */
    public static boolean IS_DEBUG;
    
    /**
     * 联系人
     */
    public static String CONTACT;
    public static String DEFAULT_CONTACT = "user1:(xx@zz.com, user1:135xxxxxxxx)<br/>user2: (user2@zz.com, user2:138xxxxxxxx)";

    
    /**
     * 文档地址
     */
    public static String DOCUMENT_URL;
    public static String DEFAULT_DOCUMENT_URL = "http://cachecloud.github.io";
    
    /**
     * 报警相关
     */
    public static String EMAILS;
    public static String DEFAULT_EMAILS = "xx@sohu.com,yy@qq.com";

    
    public static String PHONES;
    public static String DEFAULT_PHONES = "13812345678,13787654321";

    
    /**
     * maven仓库地址
     */
    public static String MAVEN_WAREHOUSE;
    public static String DEFAULT_MAVEN_WAREHOUSE = "http://your_maven_house";
    
    
    /**
     * 客户端可用版本
     */
    public static String GOOD_CLIENT_VERSIONS;
    public static String DEFAULT_GOOD_CLIENT_VERSIONS = "1.0-SNAPSHOT";

    /**
     * 客户端警告版本
     */
    public static String WARN_CLIENT_VERSIONS;
    public static String DEFAULT_WARN_CLIENT_VERSIONS = "0.1";
    
    
    /**
     * 客户端错误版本
     */
    public static String ERROR_CLIENT_VERSIONS;
    public static String DEFAULT_ERROR_CLIENT_VERSIONS = "0.0";

    
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
    

}


