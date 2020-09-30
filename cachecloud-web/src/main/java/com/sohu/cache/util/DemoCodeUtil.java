package com.sohu.cache.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by hym
 */
public class DemoCodeUtil {

    public static final List<String> redisCluster;
    public static final List<String> redisSentinel;
    public static final List<String> redisStandalone;

    public static final List<String> redisSentinelSpring;
    public static final List<String> redisClusterSpring;


    private static final String springAppId = "${your appId}";

    static {
        List<String> tmpRedisCluster = new ArrayList<>();
        tmpRedisCluster.add("PipelineCluster redisCluster = null;                                   ");
        tmpRedisCluster.add("// 使用默认配置                                                             ");
        tmpRedisCluster.add("//redisCluster = ClientBuilder.redisCluster(appId).build();            ");
        tmpRedisCluster.add("/**                                                                       ");
        tmpRedisCluster.add(" * 使用自定义配置：                                                         ");
        tmpRedisCluster.add(" *  1. setTimeout：redis操作的超时设置，默认2000毫秒。                                    ");
        tmpRedisCluster.add(" *  2. setMaxRedirections：节点重定向的最大次数，不建议修改。                              ");
        tmpRedisCluster.add(" */                                                                       ");
        tmpRedisCluster.add("JedisPoolConfig poolConfig = new JedisPoolConfig();       ");
        tmpRedisCluster.add("redisCluster = ClientBuilder.redisCluster(appId)                       ");
        tmpRedisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.setJedisPoolConfig(poolConfig)                                   ");
        tmpRedisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.setTimeout(Protocol.DEFAULT_TIMEOUT)                                                    ");
        tmpRedisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.setMaxRedirections(5)                                                    ");
        tmpRedisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.build();                                                         ");
        tmpRedisCluster.add("//1.字符串value");
        tmpRedisCluster.add("redisCluster.set(\"key1\", \"value1\");                                ");
        tmpRedisCluster.add("assertEquals(\"value1\", redisCluster.get(\"key1\"));                  ");
        tmpRedisCluster.add("//2.实体型value");
        tmpRedisCluster.add("//高效的序列化工具(spring中要配置成单例)");
        tmpRedisCluster.add("ProtostuffSerializer protostuffSerializer = new ProtostuffSerializer();");
        tmpRedisCluster.add("long vid = 120;");
        tmpRedisCluster.add("String videoName = \"屌丝男士\";");
        tmpRedisCluster.add("String videoKey = \"video:\" + vid;");
        tmpRedisCluster.add("//long vid; String videoName");
        tmpRedisCluster.add("Video video = new Video(vid, videoName);");
        tmpRedisCluster.add("redisCluster.set(videoKey, protostuffSerializer.serialize(video));");
        tmpRedisCluster.add("byte[] resultBytes = redisCluster.getBytes(videoKey);");
        tmpRedisCluster.add("// 反序列化获取结果");
        tmpRedisCluster.add("Video resultVideo = protostuffSerializer.deserialize(resultBytes);");
        tmpRedisCluster.add("assertEquals(videoName, resultVideo.getName());");
        redisCluster = Collections.unmodifiableList(tmpRedisCluster);

        List<String> tmpRedisSentinel = new ArrayList<>();
        tmpRedisSentinel.add("JedisSentinelPool sentinelPool = null;                                   ");
        tmpRedisSentinel.add("// 使用默认配置                                                           ");
        tmpRedisSentinel.add("//sentinelPool = ClientBuilder.redisSentinel(appId).build();             ");
        tmpRedisSentinel.add("/**                                                                      ");
        tmpRedisSentinel.add(" * 自定义配置：setTimeout 连接和操作超时时间，默认2000毫秒。                  ");
        tmpRedisSentinel.add(" */                                                                      ");
        tmpRedisSentinel.add("JedisPoolConfig poolConfig = new JedisPoolConfig();      ");
        tmpRedisSentinel.add("sentinelPool = ClientBuilder.redisSentinel(appId)                        ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;.setTimeout(Protocol.DEFAULT_TIMEOUT)                                                ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;.setPoolConfig(poolConfig)                                       ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;.build();                                                        ");

        tmpRedisSentinel.add("Jedis jedis = null;                                                                           ");
        tmpRedisSentinel.add("try {                                                                           ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis = sentinelPool.getResource();                                         ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;//1.字符串value                                                              ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis.set(\"key1\", \"1\");                                                  ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;//2.实体型value");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;//高效的序列化工具(spring中要配置成单例)，已经内置在cachecloud的客户端中直接引入即可.");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;ProtostuffSerializer protostuffSerializer = new ProtostuffSerializer();");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;long vid = 120;");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;String videoName = \"屌丝男士\";");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;String videoKey = \"video:\" + vid;");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;//long vid; String videoName");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;Video video = new Video(vid, videoName);");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis.set(videoKey.getBytes(), protostuffSerializer.serialize(video));");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;byte[] resultBytes = jedis.get(videoKey.getBytes());");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;//反序列化获取结果");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;Video resultVideo = protostuffSerializer.deserialize(resultBytes);");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;assertEquals(videoName, resultVideo.getName());");

        tmpRedisSentinel.add("} catch (Exception e) {                                                         ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;logger.error(e.getMessage(), e);                                            ");
        tmpRedisSentinel.add("} finally {                                                                     ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;if(jedis!=null)                                                                ");
        tmpRedisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jedis.close();                                                                 ");
        tmpRedisSentinel.add("}                                                                               ");
        redisSentinel = Collections.unmodifiableList(tmpRedisSentinel);

        List<String> tmpRedisStandalone = new ArrayList<>();
        tmpRedisStandalone.add("JedisPool jedisPool = null;                                         ");
        tmpRedisStandalone.add("// 使用默认配置                                                      ");
        tmpRedisStandalone.add("//jedisPool = ClientBuilder.redisStandalone(appId).build();         ");
        tmpRedisStandalone.add("/**                                                                 ");
        tmpRedisStandalone.add(" * 自定义配置：setTimeout 连接和操作超时时间，默认2000毫秒。                  ");
        tmpRedisStandalone.add(" */                                                                 ");
        tmpRedisStandalone.add("JedisPoolConfig poolConfig = new JedisPoolConfig(); ");
        tmpRedisStandalone.add("poolConfig.setMaxWaitMillis(Protocol.DEFAULT_TIMEOUT);                                  ");
        tmpRedisStandalone.add("jedisPool = ClientBuilder.redisStandalone(appId)                    ");
        tmpRedisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;.setTimeout(Protocol.DEFAULT_TIMEOUT)                                           ");
        tmpRedisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;.setPoolConfig(poolConfig)                                  ");
        tmpRedisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;.build();                                                   ");
        tmpRedisStandalone.add("Jedis jedis = jedisPool.getResource();                              ");
        tmpRedisStandalone.add("jedis.setnx(\"key2\", \"5\");                                       ");
        tmpRedisStandalone.add("assertEquals(\"10\", jedis.incrBy(\"key2\", 5));                    ");
        tmpRedisStandalone.add("jedis.close();                                                       ");
        redisStandalone = Collections.unmodifiableList(tmpRedisStandalone);

        //redis-sentinel的spring配置
        List<String> tmpRedisSentinelSpring = new ArrayList<>();
        tmpRedisSentinelSpring.add("//spring 配置");
        tmpRedisSentinelSpring.add("&lt;bean id=\"redisSentinelFactory\" class=\"com.sohu.tv.mobil.common.data.RedisSentinelFactory\" init-method=\"init\"&gt;");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=\"appId\" value=\"" + springAppId + "\"/&gt;");
        tmpRedisSentinelSpring.add("&lt;/bean&gt;");
        tmpRedisSentinelSpring.add("&lt;bean id=\"redisSentinelPool\" factory-bean=\"redisSentinelFactory\" factory-method=\"getJedisSentinelPool\"/&gt;");
        tmpRedisSentinelSpring.add("&lt;!--高效的序列化工具--/&gt");
        tmpRedisSentinelSpring.add("&lt;bean id=\"protostuffSerializer\" class=\"redis.clients.jedis.serializable.ProtostuffSerializer\"/&gt;");
        tmpRedisSentinelSpring.add("");
        tmpRedisSentinelSpring.add("package xx.xx;");
        tmpRedisSentinelSpring.add("import com.sohu.tv.builder.ClientBuilder;");
        tmpRedisSentinelSpring.add("import redis.clients.jedis.JedisPoolConfig;");
        tmpRedisSentinelSpring.add("import org.slf4j.Logger;");
        tmpRedisSentinelSpring.add("import org.slf4j.LoggerFactory;");
        tmpRedisSentinelSpring.add("import redis.clients.jedis.JedisSentinelPool;");
        tmpRedisSentinelSpring.add("import redis.clients.jedis.Protocol;");
        tmpRedisSentinelSpring.add("public class RedisSentinelFactory {");
        tmpRedisSentinelSpring.add("");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private final Logger logger = LoggerFactory.getLogger(this.getClass());");
        tmpRedisSentinelSpring.add("");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private JedisSentinelPool jedisSentinelPool;");
        tmpRedisSentinelSpring.add("");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private int appId;");
        tmpRedisSentinelSpring.add("");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public void init(){");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//根据自己需要设置poolConfig");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;JedisPoolConfig poolConfig = new JedisPoolConfig();");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMaxWaitMillis(Protocol.DEFAULT_TIMEOUT);");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;try {");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//根据自己需要设置超时时间");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jedisSentinelPool = ClientBuilder.redisSentinel(appId)");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.setPoolConfig(poolConfig)");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.build();");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;} catch (Exception e) {");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.error(e.getMessage(), e);");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        tmpRedisSentinelSpring.add("");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public JedisSentinelPool getJedisSentinelPool() {");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return jedisSentinelPool;");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        tmpRedisSentinelSpring.add("");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public void setAppId(int appId) {");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this.appId = appId;");
        tmpRedisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        tmpRedisSentinelSpring.add("}");
        redisSentinelSpring = Collections.unmodifiableList(tmpRedisSentinelSpring);

        // redis-cluster的spring配置
        List<String> tmpRedisClusterSpring = new ArrayList<>();
        tmpRedisClusterSpring.add("//spring 配置");
        tmpRedisClusterSpring.add("&lt;bean id=\"redisClusterFactory\" class=\"xx.xx.RedisClusterFactory\" init-method=\"init\"&gt;");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=\"appId\" value=\"" + springAppId + "\"/&gt;");
        tmpRedisClusterSpring.add("&lt;/bean>");
        tmpRedisClusterSpring.add("&lt;bean id=\"redisCluster\" factory-bean=\"redisClusterFactory\" factory-method=\"getRedisCluster\"/&gt;");
        tmpRedisClusterSpring.add("&lt;!--高效的序列化工具--/&gt");
        tmpRedisClusterSpring.add("&lt;bean id=\"protostuffSerializer\" class=\"redis.clients.jedis.serializable.ProtostuffSerializer\"/&gt;");
        tmpRedisClusterSpring.add("");
        tmpRedisClusterSpring.add("package xx.xx;");
        tmpRedisClusterSpring.add("import com.sohu.tv.builder.ClientBuilder;");
        tmpRedisClusterSpring.add("import redis.clients.jedis.JedisPoolConfig;");
        tmpRedisClusterSpring.add("import org.slf4j.Logger;");
        tmpRedisClusterSpring.add("import org.slf4j.LoggerFactory;");
        tmpRedisClusterSpring.add("import redis.clients.jedis.PipelineCluster;");
        tmpRedisClusterSpring.add("import redis.clients.jedis.Protocol;");
        tmpRedisClusterSpring.add("public class RedisClusterFactory {");
        tmpRedisClusterSpring.add("");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private final Logger logger = LoggerFactory.getLogger(this.getClass());");
        tmpRedisClusterSpring.add("");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private PipelineCluster redisCluster;");
        tmpRedisClusterSpring.add("");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private int appId;");
        tmpRedisClusterSpring.add("");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public void init() {");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//根据自己需要设置poolConfig");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;JedisPoolConfig poolConfig = new JedisPoolConfig();");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMaxWaitMillis(Protocol.DEFAULT_TIMEOUT);");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;try {");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//根据自己需要修改参数");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;redisCluster = ClientBuilder.redisCluster(appId)");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.setJedisPoolConfig(poolConfig)");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.build();");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;} catch (Exception e) {");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.error(e.getMessage(), e);");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        tmpRedisClusterSpring.add("");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public PipelineCluster getRedisCluster() {");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return redisCluster;");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        tmpRedisClusterSpring.add("");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public void setAppId(int appId) {");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this.appId = appId;");
        tmpRedisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        tmpRedisClusterSpring.add("}");
        redisClusterSpring = Collections.unmodifiableList(tmpRedisClusterSpring);
    }

    /**
     * 获取依赖
     *
     * @return
     */
    public static List<String> getDependencyRedis() {
        List<String> dependencyRedis = new ArrayList<String>();

        // redis版本
        String redisGoodVersion = getGoodVersion();

        // 依赖
        dependencyRedis.add("&lt;dependency&gt;                                                     ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;com.sohu.tv&lt;/groupId&gt;                                   ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;cachecloud-client-redis&lt;/artifactId&gt;                       ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;" + redisGoodVersion + "&lt;/version&gt;                                           ");

        dependencyRedis.add("&lt;/dependency&gt;                                                    ");
        dependencyRedis.add("&lt;repositories&gt;                                                   ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;repository&gt;                                                     ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;id&gt;sohu.nexus&lt;/id&gt;                                              ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;url&gt;" + ConstUtils.MAVEN_WAREHOUSE + "&lt;/url&gt;");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;/repository&gt;                                                    ");
        dependencyRedis.add("&lt;/repositories&gt;                                                  ");

        return dependencyRedis;
    }

    /**
     * 获取最好的版本
     *
     * @return
     */
    private static String getGoodVersion() {
        String[] redisGoodVersionArr = ConstUtils.GOOD_CLIENT_VERSIONS.split(ConstUtils.COMMA);
        List<String> redisGoodVersions = Arrays.asList(redisGoodVersionArr);
        String redisGoodVersion = redisGoodVersions.get(redisGoodVersions.size() - 1);
        return redisGoodVersion;
    }

    public static List<String> getCode(int appType, long appId) {
        List<String> list = null;
        switch (appType) {
            case ConstUtils.CACHE_REDIS_SENTINEL: {
                list = new ArrayList<String>(redisSentinel);
                break;
            }
            case ConstUtils.CACHE_REDIS_STANDALONE: {
                list = new ArrayList<String>(redisStandalone);
                break;
            }
            case ConstUtils.CACHE_TYPE_REDIS_CLUSTER: {
                list = new ArrayList<String>(redisCluster);
                break;
            }
            default:
                break;
        }
        if (list != null && list.size() > 0) {
            if (!list.get(0).contains("appId =")) {
                list.add(0, "long appId = " + appId + ";");
            }
        }
        return list;
    }

    public static List<String> getSpringConfig(int appType, long appId) {
        List<String> list = new ArrayList<String>();
        switch (appType) {
            case ConstUtils.CACHE_REDIS_SENTINEL: {
                list.addAll(redisSentinelSpring);
                break;
            }
            case ConstUtils.CACHE_REDIS_STANDALONE: {
                break;
            }
            case ConstUtils.CACHE_TYPE_REDIS_CLUSTER: {
                list.addAll(redisClusterSpring);
                break;
            }
            default:
                break;
        }

        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                String line = list.get(i);
                if (line != null && line.contains(springAppId)) {
                    line = line.replace(springAppId,
                            String.valueOf(appId));
                    list.set(i, line);
                }
            }
        }
        return list;
    }

    public static String getRestAPI(int appType, long appId) {
        String redisGoodVersion = getGoodVersion();
        String appTypePath = "";
        switch (appType) {
            case ConstUtils.CACHE_REDIS_SENTINEL: {
                appTypePath = "sentinel";
                break;
            }
            case ConstUtils.CACHE_REDIS_STANDALONE: {
                appTypePath = "standalone";
                break;
            }
            case ConstUtils.CACHE_TYPE_REDIS_CLUSTER: {
                appTypePath = "cluster";
                break;
            }
            default:
                break;
        }
        return "http://${domain}/cache/client/redis/" + appTypePath + "/" + appId + ".json?clientVersion="
                + redisGoodVersion;
    }

}
