package com.sohu.cache.util;

import java.util.Arrays;
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

    // 机器报警阀值
    public static double CPU_USAGE_RATIO_THRESHOLD = 80.0;
    public static double MEMORY_USAGE_RATIO_THRESHOLD = 80.0;
    public static double LOAD_THRESHOLD = 7.5;

    /**
     * 机器统一的用户名、密码、端口
     */
    public static String USERNAME;
    public static String PASSWORD;
    public static int SSH_PORT_DEFAULT;

    /**
     * 管理员
     */
    public static String SUPER_ADMIN_NAME;
    public static String SUPER_ADMIN_PASS;
    public static List<String> SUPER_MANAGER;
    
    /**
     * 是否为调试
     */
    public static boolean IS_DEBUG;
    
    /**
     * 联系人
     */
    public static String CONTACT;
    
    /**
     * 文档地址
     */
    public static String DOCUMENT_URL;
    
    /**
     * 报警相关
     */
    public static String EMAILS;
    public static String PHONES;
    
    /**
     * maven仓库地址
     */
    public static String MAVEN_WAREHOUSE;
    
    /**
     * 超级管理员列表
     */
    public static String SUPER_ADMINS;
    
    static {
        ResourceBundle configresourceBundle = ResourceBundle.getBundle("config");
        CONTACT = configresourceBundle.getString("cachecloud.contact");
        DOCUMENT_URL = configresourceBundle.getString("cachecloud.documentUrl");
        EMAILS = configresourceBundle.getString("cachecloud.owner.email");
        PHONES = configresourceBundle.getString("cachecloud.owner.phone");
        MAVEN_WAREHOUSE = configresourceBundle.getString("cachecloud.mavenWareHouse");
        SUPER_ADMINS = configresourceBundle.getString("cachecloud.superAdmin");
        SUPER_MANAGER = Arrays.asList(SUPER_ADMINS.split(","));
        
        ResourceBundle applicationResourceBundle = ResourceBundle.getBundle("application");
        IS_DEBUG = "true".equals(applicationResourceBundle.getString("isDebug"));
        USERNAME = applicationResourceBundle.getString("shell.auth.simple.user.name");
        PASSWORD = applicationResourceBundle.getString("shell.auth.simple.user.password");
        SSH_PORT_DEFAULT = Integer.parseInt(applicationResourceBundle.getString("cachecloud.machine.ssh.port"));
        SUPER_ADMIN_NAME = applicationResourceBundle.getString("cachecloud.admin.user.name");
        SUPER_ADMIN_PASS = applicationResourceBundle.getString("cachecloud.admin.user.password");
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


