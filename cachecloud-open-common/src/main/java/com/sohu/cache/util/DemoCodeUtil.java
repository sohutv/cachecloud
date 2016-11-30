package com.sohu.cache.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by hym on 14-11-3.
 */
public class DemoCodeUtil {

    public static final List<String> redisCluster = new ArrayList<String>();
    public static final List<String> redisSentinel = new ArrayList<String>();
    public static final List<String> redisStandalone = new ArrayList<String>();
    
    public static final List<String> redisSentinelSpring = new ArrayList<String>();
    public static final List<String> redisClusterSpring = new ArrayList<String>();
    
    
    private static final String springAppId = "${your appId}";
    
    static {
        redisCluster.add("/**                                                                       ");
        redisCluster.add(" * 使用自定义配置：                                                         ");
        redisCluster.add(" *  1. setTimeout：redis操作的超时设置；                                    ");
        redisCluster.add(" *  2. setMaxRedirections：节点定位重试的次数；                              ");
        redisCluster.add(" */                                                                       ");
        redisCluster.add("GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();       ");
        redisCluster.add("JedisCluster redisCluster = ClientBuilder.redisCluster(appId)                       ");
        redisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.setJedisPoolConfig(poolConfig)                                   ");
        redisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.setConnectionTimeout(1000)                                                    ");
        redisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.setSoTimeout(1000)                                                    ");
        redisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.setMaxRedirections(5)                                            ");
        redisCluster.add("&nbsp;&nbsp;&nbsp;&nbsp;.build();                                                         ");
        redisCluster.add("//1.字符串value");
        redisCluster.add("redisCluster.set(\"key1\", \"value1\");                                ");
        redisCluster.add("System.out.println(redisCluster.get(\"key1\"));                  ");

        
        redisSentinel.add("/**                                                                      ");
        redisSentinel.add(" * 自定义配置                                                             ");
        redisSentinel.add(" */                                                                      ");
        redisSentinel.add("GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();      ");
        redisSentinel.add("JedisSentinelPool sentinelPool = ClientBuilder.redisSentinel(appId)                        ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;.setConnectionTimeout(1000)                                                ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;.setSoTimeout(1000)                                                ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;.setPoolConfig(poolConfig)                                       ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;.build();                                                        ");
        
        redisSentinel.add("Jedis jedis = null;                                                                           ");
        redisSentinel.add("try {                                                                           ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis = sentinelPool.getResource();                                         ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(jedis.get(\"key1\"));                  ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;//1.字符串value                                                              ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis.set(\"key1\", \"1\");                                                  "); 
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp");
        
        redisSentinel.add("} catch (Exception e) {                                                         ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;e.printStackTrace();                                            ");
        redisSentinel.add("} finally {                                                                     ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;if(jedis!=null)                                                                ");
        redisSentinel.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis.close();                                                                 ");
        redisSentinel.add("}                                                                               ");
        
        

        redisStandalone.add("// 使用默认配置                                                      ");
        redisStandalone.add("//jedisPool = ClientBuilder.redisStandalone(appId).build();         ");
        redisStandalone.add("/**                                                                 ");
        redisStandalone.add(" * 使用自定义配置                                                    ");
        redisStandalone.add(" */                                                                 ");
        redisStandalone.add("GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig(); ");
        
        redisStandalone.add("JedisPool jedisPool = ClientBuilder.redisStandalone(appId)                    ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;.setTimeout(2000)                                           ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;.setPoolConfig(poolConfig)                                  ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;.build();                                                   ");
        
        redisStandalone.add("Jedis jedis = null;                                                                           ");
        redisStandalone.add("try {                                                                           ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis = jedisPool.getResource();                                          ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis.set(\"key1\", \"1\");                                                  ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(jedis.get(\"key1\"));                  ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp");
        
        redisStandalone.add("} catch (Exception e) {                                                         ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;e.printStackTrace();                                            ");
        redisStandalone.add("} finally {                                                                     ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;if(jedis!=null)                                                                ");
        redisStandalone.add("&nbsp;&nbsp;&nbsp;&nbsp;jedis.close();                                                                 ");
        redisStandalone.add("}                                                                               ");
        
        
        //redis-sentinel的spring配置
        redisSentinelSpring.add("//spring 配置");
        redisSentinelSpring.add("&lt;bean id=\"redisSentinelFactory\" class=\"com.sohu.tv.mobil.common.data.RedisSentinelFactory\" init-method=\"init\"&gt;");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=\"appId\" value=\""+springAppId+"\"/&gt;");
        redisSentinelSpring.add("&lt;/bean&gt;");
        redisSentinelSpring.add("&lt;bean id=\"redisSentinelPool\" factory-bean=\"redisSentinelFactory\" factory-method=\"getJedisSentinelPool\"/&gt;");
        redisSentinelSpring.add("&lt;!--高效的序列化工具--/&gt");
        redisSentinelSpring.add("&lt;bean id=\"protostuffSerializer\" class=\"redis.clients.jedis.serializable.ProtostuffSerializer\"/&gt;");
        redisSentinelSpring.add("");
        redisSentinelSpring.add("package xx.xx;");
        redisSentinelSpring.add("import com.sohu.tv.builder.ClientBuilder;");
        redisSentinelSpring.add("import org.apache.commons.pool2.impl.GenericObjectPoolConfig;");
        redisSentinelSpring.add("import org.slf4j.Logger;");
        redisSentinelSpring.add("import org.slf4j.LoggerFactory;");
        redisSentinelSpring.add("import redis.clients.jedis.JedisSentinelPool;");
        redisSentinelSpring.add("public class RedisSentinelFactory {");
        redisSentinelSpring.add("");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private final Logger logger = LoggerFactory.getLogger(this.getClass());");
        redisSentinelSpring.add("");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private JedisSentinelPool jedisSentinelPool;");
        redisSentinelSpring.add("");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private int appId;");
        redisSentinelSpring.add("");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public void init(){");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//根据自己需要设置poolConfig");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMaxTotal(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 10);");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMaxIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 5);");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMinIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 2);");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMaxWaitMillis(1000L);");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setJmxEnabled(true);");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;try {");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//根据自己需要设置超时时间");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jedisSentinelPool = ClientBuilder.redisSentinel(appId)");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.setTimeout(2000)");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.setPoolConfig(poolConfig)");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.build();");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;} catch (Exception e) {");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.error(e.getMessage(), e);");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        redisSentinelSpring.add("");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public JedisSentinelPool getJedisSentinelPool() {");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return jedisSentinelPool;");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        redisSentinelSpring.add("");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public void setAppId(int appId) {");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this.appId = appId;");
        redisSentinelSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        redisSentinelSpring.add("}");
        
        // redis-cluster的spring配置
        redisClusterSpring.add("//spring 配置");
        redisClusterSpring.add("&lt;bean id=\"redisClusterFactory\" class=\"xx.xx.RedisClusterFactory\" init-method=\"init\"&gt;");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;property name=\"appId\" value=\""+springAppId+"\"/&gt;");
        redisClusterSpring.add("&lt;/bean>");
        redisClusterSpring.add("&lt;bean id=\"redisCluster\" factory-bean=\"redisClusterFactory\" factory-method=\"getRedisCluster\"/&gt;");
        redisClusterSpring.add("&lt;!--高效的序列化工具--/&gt");
        redisClusterSpring.add("&lt;bean id=\"protostuffSerializer\" class=\"redis.clients.jedis.serializable.ProtostuffSerializer\"/&gt;");
        redisClusterSpring.add("");
        redisClusterSpring.add("package xx.xx;");
        redisClusterSpring.add("import com.sohu.tv.builder.ClientBuilder;");
        redisClusterSpring.add("import org.apache.commons.pool2.impl.GenericObjectPoolConfig;");
        redisClusterSpring.add("import org.slf4j.Logger;");
        redisClusterSpring.add("import org.slf4j.LoggerFactory;");
        redisClusterSpring.add("import redis.clients.jedis.PipelineCluster;");
        redisClusterSpring.add("public class RedisClusterFactory {");
        redisClusterSpring.add("");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private final Logger logger = LoggerFactory.getLogger(this.getClass());");
        redisClusterSpring.add("");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private PipelineCluster redisCluster;");
        redisClusterSpring.add("");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;private int appId;");
        redisClusterSpring.add("");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public void init() {");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//根据自己需要设置poolConfig");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMaxTotal(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL * 10);");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMaxIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 5);");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMinIdle(GenericObjectPoolConfig.DEFAULT_MAX_IDLE * 2);");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setMaxWaitMillis(1000L);");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolConfig.setJmxEnabled(true);");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;try {");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//根据自己需要修改参数");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;redisCluster = ClientBuilder.redisCluster(appId)");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.setJedisPoolConfig(poolConfig)");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.setTimeout(2)");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.build();");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;} catch (Exception e) {");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.error(e.getMessage(), e);");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        redisClusterSpring.add("");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public PipelineCluster getRedisCluster() {");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return redisCluster;");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        redisClusterSpring.add("");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;public void setAppId(int appId) {");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this.appId = appId;");
        redisClusterSpring.add("&nbsp;&nbsp;&nbsp;&nbsp;}");
        redisClusterSpring.add("}");

    }

    /**
     * 获取依赖
     * @return
     */
    public static List<String> getDependencyRedis() {
        List<String> dependencyRedis = new ArrayList<String>();
        
        // redis版本
        String redisGoodVersion = getGoodVersion();
        
        // 依赖
        dependencyRedis.add("&lt;dependency&gt;                                                     ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;com.sohu.tv&lt;/groupId&gt;                                   ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;cachecloud-open-client-redis&lt;/artifactId&gt;                       ");
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
        }
        if (list != null && list.size() > 0) {
            if (!list.get(0).contains("appId =")) {
                list.add(0, "long appId = " + appId + ";");
            }
//            else {
//                list.set(0, "long appId = " + appDesc.getAppId() + ";");
//            }
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
//                list = new ArrayList<String>();
//                list.add("CACHE_REDIS_STANDALONE spring");
                break;
            }
            case ConstUtils.CACHE_TYPE_REDIS_CLUSTER: {
                list.addAll(redisClusterSpring);
                break;
            }
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
        return null;
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
        }
        return ConstUtils.CC_DOMAIN + "/cache/client/redis/" + appTypePath + "/" + appId + ".json?clientVersion="
                + redisGoodVersion;
    }

    
}
