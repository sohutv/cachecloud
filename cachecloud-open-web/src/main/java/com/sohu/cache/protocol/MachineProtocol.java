package com.sohu.cache.protocol;

import java.util.ResourceBundle;

/**
 * 机器相关的一些常量
 *
 * @author: lingguo
 * @time: 2014/8/26 16:18
 */
public class MachineProtocol {
    
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
     * ssh端口
     */
    public static int SSH_PORT_DEFAULT;
    static {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
        SSH_PORT_DEFAULT = Integer.parseInt(resourceBundle.getString("cachecloud.machine.ssh.port"));
        System.out.println("==========ssh port " + SSH_PORT_DEFAULT);
    }
    /**
     * 编码
     */
    public static final String ENCODING_UTF8 = "UTF-8";

    /**
     * 系统预留的内存大小：4G
     */
    public static final Long SYSTEM_RESERVED_MEMORY = 4096000000L;
}
