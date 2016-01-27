package com.sohu.cache.constant;

/**
 * Memcached需要累加计算的指标（基于应用）
 *
 * User: lingguo
 * Date: 14-6-14
 * Time: 下午3:38
 */
public enum MemcachedAccumulation {
    GetHits("get_hits"),
    GetMisses("get_misses"),
    Evictions("evictions"),
    Expires("expired_unfetched");

    private String value;

    MemcachedAccumulation(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
