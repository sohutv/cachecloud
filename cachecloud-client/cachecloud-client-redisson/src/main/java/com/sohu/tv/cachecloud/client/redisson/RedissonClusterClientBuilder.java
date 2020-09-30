package com.sohu.tv.cachecloud.client.redisson;

import com.alibaba.fastjson.JSONObject;
import com.sohu.tv.cc.client.spectator.util.Constants;
import com.sohu.tv.cc.client.spectator.util.StringUtil;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;

/**
 * Created by yijunzhang
 */
public class RedissonClusterClientBuilder extends RedissonAbstractClientBuilder {

    private ClusterServersConfig clusterServersConfig;

    public ClusterServersConfig getClusterServersConfig() {
        if (clusterServersConfig == null) {
            config = getConfig();
            this.clusterServersConfig = config.useClusterServers();
        }
        return clusterServersConfig;
    }

    public RedissonClusterClientBuilder(long appId, String password) {
        super(appId, password);
    }

    @Override
    void config(Config config, JSONObject jsonObject, String password) {
        String nodeInfo = jsonObject.getString("shardInfo");
        String[] pairArray = nodeInfo.split(" ");
        ClusterServersConfig clusterConfig = config.useClusterServers();
        for (String pair : pairArray) {
            String[] nodes = pair.split(",");
            for (String node : nodes) {
                String[] ipAndPort = node.split(":");
                if (ipAndPort.length < 2) {
                    continue;
                }
                String ip = ipAndPort[0];
                int port = Integer.parseInt(ipAndPort[1]);
                String nodeAddress = "redis://" + ip + ":" + port;
                clusterConfig.addNodeAddress(nodeAddress);
            }
        }
        clusterConfig.setScanInterval(2000) //配置集群状态扫描间隔时间，单位毫秒
                .setReadMode(ReadMode.MASTER) //只在主服务节点里读取
                .setSubscriptionMode(SubscriptionMode.MASTER); //只在主服务节点里订阅
        if (!StringUtil.isBlank(password)) {
            clusterConfig.setPassword(password);
        }
    }

    @Override
    String clientUrl(long appId) {
        return String.format(Constants.REDIS_CLUSTER_URL, String.valueOf(appId));
    }
}
