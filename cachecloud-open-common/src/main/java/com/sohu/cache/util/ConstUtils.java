package com.sohu.cache.util;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * cachecloud常量
 * @author leifu
 * @Date 2016年3月1日
 * @Time 下午1:04:14
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

    // 机器报警阀值，应该放到数据库中，供动态调整
    public static double CPU_USAGE_RATIO_THRESHOLD = 80.0;
    public static double MEMORY_USAGE_RATIO_THRESHOLD = 80.0;
    public static double LOAD_THRESHOLD = 7.5;

    /**
     * 机器统一的用户名、密码
     */
    public static String USERNAME;
    public static String PASSWORD;
    
    static {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
        USERNAME = resourceBundle.getString("shell.auth.simple.user.name");
        PASSWORD = resourceBundle.getString("shell.auth.simple.user.password");
    }
    
    public static Set<String> SUPER_MANAGER = new HashSet<String>();

    static {
        SUPER_MANAGER.add("admin");
    }
    
    /**
     * cachecloud普通用户登录session
     */
    public static final String LOGIN_USER_SESSION_NAME = "CACHE_CLOUD_USER_SESSION";

    /**
     * maven仓库地址
     */
    public static final String MAVEN_WAREHOUSE = "http://your_maven_warehouse";
    
    /**
     * 超级管理员
     */
    public static final String SUPER_ADMIN_NAME = "admin";
    
    
    /**
     * 超级管理员
     */
    public static final String SUPER_ADMIN_PASS = "admin";
    
    /**
     * 分号
     */
    public static final String SEMICOLON = ";";
    
    /**
     * 逗号
     */
    public static final String COMMA = ",";
    
    /**
     * 换行
     */
    public static final String NEXT_LINE = "\n";
}


