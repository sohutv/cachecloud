package com.sohu.cache.constant;

/**
 * memcached slab 统计及分布
 *
 * User: lingguo
 * Date: 14-7-7
 */
public enum MemcachedSlabInfo {
    ACTIVE_SLABS("active_slabs"),
    TOTAL_MALLOCED("total_malloced"),
    SLAB_DISTRIBUTE("slab_distribute");

    private String value;

    MemcachedSlabInfo(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
