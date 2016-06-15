package com.sohu.cache.protocol;

import com.sohu.cache.util.ConstUtils;

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
    public static final String CONF_DIR = ConstUtils.CACHECLOUD_BASE_DIR + "/cachecloud/conf/";
    public static final String DATA_DIR = ConstUtils.CACHECLOUD_BASE_DIR + "/cachecloud/data";
    public static final String LOG_DIR = ConstUtils.CACHECLOUD_BASE_DIR + "/cachecloud/logs/";

    /**
     * 配置文件的临时目录；
     */
    public static final String TMP_DIR = "/tmp/cachecloud/";

    /**
     * 编码
     */
    public static final String ENCODING_UTF8 = "UTF-8";

}
