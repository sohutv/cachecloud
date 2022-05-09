package com.sohu.cache.redis.util;

import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/11/25 17:43
 * @Description:
 */
public class JedisUtil {

    public static final String SENTINEL_FLUSH_CONFIG = "flushconfig";

    public static List<LatencyItem> latencyLatest(Jedis jedis){
        Client client = jedis.getClient();
        client.sendCommand(Command.LATENCY, Keyword.LATEST.raw);
        List<LatencyItem> latencyItems = LatencyItem.from(client.getObjectMultiBulkReply());
        return latencyItems;
    }

    public static String sentinelFlushConfig(Jedis jedis){
        Client client = jedis.getClient();
        client.sentinel(SENTINEL_FLUSH_CONFIG);
        return client.getStatusCodeReply();
    }

    public static String getHostPort(Jedis jedis){
        Client client = jedis.getClient();
        return client.getHost() + ":" + client.getPort();
    }

}
