package com.sohu.tv.cachecloud.client.redis.crossroom.impl;

import com.sohu.tv.cachecloud.client.redis.crossroom.RedisCrossRoomClient;
import com.sohu.tv.cachecloud.client.redis.crossroom.command.ReadCommand;
import com.sohu.tv.cachecloud.client.redis.crossroom.command.WriteCommand;
import com.sohu.tv.cachecloud.client.redis.crossroom.entity.RedisCrossRoomTopology;
import com.sohu.tv.cachecloud.client.redis.crossroom.enums.HystrixStatCountTypeEnum;
import com.sohu.tv.cachecloud.client.redis.crossroom.enums.MultiWriteResult;
import com.sohu.tv.cachecloud.client.redis.crossroom.enums.SwitchTypeEnum;
import com.sohu.tv.cachecloud.client.redis.crossroom.mark.RedisCrossRoomAutoSwitchInterface;
import com.sohu.tv.cachecloud.client.redis.crossroom.notify.RedisCrossRoomAutoSwitchNotifier;
import com.sohu.tv.cachecloud.client.redis.crossroom.stat.RedisCrossRoomClientStatusChecker;
import com.sohu.tv.cachecloud.client.redis.crossroom.stat.RedisCrossRoomClientStatusCollector;
import com.sohu.tv.cachecloud.client.redis.crossroom.util.DateUtil;
import com.sohu.tv.cc.client.spectator.util.AtomicLongMap;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 * 跨机房智能客户端(rediscluster)
 *
 * @author leifu
 * @Date 2016年4月5日
 * @Time 下午4:32:34
 */
public class RedisClusterCrossRoomClientImpl implements RedisCrossRoomClient {
    private Logger logger = LoggerFactory.getLogger(RedisClusterCrossRoomClientImpl.class);

    /**
     * 主
     */
    private PipelineCluster majorPipelineCluster;

    /**
     * 备
     */
    private PipelineCluster minorPipelineCluster;

    /**
     * 主appid
     */
    private long majorAppId;

    /**
     * 备appid
     */
    private long minorAppId;

    /**
     * 通知
     */
    private RedisCrossRoomAutoSwitchNotifier redisCrossRoomAutoSwitchNotifier;

    /**
     * 检测有效分钟
     */
    private int alarmSwitchMinutes;

    /**
     * 检测错误率
     */
    private double alarmSwitchErrorPercentage;

    /**
     * 默认不切换major和minor
     */
    public boolean switchEnabled;

    /**
     * 至少调用xx次，才进行switch
     */
    private int switchMinCount;


    public RedisClusterCrossRoomClientImpl(long majorAppId, PipelineCluster majorPipelineCluster, long minorAppId,
                                           PipelineCluster minorPipelineCluster,
                                           RedisCrossRoomAutoSwitchNotifier redisCrossRoomAutoSwitchNotifier,
                                           int alarmSwitchMinutes, double alarmSwitchErrorPercentage, int switchMinCount, boolean switchEnabled) {
        this.majorAppId = majorAppId;
        this.majorPipelineCluster = majorPipelineCluster;
        this.minorAppId = minorAppId;
        this.minorPipelineCluster = minorPipelineCluster;
        this.redisCrossRoomAutoSwitchNotifier = redisCrossRoomAutoSwitchNotifier;
        this.alarmSwitchMinutes = alarmSwitchMinutes;
        this.alarmSwitchErrorPercentage = alarmSwitchErrorPercentage;
        this.switchMinCount = switchMinCount;
        this.switchEnabled = switchEnabled;
        RedisCrossRoomAutoSwitchInterface.AUTO_SWITCH_ENABLED.set(switchEnabled);
        //是否开启自动switch功能
        if (RedisCrossRoomAutoSwitchInterface.AUTO_SWITCH_ENABLED.get()) {
            RedisCrossRoomClientStatusChecker redisCrossRoomClientStatusChecker = new RedisCrossRoomClientStatusChecker(this);
            redisCrossRoomClientStatusChecker.start();
        }
    }

    @Override
    public MultiWriteResult<String> set(final String key, final String value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.set(key, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.set(key, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("set key %s value %s", key, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> set(final byte[] key, final byte[] value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.set(key, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.set(key, value);
            }

            @Override
            protected String getCommandParam() {

                return String.format("set key %s value %s",  new String(key, Charset.forName("UTF-8")),  new String(value, Charset.forName("UTF-8")));
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> set(final String key, final byte[] value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.set(key, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.set(key, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("set key %s value %s", key, new String(value, Charset.forName("UTF-8")));
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> setex(final String key, final int seconds, final String value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.setex(key, seconds, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.setex(key, seconds, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setex key %s value %s expire %s", key, value, seconds);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> setex(final byte[] key, final int seconds, final byte[] value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.setex(key, seconds, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.setex(key, seconds, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setex key %s value %s expire %s",  new String(key, Charset.forName("UTF-8")),  new String(value, Charset.forName("UTF-8")), seconds);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> setex(final String key, final int seconds, final byte[] value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.setex(key, seconds, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.setex(key, seconds, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setex key %s value %s expire %s", key,  new String(value, Charset.forName("UTF-8")), seconds);
            }
        }.write();
    }


    @Override
    public MultiWriteResult<Long> setnx(final String key, final String value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.setnx(key, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.setnx(key, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setnx key %s value %s", key, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> setnx(final byte[] key, final byte[] value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.setnx(key, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.setnx(key, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setnx key %s value %s", new String(key, Charset.forName("UTF-8")), new String(value, Charset.forName("UTF-8")));
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> expireAt(final String key, final long unixTime) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.expireAt(key, unixTime);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.expireAt(key, unixTime);
            }

            @Override
            protected String getCommandParam() {
                return String.format("expireAt key %s unixTime %s", key, unixTime);
            }
        }.write();
    }


    @Override
    public String get(final String key) {
        return new ReadCommand<String>() {

            @Override
            protected String readMajor() {
                return majorPipelineCluster.get(key);
            }

            @Override
            protected String readMinor() {
                return minorPipelineCluster.get(key);
            }
        }.read();

    }

    @Override
    public byte[] getBytes(final String key) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.getBytes(key);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.getBytes(key);
            }
        }.read();
    }

    @Override
    public Map<String, byte[]> mgetBytes(final List<String> keys) {
        return new ReadCommand<Map<String, byte[]>>() {

            @Override
            protected Map<String, byte[]> readMajor() {
                return majorPipelineCluster.mgetBytes(keys);
            }

            @Override
            protected Map<String, byte[]> readMinor() {
                return minorPipelineCluster.mgetBytes(keys);
            }
        }.read();
    }

    @Override
    public Map<String, String> mget(final List<String> keys) {
        return new ReadCommand<Map<String, String>>() {

            @Override
            protected Map<String, String> readMajor() {
                return majorPipelineCluster.mget(keys);
            }

            @Override
            protected Map<String, String> readMinor() {
                return minorPipelineCluster.mget(keys);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<String> msetBytes(final Map<String, byte[]> keyValueMap) {
        return new WriteCommand<String>() {

            @Override
            protected String writeMajor() {
                return majorPipelineCluster.msetBytes(keyValueMap);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.msetBytes(keyValueMap);
            }

            @Override
            protected String getCommandParam() {
                return String.format("mset %s", keyValueMap.toString());
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> mset(final Map<String, String> keyValueMap) {
        return new WriteCommand<String>() {

            @Override
            protected String writeMajor() {
                return majorPipelineCluster.mset(keyValueMap);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.mset(keyValueMap);
            }

            @Override
            protected String getCommandParam() {
                return String.format("mset %s", keyValueMap.toString());
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Map<String, Long>> mexpire(final Map<String, Integer> keyTimeMap) {
        return new WriteCommand<Map<String, Long>>() {

            @Override
            protected Map<String, Long> writeMajor() {
                return majorPipelineCluster.mexpire(keyTimeMap);
            }

            @Override
            protected Map<String, Long> writeMinor() {
                return minorPipelineCluster.mexpire(keyTimeMap);
            }

            @Override
            protected String getCommandParam() {
                return String.format("mexpire %s", keyTimeMap.toString());
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> del(final String key) {
        return new WriteCommand<Long>() {

            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.del(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.del(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("del key %s", key);
            }
        }.write();
    }

    @Override
    public Boolean exists(final String key) {
        return new ReadCommand<Boolean>() {

            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.exists(key);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.exists(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> incr(final String key) {
        return new WriteCommand<Long>() {

            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.incr(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.incr(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("incr key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> incrBy(final String key, final long integer) {
        return new WriteCommand<Long>() {

            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.incrBy(key, integer);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.incrBy(key, integer);
            }

            @Override
            protected String getCommandParam() {
                return String.format("incrby key %s integer %s", key, integer);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> expire(final String key, final int seconds) {
        return new WriteCommand<Long>() {

            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.expire(key, seconds);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.expire(key, seconds);
            }

            @Override
            protected String getCommandParam() {
                return String.format("expire key %s %s seconds", key, seconds);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> expire(final byte[] key, final int seconds) {
        return new WriteCommand<Long>() {

            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.expire(key, seconds);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.expire(key, seconds);
            }

            @Override
            protected String getCommandParam() {
                return String.format("expire key %s %s seconds",  new String(key, Charset.forName("UTF-8")), seconds);
            }
        }.write();
    }

    @Override
    public byte[] get(final byte[] key) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.get(key);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.get(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> hset(final byte[] key, final byte[] field, final byte[] value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hset(key, field, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hset(key, field, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hset key %s field %s value %s",  new String(key, Charset.forName("UTF-8")),  new String(field, Charset.forName("UTF-8")),  new String(value, Charset.forName("UTF-8")));
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> hset(final String key, final String field, final String value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hset(key, field, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hset(key, field, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hset key %s field %s value %s", key, field, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> hsetnx(final String key, final String field, final String value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hsetnx(key, field, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hsetnx(key, field, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hsetnx key %s field %s value %s", key, field, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> hmset(final String key, final Map<String, String> hash) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.hmset(key, hash);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.hmset(key, hash);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hmset key %s hash %s", key, hash);
            }
        }.write();
    }

    @Override
    public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
        return new ReadCommand<List<byte[]>>() {
            @Override
            protected List<byte[]> readMajor() {
                return majorPipelineCluster.hmget(key, fields);
            }

            @Override
            protected List<byte[]> readMinor() {
                return minorPipelineCluster.hmget(key, fields);
            }
        }.read();
    }

    @Override
    public List<String> hmget(final String key, final String... fields) {
        return new ReadCommand<List<String>>() {
            @Override
            protected List<String> readMajor() {
                return majorPipelineCluster.hmget(key, fields);
            }

            @Override
            protected List<String> readMinor() {
                return minorPipelineCluster.hmget(key, fields);
            }
        }.read();
    }

    @Override
    public String hget(final String key, final String field) {
        return new ReadCommand<String>() {
            @Override
            protected String readMajor() {
                return majorPipelineCluster.hget(key, field);
            }

            @Override
            protected String readMinor() {
                return minorPipelineCluster.hget(key, field);
            }
        }.read();
    }

    @Override
    public byte[] hget(final byte[] key, final byte[] field) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.hget(key, field);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.hget(key, field);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> hincrBy(final String key, final String field, final long value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hincrBy(key, field, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hincrBy(key, field, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hincrBy key %s field %s value %s", key, field, value);
            }
        }.write();
    }

    @Override
    public Map<String, String> hgetAll(final String key) {
        return new ReadCommand<Map<String, String>>() {
            @Override
            protected Map<String, String> readMajor() {
                return majorPipelineCluster.hgetAll(key);
            }

            @Override
            protected Map<String, String> readMinor() {
                return minorPipelineCluster.hgetAll(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> hdel(final String key, final String... fields) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hdel(key, fields);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hdel(key, fields);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hdel key %s fields %s ", key, fields == null ? "" : Arrays.toString(fields));
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> hmsetBytes(final String key, final Map<byte[], byte[]> hash) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.hmsetBytes(key, hash);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.hmsetBytes(key, hash);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hmsetBytes key %s hash %s", key, hash);
            }
        }.write();
    }

    @Override
    public Set<String> hkeys(final String key) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.hkeys(key);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.hkeys(key);
            }
        }.read();
    }

    @Override
    public Long hlen(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.hlen(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.hlen(key);
            }
        }.read();
    }

    @Override
    public Set<String> zrange(final String key, final long start, final long stop) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrange(key, start, stop);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrange(key, start, stop);
            }
        }.read();
    }

    @Override
    public Boolean sismember(final String key, final byte[] member) {

        return new ReadCommand<Boolean>() {
            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.sismember(key, member);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.sismember(key, member);
            }
        }.read();
    }

    @Override
    public Set<String> zrevrange(final String key, final long start, final long stop) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrevrange(key, start, stop);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrevrange(key, start, stop);
            }
        }.read();
    }

    @Override
    public List<String> lrange(final String key, final long start, final long stop) {
        return new ReadCommand<List<String>>() {
            @Override
            protected List<String> readMajor() {
                return majorPipelineCluster.lrange(key, start, stop);
            }

            @Override
            protected List<String> readMinor() {
                return minorPipelineCluster.lrange(key, start, stop);
            }
        }.read();
    }

    public Set<String> smembers(final String key) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.smembers(key);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.smembers(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> srem(final String key, final byte[]... member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.srem(key, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.srem(key, member);
            }

            @Override
            protected String getCommandParam() {
                StringBuilder sb = new StringBuilder();
                for(byte[] bytes : member){
                    sb.append(new String(bytes, Charset.forName("UTF-8")));
                }
                return String.format("srem key %s ,member %s", key, sb.toString());
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zrem(final String key, final byte[]... member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zrem(key, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zrem(key, member);
            }

            @Override
            protected String getCommandParam() {
                StringBuilder sb = new StringBuilder();
                for(byte[] bytes : member){
                    sb.append(new String(bytes, Charset.forName("UTF-8")));
                }
                return String.format("zrem key %s ,member %s", key, sb.toString());
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> sadd(final String key, final byte[]... member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.sadd(key, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.sadd(key, member);
            }

            @Override
            protected String getCommandParam() {
                StringBuilder sb = new StringBuilder();
                for(byte[] bytes : member){
                    sb.append(new String(bytes, Charset.forName("UTF-8")));
                }
                return String.format("sadd key %s ,member %s", key, sb.toString());
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zadd(final String key, final double score, final byte[] member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, score, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, score, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , score %s, member %s", key, score,  new String(member, Charset.forName("UTF-8")));
            }
        }.write();
    }

    public MultiWriteResult<Long> zadd(final String key, final Map<String, Double> scoreMembers) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, scoreMembers);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, scoreMembers);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , score member %s", key, scoreMembers);
            }
        }.write();
    }

    public MultiWriteResult<String> lpop(final String key) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.lpop(key);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.lpop(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lpop key %s ", key);
            }
        }.write();
    }

    public Long llen(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.llen(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.llen(key);
            }
        }.read();
    }

    public MultiWriteResult<Long> rpush(final String key, final String... string) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.rpush(key, string);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.rpush(key, string);
            }

            @Override
            protected String getCommandParam() {
                return String.format("rpush key %s , value %s", key, Arrays.asList(string));
            }
        }.write();
    }

    public MultiWriteResult<Long> rpush(final String key, final byte[]... string) {
        return new WriteCommand<Long>(

        ) {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.rpush(key, string);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.rpush(key, string);
            }

            @Override
            protected String getCommandParam() {
                return String.format("rpush key %s , value %s", key, Arrays.asList(string));
            }
        }.write();
    }

    public Boolean sismember(final String key, final String member) {
        return new ReadCommand<Boolean>() {
            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.sismember(key, member);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.sismember(key, member);
            }
        }.read();
    }

    public MultiWriteResult<Long> mdel(final List<String> keys) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.mdel(keys);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.mdel(keys);
            }

            @Override
            protected String getCommandParam() {
                return String.format("mdel keys %s ", keys);
            }
        }.write();
    }

    public MultiWriteResult<Map<String, Long>> mzadds(final Map<String, Map<String, Double>> map) {
        return new WriteCommand<Map<String, Long>>() {
            @Override
            protected Map<String, Long> writeMajor() {
                return majorPipelineCluster.mzadds(map);
            }

            @Override
            protected Map<String, Long> writeMinor() {
                return minorPipelineCluster.mzadds(map);
            }

            @Override
            protected String getCommandParam() {
                return String.format("mzadds map %s ", map);
            }
        }.write();
    }

    public MultiWriteResult<Long> zrem(final String key, final String... members) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zrem(key, members);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zrem(key, members);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zrem key %s , members %s", key, Arrays.asList(members));
            }
        }.write();
    }

    public Long ttl(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.ttl(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.ttl(key);
            }
        }.read();
    }

    public MultiWriteResult<Long> sadd(final String key, final String... member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.sadd(key, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.sadd(key, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("sadd key %s , member %s", key, Arrays.asList(member));
            }
        }.write();
    }

    public Map<String, Map<String, String>> mHgetAll(final List<String> keys) {
        return new ReadCommand<Map<String, Map<String, String>>>() {
            @Override
            protected Map<String, Map<String, String>> readMajor() {
                return majorPipelineCluster.mHgetAll(keys);
            }

            @Override
            protected Map<String, Map<String, String>> readMinor() {
                return minorPipelineCluster.mHgetAll(keys);
            }
        }.read();
    }

    public Long scard(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.scard(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.scard(key);
            }
        }.read();
    }

    public MultiWriteResult<String> set(final String key, final String value, final SetParams params) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.set(key, value, params);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.set(key, value, params);
            }

            @Override
            protected String getCommandParam() {
                return String.format("set key %s , value %s , param %s", key, value, params);
            }
        }.write();

    }


    public MultiWriteResult<String> set(final byte[] key, final byte[] value, final SetParams setParams) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.set(key, value, setParams);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.set(key, value, setParams);
            }

            @Override
            protected String getCommandParam() {
                return String.format("set key %s , value %s , param %s", key, value, setParams);
            }
        }.write();
    }

    public Map<byte[], byte[]> hgetAllBytes(final String key) {
        return new ReadCommand<Map<byte[], byte[]>>() {

            @Override
            protected Map<byte[], byte[]> readMajor() {
                return majorPipelineCluster.hgetAllBytes(key);
            }

            @Override
            protected Map<byte[], byte[]> readMinor() {
                return minorPipelineCluster.hgetAllBytes(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<StreamEntryID> xadd(final String key, final StreamEntryID id, final Map<String, String> hash) {
        return new WriteCommand<StreamEntryID>() {
            @Override
            protected StreamEntryID writeMajor() {
                return majorPipelineCluster.xadd(key, id, hash);
            }

            @Override
            protected StreamEntryID writeMinor() {
                return minorPipelineCluster.xadd(key, id, hash);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xadd key %s , id %s , hash %s", key, id, hash);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<StreamEntryID> xadd(final String key, final StreamEntryID id, final Map<String, String> hash, final long maxLen, final boolean approximateLength) {
        return new WriteCommand<StreamEntryID>() {
            @Override
            protected StreamEntryID writeMajor() {
                return majorPipelineCluster.xadd(key, id, hash, maxLen, approximateLength);
            }

            @Override
            protected StreamEntryID writeMinor() {
                return minorPipelineCluster.xadd(key, id, hash, maxLen, approximateLength);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xadd key %s , id %s , hash %s , maxLen %s , approximateLength %s", key, id, hash, maxLen, approximateLength);
            }
        }.write();
    }

    @Override
    public Long xlen(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.xlen(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.xlen(key);
            }
        }.read();
    }

    @Override
    public List<StreamEntry> xrange(final String key, final StreamEntryID start, final StreamEntryID end, final int count) {
        return new ReadCommand<List<StreamEntry>>() {
            @Override
            protected List<StreamEntry> readMajor() {
                return majorPipelineCluster.xrange(key, start, end, count);
            }

            @Override
            protected List<StreamEntry> readMinor() {
                return minorPipelineCluster.xrange(key, start, end, count);
            }
        }.read();
    }

    @Override
    public List<StreamEntry> xrevrange(final String key, final StreamEntryID end, final StreamEntryID start, final int count) {
        return new ReadCommand<List<StreamEntry>>() {
            @Override
            protected List<StreamEntry> readMajor() {
                return majorPipelineCluster.xrevrange(key, end, start, count);
            }

            @Override
            protected List<StreamEntry> readMinor() {
                return minorPipelineCluster.xrevrange(key, end, start, count);
            }
        }.read();
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xread(final int count, final long block, final Entry... streams) {
        return new ReadCommand<List<Entry<String, List<StreamEntry>>>>() {
            @Override
            protected List<Entry<String, List<StreamEntry>>> readMajor() {
                return majorPipelineCluster.xread(count, block, streams);
            }

            @Override
            protected List<Entry<String, List<StreamEntry>>> readMinor() {
                return minorPipelineCluster.xread(count, block, streams);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> xack(final String key, final String group, final StreamEntryID... ids) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.xack(key, group, ids);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.xack(key, group, ids);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xack key %s , group %s , ids %s", key, group, ids);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> xgroupCreate(final String key, final String groupName, final StreamEntryID id, final boolean makeStream) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.xgroupCreate(key, groupName, id, makeStream);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.xgroupCreate(key, groupName, id, makeStream);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xgroupCreate key %s , groupName %s , id %s , makeStream %s", key, groupName, id, makeStream);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> xgroupSetID(final String key, final String groupName, final StreamEntryID id) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.xgroupSetID(key, groupName, id);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.xgroupSetID(key, groupName, id);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xgroupSetID key %s , groupName %s , id %s", key, groupName, id);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> xgroupDestroy(final String key, final String groupName) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.xgroupDestroy(key, groupName);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.xgroupDestroy(key, groupName);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xgroupDestroy key %s , groupName %s", key, groupName);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> xgroupDelConsumer(final String key, final String groupName, final String consumerName) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.xgroupDelConsumer(key, groupName, consumerName);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.xgroupDelConsumer(key, groupName, consumerName);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xgroupDelConsumer key %s , groupName %s , consumerName %s", key, groupName, consumerName);
            }
        }.write();
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xreadGroup(final String groupName, final String consumer, final int count, final long block, final boolean noAck, final Entry... streams) {
        return new ReadCommand<List<Entry<String, List<StreamEntry>>>>() {
            @Override
            protected List<Entry<String, List<StreamEntry>>> readMajor() {
                return majorPipelineCluster.xreadGroup(groupName, consumer, count, block, noAck, streams);
            }

            @Override
            protected List<Entry<String, List<StreamEntry>>> readMinor() {
                return minorPipelineCluster.xreadGroup(groupName, consumer, count, block, noAck, streams);
            }
        }.read();
    }

    @Override
    public List<StreamPendingEntry> xpending(final String key, final String groupName, final StreamEntryID start, final StreamEntryID end, final int count, final String consumerName) {
        return new ReadCommand<List<StreamPendingEntry>>() {
            @Override
            protected List<StreamPendingEntry> readMajor() {
                return majorPipelineCluster.xpending(key, groupName, start, end, count, consumerName);
            }

            @Override
            protected List<StreamPendingEntry> readMinor() {
                return minorPipelineCluster.xpending(key, groupName, start, end, count, consumerName);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> xdel(final String key, final StreamEntryID... ids) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.xdel(key, ids);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.xdel(key, ids);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xdel key %s , ids %s", key, ids);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> xtrim(final String key, final long maxLen, final boolean approximateLength) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.xtrim(key, maxLen, approximateLength);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.xtrim(key, maxLen, approximateLength);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xtrim key %s , maxLen %s , approximateLength %s", key, maxLen, approximateLength);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<StreamEntry>> xclaim(final String key, final String group, final String consumerName, final long minIdleTime, final long newIdleTime, final int retries, final boolean force, final StreamEntryID... ids) {
        return new WriteCommand<List<StreamEntry>>() {
            @Override
            protected List<StreamEntry> writeMajor() {
                return majorPipelineCluster.xclaim(key, group, consumerName, minIdleTime, newIdleTime, retries, force, ids);
            }

            @Override
            protected List<StreamEntry> writeMinor() {
                return minorPipelineCluster.xclaim(key, group, consumerName, minIdleTime, newIdleTime, retries, force, ids);
            }

            @Override
            protected String getCommandParam() {
                return String.format("xclaim key %s , group %s , consumerName %s , minIdelTime %s , newIdleTime %s , retries %s , " +
                        "force %s , ids %s", key, group, consumerName, minIdleTime, newIdleTime, retries, force, ids);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> psetex(final byte[] key, final long milliseconds, final byte[] value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.psetex(key, milliseconds, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.psetex(key, milliseconds, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("psetex key %s , millseconds %s , value %s", key, milliseconds, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> psetex(final String key, final long milliseconds, final String value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.psetex(key, milliseconds, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.psetex(key, milliseconds, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("psetex key %s , millseconds %s , value %s", key, milliseconds, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Boolean> setbit(final byte[] key, final long offset, final boolean value) {
        return new WriteCommand<Boolean>() {
            @Override
            protected Boolean writeMajor() {
                return majorPipelineCluster.setbit(key, offset, value);
            }

            @Override
            protected Boolean writeMinor() {
                return minorPipelineCluster.setbit(key, offset, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setbit key %s , offset %s , value %s", key, offset, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Boolean> setbit(final byte[] key, final long offset, final byte[] value) {
        return new WriteCommand<Boolean>() {
            @Override
            protected Boolean writeMajor() {
                return majorPipelineCluster.setbit(key, offset, value);
            }

            @Override
            protected Boolean writeMinor() {
                return minorPipelineCluster.setbit(key, offset, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setbit key %s , offset %s , value %s", key, offset, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Boolean> setbit(final String key, final long offset, final boolean value) {
        return new WriteCommand<Boolean>() {
            @Override
            protected Boolean writeMajor() {
                return majorPipelineCluster.setbit(key, offset, value);
            }

            @Override
            protected Boolean writeMinor() {
                return minorPipelineCluster.setbit(key, offset, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setbit key %s , offset %s , value %s", key, offset, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Boolean> setbit(final String key, final long offset, final String value) {
        return new WriteCommand<Boolean>() {
            @Override
            protected Boolean writeMajor() {
                return majorPipelineCluster.setbit(key, offset, value);
            }

            @Override
            protected Boolean writeMinor() {
                return minorPipelineCluster.setbit(key, offset, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setbit key %s , offset %s , value %s", key, offset, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> setrange(final byte[] key, final long offset, final byte[] value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.setrange(key, offset, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.setrange(key, offset, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setrange key %s , offset %s , value %s", key, offset, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> setrange(final String key, final long offset, final String value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.setrange(key, offset, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.setrange(key, offset, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("setrange key %s , offset %s , value %s", key, offset, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<byte[]> getSet(final byte[] key, final byte[] value) {
        return new WriteCommand<byte[]>() {
            @Override
            protected byte[] writeMajor() {
                return majorPipelineCluster.getSet(key, value);
            }

            @Override
            protected byte[] writeMinor() {
                return minorPipelineCluster.getSet(key, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("getset key %s , value %s", key, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> getSet(final String key, final String value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.getSet(key, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.getSet(key, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("getset key %s , value %s", key, value);
            }
        }.write();
    }

    @Override
    public Boolean getbit(final String key, final long offset) {
        return new ReadCommand<Boolean>() {
            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.getbit(key, offset);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.getbit(key, offset);
            }
        }.read();
    }

    @Override
    public String getrange(final String key, final long startOffset, final long endOffset) {
        return new ReadCommand<String>() {
            @Override
            protected String readMajor() {
                return majorPipelineCluster.getrange(key, startOffset, endOffset);
            }

            @Override
            protected String readMinor() {
                return minorPipelineCluster.getrange(key, startOffset, endOffset);
            }
        }.read();
    }

    @Override
    public Boolean getbit(final byte[] key, final long offset) {
        return new ReadCommand<Boolean>() {
            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.getbit(key, offset);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.getbit(key, offset);
            }
        }.read();
    }

    @Override
    public byte[] getrange(final byte[] key, final long startOffset, final long endOffset) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.getrange(key, startOffset, endOffset);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.getrange(key, startOffset, endOffset);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> decrBy(final byte[] key, final long decrement) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.decrBy(key, decrement);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.decrBy(key, decrement);
            }

            @Override
            protected String getCommandParam() {
                return String.format("decrBy key %s , decrement %s", key, decrement);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> decr(final byte[] key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.decr(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.decr(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("decr key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> incrBy(final byte[] key, final long increment) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.incrBy(key, increment);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.incrBy(key, increment);
            }

            @Override
            protected String getCommandParam() {
                return String.format("incrBy key %s , increment %s", key, increment);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Double> incrByFloat(final byte[] key, final double increment) {
        return new WriteCommand<Double>() {
            @Override
            protected Double writeMajor() {
                return majorPipelineCluster.incrByFloat(key, increment);
            }

            @Override
            protected Double writeMinor() {
                return minorPipelineCluster.incrByFloat(key, increment);
            }

            @Override
            protected String getCommandParam() {
                return String.format("incrByFloat key %s , increment %s", key, increment);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> incr(final byte[] key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.incr(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.incr(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("incr key %s", key);
            }
        }.write();
    }

    @Override
    public Long strlen(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.strlen(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.strlen(key);
            }
        }.read();
    }

    @Override
    public Long strlen(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.strlen(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.strlen(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<List<Long>> bitfield(final String key, final String... arguments) {
        return new WriteCommand<List<Long>>() {
            @Override
            protected List<Long> writeMajor() {
                return majorPipelineCluster.bitfield(key, arguments);
            }

            @Override
            protected List<Long> writeMinor() {
                return minorPipelineCluster.bitfield(key, arguments);
            }

            @Override
            protected String getCommandParam() {
                return String.format("bitfield key %s , args %s", key, arguments);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<Long>> bitfield(final byte[] key, final byte[]... arguments) {
        return new WriteCommand<List<Long>>() {
            @Override
            protected List<Long> writeMajor() {
                return majorPipelineCluster.bitfield(key, arguments);
            }

            @Override
            protected List<Long> writeMinor() {
                return minorPipelineCluster.bitfield(key, arguments);
            }

            @Override
            protected String getCommandParam() {
                return String.format("bitfield key %s , args %s", key, arguments);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> lset(final byte[] key, final long index, final byte[] value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.lset(key, index, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.lset(key, index, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lset key %s , index %s , value %s", key, index, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> lset(final String key, final long index, final String value) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.lset(key, index, value);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.lset(key, index, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lset key %s , index %s , value %s", key, index, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> lpush(final byte[] key, final byte[]... args) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.lpush(key, args);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.lpush(key, args);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lpush key %s , args %s", key, args);
            }
        }.write();
    }

    @Override
    public Long llen(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.llen(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.llen(key);
            }
        }.read();
    }

    @Override
    public List<byte[]> lrange(final byte[] key, final long start, final long stop) {
        return new ReadCommand<List<byte[]>>() {
            @Override
            protected List<byte[]> readMajor() {
                return majorPipelineCluster.lrange(key, start, stop);
            }

            @Override
            protected List<byte[]> readMinor() {
                return minorPipelineCluster.lrange(key, start, stop);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<String> ltrim(final byte[] key, final long start, final long stop) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.ltrim(key, start, stop);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.ltrim(key, start, stop);
            }

            @Override
            protected String getCommandParam() {
                return String.format("ltrim key %s , start %s , stop %s", key, start, stop);
            }
        }.write();
    }

    @Override
    public byte[] lindex(final byte[] key, final long index) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.lindex(key, index);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.lindex(key, index);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<byte[]> lpop(final byte[] key) {
        return new WriteCommand<byte[]>() {
            @Override
            protected byte[] writeMajor() {
                return majorPipelineCluster.lpop(key);
            }

            @Override
            protected byte[] writeMinor() {
                return minorPipelineCluster.lpop(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lpop key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> lrem(final byte[] key, final long count, final byte[] value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.lrem(key, count, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.lrem(key, count, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lrem key %s , count %s , value %s", key, count, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> lpush(final String key, final String... args) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.lpush(key, args);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.lpush(key, args);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lpush key %s , args %s", key, args);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> ltrim(final String key, final long start, final long stop) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.ltrim(key, start, stop);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.ltrim(key, start, stop);
            }

            @Override
            protected String getCommandParam() {
                return String.format("ltrim key %s , start %s , stop %s", key, start, stop);
            }
        }.write();
    }

    @Override
    public String lindex(final String key, final long index) {
        return new ReadCommand<String>() {
            @Override
            protected String readMajor() {
                return majorPipelineCluster.lindex(key, index);
            }

            @Override
            protected String readMinor() {
                return minorPipelineCluster.lindex(key, index);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> lrem(final String key, final long count, final String value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.lrem(key, count, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.lrem(key, count, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lrem key %s , count %s , value %s", key, count, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> rpush(final byte[] key, final byte[]... args) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.rpush(key, args);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.rpush(key, args);
            }

            @Override
            protected String getCommandParam() {
                return String.format("rpush key %s , args %s", key, args);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<byte[]> rpop(final byte[] key) {
        return new WriteCommand<byte[]>() {
            @Override
            protected byte[] writeMajor() {
                return majorPipelineCluster.rpop(key);
            }

            @Override
            protected byte[] writeMinor() {
                return minorPipelineCluster.rpop(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("rpop key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> rpop(final String key) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.rpop(key);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.rpop(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("rpop key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> linsert(final byte[] key, final ListPosition where, final byte[] pivot, final byte[] value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.linsert(key, where, pivot, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.linsert(key, where, pivot, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("linsert key %s , where %s , pivot %s , value %s", key, where, pivot, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> lpushx(final byte[] key, final byte[]... arg) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.lpushx(key, arg);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.lpushx(key, arg);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lpushx key %s , arg %s", key, arg);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> rpushx(final byte[] key, final byte[]... arg) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.rpushx(key, arg);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.rpushx(key, arg);
            }

            @Override
            protected String getCommandParam() {
                return String.format("rpushx key %s , arg %s", key, arg);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> linsert(final String key, final ListPosition where, final String pivot, final String value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.linsert(key, where, pivot, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.linsert(key, where, pivot, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("linsert key %s , where %s , pivot %s , value %s", key, where, pivot, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> lpushx(final String key, final String... arg) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.lpushx(key, arg);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.lpushx(key, arg);
            }

            @Override
            protected String getCommandParam() {
                return String.format("lpushx key %s , arg %s", key, arg);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> rpushx(final String key, final String... arg) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.rpushx(key, arg);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.rpushx(key, arg);
            }

            @Override
            protected String getCommandParam() {
                return String.format("rpushx key %s , arg %s", key, arg);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<String>> blpop(final int timeout, final String key) {
        return new WriteCommand<List<String>>() {
            @Override
            protected List<String> writeMajor() {
                return majorPipelineCluster.blpop(timeout, key);
            }

            @Override
            protected List<String> writeMinor() {
                return minorPipelineCluster.blpop(timeout, key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("blpop key %s , timeout %s", key, timeout);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<String>> brpop(final int timeout, final String key) {
        return new WriteCommand<List<String>>() {
            @Override
            protected List<String> writeMajor() {
                return majorPipelineCluster.brpop(timeout, key);
            }

            @Override
            protected List<String> writeMinor() {
                return minorPipelineCluster.brpop(timeout, key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("brpop key %s , timeout %s", key, timeout);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<byte[]>> blpop(final int timeout, final byte[]... key) {
        return new WriteCommand<List<byte[]>>() {
            @Override
            protected List<byte[]> writeMajor() {
                return majorPipelineCluster.blpop(timeout, key);
            }

            @Override
            protected List<byte[]> writeMinor() {
                return minorPipelineCluster.blpop(timeout, key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("blpop key %s , timeout %s", key, timeout);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<byte[]>> brpop(final int timeout, final byte[]... keys) {
        return new WriteCommand<List<byte[]>>() {
            @Override
            protected List<byte[]> writeMajor() {
                return majorPipelineCluster.brpop(timeout, keys);
            }

            @Override
            protected List<byte[]> writeMinor() {
                return minorPipelineCluster.brpop(timeout, keys);
            }

            @Override
            protected String getCommandParam() {
                return String.format("brpop keys %s , timeout %s", keys, timeout);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<byte[]> rpoplpush(final byte[] srckey, final byte[] dstkey) {
        return new WriteCommand<byte[]>() {
            @Override
            protected byte[] writeMajor() {
                return majorPipelineCluster.rpoplpush(srckey, dstkey);
            }

            @Override
            protected byte[] writeMinor() {
                return minorPipelineCluster.rpoplpush(srckey, dstkey);
            }

            @Override
            protected String getCommandParam() {
                return String.format("rpoplpush srckey %s , dstkey %s", srckey, dstkey);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<byte[]> brpoplpush(final byte[] srckey, final byte[] dstkey, final int timeout) {
        return new WriteCommand<byte[]>() {
            @Override
            protected byte[] writeMajor() {
                return majorPipelineCluster.brpoplpush(srckey, dstkey, timeout);
            }

            @Override
            protected byte[] writeMinor() {
                return minorPipelineCluster.brpoplpush(srckey, dstkey, timeout);
            }

            @Override
            protected String getCommandParam() {
                return String.format("brpoplpush srckey %s , dstkey %s , timeout %s", srckey, dstkey, timeout);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<String>> sort(final String key) {
        return new WriteCommand<List<String>>() {
            @Override
            protected List<String> writeMajor() {
                return majorPipelineCluster.sort(key);
            }

            @Override
            protected List<String> writeMinor() {
                return minorPipelineCluster.sort(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("sort key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<String>> sort(final String key, final SortingParams sortingParameters) {
        return new WriteCommand<List<String>>() {
            @Override
            protected List<String> writeMajor() {
                return majorPipelineCluster.sort(key, sortingParameters);
            }

            @Override
            protected List<String> writeMinor() {
                return minorPipelineCluster.sort(key, sortingParameters);
            }

            @Override
            protected String getCommandParam() {
                return String.format("sort key %s , sortingParameters %s", key, sortingParameters);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<byte[]>> sort(final byte[] key) {
        return new WriteCommand<List<byte[]>>() {
            @Override
            protected List<byte[]> writeMajor() {
                return majorPipelineCluster.sort(key);
            }

            @Override
            protected List<byte[]> writeMinor() {
                return minorPipelineCluster.sort(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("sort key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<List<byte[]>> sort(final byte[] key, final SortingParams sortingParameters) {
        return new WriteCommand<List<byte[]>>() {
            @Override
            protected List<byte[]> writeMajor() {
                return majorPipelineCluster.sort(key, sortingParameters);
            }

            @Override
            protected List<byte[]> writeMinor() {
                return minorPipelineCluster.sort(key, sortingParameters);
            }

            @Override
            protected String getCommandParam() {
                return String.format("sort key %s , sortingParameters %s", key, sortingParameters);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> hset(final byte[] key, final Map<byte[], byte[]> hash) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hset(key, hash);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hset(key, hash);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hset key %s , map %s", key, hash);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> hset(final String key, final Map<String, String> hash) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hset(key, hash);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hset(key, hash);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hset key %s , map %s", key, hash);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> hsetnx(final byte[] key, final byte[] field, final byte[] value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hsetnx(key, field, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hsetnx(key, field, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hsetnx key %s , field %s , value %s", key, field, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> hmset(final byte[] key, final Map<byte[], byte[]> hash) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.hmset(key, hash);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.hmset(key, hash);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hmset key %s , hash %s", key, hash);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> hincrBy(final byte[] key, final byte[] field, final long value) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hincrBy(key, field, value);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hincrBy(key, field, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hincrBy key %s , field %s , value %s", key, field, value);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Double> hincrByFloat(final byte[] key, final byte[] field, final double value) {
        return new WriteCommand<Double>() {
            @Override
            protected Double writeMajor() {
                return majorPipelineCluster.hincrByFloat(key, field, value);
            }

            @Override
            protected Double writeMinor() {
                return minorPipelineCluster.hincrByFloat(key, field, value);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hincrByFloat key %s , field %s , value %s", key, field, value);
            }
        }.write();
    }

    @Override
    public Boolean hexists(final byte[] key, final byte[] field) {
        return new ReadCommand<Boolean>() {
            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.hexists(key, field);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.hexists(key, field);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> hdel(final byte[] key, final byte[]... fields) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.hdel(key, fields);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.hdel(key, fields);
            }

            @Override
            protected String getCommandParam() {
                return String.format("hdel key %s , fields %s", key, fields);
            }
        }.write();
    }

    @Override
    public Long hlen(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.hlen(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.hlen(key);
            }
        }.read();
    }

    @Override
    public Set<byte[]> hkeys(final byte[] key) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.hkeys(key);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.hkeys(key);
            }
        }.read();
    }

    @Override
    public Collection<byte[]> hvals(final byte[] key) {
        return new ReadCommand<Collection<byte[]>>() {
            @Override
            protected Collection<byte[]> readMajor() {
                return majorPipelineCluster.hvals(key);
            }

            @Override
            protected Collection<byte[]> readMinor() {
                return minorPipelineCluster.hvals(key);
            }
        }.read();
    }

    @Override
    public Boolean hexists(final String key, final String field) {
        return new ReadCommand<Boolean>() {
            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.hexists(key, field);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.hexists(key, field);
            }
        }.read();
    }

    @Override
    public List<String> hvals(final String key) {
        return new ReadCommand<List<String>>() {
            @Override
            protected List<String> readMajor() {
                return majorPipelineCluster.hvals(key);
            }

            @Override
            protected List<String> readMinor() {
                return minorPipelineCluster.hvals(key);
            }
        }.read();
    }

    @Override
    public Long hstrlen(final String key, final String field) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.hstrlen(key, field);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.hstrlen(key, field);
            }
        }.read();
    }

    @Override
    public Long hstrlen(final byte[] key, final byte[] field) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.hstrlen(key, field);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.hstrlen(key, field);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> sadd(final byte[] key, final byte[]... member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.sadd(key, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.sadd(key, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("sadd key %s , member %s", key, member);
            }
        }.write();
    }

    @Override
    public Set<byte[]> smembers(final byte[] key) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.smembers(key);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.smembers(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> srem(final byte[] key, final byte[]... member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.srem(key, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.srem(key, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("srem key %s , member %s", key, member);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<byte[]> spop(final byte[] key) {
        return new WriteCommand<byte[]>() {
            @Override
            protected byte[] writeMajor() {
                return majorPipelineCluster.spop(key);
            }

            @Override
            protected byte[] writeMinor() {
                return minorPipelineCluster.spop(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("spop key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Set<byte[]>> spop(final byte[] key, final long count) {
        return new WriteCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> writeMajor() {
                return majorPipelineCluster.spop(key, count);
            }

            @Override
            protected Set<byte[]> writeMinor() {
                return minorPipelineCluster.spop(key, count);
            }

            @Override
            protected String getCommandParam() {
                return String.format("spop key %s , count %s", key, count);
            }
        }.write();
    }

    @Override
    public Long scard(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.scard(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.scard(key);
            }
        }.read();
    }

    @Override
    public Boolean sismember(final byte[] key, final byte[] member) {
        return new ReadCommand<Boolean>() {
            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.sismember(key, member);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.sismember(key, member);
            }
        }.read();
    }

    @Override
    public byte[] srandmember(final byte[] key) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.srandmember(key);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.srandmember(key);
            }
        }.read();
    }

    @Override
    public List<byte[]> srandmember(final byte[] key, final int count) {
        return new ReadCommand<List<byte[]>>() {
            @Override
            protected List<byte[]> readMajor() {
                return majorPipelineCluster.srandmember(key, count);
            }

            @Override
            protected List<byte[]> readMinor() {
                return minorPipelineCluster.srandmember(key, count);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> srem(final String key, final String... member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.srem(key, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.srem(key, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("srem key %s , member %s", key, member);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<String> spop(final String key) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.spop(key);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.spop(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("spop key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Set<String>> spop(final String key, final long count) {
        return new WriteCommand<Set<String>>() {
            @Override
            protected Set<String> writeMajor() {
                return majorPipelineCluster.spop(key, count);
            }

            @Override
            protected Set<String> writeMinor() {
                return minorPipelineCluster.spop(key, count);
            }

            @Override
            protected String getCommandParam() {
                return String.format("spop key %s , count %s", key, count);
            }
        }.write();
    }

    @Override
    public String srandmember(final String key) {
        return new ReadCommand<String>() {
            @Override
            protected String readMajor() {
                return majorPipelineCluster.srandmember(key);
            }

            @Override
            protected String readMinor() {
                return minorPipelineCluster.srandmember(key);
            }
        }.read();
    }

    @Override
    public List<String> srandmember(final String key, final int count) {
        return new ReadCommand<List<String>>() {
            @Override
            protected List<String> readMajor() {
                return majorPipelineCluster.srandmember(key, count);
            }

            @Override
            protected List<String> readMinor() {
                return minorPipelineCluster.srandmember(key, count);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> zadd(final byte[] key, final double score, final byte[] member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, score, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, score, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , score %s , member %s", key, score, member);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zadd(final byte[] key, final double score, final byte[] member, final ZAddParams params) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, score, member, params);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, score, member, params);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , score %s , member %s , params %s", key, score, member, params);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zadd(final byte[] key, final Map<byte[], Double> scoreMembers) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, scoreMembers);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, scoreMembers);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , scoreMembers %s", key, scoreMembers);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zadd(final byte[] key, final Map<byte[], Double> scoreMembers, final ZAddParams params) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, scoreMembers, params);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, scoreMembers, params);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , scoreMembers %s , params %s", key, scoreMembers, params);
            }
        }.write();
    }

    @Override
    public Set<byte[]> zrange(final byte[] key, final long start, final long stop) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrange(key, start, stop);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrange(key, start, stop);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> zrem(final byte[] key, final byte[]... members) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zrem(key, members);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zrem(key, members);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zrem key %s , members %s", key, members);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Double> zincrby(final byte[] key, final double increment, final byte[] member) {
        return new WriteCommand<Double>() {
            @Override
            protected Double writeMajor() {
                return majorPipelineCluster.zincrby(key, increment, member);
            }

            @Override
            protected Double writeMinor() {
                return minorPipelineCluster.zincrby(key, increment, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zincrby key %s , increment %s , member %s", key, increment, member);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Double> zincrby(final byte[] key, final double increment, final byte[] member, final ZIncrByParams params) {
        return new WriteCommand<Double>() {
            @Override
            protected Double writeMajor() {
                return majorPipelineCluster.zincrby(key, increment, member, params);
            }

            @Override
            protected Double writeMinor() {
                return minorPipelineCluster.zincrby(key, increment, member, params);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zincrby key %s , increment %s , member %s , params %s", key, increment, member, params);
            }
        }.write();
    }

    @Override
    public Long zrank(final byte[] key, final byte[] member) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zrank(key, member);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zrank(key, member);
            }
        }.read();
    }

    @Override
    public Long zrevrank(final byte[] key, final byte[] member) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zrevrank(key, member);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zrevrank(key, member);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrevrange(final byte[] key, final long start, final long stop) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrevrange(key, start, stop);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrevrange(key, start, stop);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeWithScores(final byte[] key, final long start, final long stop) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeWithScores(key, start, stop);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeWithScores(key, start, stop);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final byte[] key, final long start, final long stop) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeWithScores(key, start, stop);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeWithScores(key, start, stop);
            }
        }.read();
    }

    @Override
    public Long zcard(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zcard(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zcard(key);
            }
        }.read();
    }

    @Override
    public Double zscore(final byte[] key, final byte[] member) {
        return new ReadCommand<Double>() {
            @Override
            protected Double readMajor() {
                return majorPipelineCluster.zscore(key, member);
            }

            @Override
            protected Double readMinor() {
                return minorPipelineCluster.zscore(key, member);
            }
        }.read();
    }

    @Override
    public Long zcount(final byte[] key, final double min, final double max) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zcount(key, min, max);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zcount(key, min, max);
            }
        }.read();
    }

    @Override
    public Long zcount(final byte[] key, final byte[] min, final byte[] max) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zcount(key, min, max);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zcount(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrangeByScore(key, min, max);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrangeByScore(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrangeByScore(key, min, max);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrangeByScore(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrevrangeByScore(key, max, min);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrevrangeByScore(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max, final int offset, final int count) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrangeByScore(key, min, max, offset, count);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrangeByScore(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrevrangeByScore(key, max, min);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrevrangeByScore(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrangeByScore(key, min, max, offset, count);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrangeByScore(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min, final int offset, final int count) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrevrangeByScore(key, max, min, offset, count);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrevrangeByScore(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeByScoreWithScores(key, min, max);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeByScoreWithScores(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeByScoreWithScores(key, max, min);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeByScoreWithScores(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max, final int offset, final int count) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeByScoreWithScores(key, min, max, offset, count);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrevrangeByScore(key, max, min, offset, count);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrevrangeByScore(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeByScoreWithScores(key, min, max);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeByScoreWithScores(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeByScoreWithScores(key, max, min);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeByScoreWithScores(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeByScoreWithScores(key, min, max, offset, count);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min, final int offset, final int count) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> zremrangeByRank(final byte[] key, final long start, final long stop) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zremrangeByRank(key, start, stop);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zremrangeByRank(key, start, stop);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zremrangeByRank key %s , start %s , stop %s", key, start, stop);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zremrangeByScore(final byte[] key, final double min, final double max) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zremrangeByScore(key, min, max);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zremrangeByScore(key, min, max);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zremrangeByScore key %s , min %s , max %s", key, min, max);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zremrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zremrangeByScore(key, min, max);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zremrangeByScore(key, min, max);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zremrangeByScore key %s , min %s , max %s", key, min, max);
            }
        }.write();
    }

    @Override
    public Long zlexcount(final byte[] key, final byte[] min, final byte[] max) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zlexcount(key, min, max);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zlexcount(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrangeByLex(key, min, max);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrangeByLex(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrangeByLex(key, min, max, offset, count);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrangeByLex(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrevrangeByLex(key, max, min);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrevrangeByLex(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        return new ReadCommand<Set<byte[]>>() {
            @Override
            protected Set<byte[]> readMajor() {
                return majorPipelineCluster.zrevrangeByLex(key, max, min, offset, count);
            }

            @Override
            protected Set<byte[]> readMinor() {
                return minorPipelineCluster.zrevrangeByLex(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> zadd(final String key, final double score, final String member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, score, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, score, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , score %s , member %s", key, score, member);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zadd(final String key, final double score, final String member, final ZAddParams params) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, score, member, params);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, score, member, params);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , score %s , member %s , params %s", key, score, member, params);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zadd(final String key, final Map<String, Double> scoreMembers, final ZAddParams params) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zadd(key, scoreMembers, params);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zadd(key, scoreMembers, params);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zadd key %s , scoreMembers %s , params %s", key, scoreMembers, params);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Double> zincrby(final String key, final double increment, final String member) {
        return new WriteCommand<Double>() {
            @Override
            protected Double writeMajor() {
                return majorPipelineCluster.zincrby(key, increment, member);
            }

            @Override
            protected Double writeMinor() {
                return minorPipelineCluster.zincrby(key, increment, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zincrby key %s , increment %s , member %s", key, increment, member);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Double> zincrby(final String key, final double increment, final String member, final ZIncrByParams params) {
        return new WriteCommand<Double>() {
            @Override
            protected Double writeMajor() {
                return majorPipelineCluster.zincrby(key, increment, member, params);
            }

            @Override
            protected Double writeMinor() {
                return minorPipelineCluster.zincrby(key, increment, member, params);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zincrby key %s , increment %s , member %s , params %s", key, increment, member, params);
            }
        }.write();
    }

    @Override
    public Long zrank(final String key, final String member) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zrank(key, member);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zrank(key, member);
            }
        }.read();
    }

    @Override
    public Long zrevrank(final String key, final String member) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zrevrank(key, member);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zrevrank(key, member);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeWithScores(final String key, final long start, final long stop) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeWithScores(key, start, stop);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeWithScores(key, start, stop);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long stop) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeWithScores(key, start, stop);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeWithScores(key, start, stop);
            }
        }.read();
    }

    @Override
    public Long zcard(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zcard(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zcard(key);
            }
        }.read();
    }

    @Override
    public Double zscore(final String key, final String member) {
        return new ReadCommand<Double>() {
            @Override
            protected Double readMajor() {
                return majorPipelineCluster.zscore(key, member);
            }

            @Override
            protected Double readMinor() {
                return minorPipelineCluster.zscore(key, member);
            }
        }.read();
    }

    @Override
    public Long zcount(final String key, final double min, final double max) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zcount(key, min, max);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zcount(key, min, max);
            }
        }.read();
    }

    @Override
    public Long zcount(final String key, final String min, final String max) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zcount(key, min, max);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zcount(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrangeByScore(key, min, max);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrangeByScore(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrangeByScore(key, min, max);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrangeByScore(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrevrangeByScore(key, max, min);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrevrangeByScore(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max, final int offset, final int count) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrangeByScore(key, min, max, offset, count);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrangeByScore(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final String max, final String min) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrevrangeByScore(key, max, min);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrevrangeByScore(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max, final int offset, final int count) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrangeByScore(key, min, max, offset, count);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrangeByScore(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min, final int offset, final int count) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrevrangeByScore(key, max, min, offset, count);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrevrangeByScore(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeByScoreWithScores(key, min, max);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeByScoreWithScores(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeByScoreWithScores(key, max, min);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeByScoreWithScores(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max, final int offset, final int count) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeByScoreWithScores(key, min, max, offset, count);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final String max, final String min, final int offset, final int count) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrevrangeByScore(key, max, min, offset, count);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrevrangeByScore(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeByScoreWithScores(key, min, max);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeByScoreWithScores(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max, final String min) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeByScoreWithScores(key, max, min);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeByScoreWithScores(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max, final int offset, final int count) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrangeByScoreWithScores(key, min, max, offset, count);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min, final int offset, final int count) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max, final String min, final int offset, final int count) {
        return new ReadCommand<Set<Tuple>>() {
            @Override
            protected Set<Tuple> readMajor() {
                return majorPipelineCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }

            @Override
            protected Set<Tuple> readMinor() {
                return minorPipelineCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> zremrangeByRank(final String key, final long start, final long stop) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zremrangeByRank(key, start, stop);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zremrangeByRank(key, start, stop);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zremrangeByRank key %s , start %s , stop %s", key, start, stop);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zremrangeByScore(final String key, final double min, final double max) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zremrangeByScore(key, min, max);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zremrangeByScore(key, min, max);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zremrangeByScore key %s , min %s , max %s", key, min, max);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zremrangeByScore(final String key, final String min, final String max) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zremrangeByScore(key, min, max);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zremrangeByScore(key, min, max);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zremrangeByScore key %s , min %s , max %s", key, min, max);
            }
        }.write();
    }

    @Override
    public Long zlexcount(final String key, final String min, final String max) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.zlexcount(key, min, max);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.zlexcount(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrangeByLex(key, min, max);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrangeByLex(key, min, max);
            }
        }.read();
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max, final int offset, final int count) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrangeByLex(key, min, max, offset, count);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrangeByLex(key, min, max, offset, count);
            }
        }.read();
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrevrangeByLex(key, max, min);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrevrangeByLex(key, max, min);
            }
        }.read();
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min, final int offset, final int count) {
        return new ReadCommand<Set<String>>() {
            @Override
            protected Set<String> readMajor() {
                return majorPipelineCluster.zrevrangeByLex(key, max, min, offset, count);
            }

            @Override
            protected Set<String> readMinor() {
                return minorPipelineCluster.zrevrangeByLex(key, max, min, offset, count);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> zremrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zremrangeByLex(key, min, max);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zremrangeByLex(key, min, max);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zremrangeByLex key %s , min %s , max %s", key, min, max);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> zremrangeByLex(final String key, final String min, final String max) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.zremrangeByLex(key, min, max);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.zremrangeByLex(key, min, max);
            }

            @Override
            protected String getCommandParam() {
                return String.format("zremrangeByLex key %s , min %s , max %s", key, min, max);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> del(final byte[] key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.del(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.del(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("del key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> unlink(final byte[] key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.unlink(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.unlink(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("unlink key %s", key);
            }
        }.write();
    }

    @Override
    public byte[] echo(final byte[] arg) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.echo(arg);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.echo(arg);
            }
        }.read();
    }

    @Override
    public Long bitcount(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.bitcount(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.bitcount(key);
            }
        }.read();
    }

    @Override
    public Long bitcount(final byte[] key, final long start, final long end) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.bitcount(key, start, end);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.bitcount(key, start, end);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> pfadd(final byte[] key, final byte[]... elements) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.pfadd(key, elements);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.pfadd(key, elements);
            }

            @Override
            protected String getCommandParam() {
                return String.format("pfadd key %s , elements %s", key, elements);
            }
        }.write();
    }

    @Override
    public long pfcount(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.pfcount(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.pfcount(key);
            }
        }.read();
    }

    @Override
    public ScanResult<Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor) {
        return new ReadCommand<ScanResult<Entry<byte[], byte[]>>>() {
            @Override
            protected ScanResult<Entry<byte[], byte[]>> readMajor() {
                return majorPipelineCluster.hscan(key, cursor);
            }

            @Override
            protected ScanResult<Entry<byte[], byte[]>> readMinor() {
                return minorPipelineCluster.hscan(key, cursor);
            }
        }.read();
    }

    @Override
    public ScanResult<Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return new ReadCommand<ScanResult<Entry<byte[], byte[]>>>() {
            @Override
            protected ScanResult<Entry<byte[], byte[]>> readMajor() {
                return majorPipelineCluster.hscan(key, cursor, params);
            }

            @Override
            protected ScanResult<Entry<byte[], byte[]>> readMinor() {
                return minorPipelineCluster.hscan(key, cursor, params);
            }
        }.read();
    }

    @Override
    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor) {
        return new ReadCommand<ScanResult<byte[]>>() {
            @Override
            protected ScanResult<byte[]> readMajor() {
                return majorPipelineCluster.sscan(key, cursor);
            }

            @Override
            protected ScanResult<byte[]> readMinor() {
                return minorPipelineCluster.sscan(key, cursor);
            }
        }.read();
    }

    @Override
    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return new ReadCommand<ScanResult<byte[]>>() {
            @Override
            protected ScanResult<byte[]> readMajor() {
                return majorPipelineCluster.sscan(key, cursor, params);
            }

            @Override
            protected ScanResult<byte[]> readMinor() {
                return minorPipelineCluster.sscan(key, cursor, params);
            }
        }.read();
    }

    @Override
    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor) {
        return new ReadCommand<ScanResult<Tuple>>() {
            @Override
            protected ScanResult<Tuple> readMajor() {
                return majorPipelineCluster.zscan(key, cursor);
            }

            @Override
            protected ScanResult<Tuple> readMinor() {
                return minorPipelineCluster.zscan(key, cursor);
            }
        }.read();
    }

    @Override
    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return new ReadCommand<ScanResult<Tuple>>() {
            @Override
            protected ScanResult<Tuple> readMajor() {
                return majorPipelineCluster.zscan(key, cursor, params);
            }

            @Override
            protected ScanResult<Tuple> readMinor() {
                return minorPipelineCluster.zscan(key, cursor, params);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> unlink(final String key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.unlink(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.unlink(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("unlink key %s", key);
            }
        }.write();
    }

    @Override
    public String echo(final String arg) {
        return new ReadCommand<String>() {
            @Override
            protected String readMajor() {
                return majorPipelineCluster.echo(arg);
            }

            @Override
            protected String readMinor() {
                return minorPipelineCluster.echo(arg);
            }
        }.read();
    }

    @Override
    public Long bitcount(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.bitcount(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.bitcount(key);
            }
        }.read();
    }

    @Override
    public Long bitcount(final String key, final long start, final long end) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.bitcount(key, start, end);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.bitcount(key, start, end);
            }
        }.read();
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(final String key, final String cursor) {
        return new ReadCommand<ScanResult<Entry<String, String>>>() {
            @Override
            protected ScanResult<Entry<String, String>> readMajor() {
                return majorPipelineCluster.hscan(key, cursor);
            }

            @Override
            protected ScanResult<Entry<String, String>> readMinor() {
                return minorPipelineCluster.hscan(key, cursor);
            }
        }.read();
    }

    @Override
    public ScanResult<String> sscan(final String key, final String cursor) {
        return new ReadCommand<ScanResult<String>>() {
            @Override
            protected ScanResult<String> readMajor() {
                return majorPipelineCluster.sscan(key, cursor);
            }

            @Override
            protected ScanResult<String> readMinor() {
                return minorPipelineCluster.sscan(key, cursor);
            }
        }.read();
    }

    @Override
    public ScanResult<Tuple> zscan(final String key, final String cursor) {
        return new ReadCommand<ScanResult<Tuple>>() {
            @Override
            protected ScanResult<Tuple> readMajor() {
                return majorPipelineCluster.zscan(key, cursor);
            }

            @Override
            protected ScanResult<Tuple> readMinor() {
                return minorPipelineCluster.zscan(key, cursor);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> pfadd(final String key, final String... elements) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.pfadd(key, elements);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.pfadd(key, elements);
            }

            @Override
            protected String getCommandParam() {
                return String.format("pfadd key %s , elements %s", key, elements);
            }
        }.write();
    }

    @Override
    public long pfcount(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.pfcount(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.pfcount(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> geoadd(final byte[] key, final double longitude, final double latitude, final byte[] member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.geoadd(key, longitude, latitude, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.geoadd(key, longitude, latitude, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("geoadd key %s , longitude %s , latitude %s , member %s", key, longitude, latitude, member);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> geoadd(final byte[] key, final Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.geoadd(key, memberCoordinateMap);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.geoadd(key, memberCoordinateMap);
            }

            @Override
            protected String getCommandParam() {
                return String.format("geoadd key %s , memberCoordinateMap %s", key, memberCoordinateMap);
            }
        }.write();
    }

    @Override
    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2) {
        return new ReadCommand<Double>() {
            @Override
            protected Double readMajor() {
                return majorPipelineCluster.geodist(key, member1, member2);
            }

            @Override
            protected Double readMinor() {
                return minorPipelineCluster.geodist(key, member1, member2);
            }
        }.read();
    }

    @Override
    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2, final GeoUnit unit) {
        return new ReadCommand<Double>() {
            @Override
            protected Double readMajor() {
                return majorPipelineCluster.geodist(key, member1, member2, unit);
            }

            @Override
            protected Double readMinor() {
                return minorPipelineCluster.geodist(key, member1, member2, unit);
            }
        }.read();
    }

    @Override
    public List<byte[]> geohash(final byte[] key, final byte[]... members) {
        return new ReadCommand<List<byte[]>>() {
            @Override
            protected List<byte[]> readMajor() {
                return majorPipelineCluster.geohash(key, members);
            }

            @Override
            protected List<byte[]> readMinor() {
                return minorPipelineCluster.geohash(key, members);
            }
        }.read();
    }

    @Override
    public List<GeoCoordinate> geopos(final byte[] key, final byte[]... members) {
        return new ReadCommand<List<GeoCoordinate>>() {
            @Override
            protected List<GeoCoordinate> readMajor() {
                return majorPipelineCluster.geopos(key, members);
            }

            @Override
            protected List<GeoCoordinate> readMinor() {
                return minorPipelineCluster.geopos(key, members);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude, final double radius, final GeoUnit unit) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadius(key, longitude, latitude, radius, unit);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadius(key, longitude, latitude, radius, unit);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final byte[] key, final double longitude, final double latitude, final double radius, final GeoUnit unit) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusReadonly(key, longitude, latitude, radius, unit);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusReadonly(key, longitude, latitude, radius, unit);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadius(key, longitude, latitude, radius, unit, param);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadius(key, longitude, latitude, radius, unit, param);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final byte[] key, final double longitude, final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusReadonly(key, longitude, latitude, radius, unit, param);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusReadonly(key, longitude, latitude, radius, unit, param);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius, final GeoUnit unit) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusByMember(key, member, radius, unit);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusByMember(key, member, radius, unit);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final byte[] key, final byte[] member, final double radius, final GeoUnit unit) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusByMemberReadonly(key, member, radius, unit);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusByMemberReadonly(key, member, radius, unit);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusByMember(key, member, radius, unit, param);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusByMember(key, member, radius, unit, param);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final byte[] key, final byte[] member, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusByMemberReadonly(key, member, radius, unit, param);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusByMemberReadonly(key, member, radius, unit, param);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> geoadd(final String key, final double longitude, final double latitude, final String member) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.geoadd(key, longitude, latitude, member);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.geoadd(key, longitude, latitude, member);
            }

            @Override
            protected String getCommandParam() {
                return String.format("geoadd key %s , longitude %s , latitude %s , member %s", key, longitude, latitude, member);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> geoadd(final String key, final Map<String, GeoCoordinate> memberCoordinateMap) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.geoadd(key, memberCoordinateMap);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.geoadd(key, memberCoordinateMap);
            }

            @Override
            protected String getCommandParam() {
                return String.format("geoadd key %s , memberCoordinateMap %s", key, memberCoordinateMap);
            }
        }.write();
    }

    @Override
    public Double geodist(final String key, final String member1, final String member2) {
        return new ReadCommand<Double>() {
            @Override
            protected Double readMajor() {
                return majorPipelineCluster.geodist(key, member1, member2);
            }

            @Override
            protected Double readMinor() {
                return minorPipelineCluster.geodist(key, member1, member2);
            }
        }.read();
    }

    @Override
    public Double geodist(final String key, final String member1, final String member2, final GeoUnit unit) {
        return new ReadCommand<Double>() {
            @Override
            protected Double readMajor() {
                return majorPipelineCluster.geodist(key, member1, member2, unit);
            }

            @Override
            protected Double readMinor() {
                return minorPipelineCluster.geodist(key, member1, member2, unit);
            }
        }.read();
    }

    @Override
    public List<String> geohash(final String key, final String... members) {
        return new ReadCommand<List<String>>() {
            @Override
            protected List<String> readMajor() {
                return majorPipelineCluster.geohash(key, members);
            }

            @Override
            protected List<String> readMinor() {
                return minorPipelineCluster.geohash(key, members);
            }
        }.read();
    }

    @Override
    public List<GeoCoordinate> geopos(final String key, final String... members) {
        return new ReadCommand<List<GeoCoordinate>>() {
            @Override
            protected List<GeoCoordinate> readMajor() {
                return majorPipelineCluster.geopos(key, members);
            }

            @Override
            protected List<GeoCoordinate> readMinor() {
                return minorPipelineCluster.geopos(key, members);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadius(final String key, final double longitude, final double latitude, final double radius, final GeoUnit unit) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadius(key, longitude, latitude, radius, unit);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadius(key, longitude, latitude, radius, unit);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final String key, final double longitude, final double latitude, final double radius, final GeoUnit unit) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusReadonly(key, longitude, latitude, radius, unit);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusReadonly(key, longitude, latitude, radius, unit);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadius(final String key, final double longitude, final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadius(key, longitude, latitude, radius, unit, param);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadius(key, longitude, latitude, radius, unit, param);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final String key, final double longitude, final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusReadonly(key, longitude, latitude, radius, unit, param);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusReadonly(key, longitude, latitude, radius, unit, param);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final String key, final String member, final double radius, final GeoUnit unit) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusByMember(key, member, radius, unit);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusByMember(key, member, radius, unit);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final String key, final String member, final double radius, final GeoUnit unit) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusByMemberReadonly(key, member, radius, unit);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusByMemberReadonly(key, member, radius, unit);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final String key, final String member, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusByMember(key, member, radius, unit, param);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusByMember(key, member, radius, unit, param);
            }
        }.read();
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final String key, final String member, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return new ReadCommand<List<GeoRadiusResponse>>() {
            @Override
            protected List<GeoRadiusResponse> readMajor() {
                return majorPipelineCluster.georadiusByMemberReadonly(key, member, radius, unit, param);
            }

            @Override
            protected List<GeoRadiusResponse> readMinor() {
                return minorPipelineCluster.georadiusByMemberReadonly(key, member, radius, unit, param);
            }
        }.read();
    }

    @Override
    public Boolean exists(final byte[] key) {
        return new ReadCommand<Boolean>() {
            @Override
            protected Boolean readMajor() {
                return majorPipelineCluster.exists(key);
            }

            @Override
            protected Boolean readMinor() {
                return minorPipelineCluster.exists(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> persist(final byte[] key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.persist(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.persist(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("persist key %s", key);
            }
        }.write();
    }

    @Override
    public String type(final byte[] key) {
        return new ReadCommand<String>() {
            @Override
            protected String readMajor() {
                return majorPipelineCluster.type(key);
            }

            @Override
            protected String readMinor() {
                return minorPipelineCluster.type(key);
            }
        }.read();
    }

    @Override
    public byte[] dump(final byte[] key) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.dump(key);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.dump(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<String> restore(final byte[] key, final int ttl, final byte[] serializedValue) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.restore(key, ttl, serializedValue);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.restore(key, ttl, serializedValue);
            }

            @Override
            protected String getCommandParam() {
                return String.format("restore key %s, ttl %s , serializedValue %s", key, ttl, serializedValue);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> pexpire(final byte[] key, final long milliseconds) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.pexpire(key, milliseconds);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.pexpire(key, milliseconds);
            }

            @Override
            protected String getCommandParam() {
                return String.format("pexpire key %s, milliseconds %s", key, milliseconds);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> expireAt(final byte[] key, final long unixTime) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.expireAt(key, unixTime);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.expireAt(key, unixTime);
            }

            @Override
            protected String getCommandParam() {
                return String.format("expireAt key %s, unixTime %s", key, unixTime);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> pexpireAt(final byte[] key, final long millisecondsTimestamp) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.pexpireAt(key, millisecondsTimestamp);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.pexpireAt(key, millisecondsTimestamp);
            }

            @Override
            protected String getCommandParam() {
                return String.format("pexpireAt key %s, millisecondsTimestamp %s", key, millisecondsTimestamp);
            }
        }.write();
    }

    @Override
    public Long ttl(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.ttl(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.ttl(key);
            }
        }.read();
    }

    @Override
    public Long pttl(final byte[] key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.pttl(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.pttl(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> touch(final byte[] key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.touch(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.touch(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("touch key %s", key);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> persist(final String key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.persist(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.persist(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("persist key %s", key);
            }
        }.write();
    }

    @Override
    public String type(final String key) {
        return new ReadCommand<String>() {
            @Override
            protected String readMajor() {
                return majorPipelineCluster.type(key);
            }

            @Override
            protected String readMinor() {
                return minorPipelineCluster.type(key);
            }
        }.read();
    }

    @Override
    public byte[] dump(final String key) {
        return new ReadCommand<byte[]>() {
            @Override
            protected byte[] readMajor() {
                return majorPipelineCluster.dump(key);
            }

            @Override
            protected byte[] readMinor() {
                return minorPipelineCluster.dump(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<String> restore(final String key, final int ttl, final byte[] serializedValue) {
        return new WriteCommand<String>() {
            @Override
            protected String writeMajor() {
                return majorPipelineCluster.restore(key, ttl, serializedValue);
            }

            @Override
            protected String writeMinor() {
                return minorPipelineCluster.restore(key, ttl, serializedValue);
            }

            @Override
            protected String getCommandParam() {
                return String.format("restore key %s, ttl %s , serializedValue %s", key, ttl, serializedValue);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> pexpire(final String key, final long milliseconds) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.pexpire(key, milliseconds);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.pexpire(key, milliseconds);
            }

            @Override
            protected String getCommandParam() {
                return String.format("pexpire key %s, milliseconds %s", key, milliseconds);
            }
        }.write();
    }

    @Override
    public MultiWriteResult<Long> pexpireAt(final String key, final long millisecondsTimestamp) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.pexpireAt(key, millisecondsTimestamp);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.pexpireAt(key, millisecondsTimestamp);
            }

            @Override
            protected String getCommandParam() {
                return String.format("pexpireAt key %s, millisecondsTimestamp %s", key, millisecondsTimestamp);
            }
        }.write();
    }

    @Override
    public Long pttl(final String key) {
        return new ReadCommand<Long>() {
            @Override
            protected Long readMajor() {
                return majorPipelineCluster.pttl(key);
            }

            @Override
            protected Long readMinor() {
                return minorPipelineCluster.pttl(key);
            }
        }.read();
    }

    @Override
    public MultiWriteResult<Long> touch(final String key) {
        return new WriteCommand<Long>() {
            @Override
            protected Long writeMajor() {
                return majorPipelineCluster.touch(key);
            }

            @Override
            protected Long writeMinor() {
                return minorPipelineCluster.touch(key);
            }

            @Override
            protected String getCommandParam() {
                return String.format("touch key %s", key);
            }
        }.write();
    }

    @Override
    public boolean manualSwitchMajorMinor() {
        return switchMajorMinor(SwitchTypeEnum.MANUAL);
    }

    @Override
    public boolean autoSwitchMajorMinor() {
        return switchMajorMinor(SwitchTypeEnum.AUTO);
    }

    public boolean switchMajorMinor(SwitchTypeEnum switchTypeEnum) {
        logger.warn("====================start {} switch=======================", switchTypeEnum.getInfo());
        //1. 切换主备
        PipelineCluster tempPipelineCluster = majorPipelineCluster;
        majorPipelineCluster = minorPipelineCluster;
        minorPipelineCluster = tempPipelineCluster;
        long tempAppId = majorAppId;
        majorAppId = minorAppId;
        minorAppId = tempAppId;
        logger.error("1. newMajor appId is {} and newMinor appId is {}", majorAppId, minorAppId);
        //2. 通知业务方
        boolean notifySuccess = false;
        RedisCrossRoomTopology redisCrossRoomTopology = getRedisClusterCrossRoomInfo();
        try {
            //自动切换才进行通知
            if (redisCrossRoomAutoSwitchNotifier != null && SwitchTypeEnum.AUTO.equals(switchTypeEnum)) {
                notifySuccess = redisCrossRoomAutoSwitchNotifier.notify(redisCrossRoomTopology);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.warn("2. redisCrossRoomAutoSwitchNotifier result is {}", notifySuccess);

        //3. 暂停掉统计
        if (RedisCrossRoomAutoSwitchInterface.AUTO_SWITCH_ENABLED.get()) {
            RedisCrossRoomAutoSwitchInterface.AUTO_SWITCH_ENABLED.set(Boolean.FALSE);
        } else {
            RedisCrossRoomAutoSwitchInterface.AUTO_SWITCH_ENABLED.set(Boolean.TRUE);
        }
        logger.warn("3. RedisCrossRoomAutoSwitchInterface auto_switch_enable is {}", RedisCrossRoomAutoSwitchInterface.AUTO_SWITCH_ENABLED.get());

        // 5.清理数据，保留一分钟
        Date date = DateUtils.addMinutes(new Date(), 1);
        String targetMinute = DateUtil.getCrossRoomStatusDateFormat().format(date);
        RedisCrossRoomClientStatusCollector.cleanCrossRoomStatus(targetMinute);

        logger.warn("5. after clean RedisCrossRoomClientStatusCollector, data is {}", RedisCrossRoomClientStatusCollector.getHystrixAllTypeCountMap());

        logger.warn("====================end {} switch=======================", switchTypeEnum.getInfo());
        return true;
    }

    @Override
    public RedisCrossRoomTopology getRedisClusterCrossRoomInfo() {
        RedisCrossRoomTopology redisCrossRoomTopology = new RedisCrossRoomTopology();
        try {
            List<String> majorInstanceList = new ArrayList<String>(majorPipelineCluster.getClusterNodes().keySet());
            List<String> minorInstanceList = new ArrayList<String>(minorPipelineCluster.getClusterNodes().keySet());
            redisCrossRoomTopology.setMajorAppId(majorAppId);
            redisCrossRoomTopology.setMinorAppId(minorAppId);
            redisCrossRoomTopology.setMajorInstanceList(majorInstanceList);
            redisCrossRoomTopology.setMinorInstanceList(minorInstanceList);
            return redisCrossRoomTopology;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return redisCrossRoomTopology;
    }

    @Override
    public int getAlarmSwitchMinutes() {
        return alarmSwitchMinutes;
    }

    @Override
    public double getAlarmSwitchErrorPercentage() {
        return alarmSwitchErrorPercentage;
    }

    @Override
    public int getSwitchMinCount() {
        return switchMinCount;
    }

    @Override
    public Map<String, Map<String, Long>> getRecentMinutesStat() {
        Map<String, AtomicLongMap<Integer>> map = RedisCrossRoomClientStatusCollector.getRecentMinuteData(5);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, Long>> result = new HashMap<String, Map<String, Long>>();
        for (Entry<String, AtomicLongMap<Integer>> entry : map.entrySet()) {
            String key = entry.getKey();
            Map<String, Long> tempMap = new HashMap<String, Long>();
            for (Entry<Integer, Long> typeCountEntry : entry.getValue().asMap().entrySet()) {
                int type = typeCountEntry.getKey();
                HystrixStatCountTypeEnum hystrixStatCountTypeEnum = HystrixStatCountTypeEnum.getHystrixStatCountTypeEnum(type);
                if (hystrixStatCountTypeEnum == null) {
                    continue;
                }
                tempMap.put(hystrixStatCountTypeEnum.getInfo(), typeCountEntry.getValue());
            }
            result.put(key, tempMap);
        }
        return result;
    }

    public PipelineCluster getMajorPipelineCluster() {
        return majorPipelineCluster;
    }

    public PipelineCluster getMinorPipelineCluster() {
        return minorPipelineCluster;
    }

}
