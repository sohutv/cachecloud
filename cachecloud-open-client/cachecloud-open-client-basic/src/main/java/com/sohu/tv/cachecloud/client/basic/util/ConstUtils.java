package com.sohu.tv.cachecloud.client.basic.util;

import java.util.ResourceBundle;

/**
 * 客户端常量
 * 
 * @author leifu
 * @Date 2014年6月21日
 * @Time 上午10:54:34
 */
public class ConstUtils {

    /**
     * http连接和读取超时
     */
    public static final int HTTP_CONN_TIMEOUT;
    public static final int HTTP_SOCKET_TIMEOUT;

    /**
     * 客户端版本信息
     */
    public static final String CLIENT_VERSION;

    /**
     * 上报域名和对应各个类型redis的rest url.
     */
    public static final String DOMAIN_URL;
    public static final String REDIS_CLUSTER_URL;
    public static final String REDIS_SENTINEL_URL;
    public static final String REDIS_STANDALONE_URL;
    public static final String CACHECLOUD_REPORT_URL;

    static {
        ResourceBundle rb = ResourceBundle.getBundle("cacheCloudClient");

        HTTP_CONN_TIMEOUT = Integer.valueOf(rb.getString("http_conn_timeout"));
        HTTP_SOCKET_TIMEOUT = Integer.valueOf(rb.getString("http_socket_timeout"));

        CLIENT_VERSION = rb.getString("client_version");

        DOMAIN_URL = rb.getString("domain_url");
        REDIS_CLUSTER_URL = DOMAIN_URL + rb.getString("redis_cluster_suffix") + CLIENT_VERSION;
        REDIS_SENTINEL_URL = DOMAIN_URL + rb.getString("redis_sentinel_suffix") + CLIENT_VERSION;
        REDIS_STANDALONE_URL = DOMAIN_URL + rb.getString("redis_standalone_suffix") + CLIENT_VERSION;
        CACHECLOUD_REPORT_URL = DOMAIN_URL + rb.getString("cachecloud_report_url");
    }

}
