package com.sohu.cache.constant;

/**
 * Memcached实例状态信息`stat`中部分字段常量
 *
 * User: lingguo
 * Date: 14-7-27 下午6:01
 */
public enum MemcachedStats {
    /* 最大内存 */
    MAX_MEMORY("limit_maxbytes"),

    /* 已用内存 */
    USED_MEMORY("bytes"),

    /* 当前连接数 */
    CURR_CONNECTIONS("curr_connections"),

    /* 当前的items */
    CURR_ITEMS("curr_items"),

    /* get未命中 */
    MISSES("get_misses"),

    /* get命中  */
    HITS("get_hits");

    MemcachedStats(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return this.value;
    }
}
