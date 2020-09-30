## JedisPool资源池优化

### 背景
合理的JedisPool资源池参数设置能为业务使用Redis保驾护航，本文将对JedisPool的使用、资源池的参数进行详细说明，最后给出“最合理”配置。

<a name="c1"/>

### 一、使用方法

以官方的2.9.0为例子(Jedis Release)，Maven依赖如下：

````
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.9.0</version>
    <scope>compile</scope>
</dependency>
````

Jedis使用apache commons-pool2对Jedis资源池进行管理，所以在定义JedisPool时一个很重要的参数就是资源池GenericObjectPoolConfig，使用方式如下，其中有很多资源管理和使用的参数

注意：后面会提到建议用JedisPoolConfig代替GenericObjectPoolConfig

````
GenericObjectPoolConfig jedisPoolConfig = new GenericObjectPoolConfig();
jedisPoolConfig.setMaxTotal(..);
jedisPoolConfig.setMaxIdle(..);
jedisPoolConfig.setMinIdle(..);
jedisPoolConfig.setMaxWaitMillis(..);
...
````

JedisPool的初始化如下:

````
// redisHost和redisPort是实例的IP和端口
// redisPassword是实例的密码
// timeout，这里既是连接超时又是读写超时，从Jedis 2.8开始有区分connectionTimeout和soTimeout的构造函数

JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, timeout, redisPassword);
执行命令如下：
Jedis jedis = null;
try {
    jedis = jedisPool.getResource();
    //具体的命令
    jedis.executeCommand()
} catch (Exception e) {
    logger.error(e.getMessage(), e);
} finally {
    //注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池。
    if (jedis != null) 
        jedis.close();
}
````
<a name="c2"/>

### 二、参数说明

JedisPool保证资源在一个可控范围内，并且提供了线程安全，但是一个合理的GenericObjectPoolConfig配置能为应用使用Redis保驾护航，下面将对它的一些重要参数进行说明和建议：
在当前环境下，Jedis连接就是资源，JedisPool管理的就是Jedis连接。

1. 资源设置和使用

序号 | 参数名 | 含义 | 默认值 | 使用建议
---|--- |--- |---|---
1 | maxTotal | 资源池中最大连接数 | 8	| 设置建议见下节 
2 | maxIdle | 资源池允许最大空闲的连接数 | 8 | 设置建议见下节
3 | minIdle | 	资源池确保最少空闲的连接数 | 0 | 设置建议见下节
4 | blockWhenExhausted | 当资源池用尽后，调用者是否要等待。只有当为true时，下面的maxWaitMillis才会生效 | true | 建议使用默认值
5 | maxWaitMillis | 当资源池连接用尽后，调用者的最大等待时间(单位为毫秒) | -1：表示永不超时 | 不建议使用默认值
6 | testOnBorrow | 向资源池借用连接时是否做连接有效性检测(ping)，无效连接会被移除 | false | 业务量很大时候建议设置为false(多一次ping的开销)。
7 | testOnReturn | 向资源池归还连接时是否做连接有效性检测(ping)，无效连接会被移除 | false | 业务量很大时候建议设置为false(多一次ping的开销)。
8 | jmxEnabled | 是否开启jmx监控，可用于监控 | true | 建议开启，但应用本身也要开启

2. 空闲资源监测

空闲Jedis对象检测，下面四个参数组合来完成，testWhileIdle是该功能的开关。

序号 | 参数名 | 含义 | 默认值 | 使用建议
---|--- |--- |---|---
1 | testWhileIdle | 是否开启空闲资源监测 | false | true
2 | timeBetweenEvictionRunsMillis | 空闲资源的检测周期(单位为毫秒) | -1：不检测 | 建议设置，周期自行选择，也可以默认也可以使用下面JedisPoolConfig中的配置
3 | minEvictableIdleTimeMillis | 资源池中资源最小空闲时间(单位为毫秒)，达到此值后空闲资源将被移除 | 1000 60 30 = 30分钟 | 可根据自身业务决定，大部分默认值即可，也可以考虑使用下面JeidsPoolConfig中的配置
4 | numTestsPerEvictionRun | 做空闲资源检测时，每次的采样数 | 3 | 可根据自身应用连接数进行微调,如果设置为-1，就是对所有连接做空闲监测

为了方便使用，Jedis提供了JedisPoolConfig，它本身继承了GenericObjectPoolConfig设置了一些空闲监测设置
````
public class JedisPoolConfig extends GenericObjectPoolConfig {
  public JedisPoolConfig() {
    // defaults to make your life with connection pool easier :)
    setTestWhileIdle(true);
    //
    setMinEvictableIdleTimeMillis(60000);
    //
    setTimeBetweenEvictionRunsMillis(30000);
    setNumTestsPerEvictionRun(-1);
    }
}
````
所有默认值可以从org.apache.commons.pool2.impl.BaseObjectPoolConfig中看到。

<a name="c3"/>

### 三、资源池大小(maxTotal)、空闲(maxIdle minIdle)设置建议

**1. maxTotal：最大连接数**

实际上这个是一个很难回答的问题，考虑的因素比较多：
    
+ 业务希望Redis并发量；
+ 客户端执行命令时间；
+ Redis资源：例如 nodes(例如应用个数) * maxTotal 是不能超过redis的最大连接数；
+ 资源开销：例如虽然希望控制空闲连接，但是不希望因为连接池的频繁释放创建连接造成不必靠开销。

以一个例子说明，假设:

+ 一次命令时间（borrow|return resource + Jedis执行命令(含网络) ）的平均耗时约为1ms，一个连接的QPS大约是1000
+ 业务期望的QPS是50000

那么理论上需要的资源池大小是50000 / 1000 = 50个。但事实上这是个理论值，还要考虑到要比理论值预留一些资源，通常来讲maxTotal可以比理论值大一些。
但这个值不是越大越好，一方面连接太多占用客户端和服务端资源，另一方面对于Redis这种高QPS的服务器，一个大命令的阻塞即使设置再大资源池仍然会无济于事。

**2. maxIdle minIdle**

maxIdle实际上才是业务需要的最大连接数，maxTotal是为了给出余量，所以maxIdle不要设置过小，否则会有new Jedis(新连接)开销，而minIdle是为了控制空闲资源监测。

连接池的最佳性能是maxTotal = maxIdle ,这样就避免连接池伸缩带来的性能干扰。但是如果并发量不大或者maxTotal设置过高，会导致不必要的连接资源浪费。
可以根据实际总OPS和调用redis客户端的规模整体评估每个节点所使用的连接池。

**3.监控**

实际上最靠谱的值是通过监控来得到“最佳值”的，可以考虑通过一些手段(例如jmx)实现监控，找到合理值。



