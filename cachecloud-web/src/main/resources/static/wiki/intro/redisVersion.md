Redis开源代码地址：[https://github.com/antirez/redis](https://github.com/antirez/redis)，作者blog地址：http://antirez.com/
目前Redis各大版本lastest-release如下：

> **<span id="r5">6.0-rc</span>**

[redis6.0-changlog](https://raw.githubusercontent.com/antirez/redis/6.0/00-RELEASENOTES)

````
1.新的模块系统：提供新的模块API，模块可实现在RDB中存储数据，关联服务器事件，捕获重写命令，根据key阻塞客户端等功能 
2.重写过期淘汰：新的实现支持动态调整且能够更高效的回收过期键。
3.SSL加密：所有的通信管道支持SSL。
4.ACL支持: 控制用户只能访问某些命令以及对某些键的访问。
5.新协议RESP3: 新协议可以返回更多响应语义，新客户端可以从该协议中读取更多调用内容。
6.协助客户端缓存: Redis可以辅助优化客户端缓存实现。该功能仍处于开发阶段后续会有调整，更多内容: https://redis.io/topics/client-side-caching  
7.多线程IO: Redis可使用多线程处理网络数据的读写和协议解析，在不使用pipeline情况下，提升redis2倍的IO执行效率。
8.无盘复制: 从节点也支持无盘复制，开启配置后从节点可以从socket缓冲区加载RDB数据到内存。
9.基准测试：redis-benchmark 现在支持集群模式。
10.随机命令：优化SRANDMEMBER等随机命令的分布
11.redis-cli优化
12.重写Systemd支持
13.发布集群代理架构：https://github.com/artix75/redis-cluster-proxy
14.发布Disque(消息队列)模块：https://github.com/antirez/disque-module
````

> **<span id="r4">5.0.7</span>**

[redis5.0 changlog](https://raw.githubusercontent.com/antirez/redis/5.0/00-RELEASENOTES)

````
1.新的流数据类型(Stream data type) https://redis.io/topics/streams-intro
2.新的 Redis 模块 API：定时器、集群和字典 API(Timers, Cluster and Dictionary APIs)
3.RDB 增加 LFU 和 LRU 信息
4.集群管理器从 Ruby (redis-trib.rb) 移植到了redis-cli 中的 C 语言代码
5.新的有序集合(sorted set)命令：ZPOPMIN/MAX 和阻塞变体(blocking variants)
6.升级 Active defragmentation 至 v2 版本
7.增强 HyperLogLog 的实现
8.更好的内存统计报告
9.许多包含子命令的命令现在都有一个 HELP 子命令
10.客户端频繁连接和断开连接时，性能表现更好
11.许多错误修复和其他方面的改进
12.升级 Jemalloc 至 5.1 版本
13.引入 CLIENT UNBLOCK 和 CLIENT ID
14.新增 LOLWUT 命令 http://antirez.com/news/123
15.在不存在需要保持向后兼容性的地方，弃用 "slave" 术语
16.网络层中的差异优化
17.Lua 相关的改进
18.引入动态的 HZ(Dynamic HZ) 以平衡空闲 CPU 使用率和响应性
19.对 Redis 核心代码进行了重构并在许多方面进行了改进
````


> **<span id="r3">4.0.14</span>**

[redis4.0 changlog](https://raw.githubusercontent.com/antirez/redis/4.0/00-RELEASENOTES)

```
1.Redis模块系统 modules system
2.psync2全量/增量数据同步增强
3.Lazyfree异步机制
4.支持aof+rdb混合持久化 
5.内存分析及优化(memory help查看支持内存命令) 
6.内存碎片整理
```

> **<span id="r2">3.2.12</span>**

[redis3.2 changlog](https://raw.githubusercontent.com/antirez/redis/3.2/00-RELEASENOTES)

> **<span id="r1">3.0.7</span>**

[redis3.0 changlog](https://raw.githubusercontent.com/antirez/redis/3.0/00-RELEASENOTES)