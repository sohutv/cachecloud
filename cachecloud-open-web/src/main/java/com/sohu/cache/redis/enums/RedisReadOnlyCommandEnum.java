package com.sohu.cache.redis.enums;

import org.apache.commons.lang.StringUtils;

/**
 * Created by yijunzhang on 14-10-14.
 */
public enum RedisReadOnlyCommandEnum {
    debug,
    exists,
    object,
    ttl,
    type,
    scan,
    get,
    getbit,
    getrange,
    mget,
    setrange,
    strlen,
    hexists,
    hget,
    hgetall,
    hkeys,
    hlen,
    hmget,
    hvals,
    hscan,
    lindex,
    llen,
    lrange,
    scard,
    sismember,
    sscan,
    srandmember,
    zcard,
    zcount,
    zrange,
    zrangebyscore,
    zrank,
    zrevrange,
    zscore,
    zscan,
    dbsize,
    info,
    time,
    lastsave;
    public static boolean contains(String command) {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        for (RedisReadOnlyCommandEnum readEnum : RedisReadOnlyCommandEnum.values()) {
            String readCommand = readEnum.toString();
            command = StringUtils.trim(command);
            if (command.length() < readCommand.toString().length()) {
                continue;
            }
            String head = StringUtils.substring(command, 0, readCommand.length());
            if (readCommand.equalsIgnoreCase(head)) {
                return true;
            }
        }
        return false;
    }

}
