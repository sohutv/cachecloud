package com.sohu.cache.util;

/**
 * Created by yijunzhang on 14-9-26.
 */
public class TypeUtil {

    public static boolean isRedisType(int type) {
        if (type == ConstUtils.CACHE_REDIS_SENTINEL
                || type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER
                || type == ConstUtils.CACHE_REDIS_STANDALONE) {
            return true;
        }
        return false;
    }

    public static boolean isRedisCluster(int type) {
        if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            return true;
        }
        return false;
    }

    public static boolean isRedisSentinel(int type) {
        if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            return true;
        }
        return false;
    }
    
    public static boolean isRedisStandalone(int type) {
        if (type == ConstUtils.CACHE_REDIS_STANDALONE) {
            return true;
        }
        return false;
    }

    public static boolean isRedisDataType(int type) {
        if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER
                || type == ConstUtils.CACHE_REDIS_STANDALONE) {
            return true;
        }
        return false;
    }

}
