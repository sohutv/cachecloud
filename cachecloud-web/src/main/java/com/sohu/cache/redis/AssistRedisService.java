package com.sohu.cache.redis;

import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.SetParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CacheCloud 内部使用的辅助redis
 */
public interface AssistRedisService {

    boolean reloadSentinel();

    <T> boolean set(String key, T value);

    <T> boolean set(String key, T value, int timeout);

    boolean setNx(String key, String value) ;

    String set(String key, String value, SetParams params) ;

    <T> boolean setWithNoSerialize(String key, T value);

    String getWithNoSerialize(String key);

    <T> boolean setWithNoSerialize(String key, T value, int seconds);

    boolean remove(String key);

    <T> T get(String key);

    boolean rpush(String key, String item);

    boolean rpushList(String key, List<String> items);

    boolean saddSet(String key, Set<String> items);

    boolean sadd(String key, String item);

    Set<String> smembers(String key);

    boolean srem(String key, String item);

    List<String> lrange(String key, int start, int end);

    Long llen(final String key);

    String lpop(final String key);

    boolean zadd(String key, long score, String member);

    boolean hset(String key, String field, String value);

    boolean hmset(String key, Map<String, String> map);

    Map<String, String> hgetAll(String key);

    boolean del(String key);

    void zincrby(String key, double score, String member);

    Set<Tuple> zrangeWithScores(String key, long start, long end);

    boolean exists(String key);

}
