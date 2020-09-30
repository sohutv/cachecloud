package com.sohu.tv.cachecloud.client.redisson;

import com.alibaba.fastjson.JSONObject;
import com.sohu.tv.cc.client.spectator.util.Constants;
import com.sohu.tv.cc.client.spectator.util.StringUtil;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SubscriptionMode;

/**
 * Created by yijunzhang
 */
public class RedissonSentinelClientBuilder extends RedissonAbstractClientBuilder {

    private SentinelServersConfig sentinelServersConfig;

    public SentinelServersConfig getSentinelServersConfig() {
        if (sentinelServersConfig == null) {
            config = getConfig();
            sentinelServersConfig = config.useSentinelServers();
        }
        return sentinelServersConfig;
    }

    public RedissonSentinelClientBuilder(long appId, String password) {
        super(appId, password);
    }

    @Override
    void config(Config config, JSONObject jsonObject, String password) {
        String masterName = jsonObject.getString("masterName");
        String sentinels = jsonObject.getString("sentinels");
        SentinelServersConfig sentinelConfig = config.useSentinelServers();
        sentinelConfig.setMasterName(masterName)
                .setReadMode(ReadMode.MASTER) //只在主服务节点里读取
                .setSubscriptionMode(SubscriptionMode.MASTER); //只在主服务节点里订阅

        for (String sentinelStr : sentinels.split(" ")) {
            String[] sentinelArr = sentinelStr.split(":");
            if (sentinelArr.length == 2) {
                String ip = sentinelArr[0];
                int port = Integer.parseInt(sentinelArr[1]);
                String nodeAddress = "redis://" + ip + ":" + port;
                sentinelConfig.addSentinelAddress(nodeAddress);
            }
        }

        if (!StringUtil.isBlank(password)) {
            sentinelConfig.setPassword(password);
        }
    }

    @Override
    String clientUrl(long appId) {
        return String.format(Constants.REDIS_SENTINEL_URL, String.valueOf(appId));
    }

}
