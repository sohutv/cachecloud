package com.sohu.cache.constant;

/**
 * Redis的分钟差值的统计指标，用于统计峰值以及时间段内的展示
 *
 * User: lingguo
 * Date: 14-7-1
 */
public enum RedisMinuteKey {
    KeySpaceHits("keyspace_hits"),
    KeySpaceMiss("keyspace_misses"),
    ExpiredKeys("expired_keys"),
    EvictedKeys("evicted_keys"),
    CmdExpire("cmdstat_expire"),
    CmdPing("cmdstat_ping"),
    CmdDel("cmdstat_del"),
    CmdZRemRangeByScore("cmdstat_zremrangebyscore"),
    CmdZAdd("cmdstat_zadd");

    String value;

    RedisMinuteKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
