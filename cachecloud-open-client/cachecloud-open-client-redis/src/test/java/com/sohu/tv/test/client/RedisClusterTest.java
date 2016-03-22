package com.sohu.tv.test.client;

import com.sohu.tv.builder.ClientBuilder;
import com.sohu.tv.test.base.BaseTest;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;

import redis.clients.jedis.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * cachecloud-rediscluster客户端测试
 * 
 * @author leifu
 * @Date 2014年11月21日
 * @Time 上午11:58:53
 */
public class RedisClusterTest extends BaseTest {

    private final static long appId = 0L;

    @Test
    public void pushData() throws Exception {
        JedisCluster redisCluster = ClientBuilder.redisCluster(appId)
                .setJedisPoolConfig(getPoolConfig())
                .setConnectionTimeout(2000)
                .setSoTimeout(1000)
                .build();
        for (int i = 1; i < 100; i++) {
            redisCluster.setex("tmp:key" + i, 60 * 20, "value" + i);
            TimeUnit.MILLISECONDS.sleep(20);
            logger.info("push:" + i);
        }
    }

    @Test
    public void testCluster() {
        JedisCluster redisCluster = ClientBuilder.redisCluster(appId)
                .setJedisPoolConfig(getPoolConfig())
                .setConnectionTimeout(2000)
                .setSoTimeout(1000)
                .build();
        Map<String, JedisPool> clusterMap = redisCluster.getClusterNodes();
        for (String key : clusterMap.keySet()) {
            logger.info("key={}", key);
            JedisPool jedisPool = clusterMap.get(key);
            Jedis jedis = jedisPool.getResource();

            logger.info("before:cluster-slave-validity-factor->"
                    + jedis.configGet("cluster-slave-validity-factor"));
            jedis.configSet("cluster-slave-validity-factor", "10");
            logger.info("after:cluster-slave-validity-factor->"
                    + jedis.configGet("cluster-slave-validity-factor"));
            logger.info("------------------------------------");
            logger.info("before:repl-disable-tcp-nodelay->" + jedis.configGet("repl-disable-tcp-nodelay"));
            jedis.configSet("repl-disable-tcp-nodelay", "no");
            logger.info("after:repl-disable-tcp-nodelay->" + jedis.configGet("repl-disable-tcp-nodelay"));
            logger.info("####################################");
            jedis.close();
        }
    }

    private GenericObjectPoolConfig getPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 20);
        poolConfig.setMaxIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 20);
        poolConfig.setMinIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 10);
        // JedisPool.borrowObject最大等待时间
        poolConfig.setMaxWaitMillis(1000L);
        poolConfig.setJmxNamePrefix("jedis-pool");
        poolConfig.setJmxEnabled(true);
        return poolConfig;
    }

}
