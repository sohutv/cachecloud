package com.sohu.tv.cachecloud.client.redis.crossroom;

import com.sohu.tv.cachecloud.client.redis.crossroom.entity.RedisCrossRoomTopology;
import com.sohu.tv.cachecloud.client.redis.crossroom.enums.MultiWriteResult;
import redis.clients.jedis.*;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis跨机房客户端
 *
 * @author leifu
 * @Date 2016年4月5日
 * @Time 下午5:05:32
 */
public interface RedisCrossRoomClient {

    MultiWriteResult<String> set(final String key, final String value);

    MultiWriteResult<String> set(final String key, final byte[] value);
    
    MultiWriteResult<String> set(final byte[] key, final byte[] value);
    
    MultiWriteResult<String> setex(final String key, final int seconds, final String value);
    
    MultiWriteResult<String> setex(final byte[] key, final int seconds, final byte[] value);
    
    MultiWriteResult<String> setex(final String key, final int seconds, final byte[] value);
    
    MultiWriteResult<Long> setnx(final String key, final String value);
    
    MultiWriteResult<Long> setnx(final byte[] key, final byte[] value);
    
    MultiWriteResult<Long> expireAt(final String key, final long unixTime);
    
    String get(final String key);
    
    byte[] get(final byte[] key);
    
    byte[] getBytes(String key);
    
    Map<String, byte[]> mgetBytes(List<String> keys);

    MultiWriteResult<String> msetBytes(final Map<String, byte[]> keyValueMap);
    
    Map<String, String> mget(final List<String> keys);
    
    MultiWriteResult<String> mset(final Map<String, String> keyValueMap);
    
    MultiWriteResult<Map<String, Long>> mexpire(final Map<String, Integer> keyTimeMap);
    
    MultiWriteResult<Long> del(final String key);
    
    Boolean exists(final String key);
    
    MultiWriteResult<Long> incr(final String key);
    
    MultiWriteResult<Long> incrBy(final String key, final long integer);
    
    MultiWriteResult<Long> expire(final String key, final int seconds);
    
    MultiWriteResult<Long> expire(final byte[] key, final int seconds);
    
    MultiWriteResult<Long> hset(final byte[] key, final byte[] field, final byte[] value);
    
    MultiWriteResult<Long> hset(final String key, final String field, final String value);
    
    MultiWriteResult<Long> hsetnx(final String key, final String field, final String value);
    
    MultiWriteResult<String> hmset(final String key, final Map<String, String> hash);
    
    MultiWriteResult<String> hmsetBytes(final String key, final Map<byte[],byte[]> hash);
    
    MultiWriteResult<Long> hincrBy(final String key, final String field, final long value);
    
    String hget(final String key, final String field);
    
    byte[] hget(final byte[] key, final byte[] field);
    
    List<String> hmget(final String key, final String... fields);

    List<byte[]> hmget(final byte[] key, final byte[]... fields);
    
    Map<String, String> hgetAll(final String key);
    
    MultiWriteResult<Long> hdel(final String key, final String... fields);
    
    Set<String> hkeys(final String key);
    
    Long hlen(final String key);

    // add redis crossroom api

    public Set<String> zrange(final String key, final long start, final long stop);

    public List<String> lrange(final String key, final long start, final long stop);

    public Boolean sismember(final String key, final byte[] member);

    public Set<String> smembers(final String key);

    public MultiWriteResult<Long> srem(final String key, final byte[]... member);

    public MultiWriteResult<Long> zrem(final String key, final byte[]... member) ;

    public Set<String> zrevrange(final String key, final long start, final long stop);

    public MultiWriteResult<Long> sadd(final String key, final byte[]... member);

    public MultiWriteResult<Long> zadd(final String key, final double score, final byte[] member);

    // 20190702
    public MultiWriteResult<Long> zadd(final String key, final Map<String, Double> scoreMembers);

    public MultiWriteResult<String> lpop(final String key);

    public Long llen(final String key);

    public MultiWriteResult<Long> rpush(final String key, final String... string);

    public MultiWriteResult<Long> rpush(final String key, final byte[]... string);

    public Boolean sismember(final String key, final String member);

    public MultiWriteResult<Long> mdel(final List<String> keys);

    public MultiWriteResult<Map<String, Long>> mzadds(final Map<String, Map<String, Double>> map);

    public MultiWriteResult<Long> zrem(final String key, final String... members);

    public Long ttl(final String key);

    public MultiWriteResult<Long> sadd(final String key, final String... member);

    public Map<String, Map<String, String>> mHgetAll(List<String> keys);

    public Long scard(final String key);

    // 2090912
    MultiWriteResult<String> set(final String key, final String value, final SetParams params);

    MultiWriteResult<String> set(final byte[] key,final byte[] value,final SetParams setParams);

    Map<byte[], byte[]> hgetAllBytes(String key);

    //202003 -- start
    //Stream API
    MultiWriteResult<StreamEntryID> xadd(String var1, StreamEntryID var2, Map<String, String> var3);

    MultiWriteResult<StreamEntryID> xadd(String var1, StreamEntryID var2, Map<String, String> var3, long var4, boolean var6);

    Long xlen(String var1);

    List<StreamEntry> xrange(String var1, StreamEntryID var2, StreamEntryID var3, int var4);

    List<StreamEntry> xrevrange(String var1, StreamEntryID var2, StreamEntryID var3, int var4);

    List<Map.Entry<String, List<StreamEntry>>> xread(int var1, long var2, Map.Entry... var4);

    MultiWriteResult<Long> xack(String var1, String var2, StreamEntryID... var3);

    MultiWriteResult<String> xgroupCreate(String var1, String var2, StreamEntryID var3, boolean var4);

    MultiWriteResult<String> xgroupSetID(String var1, String var2, StreamEntryID var3);

    MultiWriteResult<Long> xgroupDestroy(String var1, String var2);

    MultiWriteResult<Long> xgroupDelConsumer(String var1, String var2, String var3);

    List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String var1, String var2, int var3, long var4, boolean var6, Map.Entry... var7);

    List<StreamPendingEntry> xpending(String var1, String var2, StreamEntryID var3, StreamEntryID var4, int var5, String var6);

    MultiWriteResult<Long> xdel(String var1, StreamEntryID... var2);

    MultiWriteResult<Long> xtrim(String var1, long var2, boolean var4);

    MultiWriteResult<List<StreamEntry>> xclaim(String var1, String var2, String var3, long var4, long var6, int var8, boolean var9, StreamEntryID... var10);

    //string
    MultiWriteResult<String> psetex(byte[] key, long milliseconds, byte[] value);

    MultiWriteResult<String> psetex(String var1, long var2, String var4);

    MultiWriteResult<Boolean> setbit(byte[] key, long offset, boolean value);

    MultiWriteResult<Boolean> setbit(byte[] key, long offset, byte[] value);

    MultiWriteResult<Boolean> setbit(String var1, long var2, boolean var4);

    MultiWriteResult<Boolean> setbit(String var1, long var2, String var4);

    MultiWriteResult<Long> setrange(byte[] key, long offset, byte[] value);

    MultiWriteResult<Long> setrange(String var1, long var2, String var4);

    MultiWriteResult<byte[]> getSet(byte[] key, byte[] value);

    MultiWriteResult<String> getSet(String var1, String var2);

    Boolean getbit(String var1, long var2);

    String getrange(String var1, long var2, long var4);

    Boolean getbit(byte[] var1, long var2);

    byte[] getrange(byte[] var1, long var2, long var4);

    MultiWriteResult<Long> decrBy(byte[] var1, long var2);

    MultiWriteResult<Long> decr(byte[] var1);

    MultiWriteResult<Long> incrBy(byte[] var1, long var2);

    MultiWriteResult<Double> incrByFloat(byte[] var1, double var2);

    MultiWriteResult<Long> incr(byte[] var1);

    Long strlen(String var1);

    Long strlen(byte[] key);

    MultiWriteResult<List<Long>> bitfield(String var1, String... var2);

    MultiWriteResult<List<Long>> bitfield(byte[] key, byte[]... arguments);

    //list
    MultiWriteResult<String> lset(byte[] key, long index, byte[] value);

    MultiWriteResult<String> lset(String var1, long var2, String var4);

    MultiWriteResult<Long> lpush(byte[] key, byte[]... args);

    Long llen(byte[] key);

    List<byte[]> lrange(byte[] key, long start, long stop);

    MultiWriteResult<String> ltrim(byte[] key, long start, long stop);

    byte[] lindex(byte[] key, long index);

    MultiWriteResult<byte[]> lpop(byte[] key);

    MultiWriteResult<Long> lrem(byte[] key, long count, byte[] value);

    MultiWriteResult<Long> lpush(String var1, String... var2);

    MultiWriteResult<String> ltrim(String var1, long var2, long var4);

    String lindex(String var1, long var2);

    MultiWriteResult<Long> lrem(String var1, long var2, String var4);

    MultiWriteResult<Long> rpush(byte[] key, byte[]... args);

    MultiWriteResult<byte[]> rpop(byte[] key);

    MultiWriteResult<String> rpop(String var1);

    MultiWriteResult<Long> linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value);

    MultiWriteResult<Long> lpushx(byte[] key, byte[]... arg);

    MultiWriteResult<Long> rpushx(byte[] key, byte[]... arg);

    MultiWriteResult<Long> linsert(String var1, ListPosition var2, String var3, String var4);

    MultiWriteResult<Long> lpushx(String var1, String... var2);

    MultiWriteResult<Long> rpushx(String var1, String... var2);

    MultiWriteResult<List<String>> blpop(int var1, String var2);

    MultiWriteResult<List<String>> brpop(int var1, String var2);

    MultiWriteResult<List<byte[]>> blpop(int var1, byte[]... var2);

    MultiWriteResult<List<byte[]>> brpop(int var1, byte[]... var2);

    MultiWriteResult<byte[]> rpoplpush(byte[] var1, byte[] var2);

    MultiWriteResult<byte[]> brpoplpush(byte[] var1, byte[] var2, int var3);

    MultiWriteResult<List<String>> sort(String var1);

    MultiWriteResult<List<String>> sort(String var1, SortingParams var2);

    MultiWriteResult<List<byte[]>> sort(byte[] key);

    MultiWriteResult<List<byte[]>> sort(byte[] key, SortingParams sortingParameters);

    //hash
    MultiWriteResult<Long> hset(byte[] key, Map<byte[], byte[]> hash);

    MultiWriteResult<Long> hset(String var1, Map<String, String> var2);

    MultiWriteResult<Long> hsetnx(byte[] key, byte[] field, byte[] value);

    MultiWriteResult<String> hmset(byte[] key, Map<byte[], byte[]> hash);

    MultiWriteResult<Long> hincrBy(byte[] var1, byte[] var2, long var3);

    MultiWriteResult<Double> hincrByFloat(byte[] var1, byte[] var2, double var3);

    Boolean hexists(byte[] var1, byte[] var2);

    MultiWriteResult<Long> hdel(byte[] var1, byte[]... var2);

    Long hlen(byte[] var1);

    Set<byte[]> hkeys(byte[] var1);

    Collection<byte[]> hvals(byte[] var1);

    Boolean hexists(String var1, String var2);

    List<String> hvals(String var1);

    Long hstrlen(String var1, String var2);

    Long hstrlen(byte[] var1, byte[] var2);

    //set
    MultiWriteResult<Long> sadd(byte[] key, byte[]... member);

    Set<byte[]> smembers(byte[] key);

    MultiWriteResult<Long> srem(byte[] key, byte[]... member);

    MultiWriteResult<byte[]> spop(byte[] key);

    MultiWriteResult<Set<byte[]>> spop(byte[] key, long count);

    Long scard(byte[] key);

    Boolean sismember(byte[] key, byte[] member);

    byte[] srandmember(byte[] key);

    List<byte[]> srandmember(byte[] key, int count);

    MultiWriteResult<Long> srem(String var1, String... var2);

    MultiWriteResult<String> spop(String var1);

    MultiWriteResult<Set<String>> spop(String var1, long var2);

    String srandmember(String var1);

    List<String> srandmember(String var1, int var2);

    //sorted set
    MultiWriteResult<Long> zadd(byte[] key, double score, byte[] member);

    MultiWriteResult<Long> zadd(byte[] key, double score, byte[] member, ZAddParams params);

    MultiWriteResult<Long> zadd(byte[] key, Map<byte[], Double> scoreMembers);

    MultiWriteResult<Long> zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params);

    Set<byte[]> zrange(byte[] key, long start, long stop);

    MultiWriteResult<Long> zrem(byte[] key, byte[]... members);

    MultiWriteResult<Double> zincrby(byte[] key, double increment, byte[] member);

    MultiWriteResult<Double> zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params);

    Long zrank(byte[] key, byte[] member);

    Long zrevrank(byte[] key, byte[] member);

    Set<byte[]> zrevrange(byte[] key, long start, long stop);

    Set<Tuple> zrangeWithScores(byte[] key, long start, long stop);

    Set<Tuple> zrevrangeWithScores(byte[] key, long start, long stop);

    Long zcard(byte[] key);

    Double zscore(byte[] key, byte[] member);

    Long zcount(byte[] key, double min, double max);

    Long zcount(byte[] key, byte[] min, byte[] max);

    Set<byte[]> zrangeByScore(byte[] key, double min, double max);

    Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max);

    Set<byte[]> zrevrangeByScore(byte[] key, double max, double min);

    Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count);

    Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min);

    Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count);

    Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max);

    Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count);

    Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max);

    Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count);

    MultiWriteResult<Long> zremrangeByRank(byte[] key, long start, long stop);

    MultiWriteResult<Long> zremrangeByScore(byte[] key, double min, double max);

    MultiWriteResult<Long> zremrangeByScore(byte[] key, byte[] min, byte[] max);

    Long zlexcount(byte[] key, byte[] min, byte[] max);

    Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max);

    Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset,
                            int count);

    Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min);

    Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset,
                               int count);

    MultiWriteResult<Long> zadd(String var1, double var2, String var4);

    MultiWriteResult<Long> zadd(String var1, double var2, String var4, ZAddParams var5);

    MultiWriteResult<Long> zadd(String var1, Map<String, Double> var2, ZAddParams var3);

    MultiWriteResult<Double> zincrby(String var1, double var2, String var4);

    MultiWriteResult<Double> zincrby(String var1, double var2, String var4, ZIncrByParams var5);

    Long zrank(String var1, String var2);

    Long zrevrank(String var1, String var2);

    Set<Tuple> zrangeWithScores(String var1, long var2, long var4);

    Set<Tuple> zrevrangeWithScores(String var1, long var2, long var4);

    Long zcard(String var1);

    Double zscore(String var1, String var2);

    Long zcount(String var1, double var2, double var4);

    Long zcount(String var1, String var2, String var3);

    Set<String> zrangeByScore(String var1, double var2, double var4);

    Set<String> zrangeByScore(String var1, String var2, String var3);

    Set<String> zrevrangeByScore(String var1, double var2, double var4);

    Set<String> zrangeByScore(String var1, double var2, double var4, int var6, int var7);

    Set<String> zrevrangeByScore(String var1, String var2, String var3);

    Set<String> zrangeByScore(String var1, String var2, String var3, int var4, int var5);

    Set<String> zrevrangeByScore(String var1, double var2, double var4, int var6, int var7);

    Set<Tuple> zrangeByScoreWithScores(String var1, double var2, double var4);

    Set<Tuple> zrevrangeByScoreWithScores(String var1, double var2, double var4);

    Set<Tuple> zrangeByScoreWithScores(String var1, double var2, double var4, int var6, int var7);

    Set<String> zrevrangeByScore(String var1, String var2, String var3, int var4, int var5);

    Set<Tuple> zrangeByScoreWithScores(String var1, String var2, String var3);

    Set<Tuple> zrevrangeByScoreWithScores(String var1, String var2, String var3);

    Set<Tuple> zrangeByScoreWithScores(String var1, String var2, String var3, int var4, int var5);

    Set<Tuple> zrevrangeByScoreWithScores(String var1, double var2, double var4, int var6, int var7);

    Set<Tuple> zrevrangeByScoreWithScores(String var1, String var2, String var3, int var4, int var5);

    MultiWriteResult<Long> zremrangeByRank(String var1, long var2, long var4);

    MultiWriteResult<Long> zremrangeByScore(String var1, double var2, double var4);

    MultiWriteResult<Long> zremrangeByScore(String var1, String var2, String var3);

    Long zlexcount(String var1, String var2, String var3);

    Set<String> zrangeByLex(String var1, String var2, String var3);

    Set<String> zrangeByLex(String var1, String var2, String var3, int var4, int var5);

    Set<String> zrevrangeByLex(String var1, String var2, String var3);

    Set<String> zrevrangeByLex(String var1, String var2, String var3, int var4, int var5);

    MultiWriteResult<Long> zremrangeByLex(byte[] key, byte[] min, byte[] max);

    MultiWriteResult<Long> zremrangeByLex(String var1, String var2, String var3);

    //scan
    MultiWriteResult<Long> del(byte[] key);

    MultiWriteResult<Long> unlink(byte[] key);

    byte[] echo(byte[] arg);

    Long bitcount(byte[] key);

    Long bitcount(byte[] key, long start, long end);

    MultiWriteResult<Long> pfadd(byte[] key, byte[]... elements);

    long pfcount(byte[] key);

    ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor);

    ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params);

    ScanResult<byte[]> sscan(byte[] key, byte[] cursor);

    ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params);

    ScanResult<Tuple> zscan(byte[] key, byte[] cursor);

    ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params);

    MultiWriteResult<Long> unlink(String var1);

    String echo(String var1);

    Long bitcount(String var1);

    Long bitcount(String var1, long var2, long var4);

    ScanResult<Map.Entry<String, String>> hscan(String var1, String var2);

    ScanResult<String> sscan(String var1, String var2);

    ScanResult<Tuple> zscan(String var1, String var2);

    MultiWriteResult<Long> pfadd(String var1, String... var2);

    long pfcount(String var1);

    // Geo Commands
    MultiWriteResult<Long> geoadd(byte[] key, double longitude, double latitude, byte[] member);

    MultiWriteResult<Long> geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap);

    Double geodist(byte[] key, byte[] member1, byte[] member2);

    Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit);

    List<byte[]> geohash(byte[] key, byte[]... members);

    List<GeoCoordinate> geopos(byte[] key, byte[]... members);

    List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius,
                                      GeoUnit unit);

    List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
                                              GeoUnit unit);

    List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius,
                                      GeoUnit unit, GeoRadiusParam param);

    List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
                                              GeoUnit unit, GeoRadiusParam param);

    List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
                                              GeoRadiusParam param);

    List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit,
                                                      GeoRadiusParam param);

    MultiWriteResult<Long> geoadd(String var1, double var2, double var4, String var6);

    MultiWriteResult<Long> geoadd(String var1, Map<String, GeoCoordinate> var2);

    Double geodist(String var1, String var2, String var3);

    Double geodist(String var1, String var2, String var3, GeoUnit var4);

    List<String> geohash(String var1, String... var2);

    List<GeoCoordinate> geopos(String var1, String... var2);

    List<GeoRadiusResponse> georadius(String var1, double var2, double var4, double var6, GeoUnit var8);

    List<GeoRadiusResponse> georadiusReadonly(String var1, double var2, double var4, double var6, GeoUnit var8);

    List<GeoRadiusResponse> georadius(String var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9);

    List<GeoRadiusResponse> georadiusReadonly(String var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9);

    List<GeoRadiusResponse> georadiusByMember(String var1, String var2, double var3, GeoUnit var5);

    List<GeoRadiusResponse> georadiusByMemberReadonly(String var1, String var2, double var3, GeoUnit var5);

    List<GeoRadiusResponse> georadiusByMember(String var1, String var2, double var3, GeoUnit var5, GeoRadiusParam var6);

    List<GeoRadiusResponse> georadiusByMemberReadonly(String var1, String var2, double var3, GeoUnit var5, GeoRadiusParam var6);

    //basic
    Boolean exists(byte[] key);

    MultiWriteResult<Long> persist(byte[] key);

    String type(byte[] key);

    byte[] dump(byte[] key);

    MultiWriteResult<String> restore(byte[] key, int ttl, byte[] serializedValue);

    MultiWriteResult<Long> pexpire(byte[] key, long milliseconds);

    MultiWriteResult<Long> expireAt(byte[] key, long unixTime);

    MultiWriteResult<Long> pexpireAt(byte[] key, long millisecondsTimestamp);

    Long ttl(byte[] key);

    Long pttl(byte[] key);

    MultiWriteResult<Long> touch(byte[] key);

    MultiWriteResult<Long> persist(String var1);

    String type(String var1);

    byte[] dump(String var1);

    MultiWriteResult<String> restore(String var1, int var2, byte[] var3);

    MultiWriteResult<Long> pexpire(String var1, long var2);

    MultiWriteResult<Long> pexpireAt(String var1, long var2);

    Long pttl(String var1);

    MultiWriteResult<Long> touch(String var1);

    //202003 -- end

    /**
     * 手动切换major和minor
     * @return
     */
    boolean manualSwitchMajorMinor();

    /**
     * 自动切换major和minor
     * @return
     */
    boolean autoSwitchMajorMinor();
    
    /**
     * 获取拓扑
     * @return
     */
    RedisCrossRoomTopology getRedisClusterCrossRoomInfo();

    /**
     * 检测几分钟前的错误
     * @return
     */
    public int getAlarmSwitchMinutes();

    /**
     * 错误报警阀值
     * @return
     */
    public double getAlarmSwitchErrorPercentage();
    
    /**
     * 自动切换至少次数
     */
    public int getSwitchMinCount();

    /**
     * 获取最近的统计信息
     */
    public Map<String, Map<String,Long>> getRecentMinutesStat();

    /**
     * 获取 主pipelineCluster
     */
    public PipelineCluster getMajorPipelineCluster();

    /**
     * 获取 备pipelineCluster
     */
    public PipelineCluster getMinorPipelineCluster();

}
