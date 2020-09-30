package com.sohu.tv.cachecloud.client.redisson;


import com.alibaba.fastjson.JSONObject;
import com.sohu.tv.cc.client.spectator.util.Constants;
import com.sohu.tv.cc.client.spectator.util.StringUtil;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

/**
 * Created by yijunzhang
 */
public class RedissonStandaloneClientBuilder extends RedissonAbstractClientBuilder {

    private SingleServerConfig singleServerConfig;

    public SingleServerConfig getSingleServerConfig() {
        if (singleServerConfig == null) {
            config = getConfig();
            singleServerConfig = config.useSingleServer();
        }
        return singleServerConfig;
    }

    public RedissonStandaloneClientBuilder(long appId, String password) {
        super(appId, password);
    }

    @Override
    void config(Config config, JSONObject jsonObject, String password) {
        String standalone = jsonObject.getString("standalone");
        if (StringUtil.isBlank(standalone)) {
            throw new IllegalStateException("standalone is null json: " + jsonObject.toJSONString());
        }
        String[] instanceArr = standalone.split(":");
        String ip = instanceArr[0];
        int port = Integer.parseInt(instanceArr[1]);
        String nodeAddress = "redis://" + ip + ":" + port;
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(nodeAddress);
        if (!StringUtil.isBlank(password)) {
            singleServerConfig.setPassword(password);
        }
    }

    @Override
    String clientUrl(long appId) {
        return String.format(Constants.REDIS_STANDALONE_URL, String.valueOf(appId));
    }



}
