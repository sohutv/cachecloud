package com.sohu.cache.redis.impl;

import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.redis.util.Command;
import com.sohu.cache.redis.util.ProtostuffSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.SafeEncoder;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AssistRedisServiceImpl implements AssistRedisService {
    private Logger logger = LoggerFactory.getLogger(AssistRedisServiceImpl.class);

    @Value("${cachecloud.redis.main.host:127.0.0.1}")
    private String mainHost;

    @Value("${cachecloud.redis.main.port:6379}")
    private int mainPort;

    @Value("${cachecloud.redis.main.password:}")
    private String mainPassword;

    private JedisPool jedisPoolMain;

    private ProtostuffSerializer protostuffSerializer = new ProtostuffSerializer();

    @PostConstruct
    public void init() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        if(StringUtils.isNotBlank(mainPassword)){
            jedisPoolMain = new JedisPool(config, mainHost, mainPort, Protocol.DEFAULT_TIMEOUT, mainPassword);
        }else{
            logger.error("The assist redis password is not configured, please confirm and strongly recommend config it.");
            jedisPoolMain = new JedisPool(config, mainHost, mainPort, Protocol.DEFAULT_TIMEOUT);
        }
    }

    /**
     * low版本，应该用vip或者hystrix，这里是以防万一
     *
     * @return
     */
    private Jedis getFromJedisPool() throws Exception{
        try {
            return jedisPoolMain.getResource();
        } catch (JedisConnectionException ce){
            logger.warn("Please Make sure the file:application-${profile}.yml connection pool is configured correctly !  cachecloud.redis.main.host:{} cachecloud.redis.main.port:{} cachecloud.redis.main.password:{}",mainHost,mainPort,mainPassword);
            throw ce;
        } catch (Exception e) {
            logger.warn(e.getMessage(),e);
            throw e;
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
            logger.warn("rpush {} {} error " + e.getMessage(), key, item, e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean rpush(String key, String... items) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.rpush(key, items);
            return true;
        } catch (Exception e) {
            logger.warn("rpush {} {} error " + e.getMessage(), key, items, e);
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
            logger.warn("lrange {} {} {} error " + e.getMessage(), key, start, end);
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
            logger.warn("rpushList {} {} error " + e.getMessage(), key, items);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long llen(final String key){
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            Long llen = jedis.llen(key);
            return llen;
        } catch (Exception e) {
            logger.warn("llen {} {} error " + e.getMessage(), key);
            return 0L;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String lpop(final String key){
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            String lpop = jedis.lpop(key);
            return lpop;
        } catch (Exception e) {
            logger.warn("rpushList {} {} error " + e.getMessage(), key);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long lrem(final String key, long count, String element){
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            Long lrem = jedis.lrem(key, count, element);
            return lrem;
        } catch (Exception e) {
            logger.warn("lrem {} {} {} error " + e.getMessage(), key, count, element);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String ltrim(final String key, long start, long end){
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            String lrem = jedis.ltrim(key, start, end);
            return lrem;
        } catch (Exception e) {
            logger.warn("ltrim {} {} {} error " + e.getMessage(), key, start, end);
            return null;
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
            logger.warn("saddList {} {} error " + e.getMessage(), key, items);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean sadd(String key, String item) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.sadd(key, item);
            return true;
        } catch (Exception e) {
            logger.warn("sadd {} {} error " + e.getMessage(), key, item);
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
            logger.warn("smembers {} error " + e.getMessage(), key);
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
            logger.warn("srem {} {} error " + e.getMessage(), key, item);
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
            logger.warn("set {} error " + e.getMessage(), key, e);
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
            logger.warn("setex {} {} error " + e.getMessage(), key, seconds);
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
            logger.warn("setnx {} {} error:{} ", key, value, e.getMessage());
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
            logger.warn("set {} {} {} error " + e.getMessage(), key, value, params);
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
            logger.warn("setWithNoSerialize {} error " + e.getMessage(), key);
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
            logger.warn("setWithNoSerialize {} error " + e.getMessage(), key);
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
            logger.warn("getWithNoSerialize {} error " + e.getMessage(), key);
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
            logger.warn("remove {} error " + e.getMessage(), key);
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
            logger.warn("zadd {} {} {} error " + e.getMessage(), key, score, member);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String hget(String key, String field){
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.hget(key, field);
        } catch (Exception e) {
            logger.warn("hget {} {} error " + e.getMessage(), key, field);
            return null;
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
            logger.warn("hset {} {} {} error " + e.getMessage(), key, field, value);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long hsetnx(String key, String field, String value){
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.hsetnx(key, field, value);
        } catch (Exception e) {
            logger.warn("hsetnx {} {} {} error " + e.getMessage(), key, field, value);
            return 0L;
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
            logger.warn("hset {} {} error " + e.getMessage(), key, map);
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
            logger.warn("hgetAll {} error " + e.getMessage(), key);
            return Collections.emptyMap();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long hdel(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.hdel(key, field);
        } catch (Exception e) {
            logger.warn("hdel {} error " + e.getMessage(), key);
            return 0L;
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
            logger.warn("get {} error " + e.getMessage(), key);
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
            logger.warn("del {} error " + e.getMessage(), key);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean delMulti(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            jedis.del(keys);
            return true;
        } catch (Exception e) {
            logger.warn("delMulti {} error " + e.getMessage(), keys);
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
            logger.warn("zincrby {} {} {} error " + e.getMessage(), key, score, member);
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
            logger.warn("zrangeWithScores {} {} {}error " + e.getMessage(), key, start, end);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            return jedis.exists(key);
        } catch (Exception e) {
            logger.warn("del {} error " + e.getMessage(), key);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setProtostuffSerializer(ProtostuffSerializer protostuffSerializer) {
        this.protostuffSerializer = protostuffSerializer;
    }

    @Override
    public boolean setNEX(String key, String value, int seconds) {
        Jedis jedis = null;
        try {
            jedis = getFromJedisPool();
            Object rst = jedis.sendCommand(Command.SET, key, value, "NX", "EX", String.valueOf(seconds));
            if(rst != null){
                String encode = SafeEncoder.encode((byte[]) rst);
                if("OK".equals(encode)){
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.warn("smembers {} error " + e.getMessage(), key);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
