package com.sohu.cache.protocol;

/**
 * 机器相关的一些常量
 *
 * @author: lingguo
 * @time: 2014/8/26 16:18
 */
public interface MachineProtocol {
    
    /**
     * 统一的目录结构
     */
    public static final String CONF_DIR = "/opt/cachecloud/conf/";
    public static final String DATA_DIR = "/opt/cachecloud/data";
    public static final String LOG_DIR = "/opt/cachecloud/logs/";

    /**
     * 配置文件的临时目录；
     */
    public static final String TMP_DIR = "/tmp/cachecloud/";


    /**
     * ssh端口(默认22、其他例如32200)
     */
    public static final int SSH_PORT_DEFAULT = 22;
    public static final int SSH_PORT_56 = 32200;

    /**
     * 编码
     */
    public static final String ENCODING_UTF8 = "UTF-8";

    /**
     * 系统预留的内存大小：4G
     */
    public static final Long SYSTEM_RESERVED_MEMORY = 4096000000L;
}
