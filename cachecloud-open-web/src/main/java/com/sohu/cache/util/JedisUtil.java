package com.sohu.cache.util;

import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

/**
 * 获取jedis连接
 * 
 * @author leifu
 * @Date 2016年6月14日
 * @Time 上午10:38:36
 */
public class JedisUtil {

    public static Jedis getJedis(String host, int port) throws Exception {
        return getJedis(host, port, Protocol.DEFAULT_TIMEOUT, null);
    }

    public static Jedis getJedis(String host, int port, int timeout) throws Exception {
        return getJedis(host, port, timeout, null);
    }
    
    public static Jedis getJedis(String host, int port, int connectionTimeout, int soTimeout) throws Exception {
        return getJedis(host, port, connectionTimeout, soTimeout, null);
    }

    public static Jedis getJedis(String host, int port, String password) throws Exception {
        return getJedis(host, port, Protocol.DEFAULT_TIMEOUT, password);
    }

    public static Jedis getJedis(String host, int port, int timeout, String password) throws Exception {
        return getJedis(host, port, timeout, timeout, password);
    }
    
    public static Jedis getJedis(String host, int port, int connectionTimeout, int soTimeout, String password) throws Exception {
        try {
            Jedis jedis = new Jedis(host, port, connectionTimeout, soTimeout);
            if (StringUtils.isNotBlank(password)) {
                jedis.auth(password);
            }
            return jedis;
        } catch (Exception e) {
            throw e;
        }
    }
}
