package com.sohu.tv.test.client;

import com.sohu.tv.builder.ClientBuilder;
import com.sohu.tv.test.base.BaseTest;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;

import redis.clients.jedis.*;

import java.util.Set;

public class RedisStandaloneTest extends BaseTest {

    @Test
    public void testStandalone() {
        long appId = 10121;
        JedisPool jedisPool = ClientBuilder.redisStandalone(appId)
                .setPoolConfig(new GenericObjectPoolConfig())
                .setTimeout(2000)
                .build();
        Jedis jedis = jedisPool.getResource();
        for (int i = 0; i < 10; i++) {
            jedis.zadd("key-zset", i * 0.1, "value-" + i);
        }
        Set<String> result = jedis.zrevrangeByScore("key-zset", 2, 0);
        for (String s : result) {
            logger.info("{}->", s);
        }
        jedis.close();
        jedisPool.destroy();
    }

    @Test
    public void testStandaloneExample() {
        long appId = 10122;
        JedisPool jedisPool = null;

        // 使用默认配置
//        jedisPool = ClientBuilder.redisStandalone(appId).build();

        /**
         * 使用自定义配置
         */
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 3);
        poolConfig.setMinIdle(GenericObjectPoolConfig.DEFAULT_MIN_IDLE * 2);
        poolConfig.setJmxEnabled(true);
        poolConfig.setMaxWaitMillis(3000);

        jedisPool = ClientBuilder.redisStandalone(appId)
                .setPoolConfig(poolConfig)
                .setTimeout(2000)
                .build();

        Jedis jedis = jedisPool.getResource();
        jedis.setnx("key2", "5");
        assertEquals("10", jedis.incrBy("key2", 5));
        jedis.close();
    }

}
