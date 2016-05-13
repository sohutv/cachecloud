package com.sohu.tv.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.tv.cachecloud.client.basic.heartbeat.ClientStatusEnum;
import com.sohu.tv.cachecloud.client.basic.heartbeat.HeartbeatInfo;
import com.sohu.tv.cachecloud.client.basic.util.ConstUtils;
import com.sohu.tv.cachecloud.client.basic.util.HttpUtils;
import com.sohu.tv.cachecloud.client.jedis.stat.ClientDataCollectReportExecutor;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
    private JedisCluster jedisCluster;

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
    private int maxRedirections = 5;

    /**
     * 构建锁
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 构造函数package访问域，package外不能直接构造实例；
     *
     * @param appId
     */
    RedisClusterBuilder(final long appId) {
        this.appId = appId;
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 5);
        poolConfig.setMaxIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 2);
        poolConfig.setMinIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE);
        //JedisPool.borrowObject最大等待时间
        poolConfig.setMaxWaitMillis(1000L);
        poolConfig.setJmxNamePrefix("jedis-pool");
        poolConfig.setJmxEnabled(true);
        this.jedisPoolConfig = poolConfig;
    }

    public JedisCluster build() {
        if (jedisCluster == null) {
            while (true) {
                try {
                    lock.tryLock(10, TimeUnit.SECONDS);
                    if (jedisCluster != null) {
                        return jedisCluster;
                    }
                    String url = String.format(ConstUtils.REDIS_CLUSTER_URL, String.valueOf(appId));
                    String response = HttpUtils.doGet(url);
                    ObjectMapper objectMapper = new ObjectMapper();
                    HeartbeatInfo heartbeatInfo = null;
                    try {
                        heartbeatInfo = objectMapper.readValue(response, HeartbeatInfo.class);
                    } catch (IOException e) {
                        logger.error("remote build error, appId: {}", appId, e);
                    }
                    if (heartbeatInfo == null) {
                        continue;
                    }

                    /** 检查客户端版本 **/
                    if (heartbeatInfo.getStatus() == ClientStatusEnum.ERROR.getStatus()) {
                        throw new IllegalStateException(heartbeatInfo.getMessage());
                    } else if (heartbeatInfo.getStatus() == ClientStatusEnum.WARN.getStatus()) {
                        logger.warn(heartbeatInfo.getMessage());
                    } else {
                        logger.info(heartbeatInfo.getMessage());
                    }

                    Set<HostAndPort> nodeList = new HashSet<HostAndPort>();
                    //形如 ip1:port1,ip2:port2,ip3:port3
                    String nodeInfo = heartbeatInfo.getShardInfo();
                    //为了兼容,如果允许直接nodeInfo.split(" ")
                    nodeInfo = nodeInfo.replace(" ", ",");
                    String[] nodeArray = nodeInfo.split(",");
                    for (String node : nodeArray) {
                        String[] ipAndPort = node.split(":");
                        if (ipAndPort.length < 2) {
                            continue;
                        }
                        String ip = ipAndPort[0];
                        int port = Integer.parseInt(ipAndPort[1]);
                        nodeList.add(new HostAndPort(ip, port));
                    }
                    
                    //收集上报数据
//                    ClientDataCollectReportExecutor.getInstance();
                    
                    jedisCluster = new JedisCluster(nodeList, connectionTimeout, soTimeout, maxRedirections, jedisPoolConfig);
                    return jedisCluster;
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
            return jedisCluster;
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
     * 设置jedis连接超时时间
     * @param connectionTimeout
     */
    public RedisClusterBuilder setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * 设置jedis读写超时时间
     * @param soTimeout
     */
    public RedisClusterBuilder setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    /**
     * 节点定位重试次数:默认5次
     */
    public RedisClusterBuilder setMaxRedirections(final int maxRedirections) {
        this.maxRedirections = maxRedirections;
        return this;
    }
}
