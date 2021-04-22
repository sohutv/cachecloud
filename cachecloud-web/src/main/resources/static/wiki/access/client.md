# 客户端接入

<a name="access"/>

## 一、<span id="cc1">Java接入方法</span>

### 1、<span id="cc12">cachecloud-client-redis接入方式</span>

**Maven坐标:**

```
<!-- cachecloud封装的jedis客户端，需要用cachecloud-client下的jedis模块打包 -->
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>x.y.z-RELEASE</version>
</dependency>

<!-- cachecloud客户端 -->
<dependency>
    <groupId>com.sohu.tv</groupId>
    <artifactId>cachecloud-client-redis</artifactId>
    <version>u.v.w-RELEASE</version>
</dependency>

<!-- 公司内部Nexus仓库配置 -->
<repositories>
    <repository>
        <id>xx.nexus</id>
        <url>http://xx/nexus/content/groups/public</url>
    </repository>
</repositories>

```

**Configuration:**

```
@Configuration
public class RedisConfiguration {
    
    /**
     * Redis Cluster
     */
    @Bean(destroyMethod = "close")
    public PipelineCluster pipelineCluster(@Value("${cachecloud.demo.appId}") long appId) {
        //默认配置
        PipelineCluster pipelineCluster = ClientBuilder.redisCluster(appId).build();
        return pipelineCluster;
    }
    
    /**
     * Redis Sentinel
     */
    @Bean(destroyMethod = "destroy")
    public JedisSentinelPool jedisSentinelPool(@Value("${cachecloud.demo.appId}") long appId) {
        //默认配置
        JedisSentinelPool jedisSentinelPool = ClientBuilder.redisSentinel(appId).build();
        return jedisSentinelPool;
    }
    
    /**
     * Redis Standalone
     */
    @Bean(destroyMethod = "destroy")
    public JedisPool jedisPool(@Value("${cachecloud.demo.appId}") long appId) {
        //默认配置
        JedisPool jedisPool = ClientBuilder.redisStandalone(appId).build();
        return jedisPool;
    }
}
    
    
```
**Usage:**
```
@Component
@Slf4j
public class RedisDao {

    @Autowired
    private PipelineCluster pipelineCluster;

    @Autowired
    private JedisSentinelPool jedisSentinelPool;

    @Autowired
    private JedisPool jedisPool;

    public String getFromCluster(String key) {
        String value = pipelineCluster.get(key);
        log.info("value={}", value);
        return value;
    }

    public String getFromSentinel(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisSentinelPool.getResource();
            String value = jedis.get(key);
            log.info("value={}", value);
            return value;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public String getFromStandalone(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String value = jedis.get(key);
            log.info("value={}", value);
            return value;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }
}

```


### 2、<span id="cc3">cachecloud-client-lettuce接入方式</span>

**Maven坐标:**

```

<!-- lettuce客户端依赖 -->
<dependency>
    <groupId>com.sohu.tv</groupId>
    <artifactId>cachecloud-client-lettuce</artifactId>
    <version>x.y.z-RELEASE</version>
</dependency>

```

**Configuration:**
```
@Configuration
public class LettuceConfiguration {

    @Bean
    public ClientResources.Builder clientResourcesBuilder() {
        return DefaultClientResources.builder()
                .ioThreadPoolSize(8)
                .computationThreadPoolSize(10);
    }

    @Bean
    public ClusterClientOptions.Builder clusterClientOptionsBuilder() {
        SocketOptions socketOptions = SocketOptions.builder().keepAlive(true).tcpNoDelay(false)
                .connectTimeout(Duration.ofSeconds(5)).build();

        ClusterClientOptions.Builder clientOptionsBuilder = ClusterClientOptions.builder()
                .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(5)))
                .socketOptions(socketOptions);

        return clientOptionsBuilder;
    }

    @Bean(destroyMethod = "shutdown")
    public RedisClusterClient redisClusterClient(@Value("${cachecloud.demo.appId}") long appId,
                                                 @Value("${cachecloud.demo.password}") String password,
                                                 ClientResources.Builder clientResourcesBuilder,
                                                 ClusterClientOptions.Builder clusterClientOptionsBuilder) {

        RedisClusterClient redisClusterClient = LettuceClientBuilder
                .redisCluster(appId, password)
                .setClientResourcesBuilder(clientResourcesBuilder)
                .setClusterClientOptionsBuilder(clusterClientOptionsBuilder)
                .build();

        return redisClusterClient;
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisClusterConnection<String, String> clusterConnection(RedisClusterClient redisClusterClient) {

        StatefulRedisClusterConnection<String, String> connection = redisClusterClient.connect();
        connection.setReadFrom(ReadFrom.REPLICA_PREFERRED);
        return connection;
    }

}

```
**Usage:**
```
@Component
@Slf4j
public class RedisDao {

    @Autowired
    private StatefulRedisClusterConnection<String, String> clusterConnection;

    public String get(String key){
        RedisAdvancedClusterCommands<String, String> clusterCommands = clusterConnection.sync();
        String value = clusterCommands.get(key);
        log.info("value={}", value);
        return value;
    }
}
```

### 3、<span id="cc4">跨机房客户端cachecloud-client-crossroom-redis接入方式</span>

**Maven坐标:**

```
<!-- 跨机房客户端依赖 -->
<dependency>
    <groupId>com.sohu.tv</groupId>
    <artifactId>cachecloud-client-crossroom-redis</artifactId>
    <version>x.y.z-RELEASE</version>
</dependency>

```

**Configuration:**
```
@Configuration
public class RedisConfiguration {

    /**
     * 需要同一个业务申请两个应用，部署在不同机房，目前支持两个机房的跨机房使用
     *
     * @param majorAppId 部署在机房一的应用Id
     * @param minorAppId 部署在机房二的应用Id
     */
    @Bean
    public RedisCrossRoomClient redisCrossRoomClient(@Value("${cachecloud.demo.majorAppId}") long majorAppId,
                                                     @Value("${cachecloud.demo.minorAppId}") long minorAppId) {
        PipelineCluster majorPipelineCluster = ClientBuilder.redisCluster(majorAppId).build();
        PipelineCluster minorPipelineCluster = ClientBuilder.redisCluster(minorAppId).build();
        RedisCrossRoomClient redisCrossRoomClient = RedisCrossRoomClientBuilder
                .redisCluster(majorAppId, majorPipelineCluster, minorAppId, minorPipelineCluster)
                .build();
        return redisCrossRoomClient;
    }
}

```

**usage:** 和cachecloud-client-redis使用方式相同

## 二、<span id="cc2">REST API接入方法</span>

### (1) 接口地址
	http://ip:port/cache/client/redis/{appType}/{appId}.json?clientVersion={clientVersion}
	
	appType=cluster|sentinel|standalone    

	appId是应用id

	clientVersion是客户端版本

### (2) REST接口结果
	{
	  message: "client is up to date, Cheers!",
	  shardNum: 10,
	  appId: 10192,
	  status: 1,
	  shardInfo: "10.10.xx.xx:6390,10.10.xx.xx:6382 10.10.xx.xx:6387,10.10.xx.xx:6379 10.10.xx.xx:6387,10.10.xx.xx:7382 10.10.xx.xx:6380,10.10.xx.xx:6392"
	}

## <span id="cc3">三、python客户端接入</span>

**Redis Cluster:** 这里使用支持集群模式的python客户端[redis-py-cluster](https://github.com/Grokzen/redis-py-cluster)，需要先安装 `` pip install redis-py-cluster ``

```
import requests
from rediscluster import RedisCluster

app_id = 10782
redis_type = 'cluster'
password = 'password'
domain = 'xxx.com'

url = 'http://{0}/cache/client/redis/{1}/{2}.json?clientVersion=2.0.3-RELEASE'.format(domain, redis_type, app_id)

response = requests.get(url, timeout=1)

json = response.json()

shard_info_array = json['shardInfo'].split(' ')

seed = {'host': 'localhost', 'port': '6379'}

if len(shard_info_array) > 0:
    seed['host'] = shard_info_array[0].split(':')[0]
    seed['port'] = shard_info_array[0].split(':')[1]

startup_nodes = [seed]

# Note: decode_responses must be set to True when used with python3
rc = RedisCluster(startup_nodes=startup_nodes, decode_responses=True, password=password)

rc.set("foo", "bar")

print(rc.get("foo"))

```

**Redis Sentinel&Standalone:** 使用[redis.io](https://redis.io/clients#python)推荐的python客户端[redis-py](https://github.com/andymccurdy/redis-py)，
需要先安装 `` pip install redis-py ``

Redis Sentinel:

```
from redis.sentinel import Sentinel
import requests

app_id = 10669
redis_type = 'sentinel'
password = 'password'
domain = 'xxx.com'

url = 'http://{0}/cache/client/redis/{1}/{2}.json?clientVersion=2.0.3-RELEASE'.format(domain, redis_type, app_id)

response = requests.get(url, timeout=1)

json = response.json()

sentinels_array = json['sentinels'].split(' ')

sentinels = []

for node in sentinels_array:
    sentinels.append((node.split(':')))

master_name = json['masterName']

sentinel = Sentinel(sentinels)

master = sentinel.master_for(master_name, socket_timeout=1, password=password)

master.set("key1", "value")

v1 = master.get('key1')

print(v1)

```
Redis Standalone:

```
import redis
import requests

app_id = 10744
redis_type = 'standalone'
password = 'password'
domain = 'xxx.com'

url = 'http://{0}/cache/client/redis/{0}/{1}.json?clientVersion=2.0.3-RELEASE'.format(domain, redis_type, app_id)

response = requests.get(url, timeout=1)

json = response.json()

host_port = json['standalone'].split(':')

host = 'localhost'
port = 6379

if len(host_port) > 0:
    host = host_port[0]
    port = host_port[1]

r = redis.Redis(host=host, port=port, password=password)

v2 = r.get('key1')

print(v2)

```


