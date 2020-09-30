package redis.clients.jedis;

import com.sohu.tv.cc.client.spectator.StatsCollector;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.util.Map;
import java.util.Set;

public abstract class JedisClusterConnectionHandler implements Closeable {
  protected final JedisClusterInfoCache cache;

  public JedisClusterConnectionHandler(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig, final StatsCollector statsCollector,
      int connectionTimeout, int soTimeout, String password) {
    this(nodes, poolConfig, statsCollector, connectionTimeout, soTimeout, password, null);
  }

  public JedisClusterConnectionHandler(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig, final StatsCollector statsCollector,
                                       int connectionTimeout, int soTimeout, String password, String clientName) {
    this(nodes, poolConfig, statsCollector, connectionTimeout, soTimeout, null, password, clientName, false, null, null, null, null);
  }

  public JedisClusterConnectionHandler(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig,
      int connectionTimeout, int soTimeout, String user, String password, String clientName) {
    this(nodes, poolConfig, null, connectionTimeout, soTimeout, user, password, clientName, false, null, null, null, null);
  }

  public JedisClusterConnectionHandler(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig, final StatsCollector statsCollector,
      int connectionTimeout, int soTimeout, String user, String password, String clientName,
      boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier, JedisClusterHostAndPortMap portMap) {
    this.cache = new JedisClusterInfoCache(poolConfig, statsCollector, connectionTimeout, soTimeout, user, password, clientName,
        ssl, sslSocketFactory, sslParameters, hostnameVerifier, portMap);
    initializeSlotsCache(nodes, statsCollector, connectionTimeout, soTimeout, user, password, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
  }

  abstract Jedis getConnection();

  abstract Jedis getConnectionFromSlot(int slot);

  public Jedis getConnectionFromNode(HostAndPort node) {
    return cache.setupNodeIfNotExist(node).getResource();
  }

  public void setupNodeIfNotExist(String hostPort){
      HostAndPort node = new HostAndPort(hostPort.split(":")[0],
              Integer.parseInt(hostPort.split(":")[1]));
      cache.setupNodeIfNotExist(node);
  }

  public void removeNodeIfExist(String hostPort){
    HostAndPort node = new HostAndPort(hostPort.split(":")[0],
            Integer.parseInt(hostPort.split(":")[1]));
    cache.removeNodeIfExist(node);
  }

  public boolean isInSlots(JedisPool jedisPool){
    return cache.isInSlots(jedisPool);
  }

  public Map<String, JedisPool> getNodes() {
    return cache.getNodes();
  }

  private void initializeSlotsCache(Set<HostAndPort> startNodes, StatsCollector statsCollector,
      int connectionTimeout, int soTimeout, String user, String password, String clientName,
      boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
    for (HostAndPort hostAndPort : startNodes) {
      Jedis jedis = null;
      try {
        jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier, statsCollector);
        if (user != null) {
          jedis.auth(user, password);
        } else if (password != null) {
          jedis.auth(password);
        }
        if (clientName != null) {
          jedis.clientSetname(clientName);
        }
        cache.discoverClusterNodesAndSlots(jedis);
        break;
      } catch (JedisConnectionException e) {
        // try next nodes
      } finally {
        if (jedis != null) {
          jedis.close();
        }
      }
    }
  }

  public void renewSlotCache() {
    cache.renewClusterSlots(null);
  }

  public void renewSlotCache(Jedis jedis) {
    cache.renewClusterSlots(jedis);
  }

  @Override
  public void close() {
    cache.reset();
    cache.shutdownCollector();
  }
  public JedisPool getJedisPoolFromSlot(int slot){
      return cache.getSlotPool(slot);
  }

  public Map<Integer, JedisPool> getSlots() {
      return cache.getSlots();
  }
}
