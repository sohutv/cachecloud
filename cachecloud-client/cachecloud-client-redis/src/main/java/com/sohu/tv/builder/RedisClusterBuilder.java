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
import redis.clients.jedis.*;
import redis.clients.jedis.util.AuthUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * redis cluster 客户端builder
 * Created by yijunzhang on 14-7-27.
 */
public class RedisClusterBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 应用id
     */
    private final long appId;
    /**
     * jedis对象池配置
     */
    private GenericObjectPoolConfig jedisPoolConfig;
    /**
     * jedis集群对象
     */
    private volatile PipelineCluster pipelineCluster;

    /**
     * jedis连接超时(单位:毫秒)
     */
    private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;

    /**
     * jedis读写超时(单位:毫秒)
     */
    private int soTimeout = Protocol.DEFAULT_TIMEOUT;

    /**
     * 节点定位重试次数:默认3次
     */
    private int maxAttempts = 5;

    /**
     * 是否为每个JeidsPool初始化Jedis对象
     */
    private boolean whetherInitIdleJedis = false;

    /**
     * 构建锁
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 是否开启统计,默认开启
     */
    private boolean clientStatIsOpen = true;

    /**
     * 构造函数package访问域，package外不能直接构造实例；
     *
     * @param appId
     */
    RedisClusterBuilder(final long appId) {
        this.appId = appId;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 5);
        poolConfig.setMaxIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 2);
        poolConfig.setMinIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE);
        //JedisPool.borrowObject最大等待时间
        poolConfig.setMaxWaitMillis(Protocol.DEFAULT_TIMEOUT);
        poolConfig.setJmxNamePrefix("jedis-pool");
        this.jedisPoolConfig = poolConfig;
    }

    public PipelineCluster build() {
        if (pipelineCluster == null) {
            while (true) {
                try {
                    lock.tryLock(10, TimeUnit.SECONDS);
                    if (pipelineCluster != null) {
                        return pipelineCluster;
                    }
                    String url = String.format(Constants.REDIS_CLUSTER_URL, String.valueOf(appId));
                    String response = HttpUtils.doGet(url);
                    Map<String, Object> jsonObject = null;
                    try {
                        Object object = JSONUtils.parse(response);
                        if (object instanceof Map) {
                            jsonObject = (Map) object;
                        }
                    } catch (Exception e) {
                        logger.error("remote build error, appId: {}", appId, e);
                    }
                    if (jsonObject == null) {
                        logger.error("get cluster info for appId: {} error. continue...", appId);
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

                    Set<HostAndPort> nodeList = new HashSet<HostAndPort>();

                    String nodeInfo = String.valueOf(jsonObject.get("shardInfo"));
                    String[] pairArray = nodeInfo.split(" ");
                    for (String pair : pairArray) {
                        String[] nodes = pair.split(",");
                        for (String node : nodes) {
                            String[] ipAndPort = node.split(":");
                            if (ipAndPort.length < 2) {
                                continue;
                            }
                            String ip = ipAndPort[0];
                            int port = Integer.parseInt(ipAndPort[1]);
                            nodeList.add(new HostAndPort(ip, port));
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
                    pipelineCluster = new PipelineCluster(jedisPoolConfig, nodeList, connectionTimeout, soTimeout,
                            maxAttempts, pkey, whetherInitIdleJedis, statsCollector);

                    //启动主动刷新集群拓扑线程
                    ClusterAdaptiveRefreshScheduler scheduler = new ClusterAdaptiveRefreshScheduler(pipelineCluster);
                    scheduler.start();

                    return pipelineCluster;
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
            return pipelineCluster;
        }
    }

    /**
     * 设置配置
     *
     * @param jedisPoolConfig
     * @return
     */
    public RedisClusterBuilder setJedisPoolConfig(GenericObjectPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
        return this;
    }

    /**
     * 兼容老版本参数
     *
     * @param timeout
     * @return
     */
    public RedisClusterBuilder setTimeout(final int timeout) {
        this.connectionTimeout = compatibleTimeout(timeout);
        this.soTimeout = compatibleTimeout(timeout);
        return this;
    }

    /**
     * 设置jedis连接超时时间
     *
     * @param connectionTimeout
     */
    public RedisClusterBuilder setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = compatibleTimeout(connectionTimeout);
        return this;
    }

    /**
     * 设置jedis读写超时时间
     *
     * @param soTimeout
     */
    public RedisClusterBuilder setSoTimeout(int soTimeout) {
        this.soTimeout = compatibleTimeout(soTimeout);
        return this;
    }

    /**
     * 是否为每个JedisPool创建空闲Jedis
     *
     * @param whetherInitIdleJedis
     * @return
     */
    public RedisClusterBuilder setWhetherInitIdleJedis(boolean whetherInitIdleJedis) {
        this.whetherInitIdleJedis = whetherInitIdleJedis;
        return this;
    }

    /**
     * redis操作超时时间:默认2秒
     * 如果timeout小于0 超时:200微秒
     * 如果timeout小于100 超时:timeout*10000微秒
     * 如果timeout大于100 超时:timeout微秒
     */
    private int compatibleTimeout(int paramTimeOut) {
        if (paramTimeOut <= 0) {
            return Protocol.DEFAULT_TIMEOUT;
        } else if (paramTimeOut < 100) {
            return paramTimeOut * 1000;
        } else {
            return paramTimeOut;
        }
    }

    /**
     * 兼容老的api
     *
     * @param maxRedirections
     * @return
     */
    public RedisClusterBuilder setMaxRedirections(final int maxRedirections) {
        return setMaxAttempts(maxRedirections);
    }

    /**
     * 节点定位重试次数:默认5次
     */
    public RedisClusterBuilder setMaxAttempts(final int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * 是否开启统计
     *
     * @param clientStatIsOpen
     * @return
     */
    public RedisClusterBuilder setClientStatIsOpen(boolean clientStatIsOpen) {
        this.clientStatIsOpen = clientStatIsOpen;
        return this;
    }

    private Map<String, Object> getConfigMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("connectTimeout", connectionTimeout);
        map.put("soTimeout", soTimeout);
        map.put("maxTotal", jedisPoolConfig.getMaxTotal());
        map.put("minIdle", jedisPoolConfig.getMinIdle());
        map.put("maxIdle", jedisPoolConfig.getMaxIdle());
        map.put("maxWaitMillis", jedisPoolConfig.getMaxWaitMillis());
        map.put("testWhileIdle", jedisPoolConfig.getTestWhileIdle());
        map.put("testOnBorrow", jedisPoolConfig.getTestOnBorrow());
        map.put("testOnCreate", jedisPoolConfig.getTestOnCreate());
        map.put("testOnReturn", jedisPoolConfig.getTestOnReturn());
        map.put("timeBetweenEvictionRunsMillis", jedisPoolConfig.getTimeBetweenEvictionRunsMillis());
        return map;
    }
}
