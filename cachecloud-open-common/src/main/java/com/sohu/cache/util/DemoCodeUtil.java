package com.sohu.cache.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Created by hym on 14-11-3.
 */
public class DemoCodeUtil {

    public static final List<String> dependencyRedis = new ArrayList<String>();
    public static final List<String> redisCluster = new ArrayList<String>();
    public static final List<String> redisSentinel = new ArrayList<String>();
    public static final List<String> redisStandalone = new ArrayList<String>();
    
    static {
        ResourceBundle rb = ResourceBundle.getBundle("client");
        String[] redisGoodVersionArr = rb.getString("good_versions").split(",");
        List<String> redisGoodVersions = Arrays.asList(redisGoodVersionArr);
        String redisGoodVersion = redisGoodVersions.get(redisGoodVersions.size() - 1);
        
        dependencyRedis.add("&lt;dependency&gt;                                                     ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;com.sohu.tv&lt;/groupId&gt;                                   ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;cachecloud-open-client-redis&lt;/artifactId&gt;                       ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;" + redisGoodVersion + "&lt;/version&gt;                                           ");

        dependencyRedis.add("&lt;/dependency&gt;                                                    ");
        dependencyRedis.add("&lt;repositories&gt;                                                   ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;repository&gt;                                                     ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;id&gt;nexus_id&lt;/id&gt;                                              ");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;url&gt;" + ConstUtils.MAVEN_WAREHOUSE + "&lt;/url&gt;");
        dependencyRedis.add("&nbsp;&nbsp;&nbsp;&nbsp;&lt;/repository&gt;                                                    ");
        dependencyRedis.add("&lt;/repositories&gt;                                                  ");

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

    public static List<String> getDependency(int appType) {
        List<String> list = null;
        switch (appType) {
            case ConstUtils.CACHE_REDIS_SENTINEL: {
                list = dependencyRedis;
                break;
            }
            case ConstUtils.CACHE_REDIS_STANDALONE: {
                list = dependencyRedis;
                break;
            }
            case ConstUtils.CACHE_TYPE_REDIS_CLUSTER: {
                list = dependencyRedis;
                break;
            }
        }
        return list;

    }

    public static List<String> getSpringConfig(int appType, long appId) {
        /**
         * your code
         */
        return null;
    }

    
}
