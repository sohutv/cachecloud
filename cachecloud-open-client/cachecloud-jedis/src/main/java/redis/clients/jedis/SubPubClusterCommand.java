package redis.clients.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.*;
import redis.clients.util.JedisClusterCRC16;

/**
 * Created by yijunzhang on 14-7-19.
 */
public class SubPubClusterCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final PipelineCluster pipelineCluster;

    private final JedisClusterConnectionHandler connectionHandler;

    private final int redirections;

    private ThreadLocal<Jedis> askConnection = new ThreadLocal<Jedis>();

    public SubPubClusterCommand(PipelineCluster pipelineCluster, JedisClusterConnectionHandler connectionHandler,
            int redirections) {
        this.pipelineCluster = pipelineCluster;
        this.connectionHandler = connectionHandler;
        this.redirections = redirections;
    }

    private Jedis returnRetriesJedis(String key, int redirections,
                                     boolean tryRandomNode, boolean asking) {
        if (redirections <= 0) {
            throw new JedisClusterMaxRedirectionsException(
                    "Too many Cluster redirections? key=" + key);
        }

        Jedis jedis = null;
        try {
            if (asking) {
                // TODO: Pipeline asking with the original command to make it
                // faster....
                jedis = askConnection.get();
                jedis.asking();

                // if asking success, reset asking flag
                asking = false;
            } else if (tryRandomNode) {
                jedis = connectionHandler.getConnection();
            } else {
                jedis = connectionHandler.getConnectionFromSlot(JedisClusterCRC16.getSlot(key));
            }

            return jedis;
        } catch (JedisConnectionException jce) {
            if (tryRandomNode) {
                // maybe all connection is down
                throw jce;
            }

            releaseConnection(jedis);
            // retry with random connection
            return returnRetriesJedis(key, redirections--, true, asking);
        }  catch (JedisRedirectionException jre) {
            // if MOVED redirection occurred,
            if (jre instanceof JedisMovedDataException) {
                // it rebuilds cluster's slot cache
                // recommended by Redis cluster specification
                this.connectionHandler.renewSlotCache(jedis);
            }

            // release current connection before recursion or renewing
            releaseConnection(jedis);
            jedis = null;

            if (jre instanceof JedisAskDataException) {
                asking = true;
                askConnection.set(this.connectionHandler.getConnectionFromNode(jre.getTargetNode()));
            } else if (jre instanceof JedisMovedDataException) {
            } else {
                throw new JedisClusterException(jre);
            }

            return returnRetriesJedis(key, redirections - 1, false, asking);
        }
    }

    public Jedis getJedis(String channel) {
        return returnRetriesJedis(channel, this.redirections, false, false);
    }

    public Jedis getNewJedis(String channel, int timeout) {
        Jedis jedis = getJedis(channel);
        try {
            String host = jedis.getClient().getHost();
            int port = jedis.getClient().getPort();
            Jedis newJedis = new Jedis(host, port, timeout);
            String pong = newJedis.ping();
            if (pong == null || !pong.equals("PONG")) {
                throw new JedisException("SubPubCluster:jedis is not ping !");
            }
            return newJedis;
        } finally {
            releaseConnection(jedis);
        }
    }

    public void releaseConnection(Jedis connection) {
        if (connection != null) {
            connection.close();
        }
    }

}
