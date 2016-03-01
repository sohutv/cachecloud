package com.sohu.cache.constant;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * cachecloud常量
 * @author leifu
 * @Date 2014年11月26日
 * @Time 上午10:38:34
 */
public class CacheCloudConstants {
	
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
        SUPER_MANAGER.add("yijunzhang");
        SUPER_MANAGER.add("leifu");
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
