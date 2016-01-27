package com.sohu.cache.constant;

/**
 * redis需要累加计算的指标
 * Created by yijunzhang on 14-6-12.
 */
public enum RedisAccumulation {
    EXPIRED_KEYS(RedisConstant.Stats,"expired_keys"),
    EVICTED_KEYS(RedisConstant.Stats,"evicted_keys"),
    KEYSPACE_HITS(RedisConstant.Stats,"keyspace_hits"),
    KEYSPACE_MISSES(RedisConstant.Stats,"keyspace_misses"),
    TOTAL_NET_INPUT_BYTES(RedisConstant.Stats,"total_net_input_bytes"),
    TOTAL_NET_OUTPUT_BYTES(RedisConstant.Stats,"total_net_output_bytes");

    private RedisConstant constant;
    private String value;

    RedisAccumulation(RedisConstant constant, String value) {
        this.constant = constant;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public RedisConstant getConstant() {
        return constant;
    }

    @Override
    public String toString() {
        return value;
    }
}
