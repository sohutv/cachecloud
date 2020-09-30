package com.sohu.cache.util;

/**
 * Created by chenshi
 */
public class RedisConstUtils {

    /**
     * Redis版本安装脚本路径
     */
    public static final String REDIS_SHELL_DIR = "/opt/cachecloud/sh/";

    /**
     * Redis安装包后缀
     */
    public static final String REDIS_INSTALL_PACKAGE_SUFFIX = ".tar.gz";

    /**
     * Redis make包后缀
     */
    public static final String REDIS_INSTALL_MAKE_PACKAGE_SUFFIX = "-make.tar.gz";

    /**
     * Redis版本名称前缀
     */
    public static final String REDIS_VERSION_PREFIX = "redis-";

    /**
     * Redis单版本安装日志
     */
    public static final String REDIS_INSTALL_LOG = "/opt/cachecloud/sh/install.log.%s";

}
