package com.sohu.cache.redis.impl;

import com.sohu.cache.redis.AssistRedisService;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.serializable.ProtostuffSerializer;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AssistRedisServiceImpl implements AssistRedisService {
    private Logger logger = LoggerFactory.getLogger(AssistRedisServiceImpl.class);

    @Value("${cachecloud.redis.main.host}")
    private String mainHost;

    @Value("${cachecloud.redis.main.port}")
    private int mainPort;

    @Value("${cachecloud.redis.main.password}")
    private String mainPassword;

    private JedisPool jedisPoolMain;

    private ProtostuffSerializer protostuffSerializer = new ProtostuffSerializer();

    @PostConstruct
    public void init() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        jedisPoolMain = new JedisPool(config, mainHost, mainPort, Protocol.DEFAULT_TIMEOUT, mainPassword);
    }

    /**
     * low版本，应该用vip或者hystrix，这里是以防万一
     *
     * @return
     */
    private Jedis getFromJedisPool() {
        try {
            return jedisPoolMain.getResource();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean rpush(String key, String item) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.rpush(key, item);
            return true;
        } catch (Exception e) {
            logger.error("rpush {} {} error " + e.getMessage(), key, item, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public List<String> lrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error("lrange {} {} {} error " + e.getMessage(), key, start, end, e);
            return Collections.emptyList();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean rpushList(String key, List<String> items) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.rpush(key, items.toArray(new String[items.size()]));
            return true;
        } catch (Exception e) {
            logger.error("rpushList {} {} error " + e.getMessage(), key, items, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean saddSet(String key, Set<String> items) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.sadd(key, items.toArray(new String[items.size()]));
            return true;
        } catch (Exception e) {
            logger.error("saddList {} {} error " + e.getMessage(), key, items, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Set<String> smembers(String key) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.smembers(key);
        } catch (Exception e) {
            logger.error("smembers {} error " + e.getMessage(), key, e);
            return Collections.emptySet();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean srem(String key, String item) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.srem(key, item);
            return true;
        } catch (Exception e) {
            logger.error("srem {} {} error " + e.getMessage(), key, item, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean reloadSentinel() {
        return false;
    }

    @Override
    public <T> boolean set(String key, T value) {
        if (value == null) {
            return false;
        }
        byte[] bytes = protostuffSerializer.serialize(value);

        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.set(key.getBytes(Charset.forName("UTF-8")), bytes);
            return true;
        } catch (Exception e) {
            logger.error("set {} error " + e.getMessage(), key, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> boolean set(String key, T value, int seconds) {
        if (value == null) {
            return false;
        }
        byte[] bytes = protostuffSerializer.serialize(value);

        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.setex(key.getBytes(Charset.forName("UTF-8")), seconds, bytes);
            return true;
        } catch (Exception e) {
            logger.error("setex {} {} error " + e.getMessage(), key, seconds, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean setNx(String key, String value) {
        Jedis jedis = null;
        Long result = 0l;
        try {
            jedis = getFromJedisPool();
            result = jedis.setnx(key, value);
        } catch (Exception e) {
            logger.error("setnx {} {} error:{} ", key, value, e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result == 1 ? true : false;
    }

    public String set(String key, String value, SetParams params) {

        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.set(key, value, params);
        } catch (Exception e) {
            logger.error("set {} {} {} error " + e.getMessage(), key, value, params, e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> boolean setWithNoSerialize(String key, T value) {
        if (value == null) {
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.set(key, value.toString());
            return true;
        } catch (Exception e) {
            logger.error("setWithNoSerialize {} error " + e.getMessage(), key, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> boolean setWithNoSerialize(String key, T value, int seconds) {
        if (value == null) {
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.setex(key, seconds, value.toString());
            return true;
        } catch (Exception e) {
            logger.error("setWithNoSerialize {} error " + e.getMessage(), key, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String getWithNoSerialize(String key) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.get(key);
        } catch (Exception e) {
            logger.error("getWithNoSerialize {} error " + e.getMessage(), key, e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean remove(String key) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.del(key);
            return true;
        } catch (Exception e) {
            logger.error("remove {} error " + e.getMessage(), key, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean zadd(String key, long score, String member) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.zadd(key, score, member);
            return true;
        } catch (Exception e) {
            logger.error("zadd {} {} {} error " + e.getMessage(), key, score, member, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean hset(String key, String field, String value) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.hset(key, field, value);
            return true;
        } catch (Exception e) {
            logger.error("hset {} {} {} error " + e.getMessage(), key, field, value, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    @Override
    public boolean hmset(String key, Map<String, String> map) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.hmset(key, map);
            return true;
        } catch (Exception e) {
            logger.error("hset {} {} error " + e.getMessage(), key, map, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.hgetAll(key);
        } catch (Exception e) {
            logger.error("hgetAll {} error " + e.getMessage(), key, e);
            return Collections.emptyMap();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> T get(String key) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            byte[] bytes = jedis.get(key.getBytes(Charset.forName("UTF-8")));
            if (bytes == null) {
                return null;
            }
            T t = protostuffSerializer.deserialize(bytes);
            return t;
        } catch (Exception e) {
            logger.error("get {} error " + e.getMessage(), key, e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean del(String key) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.del(key);
            return true;
        } catch (Exception e) {
            logger.error("del {} error " + e.getMessage(), key, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void zincrby(String key, double score, String member) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.zincrby(key, score, member);
        } catch (Exception e) {
            logger.error("zincrby {} {} {} error " + e.getMessage(), key, score, member, e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.zrangeWithScores(key, start, end);
        } catch (Exception e) {
            logger.error("zrangeWithScores {} {} {}error " + e.getMessage(), key, start, end, e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setProtostuffSerializer(ProtostuffSerializer protostuffSerializer) {
        this.protostuffSerializer = protostuffSerializer;
    }
}
