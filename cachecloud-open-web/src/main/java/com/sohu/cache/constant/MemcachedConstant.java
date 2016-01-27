package com.sohu.cache.constant;

/**
 * Memcached 统计的常量
 *
 * User: lingguo
 * Date: 14-6-11
 * Time: 下午4:46
 */
public enum  MemcachedConstant {
    Stats("stats"),
    Items("items"),
    Slabs("slabs"),
    Diff("diff"),
    IncrHits("incr_hits"),
    IncrMisses("incr_misses"),
    DecrHits("decr_hits"),
    DecrMisses("decr_misses"),
    CasHits("cas_hits"),
    CasMisses("cas_misses");

    private String value;

    MemcachedConstant(String value) {
        this.value = value;
    }

    public static MemcachedConstant value(String input) {
        MemcachedConstant[] constantArr = MemcachedConstant.values();
        for (MemcachedConstant constant: constantArr) {
            if (constant.value.equals(input)) {
                return constant;
            }
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
