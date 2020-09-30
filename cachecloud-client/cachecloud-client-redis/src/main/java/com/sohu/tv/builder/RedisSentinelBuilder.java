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
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.AuthUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * redis sentinel 客户端的builder
 * Created by yijunzhang on 14-9-11.
 */
public class RedisSentinelBuilder {
    private static Logger logger = LoggerFactory.getLogger(RedisSentinelBuilder.class);

    /**
     * 应用id
     */
    private final long appId;

    /**
     * jedis对象池配置
     */
    private GenericObjectPoolConfig poolConfig;

    /**
     * jedis连接超时(单位:毫秒)
     */
    private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;

    /**
     * jedis读写超时(单位:毫秒)
     */
    private int soTimeout = Protocol.DEFAULT_TIMEOUT;

    /**
     * jedis sentinel连接池
     */
    private volatile JedisSentinelPool sentinelPool;

    /**
     * 构建锁
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 是否开启统计
     */
    private boolean clientStatIsOpen = true;

    /**
     * 构造函数package访问域，package外不能直接构造实例；
     *
     * @param appId
     */
    RedisSentinelBuilder(final long appId) {
        this.appId = appId;
        poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 3);
        poolConfig.setMaxIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 2);
        poolConfig.setMinIdle(GenericObjectPoolConfig.DEFAULT_MIN_IDLE);
        poolConfig.setMaxWaitMillis(Protocol.DEFAULT_TIMEOUT);
        poolConfig.setJmxNamePrefix("jedis-sentinel-pool");
        poolConfig.setJmxEnabled(true);
    }

    public JedisSentinelPool build() {
        if (sentinelPool == null) {
            while (true) {
                try {
                    lock.tryLock(10, TimeUnit.SECONDS);
                    if (sentinelPool == null) {
                        /**
                         * http请求返回的结果是空的；
                         */
                        String response = HttpUtils.doGet(String.format(Constants.REDIS_SENTINEL_URL, appId));
                        if (response == null || response.isEmpty()) {
                            logger.warn("get response from remote server error, appId: {}, continue...", appId);
                            continue;
                        }
                        /**
                         * http请求返回的结果是无效的；
                         */
                        Map<String, Object> jsonObject = null;
                        try {
                            Object object = JSONUtils.parse(response);
                            if (object instanceof Map) {
                                jsonObject = (Map) object;
                            }
                        } catch (Exception e) {
                            logger.error("heartbeat error, appId: {}. continue...", appId, e);
                        }
                        if (jsonObject == null) {
                            logger.error("get sentinel info for appId: {} error. continue...", appId);
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
                         * 有效的请求：取出masterName和sentinels，并创建JedisSentinelPool的实例；
                         */
                        String masterName = String.valueOf(jsonObject.get("masterName"));
                        String sentinels = String.valueOf(jsonObject.get("sentinels"));
                        Set<String> sentinelSet = new HashSet<String>();
                        for (String sentinelStr : sentinels.split(" ")) {
                            String[] sentinelArr = sentinelStr.split(":");
                            if (sentinelArr.length == 2) {
                                sentinelSet.add(sentinelStr);
                            }
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

                        sentinelPool = new JedisSentinelPool(masterName, sentinelSet, poolConfig, statsCollector, connectionTimeout,
                                soTimeout, pkey, Protocol.DEFAULT_DATABASE);
                        return sentinelPool;
                    }
                } catch (Throwable e) {//容错
                    logger.error("error in build, appId: {}", appId, e);
                } finally {
                    lock.unlock();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(200 + new Random().nextInt(1000));//活锁
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return sentinelPool;
    }

    /**
     * 设置配置参数
     *
     * @param poolConfig
     * @return
     */
    public RedisSentinelBuilder setPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        return this;
    }

    /**
     * 设置jedis连接超时时间
     *
     * @param connectionTimeout
     */
    public RedisSentinelBuilder setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * 设置jedis读写超时时间
     *
     * @param soTimeout
     */
    public RedisSentinelBuilder setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    /**
     * (兼容老客户端)
     *
     * @param timeout 单位:毫秒
     * @return
     */
    public RedisSentinelBuilder setTimeout(int timeout) {
        //兼容老版本
        this.connectionTimeout = timeout;
        this.soTimeout = timeout;
        return this;
    }

    /**
     * 是否开启统计
     *
     * @param clientStatIsOpen
     * @return
     */
    public RedisSentinelBuilder setClientStatIsOpen(boolean clientStatIsOpen) {
        this.clientStatIsOpen = clientStatIsOpen;
        return this;
    }

    private Map<String, Object> getConfigMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("connectTimeout", connectionTimeout);
        map.put("soTimeout", soTimeout);
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
