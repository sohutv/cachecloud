## 以下是关于CacheCloud定制客户端版本变更记录

### <span style="color:green">CacheCloud定制客户端分类说明：</span>

CacheCloud 3.0以上版本中已移除cachecloud-client模块，请移至[cachecloud-client](https://github.com/sohutv/cachecloud-client)开源地址查看。

- 1.[cachecloud-client-redis](#j1) (基于Jedis定制客户端)
- 2.[cachecloud-jedis](#j2) (定制完善Jedis客户端)
- 3.[cachecloud-client-crossroom-redis](#j3) (跨机房容灾高可用客户端)
- 4.[cachecloud-client-lettuce](#j4) (基于lettuce定制客户端)
- 5.[cachecloud-client-spectator](#j5) (客户端上报类库)


### 一、**<span id="j1">cachecloud-client-redis客户端</span>**

> <span id="j10">2.0.1版本</span>

  - **5. 版本号: 2.0.1-RELEASE**
      + 4.1. 发布日期：2020-03-17
      + 4.2. 重要变更：    
        ````
         1) 增加对指标收集器StatCollector非空判断，为空则不上报指标，兼容不通过Builder构造Jedis实例的情况；
         2) 优化命令调用逻辑，解决因为命令调用较少导致的漏报和延迟上报的问题。
        ````

> <span id="j11">2.0.0版本</span>

  - **4. 版本号: 2.0.0-RELEASE**
      + 4.1. 发布日期：2020-02-12
      + 4.2. 重要变更：    
        ````
         1) 支持Redis 5 API，Jedis相关类路径变更；
         2) 重构Jedis客户端，增加命令超时和连接异常等指标统计上报逻辑；
         3) RedisClusterBuilder/RedisSentinelBuilder/RedisStandaloneBuilder客户端对同一appid只实例化一次。
        ````
      
> <span id="j12">1.7.0版本</span>

   - **3. 版本号: 1.7.1-SNAPSHOT**
     
      + 3.1. 发布日期：2019-03-21
      + 3.2. 重要变更：
         ````
         1) 优化Redis集群下应用只用pipeline批量读写，当redis节点变更或下线，客户端无法获取到新节点拓扑。
         ````
      
   - **2. 版本号: 1.7.0-SNAPSHOT**
     + 2.1 发布日期：2018-09-14
     + 2.2 重要变更：
         ````
         1) 支持standalone/sentinel/cluster在线密码修改；
         2) 修复上报因版本编译产生的内存泄漏问题。
         ````

> <span id="j13">1.6.0版本</span>

   - **1. 版本号: 1.6.0-SNAPSHOT**
     + 1.1 发布日期：2018-09-14
     + 1.2 重要变更：
         ````
         1) 支持connectTimeOut和soTimeOut参数，同时兼容老版本的setTime方法；
         2) 增加支持客户端上报开关；
         3) 客户端键值区间支持。
         ````

### 二、**<span id="j2">Jedis</span>**

> <span id="j20">3.1.0版本</span>

- **3. 版本号: 3.1.0-CC-2-RELEASE**
     + 2.1 发布日期：2020-03-17
     + 2.2 重要变更：
         ````
         1) 加上对指标收集器StatCollector非空判断，为空则不上报指标，兼容不通过Builder构造Jedis实例的情况。
         ````

- **3. 版本号: 3.1.0-CC-1-RELEASE**
     + 2.1 发布日期：2020-02-12
     + 2.2 重要变更：
         ````
         1) 合并官方jedis3.1.0版本；
         2) 支持Redis 5相关api。
         ````

> <span id="j22">2.9.0版本</span>

- **2. 版本号: 2.9.0-CC-4-SNAPSHOT**
     + 1.1 发布日期：2019-06-28
     + 1.2 重要变更：
         ````
          1) 合并官方jedis2.9.0版本；
          2) 优化集群风暴。
         ````
- **1. 版本号: 2.9.0-CC-3-SNAPSHOT**
     + 1.1 发布日期：2018-09-14
     + 1.2 重要变更：
         ````
          1) 支持standalone/sentinel/cluster在线密码修改。
         ````

### 三、**<span id="j3">cachecloud-client-crossroom-redis</span>**

><span id="j31">1.4.0版本</span>

- **3. 版本号: 1.4.0-RELEASE**
  
     + 3.1 发布日期：2020-02-12
     + 3.2 重要变更：
         ````
         1) 升级Jedis版本至3.1.0-CC-1-RELEASE，cachecloud-client-redis版本至2.0.0-RELEASE；
         ````
><span id="j32">1.3.2版本</span>
- **2. 版本号: 1.3.2-SNAPSHOT**
     + 2.1 发布日期：2019-12-09
     + 2.2 重要变更：
       
         ````
         1) 跨机房api支持命令zadd、lpop、llen、rpush、zrem、mzadds、mHgetAll、sadd、sismember、scard等。
         ````
><span id="j33">1.2版本</span>
- **1. 版本号: 1.2-SNAPSHOT**
     + 1.1 发布日期：2016-09-28
     + 1.2 重要变更：
         ````
         1) 修改客户端初始化方式。
         ````

### 四、**<span id="j4">cachecloud-client-lettuce</span>**

><span id="j41">1.0版本</span>
- **2. 版本号: 1.0-RELEASE**
     + 1.1 发布日期：2020-02-20
     + 1.2 重要变更：
         ````
         1) 升级原生lettuce至5.2.0.RELEASE版本。
         ````
- **1. 版本号: 1.0-SNAPSHOT**
     + 1.1 发布日期：2018-12-24
     + 1.2 重要变更：
         ````
         1) 增加CacheCloud初始化方式。
         ````

### 五、**<span id="j5">cachecloud-client-spectator</span>**

><span id="j51">1.0版本</span>
- **2. 版本号: 1.0.1-RELEASE**
     + 1.1 发布日期：2020-03-17
     + 1.2 重要变更：
         ````
         1) 优化命令调用逻辑，解决因为命令调用较少导致的漏报和延迟上报的问题。
         ````

- **1. 版本号: 1.0.0-RELEASE**
     + 1.1 发布日期：2020-02-12
     + 1.2 重要变更：
         ````
         1) 重构Jedis命令调用和异常上报逻辑。
         ````