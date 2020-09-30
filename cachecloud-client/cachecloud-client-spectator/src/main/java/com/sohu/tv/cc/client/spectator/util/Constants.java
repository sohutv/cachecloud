package com.sohu.tv.cc.client.spectator.util;

import java.util.ResourceBundle;

/**
 * @author wenruiwu
 * @create 2019/12/17 10:57
 * @description
 */
public class Constants {

    public static final String CLIENT_VERSION_KEY = "cc_client_version";
    public static final String DOMAIN_URL_KEY = "cc_domain_url";
    public static final String HTTP_CONN_TIMEOUT_KEY = "http_conn_timeout";
    public static final String HTTP_SOCKET_TIMEOUT_KEY = "http_socket_timeout";
    public static final String DOMAIN_URL;
    public static final String CLIENT_VERSION;
    public static final String REDIS_CLUSTER_URL;
    public static final String REDIS_SENTINEL_URL;
    public static final String REDIS_STANDALONE_URL;

    public static final String CACHECLOUD_EXP_REPORT_URL;
    public static final String CACHECLOUD_COMMAND_REPORT_URL;

    public static final int HTTP_CONN_TIMEOUT;
    public static final int HTTP_SOCKET_TIMEOUT;

    public static final ResourceBundle resources;

    static {
        resources = ResourceBundle.getBundle("cachecloud-client");
        DOMAIN_URL = getPropertyFromMulSource(DOMAIN_URL_KEY);
        CLIENT_VERSION = getPropertyFromFile(CLIENT_VERSION_KEY);
        REDIS_CLUSTER_URL = DOMAIN_URL + "/cache/client/redis/cluster/%s.json?clientVersion=" + CLIENT_VERSION;
        REDIS_SENTINEL_URL = DOMAIN_URL + "/cache/client/redis/sentinel/%s.json?clientVersion=" + CLIENT_VERSION;
        REDIS_STANDALONE_URL = DOMAIN_URL + "/cache/client/redis/standalone/%s.json?clientVersion=" + CLIENT_VERSION;
        CACHECLOUD_EXP_REPORT_URL = DOMAIN_URL + "/cachecloud/client/v1/reportData/exception";
        CACHECLOUD_COMMAND_REPORT_URL = DOMAIN_URL + "/cachecloud/client/v1/reportData/command";

        HTTP_CONN_TIMEOUT = Integer.parseInt(getPropertyFromMulSource(HTTP_CONN_TIMEOUT_KEY));
        HTTP_SOCKET_TIMEOUT = Integer.parseInt(getPropertyFromMulSource(HTTP_SOCKET_TIMEOUT_KEY));
    }

    public static String getPropertyFromMulSource(String key) {
        //1. System Property
        String property = System.getProperty(key);
        if (property == null || property.length() == 0) {
            //2. OS Env
            property = System.getenv(key.toUpperCase());
        }
        if (property == null || property.length() == 0) {
            //3. Properties file
            property = resources.getString(key);
        }
        return property;
    }

    public static String getPropertyFromFile(String key) {
        //1. Properties file
        String property = resources.getString(key);
        return property;
    }
}
