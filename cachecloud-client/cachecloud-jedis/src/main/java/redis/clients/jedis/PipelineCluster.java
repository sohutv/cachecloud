package redis.clients.jedis;

import com.sohu.tv.cc.client.spectator.StatsCollector;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.valueobject.BitOffsetValue;
import redis.clients.jedis.valueobject.RangeRankVO;
import redis.clients.jedis.valueobject.RangeScoreVO;
import redis.clients.jedis.valueobject.SortedSetVO;
import redis.clients.jedis.util.SafeEncoder;

import java.util.*;

/**
 * Created by yijunzhang on 14-6-23.
 */
public class PipelineCluster extends JedisCluster {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PipelineCluster(GenericObjectPoolConfig poolConfig, Set<HostAndPort> nodes, int timeout) {
        super(nodes, timeout, poolConfig);
    }

    public PipelineCluster(GenericObjectPoolConfig poolConfig, Set<HostAndPort> nodes) {
        super(nodes, poolConfig);
    }

    public PipelineCluster(GenericObjectPoolConfig poolConfig, Set<HostAndPort> nodes, int timeout,
                           int maxAttempts) {
        super(nodes, timeout, maxAttempts, poolConfig);
    }

    public PipelineCluster(GenericObjectPoolConfig poolConfig, Set<HostAndPort> nodes, int connectionTimeout,
                           int soTimeout, int maxAttempts) {
        super(nodes, connectionTimeout, soTimeout, maxAttempts, poolConfig);
    }

    public PipelineCluster(GenericObjectPoolConfig poolConfig, Set<HostAndPort> nodes, int connectionTimeout,
                           int soTimeout, int maxAttempts, String password) {
        super(nodes, connectionTimeout, soTimeout, maxAttempts, password, poolConfig);
    }

    public PipelineCluster(GenericObjectPoolConfig poolConfig, Set<HostAndPort> nodes, int connectionTimeout,
                           int soTimeout, int maxAttempts, String password, boolean whetherInitIdleJedis) {
        super(nodes, connectionTimeout, soTimeout, maxAttempts, password, poolConfig);
        if (whetherInitIdleJedis) {
            initCreateIdleJedisForPool(poolConfig);
        }
    }

    public PipelineCluster(GenericObjectPoolConfig poolConfig, Set<HostAndPort> nodes, int connectionTimeout,
                           int soTimeout, int maxAttempts, String password, boolean whetherInitIdleJedis, StatsCollector statsCollector) {
        super(nodes, connectionTimeout, soTimeout, maxAttempts, password, poolConfig, statsCollector);
        if (whetherInitIdleJedis) {
            initCreateIdleJedisForPool(poolConfig);
        }
    }

    public PipelineCluster(GenericObjectPoolConfig poolConfig, Set<HostAndPort> nodes, int connectionTimeout,
                           int soTimeout, int maxAttempts, boolean whetherInitIdleJedis) {
        super(nodes, connectionTimeout, soTimeout, maxAttempts, poolConfig);
        if (whetherInitIdleJedis) {
            initCreateIdleJedisForPool(poolConfig);
        }
    }

    /**
     * 为所有jedisPool初始化一些活的jedis
     *
     * @param poolConfig
     */
    private void initCreateIdleJedisForPool(GenericObjectPoolConfig poolConfig) {
        long start = System.currentTimeMillis();
        logger.info("start to initCreateIdleJedisForPool");
        // 最多初始化GenericObjectPoolConfig.DEFAULT_MAX_TOTAL个连接
        int minIdle = poolConfig.getMinIdle();
        //        if (minIdle > GenericObjectPoolConfig.DEFAULT_MAX_TOTAL) {
        //            minIdle = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;
        //        }
        // 只初始化master，所以这里用的是<slot, JedisPool>
        Map<Integer, JedisPool> jedisPoolMap = this.connectionHandler.getSlots();
        // 过滤重复的JedisPool
        Set<JedisPool> jedisPoolSet = new LinkedHashSet<JedisPool>();
        for (JedisPool jedisPool : jedisPoolMap.values()) {
            jedisPoolSet.add(jedisPool);
        }
        // 遍历所有的jedisPool，使用maxTotal个Jedis，最后一起归还
        for (JedisPool jedisPool : jedisPoolSet) {
            List<Jedis> jedisList = new ArrayList<Jedis>(minIdle);
            // 1. 借用maxTotal个Jedis，最后统一还
            for (int i = 0; i < minIdle; i++) {
                Jedis jedis = null;
                try {
                    jedis = jedisPool.getResource();
                    logger.debug("{}:{} getResource()");
                    if (jedis != null && "PONG".equalsIgnoreCase(jedis.ping())) {
                        jedisList.add(jedis);
                    }
                } catch (Exception e) {
                    if(jedis != null){
                        logger.error("{}:{} init ping error" + e.getMessage(), jedis.getClient().getHost(),
                                jedis.getClient().getPort(), e);
                    }
                }
            }
            // 2. 归还Jedis
            for (int i = 0; i < jedisList.size(); i++) {
                Jedis jedis = null;
                try {
                    jedis = jedisList.get(i);
                    jedis.close();
                } catch (Exception e) {
                    if(jedis != null){
                        logger.error("{}:{} init close error" + e.getMessage(), jedis.getClient().getHost(),
                                jedis.getClient().getPort(), e);
                    }
                }
            }
        }
        logger.info("end to initCreateIdleJedisForPool size {}, cost time {} ms", jedisPoolSet.size(),
                (System.currentTimeMillis() - start));
    }

    public String set(final String key, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<String>(connectionHandler, maxAttempts) {
            public String execute(Jedis connection) {
                return connection.set(keyByte, value);
            }
        }.runBinary(keyByte);
    }

    public String set(final String key, final byte[] value, final String expx, final long time) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<String>(connectionHandler, maxAttempts) {
            @Override
            public String execute(Jedis connection) {
                SetParams params = SetParams.setParams();
                if (expx.equalsIgnoreCase("px")) {
                    params.px(time);
                } else {
                    params.ex((int) time);
                }
                return connection.set(keyByte, value, params);
            }
        }.runBinary(keyByte);
    }

    public byte[] getBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.get(keyByte);
            }
        }.runBinary(keyByte);
    }

    public Boolean setbit(final String key, final long offset,
                          final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Boolean>(connectionHandler, maxAttempts) {
            public Boolean execute(Jedis connection) {
                return connection.setbit(keyByte, offset, value);
            }
        }.runBinary(keyByte);
    }

    public Long setrange(final String key, final long offset, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.setrange(keyByte, offset, value);
            }
        }.runBinary(keyByte);
    }

    public byte[] getrangeBytes(final String key, final long startOffset, final long endOffset) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.getrange(keyByte, startOffset, endOffset);
            }
        }.runBinary(keyByte);
    }

    public byte[] getSetBytes(final String key, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.getSet(keyByte, value);
            }
        }.runBinary(keyByte);
    }

    public Long setnx(final String key, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.setnx(keyByte, value);
            }
        }.runBinary(keyByte);
    }

    public String setex(final String key, final int seconds, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<String>(connectionHandler, maxAttempts) {
            public String execute(Jedis connection) {
                return connection.setex(keyByte, seconds, value);
            }
        }.runBinary(keyByte);
    }

    public byte[] substrBytes(final String key, final int start, final int end) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.substr(keyByte, start, end);
            }
        }.runBinary(keyByte);
    }

    public Long hset(final String key, final String field, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.hset(keyByte, SafeEncoder.encode(field), value);
            }
        }.runBinary(keyByte);
    }

    public byte[] hgetBytes(final String key, final String field) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.hget(keyByte, SafeEncoder.encode(field));
            }
        }.runBinary(keyByte);
    }

    public Long hsetnx(final String key, final String field, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.hsetnx(keyByte, SafeEncoder.encode(field), value);
            }
        }.runBinary(keyByte);
    }

    public String hmsetBytes(final String key, final Map<byte[], byte[]> hash) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<String>(connectionHandler, maxAttempts) {
            public String execute(Jedis connection) {
                return connection.hmset(keyByte, hash);
            }
        }.runBinary(keyByte);
    }

    public List<byte[]> hmget(final String key, final byte[]... fields) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts) {
            public List<byte[]> execute(Jedis connection) {
                return connection.hmget(keyByte, fields);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> hkeysBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.hkeys(SafeEncoder.encode(key));
            }
        }.runBinary(keyByte);
    }

    public List<byte[]> hvalsBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts) {
            public List<byte[]> execute(Jedis connection) {
                return connection.hvals(keyByte);
            }
        }.runBinary(keyByte);
    }

    public Map<byte[], byte[]> hgetAllBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Map<byte[], byte[]>>(connectionHandler, maxAttempts) {
            public Map<byte[], byte[]> execute(Jedis connection) {
                return connection.hgetAll(keyByte);
            }
        }.runBinary(keyByte);
    }

    public Long rpush(final String key, final byte[]... string) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.rpush(keyByte, string);
            }
        }.runBinary(keyByte);
    }

    public Long lpush(final String key, final byte[]... string) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.lpush(keyByte, string);
            }
        }.runBinary(keyByte);
    }

    public List<byte[]> lrangeBytes(final String key, final long start, final long end) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts) {
            public List<byte[]> execute(Jedis connection) {
                return connection.lrange(keyByte, start, end);
            }
        }.runBinary(keyByte);
    }

    public byte[] lindexBytes(final String key, final long index) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.lindex(keyByte, index);
            }
        }.runBinary(keyByte);
    }

    public String lset(final String key, final long index, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<String>(connectionHandler, maxAttempts) {
            public String execute(Jedis connection) {
                return connection.lset(keyByte, index, value);
            }
        }.runBinary(keyByte);
    }

    public Long lrem(final String key, final long count, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.lrem(keyByte, count, value);
            }
        }.runBinary(keyByte);
    }

    public byte[] lpopBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.lpop(keyByte);
            }
        }.runBinary(keyByte);
    }

    public byte[] rpopBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.rpop(keyByte);
            }
        }.runBinary(keyByte);
    }

    public Long sadd(final String key, final byte[]... member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.sadd(keyByte, member);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> smembersBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.smembers(keyByte);
            }
        }.runBinary(keyByte);
    }

    public Long srem(final String key, final byte[]... member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.srem(keyByte, member);
            }
        }.runBinary(keyByte);
    }

    public byte[] spopBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.spop(keyByte);
            }
        }.runBinary(keyByte);
    }

    public Boolean sismember(final String key, final byte[] member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Boolean>(connectionHandler, maxAttempts) {
            public Boolean execute(Jedis connection) {
                return connection.sismember(keyByte, member);
            }
        }.runBinary(keyByte);
    }

    public byte[] srandmemberBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<byte[]>(connectionHandler, maxAttempts) {
            public byte[] execute(Jedis connection) {
                return connection.srandmember(keyByte);
            }
        }.runBinary(keyByte);
    }

    public Long zadd(final String key, final double score, final byte[] member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.zadd(keyByte, score, member);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrangeBytes(final String key, final long start, final long end) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrange(keyByte, start, end);
            }
        }.runBinary(keyByte);
    }

    public Long zrem(final String key, final byte[]... member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.zrem(keyByte, member);
            }
        }.runBinary(keyByte);
    }

    public Double zincrby(final String key, final double score,
                          final byte[] member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Double>(connectionHandler, maxAttempts) {
            public Double execute(Jedis connection) {
                return connection.zincrby(keyByte, score, member);
            }
        }.runBinary(keyByte);
    }

    public Long zrank(final String key, final byte[] member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.zrank(keyByte, member);
            }
        }.runBinary(keyByte);
    }

    public Long zrevrank(final String key, final byte[] member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.zrevrank(keyByte, member);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrevrangeBytes(final String key, final long start,
                                      final long end) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrange(keyByte, start, end);
            }
        }.runBinary(keyByte);
    }

    public Set<Tuple> zrangeWithScoresBytes(final String key, final long start, final long end) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeWithScores(keyByte, start, end);
            }
        }.runBinary(keyByte);
    }

    public Set<Tuple> zrevrangeWithScoresBytes(final String key, final long start, final long end) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeWithScores(keyByte, start, end);
            }
        }.runBinary(keyByte);
    }

    public Double zscore(final String key, final byte[] member) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Double>(connectionHandler, maxAttempts) {
            public Double execute(Jedis connection) {
                return connection.zscore(keyByte, member);
            }
        }.runBinary(keyByte);
    }

    public Map<String, Double> mzscore(final Map<String, String> keyMemberMap) {
        if (keyMemberMap == null || keyMemberMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Double>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    String member = keyMemberMap.get(key);
                    pipeline.zscore(key, member);
                }
            }

            @Override
            public Map<String, Double> getResult(Map<String, Object> resultMap) {
                Map<String, Double> result = new HashMap<String, Double>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String member = keyMemberMap.get(key);
                            Double value = pipelineCluster.zscore(key, member);
                            if (value != null) {
                                result.put(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Double.parseDouble(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyMemberMap.keySet()));
    }

    public List<byte[]> sortBytes(final String key) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts) {
            public List<byte[]> execute(Jedis connection) {
                return connection.sort(keyByte);
            }
        }.runBinary(keyByte);
    }

    public List<byte[]> sortBytes(final String key, final SortingParams sortingParameters) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts) {

            public List<byte[]> execute(Jedis connection) {
                return connection.sort(keyByte, sortingParameters);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrangeByScoreBytes(final String key, final double min, final double max) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {

            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByScore(keyByte, min, max);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrangeByScoreBytes(final String key, final String min, final String max) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {

            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByScore(keyByte, SafeEncoder.encode(min), SafeEncoder.encode(max));
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrevrangeByScoreBytes(final String key, final double max, final double min) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByScore(SafeEncoder.encode(key), max, min);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrangeByScoreBytes(final String key, final double min,
                                          final double max, final int offset, final int count) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByScore(keyByte, min, max, offset, count);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrevrangeByScoreBytes(final String key, final String max, final String min) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByScore(keyByte,
                        SafeEncoder.encode(max), SafeEncoder.encode(min));
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrangeByScoreBytes(final String key, final String min,
                                          final String max, final int offset, final int count) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {

            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByScore(keyByte,
                        SafeEncoder.encode(min), SafeEncoder.encode(max), offset, count);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrevrangeByScoreBytes(final String key, final double max,
                                             final double min, final int offset, final int count) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByScore(keyByte, max, min, offset, count);
            }
        }.runBinary(keyByte);
    }

    public Set<byte[]> zrevrangeByScoreBytes(final String key, final String max,
                                             final String min, final int offset, final int count) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts) {
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByScore(keyByte,
                        SafeEncoder.encode(max), SafeEncoder.encode(min), offset, count);
            }
        }.runBinary(keyByte);
    }

    public Long linsert(final String key, final ListPosition where,
                        final byte[] pivot, final byte[] value) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.linsert(keyByte, where, pivot, value);
            }
        }.runBinary(keyByte);
    }

    public Long lpushx(final String key, final byte[]... string) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.lpushx(keyByte, string);
            }
        }.runBinary(keyByte);
    }

    public Long rpushx(final String key, final byte[]... string) {
        final byte[] keyByte = SafeEncoder.encode(key);
        return new JedisClusterCommand<Long>(connectionHandler, maxAttempts) {
            public Long execute(Jedis connection) {
                return connection.rpushx(keyByte, string);
            }
        }.runBinary(keyByte);
    }

    public List<byte[]> blpopBytes(final String arg) {
        final byte[] keyByte = SafeEncoder.encode(arg);
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts) {
            public List<byte[]> execute(Jedis connection) {
                return connection.blpop(keyByte);
            }
        }.runBinary(keyByte);
    }

    public List<byte[]> brpopBytes(final String arg) {
        final byte[] keyByte = SafeEncoder.encode(arg);
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts) {
            public List<byte[]> execute(Jedis connection) {
                return connection.brpop(keyByte);
            }
        }.runBinary(keyByte);
    }

    public Map<String, String> mget(final List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, String>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    pipeline.get(key);
                }
            }

            @Override
            public Map<String, String> getResult(Map<String, Object> resultMap) {
                Map<String, String> result = new HashMap<String, String>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String value = pipelineCluster.get(key);
                            if (value != null) {
                                result.put(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, object.toString());
                    }
                }
                return result;
            }
        }.run(keys);
    }

    public Map<String, Boolean> mGetbit(final Map<String, Long> keyOffsetMap) {
        if (keyOffsetMap == null || keyOffsetMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Boolean>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    Long offset = keyOffsetMap.get(key);
                    pipeline.getbit(key, offset);
                }
            }

            @Override
            public Map<String, Boolean> getResult(Map<String, Object> resultMap) {
                Map<String, Boolean> result = new HashMap<String, Boolean>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Long offset = keyOffsetMap.get(key);
                            Boolean value = pipelineCluster.getbit(key, offset);
                            if (value != null) {
                                result.put(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, (Boolean) object);
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyOffsetMap.keySet()));
    }

    public Map<String, Boolean> mSetbit(final Map<String, BitOffsetValue> keyOffsetValueMap) {
        if (keyOffsetValueMap == null || keyOffsetValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Boolean>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    BitOffsetValue bitOffsetValue = keyOffsetValueMap.get(key);
                    pipeline.setbit(key, bitOffsetValue.getOffset(), bitOffsetValue.getValue());
                }
            }

            @Override
            public Map<String, Boolean> getResult(Map<String, Object> resultMap) {
                Map<String, Boolean> result = new HashMap<String, Boolean>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            BitOffsetValue bitOffsetValue = keyOffsetValueMap.get(key);
                            Boolean value = pipelineCluster
                                    .setbit(key, bitOffsetValue.getOffset(), bitOffsetValue.getValue());
                            if (value != null) {
                                result.put(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, (Boolean) object);
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyOffsetValueMap.keySet()));
    }

    public Map<String, Long> mexpire(final Map<String, Integer> keyTimeMap) {
        if (keyTimeMap == null || keyTimeMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Long>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    Integer seconds = keyTimeMap.get(key);
                    pipeline.expire(key, seconds);
                }
            }

            @Override
            public Map<String, Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String value = pipelineCluster.get(key);
                            Integer seconds = keyTimeMap.get(key);
                            if (value != null) {
                                pipelineCluster.expire(key, seconds);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Long.valueOf(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyTimeMap.keySet()));
    }

    public Map<String, Map<String, String>> mHgetAll(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Map<String, String>>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    pipeline.hgetAll(key);
                }
            }

            @Override
            public Map<String, Map<String, String>> getResult(Map<String, Object> resultMap) {
                Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Map<String, String> exceptionHgetAllMap = pipelineCluster.hgetAll(key);
                            if (exceptionHgetAllMap != null && !exceptionHgetAllMap.isEmpty()) {
                                result.put(key, exceptionHgetAllMap);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        Map<String, String> map = (Map<String, String>) object;
                        if (map.size() > 0) {
                            result.put(key, map);
                        }
                    }
                }
                return result;
            }
        }.run(keys);
    }

    public Map<String, List<String>> mhmget(final Map<String, List<String>> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, List<String>>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    List<String> fields = keyValueMap.get(key);
                    pipeline.hmget(key, fields.toArray(new String[fields.size()]));
                }
            }

            @Override
            public Map<String, List<String>> getResult(Map<String, Object> resultMap) {
                Map<String, List<String>> result = new HashMap<String, List<String>>();
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            List<String> fields = keyValueMap.get(key);
                            List<String> exceptionHmgetList = pipelineCluster
                                    .hmget(key, fields.toArray(new String[fields.size()]));
                            if (exceptionHmgetList != null && !exceptionHmgetList.isEmpty()) {
                                result.put(key, exceptionHmgetList);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        List<String> hmgetList = (List<String>) object;
                        if (hmgetList.size() > 0) {
                            result.put(key, hmgetList);
                        }
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }

    public Map<String, String> mhmset(final Map<String, Map<String, String>> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, String>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    Map<String, String> map = keyValueMap.get(key);
                    pipeline.hmset(key, map);
                }
            }

            @Override
            public Map<String, String> getResult(Map<String, Object> resultMap) {
                Map<String, String> result = new HashMap<String, String>();
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Map<String, String> value = keyValueMap.get(key);
                            if (value != null) {
                                pipelineCluster.hmset(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            result.put(key, "FAIL");
                        }
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }

    public String mset(final Map<String, String> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<String>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    String value = keyValueMap.get(key);
                    pipeline.set(key, value);
                }
            }

            @Override
            public String getResult(Map<String, Object> resultMap) {
                String result = "OK";
                if (resultMap == null || resultMap.isEmpty()) {
                    result = "FAIL";
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String value = keyValueMap.get(key);
                            if (value != null) {
                                pipelineCluster.set(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }

    public String msetnx(final Map<String, String> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<String>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    String value = keyValueMap.get(key);
                    pipeline.setnx(key, value);
                }
            }

            @Override
            public String getResult(Map<String, Object> resultMap) {
                String result = "OK";
                if (resultMap == null || resultMap.isEmpty()) {
                    result = "FAIL";
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String value = keyValueMap.get(key);
                            if (value != null) {
                                pipelineCluster.setnx(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }

    public Map<String, Long> msetnxs(final Map<String, String> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Long>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    String value = keyValueMap.get(key);
                    pipeline.setnx(key, value);
                }
            }

            @Override
            public Map<String, Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String value = keyValueMap.get(key);
                            if (value != null) {
                                result.put(key, pipelineCluster.setnx(key, value));
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            result.put(key, null);
                        }
                    } else {
                        result.put(key, Long.parseLong(object.toString()));
                    }

                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }


    public Map<String, Long> mhsetnx(final Map<String, Map<String, String>> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Long>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    Map<String, String> value = keyValueMap.get(key);
                    if(value != null && value.size() == 1){     //只允许一个元素
                        for (Map.Entry<String, String> entry : value.entrySet()){
                            pipeline.hsetnx(key, entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
            @Override
            public Map<String, Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Map<String, String> valueMap = keyValueMap.get(key);
                            if(valueMap != null && valueMap.size() == 1) {     //只允许一个元素
                                for (Map.Entry<String, String> valueEntry : valueMap.entrySet()){
                                    result.put(key, pipelineCluster.hsetnx(key, valueEntry.getKey(), valueEntry.getValue()));
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Long.parseLong(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }

    public String msetex(final Map<String, String> keyValueMap, final int seconds) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<String>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    String value = keyValueMap.get(key);
                    pipeline.setex(key, seconds, value);
                }
            }

            @Override
            public String getResult(Map<String, Object> resultMap) {
                String result = "OK";
                if (resultMap == null || resultMap.isEmpty()) {
                    result = "FAIL";
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String value = keyValueMap.get(key);
                            if (value != null) {
                                pipelineCluster.setex(key, seconds, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }

    public String mset(final Map<String, String> keyValueMap, final SetParams params) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<String>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    String value = keyValueMap.get(key);
                    pipeline.set(key, value, params);
                }
            }

            @Override
            public String getResult(Map<String, Object> resultMap) {
                String result = "OK";
                if (resultMap == null || resultMap.isEmpty()) {
                    result = "FAIL";
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String value = keyValueMap.get(key);
                            if (value != null) {
                                pipelineCluster.set(key, value, params);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }


    public Map<String, Long> mincrBy(final Map<String, Long> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Long>>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    Long value = keyValueMap.get(key);
                    pipeline.incrBy(key, value);
                }
            }

            @Override
            public Map<String, Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Long value = keyValueMap.get(key);
                            if (value != null) {
                                result.put(key, pipelineCluster.incrBy(key, value));
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            result.put(key, null);
                        }
                    } else {
                        result.put(key, (Long) object);
                    }
                }
                return result;
            }

        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }

    public Long mdel(final List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Long>(this, connectionHandler) {
            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    pipeline.del(key);
                }
            }

            @Override
            public Long getResult(Map<String, Object> resultMap) {
                Long result = 0L;
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Long value = pipelineCluster.del(key);
                            if (value != null && value > 0) {
                                result++;
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result++;
                    }
                }
                return result;
            }
        }.run(keys);
    }

    public Map<String, Long> mzadd(final Map<String, SortedSetVO> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Long>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    SortedSetVO vo = map.get(key);
                    if (vo.getMemberStr() != null) {
                        pipeline.zadd(key, vo.getScore(), vo.getMemberStr());
                    } else {
                        pipeline.zadd(SafeEncoder.encode(key), vo.getScore(), vo.getBytesBytes());
                    }
                }
            }

            @Override
            public Map<String, Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            SortedSetVO vo = map.get(key);
                            Long value;
                            if (vo.getMemberStr() != null) {
                                value = pipelineCluster.zadd(key, vo.getScore(), vo.getMemberStr());
                            } else {
                                value = pipelineCluster
                                        .zadd(SafeEncoder.encode(key), vo.getScore(), vo.getBytesBytes());
                            }
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Long.parseLong(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(map.keySet()));
    }

    public Map<String, Long> mzadds(final Map<String, Map<String, Double>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Long>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    Map<String, Double> scoreMemberMap = map.get(key);
                    pipeline.zadd(key, scoreMemberMap);
                }
            }

            @Override
            public Map<String, Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Map<String, Double> voMap = map.get(key);
                            Long value = pipelineCluster.zadd(key, voMap);
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Long.parseLong(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(map.keySet()));
    }

    public Map<String, Long> mzrem(final Map<String, String[]> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Long>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    String[] members = map.get(key);
                    pipeline.zrem(key, members);
                }
            }

            @Override
            public Map<String, Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String[] members = map.get(key);
                            Long value = pipelineCluster.zrem(key, members);
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Long.parseLong(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(map.keySet()));
    }

    public Map<String, Long> mzcard(final List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, Long>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    pipeline.zcard(key);
                }
            }

            @Override
            public Map<String, Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Long value = pipelineCluster.zcard(key);
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Long.parseLong(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keys));
    }

    public Map<String, Long> mzrank(final Map<String,String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String,Long>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    String member = map.get(key);
                    pipeline.zrank(key,member);
                }
            }

            @Override
            public Map<String,Long> getResult(Map<String, Object> resultMap) {
                Map<String, Long> result = new HashMap<String, Long>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            String member = map.get(key);
                            Long value = pipelineCluster.zrank(key,member);
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Long.parseLong(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(map.keySet()));
    }

    public Map<String, Boolean> msismember(final Map<String,List<String>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String,Boolean>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    List<String> members = map.get(key);
                    for(String member : members){
                        pipeline.sismember(key,member);
                    }
                }
            }

            @Override
            public Map<String,Boolean> getResult(Map<String, Object> resultMap) {
                Map<String, Boolean> result = new HashMap<String, Boolean>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            List<String> members = map.get(key);
                            for(String member : members){
                                Boolean value = pipelineCluster.sismember(key,member);
                                result.put(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, Boolean.valueOf(object.toString()));
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(map.keySet()));
    }

    public Map<String, Set<String>> mzrangeByScore(final List<String> keys, final double min,
                                                   final double max) {
        return new PipelineClusterCommand<Map<String, Set<String>>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    pipeline.zrangeByScore(key, min, max);
                }
            }

            @Override
            public Map<String, Set<String>> getResult(Map<String, Object> resultMap) {
                Map<String, Set<String>> result = new HashMap<String, Set<String>>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Set<String> value = pipelineCluster.zrangeByScore(key, min, max);
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, (Set<String>) object);
                    }
                }
                return result;
            }
        }.run(keys);
    }

    public Map<String, Set<Tuple>> mzrevrangeWithScores(final Map<String, RangeRankVO> keyRankMap) {
        return new PipelineClusterCommand<Map<String, Set<Tuple>>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    RangeRankVO vo = keyRankMap.get(key);
                    pipeline.zrevrangeWithScores(key, vo.getMin(), vo.getMax());
                }
            }

            @Override
            public Map<String, Set<Tuple>> getResult(Map<String, Object> resultMap) {
                Map<String, Set<Tuple>> result = new HashMap<String, Set<Tuple>>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            RangeRankVO vo = keyRankMap.get(key);
                            Set<Tuple> value = pipelineCluster.zrevrangeWithScores(key, vo.getMin(), vo.getMax());
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, (Set<Tuple>) object);
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyRankMap.keySet()));
    }

    public Map<String, Set<String>> mzrangeByScore(final Map<String, RangeScoreVO> keyScoreMap) {
        return new PipelineClusterCommand<Map<String, Set<String>>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    RangeScoreVO vo = keyScoreMap.get(key);
                    pipeline.zrangeByScore(key, vo.getMin(), vo.getMax());
                }

            }

            @Override
            public Map<String, Set<String>> getResult(Map<String, Object> resultMap) {
                Map<String, Set<String>> result = new HashMap<String, Set<String>>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            RangeScoreVO vo = keyScoreMap.get(key);
                            Set<String> value = pipelineCluster.zrangeByScore(key, vo.getMin(), vo.getMax());
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, (Set<String>) object);
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyScoreMap.keySet()));
    }

    public Map<String, Set<String>> mzrangeByScore(final List<String> keys, final String min,
                                                   final String max) {
        return new PipelineClusterCommand<Map<String, Set<String>>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    pipeline.zrangeByScore(key, min, max);
                }
            }

            @Override
            public Map<String, Set<String>> getResult(Map<String, Object> resultMap) {
                Map<String, Set<String>> result = new HashMap<String, Set<String>>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            Set<String> value = pipelineCluster.zrangeByScore(key, min, max);
                            result.put(key, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, (Set<String>) object);
                    }
                }
                return result;
            }

        }.run(keys);
    }

    public String mzremrangeByScore(final Map<String, RangeScoreVO> keyScoreMap) {
        return new PipelineClusterCommand<String>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    RangeScoreVO vo = keyScoreMap.get(key);
                    pipeline.zremrangeByScore(key, vo.getMin(), vo.getMax());
                }
            }

            @Override
            public String getResult(Map<String, Object> resultMap) {
                String result = "OK";
                if (resultMap == null || resultMap.isEmpty()) {
                    result = "FAIL";
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            RangeScoreVO vo = keyScoreMap.get(key);
                            if (vo != null) {
                                pipelineCluster.zremrangeByScore(key, vo.getMin(), vo.getMax());
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyScoreMap.keySet()));
    }

    public Map<String, byte[]> mgetBytes(final List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<Map<String, byte[]>>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    pipeline.get(SafeEncoder.encode(key));
                }
            }

            @Override
            public Map<String, byte[]> getResult(Map<String, Object> resultMap) {
                Map<String, byte[]> result = new HashMap<String, byte[]>();
                if (resultMap == null || resultMap.isEmpty()) {
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            byte[] value = pipelineCluster.getBytes(key);
                            if (value != null) {
                                result.put(key, value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        result.put(key, (byte[]) object);
                    }
                }
                return result;
            }
        }.run(keys);
    }

    public String msetBytes(final Map<String, byte[]> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return null;
        }
        return new PipelineClusterCommand<String>(this, connectionHandler) {

            @Override
            public void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys) {
                for (String key : pipelineKeys) {
                    byte[] value = keyValueMap.get(key);
                    pipeline.set(SafeEncoder.encode(key), value);
                }
            }

            @Override
            public String getResult(Map<String, Object> resultMap) {
                String result = "OK";
                if (resultMap == null || resultMap.isEmpty()) {
                    result = "FAIL";
                    return result;
                }
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    if (object == null) {
                        continue;
                    }
                    if (checkRedirectException(object)) {
                        try {
                            byte[] value = keyValueMap.get(key);
                            if (value != null) {
                                pipelineCluster.set(SafeEncoder.encode(key), value);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                return result;
            }
        }.run(new ArrayList<String>(keyValueMap.keySet()));
    }

    /**
     * @param channel
     * @param message
     * @return
     */
    public Long publish(final String channel, final String message) {
        SubPubClusterCommand subPubClusterCommand = new SubPubClusterCommand(this, connectionHandler, maxAttempts);
        Jedis jedis = subPubClusterCommand.getJedis(channel);
        try {
            return jedis.publish(channel, message);
        } finally {
            subPubClusterCommand.releaseConnection(jedis);
        }
    }

    /**
     * @param channel
     * @param message
     * @return
     */
    public Long publish(final String channel, final byte[] message) {
        SubPubClusterCommand subPubClusterCommand = new SubPubClusterCommand(this, connectionHandler, maxAttempts);
        Jedis jedis = subPubClusterCommand.getJedis(channel);
        try {
            return jedis.publish(SafeEncoder.encode(channel), message);
        } finally {
            subPubClusterCommand.releaseConnection(jedis);
        }
    }

    /**
     * 订阅单一频道(阻塞操作)
     *
     * @param jedisPubSub
     * @param channel
     */
    public void subscribe(final JedisPubSub jedisPubSub, final String channel, final int timeout) {
        SubPubClusterCommand subPubClusterCommand = new SubPubClusterCommand(this, connectionHandler, maxAttempts);
        Jedis jedis = subPubClusterCommand.getNewJedis(channel, timeout);
        try {
            jedis.subscribe(jedisPubSub, channel);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 按校验和执行脚本
     *
     * @param sha
     * @param key
     * @param args
     * @return
     */
    public Object evalsha(String sha, String key, String... args) {
        SubPubClusterCommand subPubClusterCommand = new SubPubClusterCommand(this, connectionHandler, maxAttempts);
        Jedis jedis = subPubClusterCommand.getJedis(key);
        try {
            return jedis.evalsha(sha, 1, getKeys(key, args));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private String[] getKeys(String key, String... args) {
        List<String> list = new ArrayList<String>();
        list.add(key);
        if (args != null && args.length > 0) {
            for (String arg : args) {
                list.add(arg);
            }
            return list.toArray(new String[0]);
        } else {
            return new String[]{key};
        }
    }

    /**
     * 按校验和执行脚本
     *
     * @param sha
     * @param key
     * @return
     */
    public Object evalsha(String sha, String key) {
        return evalsha(sha, key, null);
    }

    /**
     * 按内容执行脚本
     *
     * @param sha
     * @param key
     * @param args
     * @return
     */
    public Object eval(String sha, String key, String... args) {
        SubPubClusterCommand subPubClusterCommand = new SubPubClusterCommand(this, connectionHandler, maxAttempts);
        Jedis jedis = subPubClusterCommand.getJedis(key);

        try {
            return jedis.eval(sha, 1, getKeys(key, args));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 按内容执行脚本
     *
     * @param sha
     * @param key
     * @return
     */
    public Object eval(String sha, String key) {
        return eval(sha, key, null);
    }

    /**
     * 将脚本script添加到集群所有节点缓存中
     *
     * @param script
     * @return
     */
    public String loadScruptAllNodes(String script) {
        Map<String, JedisPool> nodeMap = getClusterNodes();
        String md5 = null;
        for (JedisPool jedisPool : nodeMap.values()) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                String returnMd5 = jedis.scriptLoad(script);
                if (returnMd5 != null) {
                    md5 = returnMd5;
                }
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return md5;
    }

    public ScanResult<String> sscan(final String key, final String cursor, final ScanParams params) {
        return new JedisClusterCommand<ScanResult<String>>(connectionHandler, maxAttempts) {
            @Override
            public ScanResult<String> execute(Jedis connection) {
                return connection.sscan(key, cursor, params);
            }
        }.run(key);
    }

    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return new JedisClusterCommand<ScanResult<byte[]>>(connectionHandler, maxAttempts) {
            @Override
            public ScanResult<byte[]> execute(Jedis connection) {
                return connection.sscan(key, cursor, params);
            }
        }.runBinary(key);
    }

}
