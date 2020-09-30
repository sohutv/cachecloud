package com.sohu.tv.cachecloud.client;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sohu.tv.cc.client.spectator.heartbeat.ClientStatusEnum;
import com.sohu.tv.cc.client.spectator.util.Constants;
import com.sohu.tv.cc.client.spectator.util.HttpUtils;
import com.sohu.tv.cc.client.spectator.util.StringUtil;
import io.lettuce.core.ConnectionId;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.event.ClusterTopologyChangedEvent;
import io.lettuce.core.event.Event;
import io.lettuce.core.event.metrics.CommandLatencyEvent;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Created by yijunzhang on 2018-12-18
 */
public class RedisClusterClientBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long appId;
    private final String password;

    private volatile RedisClusterClient redisClusterClient;

    //集群节点列表
    private volatile List<RedisURI> redisURIs;

    //构建锁
    private final Lock lock = new ReentrantLock();

    //客户端配置
    private ClusterClientOptions.Builder clusterClientOptionsBuilder;

    //客户端资源
    private ClientResources.Builder clientResourcesBuilder;

    //事件消费定义
    private Consumer<Event> eventConsumer;

    public RedisClusterClientBuilder(long appId, String password) {
        this.appId = appId;
        this.password = password;
    }

    public RedisClusterClient build() {
        if (redisClusterClient == null) {
            while (true) {
                try {
                    if(lock.tryLock(10, TimeUnit.SECONDS)){
                        if (redisClusterClient != null) {
                            return redisClusterClient;
                        }
                        String url = String.format(Constants.REDIS_CLUSTER_URL, String.valueOf(appId));
                        String response = HttpUtils.doGet(url);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        if (jsonObject == null) {
                            logger.warn("cluster is invalid, info: {}, appId: {}, continue...", response, appId);
                            continue;
                        }
                        int status = jsonObject.getIntValue("status");
                        String message = jsonObject.getString("message");

                        /** 检查客户端版本 **/
                        if (status == ClientStatusEnum.ERROR.getStatus()) {
                            throw new IllegalStateException(message);
                        } else if (status == ClientStatusEnum.WARN.getStatus()) {
                            logger.warn(message);
                        } else {
                            logger.info(message);
                        }

                        this.redisURIs = Lists.newArrayList();

                        String nodeInfo = jsonObject.getString("shardInfo");
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
                                RedisURI redisURI = RedisURI.create(ip, port);
                                if (!StringUtil.isBlank(password)) {
                                    redisURI.setPassword(password);
                                }
                                redisURIs.add(redisURI);
                            }
                        }
                        ClusterClientOptions clusterClientOptions;
                        ClientResources clientResources;

                        if (clusterClientOptionsBuilder == null) {
                            clusterClientOptionsBuilder = ClusterClientOptions.builder();
                        }
                        // cachecloud 接口每次会返回所有在线节点列表,只使用种子节点作为拓扑发现的源节点，
                        // 如果为true，会尝试连接下线节点(ClusterTopologyRefresh.discoveredNodes)导致错误日志
                        // 如果为false，种子节点失效，会导致服务发现失败。

                        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                                .dynamicRefreshSources(true)
                                .enableAllAdaptiveRefreshTriggers()
                                .enablePeriodicRefresh(true)//启动定期自动更新拓扑
                                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(10))// 间隔10秒更新拓扑
                                .closeStaleConnections(true)
                                .build();
                        clusterClientOptionsBuilder.topologyRefreshOptions(topologyRefreshOptions);

                        clusterClientOptions = clusterClientOptionsBuilder.build();

                        if (clientResourcesBuilder == null) {
                            clientResourcesBuilder = DefaultClientResources.builder();
                        }

                        clientResources = clientResourcesBuilder.build();
                        if (eventConsumer != null) {
                            clientResources.eventBus().get().subscribe(eventConsumer);
                        } else {
                            clientResources.eventBus().get().subscribe(e -> {
                                //连接事件
                                if (e instanceof ConnectionId) {
                                    ConnectionId event = (ConnectionId) e;
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("ConnectionId={} local={} remote={}", event.getClass().getSimpleName(),
                                                event.localAddress(),
                                                event.remoteAddress());
                                    }
                                }
                                if (e instanceof CommandLatencyEvent) {
                                    CommandLatencyEvent latencyEvent = (CommandLatencyEvent) e;
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("CommandLatencyEvent {}", latencyEvent);
                                    }
                                }
                                //集群拓扑事件
                                if (e instanceof ClusterTopologyChangedEvent) {
                                    ClusterTopologyChangedEvent event = (ClusterTopologyChangedEvent) e;
                                    logger.warn("ClusterTopologyChangedEvent before={} after={}", event.before(),
                                            event.after());
                                }
                            });
                        }

                        this.redisClusterClient = RedisClusterClient.create(clientResources, redisURIs);
                        redisClusterClient.setOptions(clusterClientOptions);
                        return redisClusterClient;
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
        }

        return redisClusterClient;
    }

    public RedisClusterClientBuilder setClusterClientOptionsBuilder(ClusterClientOptions.Builder clusterClientOptionsBuilder) {
        this.clusterClientOptionsBuilder = clusterClientOptionsBuilder;
        return this;
    }

    public RedisClusterClientBuilder setClientResourcesBuilder(ClientResources.Builder clientResourcesBuilder) {
        this.clientResourcesBuilder = clientResourcesBuilder;
        return this;
    }

    public RedisClusterClientBuilder setEventConsumer(Consumer<Event> eventConsumer) {
        this.eventConsumer = eventConsumer;
        return this;
    }

    public List<RedisURI> getRedisURIs() {
        return redisURIs;
    }
}
