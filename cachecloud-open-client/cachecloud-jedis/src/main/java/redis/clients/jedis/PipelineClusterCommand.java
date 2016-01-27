package redis.clients.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisMovedDataException;
import redis.clients.jedis.exceptions.JedisRedirectionException;
import redis.clients.util.JedisClusterCRC16;

import java.util.*;

/**
 * Created by yijunzhang on 14-5-26.
 */
public abstract class PipelineClusterCommand<T> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JedisClusterConnectionHandler connectionHandler;

    protected final PipelineCluster pipelineCluster;

    public PipelineClusterCommand(PipelineCluster pipelineCluster, JedisClusterConnectionHandler connectionHandler) {
        this.pipelineCluster = pipelineCluster;
        this.connectionHandler = connectionHandler;
    }

    /**
     * 执行批处理命令
     *
     * @param pipeline
     */
    public abstract void pipelineCommand(Pipeline pipeline, List<String> pipelineKeys);

    public abstract T getResult(Map<String, Object> resultMap);

    public T run(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        Map<JedisPool, List<String>> poolKeysMap = getPoolKeyMap(keys);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (Map.Entry<JedisPool, List<String>> entry : poolKeysMap.entrySet()) {
            JedisPool jedisPool = entry.getKey();
            List<String> subkeys = entry.getValue();
            if (subkeys == null || subkeys.isEmpty()) {
                continue;
            }
            //申请jedis对象
            Jedis jedis = null;
            Pipeline pipeline = null;
            List<Object> subResultList = null;
            try {
                jedis = jedisPool.getResource();
                pipeline = jedis.pipelined();
                pipelineCommand(pipeline, subkeys);
                subResultList = pipeline.syncAndReturnAll();
            } catch (JedisConnectionException e) {
                logger.error("RedisConnectionError-{}:{} keys={}", jedisPool.getHost(), jedisPool.getPort(), subkeys);
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (pipeline != null)
                    pipeline.clean();
                //释放jedis对象
                if (jedis != null) {
                    jedis.close();
                }
            }
            if (subResultList == null || subResultList.isEmpty()) {
                continue;
            }
            if (subResultList.size() == subkeys.size()) {
                for (int i = 0; i < subkeys.size(); i++) {
                    String key = subkeys.get(i);
                    Object result = subResultList.get(i);
                    resultMap.put(key, result);
                }
            } else {
                logger.error("PipelineClusterCommand:subkeys={} subResultList={}", subkeys, subResultList);
            }
        }
        return getResult(resultMap);
    }

    private Map<JedisPool, List<String>> getPoolKeyMap(List<String> keys) {
        Map<JedisPool, List<String>> poolKeysMap = new LinkedHashMap<JedisPool, List<String>>();
        try {
            for (String key : keys) {
                JedisPool jedisPool;
                int slot = JedisClusterCRC16.getSlot(key);
                jedisPool = connectionHandler.getJedisPoolFromSlot(slot);
                if (poolKeysMap.containsKey(jedisPool)) {
                    poolKeysMap.get(jedisPool).add(key);
                } else {
                    List<String> subKeyList = new ArrayList<String>();
                    subKeyList.add(key);
                    poolKeysMap.put(jedisPool, subKeyList);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return poolKeysMap;
    }

    protected boolean checkException(Object obj) {
        if (obj instanceof Exception) {
            Exception e = (Exception) obj;
            if (e instanceof JedisRedirectionException) {
                //重定向slot 映射.
                if (e instanceof JedisMovedDataException) {
                    // it rebuilds cluster's slot cache
                    // recommended by Redis cluster specification
                    this.connectionHandler.renewSlotCache();
                    logger.warn("JedisMovedDataException:" + e.getMessage(), e);
                } else {
                    logger.error("pipeline-error:" + e.getMessage(), e);
                }
            } else {
                logger.error(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }

}
