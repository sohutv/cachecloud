package com.sohu.tv.cachecloud.client;

import com.alibaba.fastjson.JSONObject;
import com.sohu.tv.cc.client.spectator.heartbeat.ClientStatusEnum;
import com.sohu.tv.cc.client.spectator.util.Constants;
import com.sohu.tv.cc.client.spectator.util.HttpUtils;
import com.sohu.tv.cc.client.spectator.util.StringUtil;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ConnectionId;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.event.ClusterTopologyChangedEvent;
import io.lettuce.core.event.Event;
import io.lettuce.core.event.metrics.CommandLatencyEvent;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Created by yijunzhang on 2018-12-21
 */
public class RedisSentinelClientBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long appId;
    private final String password;

    private volatile RedisClient redisClient;

    private volatile RedisURI redisURI;

    //构建锁
    private final Lock lock = new ReentrantLock();

    //客户端配置
    private ClientOptions.Builder clientOptionsBuilder;

    //客户端资源
    private ClientResources.Builder clientResourcesBuilder;

    //事件消费定义
    private Consumer<Event> eventConsumer;

    public RedisSentinelClientBuilder(long appId, String password) {
        this.appId = appId;
        this.password = password;
    }

    public RedisClient build() {
        if (redisClient == null) {
            while (true) {
                try {
                    if(lock.tryLock(10, TimeUnit.MILLISECONDS)){
                        if (redisClient != null) {
                            return redisClient;
                        }
                        String response = HttpUtils.doGet(String.format(Constants.REDIS_SENTINEL_URL, appId));
                        if (response == null || response.isEmpty()) {
                            logger.warn("get response from remote server error, appId: {}, continue...", appId);
                            continue;
                        }

                        JSONObject jsonObject = JSONObject.parseObject(response);
                        if (jsonObject == null) {
                            logger.warn("sentinel is invalid, info: {}, appId: {}, continue...", response, appId);
                            continue;
                        }
                        int status = jsonObject.getIntValue("status");
                        String message = jsonObject.getString("message");

                        /** 客户端版本检查 **/
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
                        String masterName = jsonObject.getString("masterName");
                        String sentinels = jsonObject.getString("sentinels");
                        RedisURI.Builder uriBuilder = null;

                        for (String sentinelStr : sentinels.split(" ")) {
                            String[] sentinelArr = sentinelStr.split(":");
                            if (sentinelArr.length == 2) {
                                String host = sentinelArr[0];
                                int port = Integer.parseInt(sentinelArr[1]);
                                if (uriBuilder == null) {
                                    uriBuilder = RedisURI.Builder.sentinel(host, port);
                                } else {
                                    uriBuilder.withSentinel(host, port);
                                }
                            }
                        }
                        uriBuilder.withSentinelMasterId(masterName);

                        if (!StringUtil.isBlank(password)) {
                            uriBuilder.withPassword(password);
                        }

                        if (clientOptionsBuilder == null) {
                            clientOptionsBuilder = ClientOptions.builder();
                        }

                        ClientOptions clientOptions = clientOptionsBuilder.build();

                        if (clientResourcesBuilder == null) {
                            clientResourcesBuilder = DefaultClientResources.builder();
                        }

                        ClientResources clientResources = clientResourcesBuilder.build();

                        if (eventConsumer != null) {
                            clientResources.eventBus().get().subscribe(eventConsumer);
                        } else {
                            clientResources.eventBus().get().subscribe(e -> {
                                //连接事件
                                if (e instanceof ConnectionId) {
                                    ConnectionId event = (ConnectionId) e;
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("ConnectionId={} local={} remote={}",
                                                event.getClass().getSimpleName(),
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
                        this.redisURI = uriBuilder.build();
                        redisClient = RedisClient.create(clientResources, redisURI);
                        redisClient.setOptions(clientOptions);
                        return redisClient;
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
        return redisClient;
    }

    public RedisSentinelClientBuilder setClientOptionsBuilder(ClientOptions.Builder clientOptionsBuilder) {
        this.clientOptionsBuilder = clientOptionsBuilder;
        return this;
    }

    public RedisSentinelClientBuilder setClientResourcesBuilder(ClientResources.Builder clientResourcesBuilder) {
        this.clientResourcesBuilder = clientResourcesBuilder;
        return this;
    }

    public RedisSentinelClientBuilder setEventConsumer(Consumer<Event> eventConsumer) {
        this.eventConsumer = eventConsumer;
        return this;
    }

    public RedisURI getRedisURI() {
        return redisURI;
    }

}
