package com.sohu.tv.cachecloud.client.redisson;

import com.alibaba.fastjson.JSONObject;
import com.sohu.tv.cc.client.spectator.heartbeat.ClientStatusEnum;
import com.sohu.tv.cc.client.spectator.util.HttpUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yijunzhang
 */
public abstract class RedissonAbstractClientBuilder {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long appId;
    private final String password;

    private volatile RedissonClient redissonClient;

    //构建锁
    private final Lock lock = new ReentrantLock();

    //客户端配置
    protected Config config;

    //配置客户端
    abstract void config(Config config, JSONObject jsonObject, String password);

    abstract String clientUrl(long appId);

    public RedissonAbstractClientBuilder(long appId, String password) {
        this.appId = appId;
        this.password = password;
    }

    public RedissonClient build() {
        if (redissonClient == null) {
            while (true) {
                try {
                    lock.tryLock(10, TimeUnit.SECONDS);
                    if (redissonClient != null) {
                        return redissonClient;
                    }
                    String url = clientUrl(appId);
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
                    if (config != null) {
                        // clone for Config
                        config = new Config(config);
                    } else {
                        config = new Config();
                    }
                    this.config(config, jsonObject, password);
                    //logger.info("redisson config: {}", config.toYAML());
                    redissonClient = Redisson.create(config);
                    return redissonClient;
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
        return redissonClient;
    }

    public Config getConfig() {
        if(config == null){
            config = new Config();
        }
        return config;
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

}
