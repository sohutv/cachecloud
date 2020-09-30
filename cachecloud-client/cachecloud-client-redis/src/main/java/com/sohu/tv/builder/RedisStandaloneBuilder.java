package com.sohu.tv.builder;

import com.sohu.tv.cc.client.spectator.AsyncStatsCollector;
import com.sohu.tv.cc.client.spectator.ClientConfig;
import com.sohu.tv.cc.client.spectator.StatsCollector;
import com.sohu.tv.cc.client.spectator.heartbeat.ClientStatusEnum;
import com.sohu.tv.cc.client.spectator.json.JSONUtils;
import com.sohu.tv.cc.client.spectator.util.Constants;
import com.sohu.tv.cc.client.spectator.util.HttpUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.AuthUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 构造redis单机的builder；
 *
 * @author: lingguo
 * @time: 2014/9/23 17:42
 */
public class RedisStandaloneBuilder {
    private Logger logger = LoggerFactory.getLogger(RedisStandaloneBuilder.class);

    private final Lock lock = new ReentrantLock();
    private volatile JedisPool jedisPool;
    private GenericObjectPoolConfig poolConfig;
    private final long appId;
    private int timeout = Protocol.DEFAULT_TIMEOUT;
    /**
     * 是否开启统计
     */
    private boolean clientStatIsOpen = true;

    /**
     * 构造函数package访问域，package外直接构造实例；
     *
     * @param appId
     */
    RedisStandaloneBuilder(final long appId) {
        this.appId = appId;
        poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 3);
        poolConfig.setMaxIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 2);
        poolConfig.setMinIdle(GenericObjectPoolConfig.DEFAULT_MIN_IDLE);
        poolConfig.setJmxNamePrefix("jedis-pool");
    }

    public JedisPool build() {
        if (jedisPool == null) {
            while (true) {
                try {
                    lock.tryLock(10, TimeUnit.SECONDS);
                    if (jedisPool == null) {
                        /**
                         * 心跳返回的请求为空；
                         */
                        String response = HttpUtils.doGet(String.format(Constants.REDIS_STANDALONE_URL, appId));
                        if (response == null || response.isEmpty()) {
                            logger.warn("cannot get response from server, appId={}. continue...", appId);
                            continue;
                        }
                        Map<String, Object> jsonObject = null;
                        try {
                            Object object = JSONUtils.parse(response);
                            if(object instanceof Map){
                                jsonObject = (Map)object;
                            }
                        } catch (Exception e) {
                            logger.error("read json from response error, appId: {}.", appId, e);
                        }
                        if (jsonObject == null) {
                            logger.warn("invalid response, appId: {}. continue...", appId);
                            continue;
                        }
                        int status = Integer.parseInt(String.valueOf(jsonObject.get("status")));
                        String message = String.valueOf(jsonObject.get("message"));

                        /** 检查客户端版本 **/
                        if (status == ClientStatusEnum.ERROR.getStatus()) {
                            throw new IllegalStateException(message);
                        } else if (status == ClientStatusEnum.WARN.getStatus()) {
                            logger.warn(message);
                        } else {
                            logger.info(message);
                        }
                        /**
                         * 从心跳中提取HostAndPort，构造JedisPool实例；
                         */
                        String instance = String.valueOf(jsonObject.get("standalone"));
                        String[] instanceArr = instance.split(":");
                        if (instanceArr.length != 2) {
                            logger.warn("instance info is invalid, instance: {}, appId: {}, continue...", instance,
                                    appId);
                            continue;
                        }

                        String pkey = String.valueOf(jsonObject.get("pkey"));
                        if (pkey != null && pkey.length() > 0) {
                            pkey = AuthUtil.getAppIdMD5(pkey);
                        } else {
                            pkey = "";
                        }
                        ClientConfig clientConfig = new ClientConfig(getConfigMap(), clientStatIsOpen);
                        StatsCollector statsCollector = new AsyncStatsCollector(appId, clientConfig);
                        statsCollector.start();

                        //String password = appId + AuthUtil.SPLIT_KEY + pkey;

                        jedisPool = new JedisPool(poolConfig, statsCollector, instanceArr[0], Integer.valueOf(instanceArr[1]),
                                timeout, pkey);
                        return jedisPool;
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    lock.unlock();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(200 + new Random().nextInt(1000));//活锁
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            return jedisPool;
        }
    }

    /**
     * 配置
     *
     * @param poolConfig
     * @return
     */
    public RedisStandaloneBuilder setPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        return this;
    }

    /**
     * @param timeout 单位:毫秒
     * @return
     */
    public RedisStandaloneBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 是否开启统计
     *
     * @param clientStatIsOpen
     * @return
     */
    public RedisStandaloneBuilder setClientStatIsOpen(boolean clientStatIsOpen) {
        this.clientStatIsOpen = clientStatIsOpen;
        return this;
    }

    private Map<String, Object> getConfigMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("connectTimeout", timeout);
        map.put("soTimeout", timeout);
        map.put("maxTotal", poolConfig.getMaxTotal());
        map.put("minIdle", poolConfig.getMinIdle());
        map.put("maxIdle", poolConfig.getMaxIdle());
        map.put("maxWaitMillis", poolConfig.getMaxWaitMillis());
        map.put("testWhileIdle", poolConfig.getTestWhileIdle());
        map.put("testOnBorrow", poolConfig.getTestOnBorrow());
        map.put("testOnCreate", poolConfig.getTestOnCreate());
        map.put("testOnReturn", poolConfig.getTestOnReturn());
        map.put("timeBetweenEvictionRunsMillis", poolConfig.getTimeBetweenEvictionRunsMillis());
        return map;
    }
}
