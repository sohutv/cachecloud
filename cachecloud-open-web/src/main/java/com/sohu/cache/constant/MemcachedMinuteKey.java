package com.sohu.cache.constant;

/**
 * Memcached的分钟差值的统计指标，用于统计峰值及时间段内的展示
 *
 * User: lingguo
 * Date: 14-6-30
 */
public enum MemcachedMinuteKey {
    CMD_GET("cmd_get"),            // get命令总数
    CMD_SET("cmd_set"),            // set命令总数
    CMD_INCR("cmd_incr");           // incr命令总数

    String value;

    MemcachedMinuteKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
