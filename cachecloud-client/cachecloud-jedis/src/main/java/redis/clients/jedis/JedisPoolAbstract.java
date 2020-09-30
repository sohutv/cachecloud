package redis.clients.jedis;

import com.sohu.tv.cc.client.spectator.StatsCollector;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.util.Pool;

public class JedisPoolAbstract extends Pool<Jedis> {

  protected StatsCollector statsCollector;

  public JedisPoolAbstract() {
    super();
  }

  public JedisPoolAbstract(GenericObjectPoolConfig poolConfig, PooledObjectFactory<Jedis> factory) {
    super(poolConfig, factory);
  }

  @Override
  protected void returnBrokenResource(Jedis resource) {
    super.returnBrokenResource(resource);
  }

  @Override
  protected void returnResource(Jedis resource) {
    super.returnResource(resource);
  }

  @Override
  public void close() {
    destroy();
    //关闭指标采集器
    if(statsCollector != null){
      statsCollector.shutdown();
    }
  }
}
