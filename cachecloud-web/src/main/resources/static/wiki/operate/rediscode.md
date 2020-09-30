## CacheCloud支持密码方案

### 一. 密码设计

	密码粒度分布
		密码按照应用划分，相同应用的redis使用同一个密码。
		密码保持健壮性，防止被暴力破解，密码格式: `md5(key+{appId})`
		密码需要埋入SDK项目内部。

## 二. 接入方式		

- 改造Jedis: 优点:升级sdk支持在线切换，数据没有丢失。缺点：只支持Jedis，其他接入方式需要手动修改修改密码，密码生成过程固定。
````   
    a. standalone 模式: 改造JeidsPool.makeObject.
    b. cluster 模式: 集群内所有节点使用同一密码，防止failover失败。Jedis改造JedisClusterConnectionHandler.initializeSlotsCache 方法。
    c. sentinel模式:  主从所有节点必须使用同一个密码。改造JedisSentinelPool.initPool 
````
- 支持失败判定：NOAUTH Authentication required 异常后，中断连接下次重连。
````    
    a. RESTful请求携带密码。优点:不需要改造客户端，密码生成规则灵活 缺点：升级协同困难。
    b. RESTful数据中密码字段使用对称加密，防止直接传输明文。
````
- 客户端sdk加入判断是否设置密码功能。
````    
    a. 在线修改密码:
        redis客户端连接未使用密码时，修改密码后报错需要重连。
        redis客户端连接使用密码连接时，修改密码原有连接继续可用。
````
					
### 三、Cachecloud改造

- 确保CacheCloud管理功能支持密码 修改和校验。
- 代码点: Jeids,jedisPool , PipelineCluster , JedisCluster, redis-cli(shell)
	应用界面支持密码查询。
- cachecloud 增加redis节点(三种类型)在线密码修改及配置落地。
    - standalone 
    ````
    1. 在线添加密码：config set requirepass 123
    2. 有密码未认证测试：ping	=> NOAUTH Authentication required
    3. 有密码已认证测试: 验证密码 auth 123	 | ping => PONG
    4. 持久化配置后测试：配置落地 config rewrite | auth 123 | ping=>PONG
    ````
    **结论**：standalone模式能够在线支持密码修改，并持久化配置，客户端auth之后可以正常使用，测试通过.

	- sentinel哨兵
	````
	**观察设置密码过程中master 、slave、sentinel的表现**
	
	1. 在线对maste添加密码：config set requirepass 123
        1.1 slave表现为从master断开
        1.2 由于sentinel无法和设有密码的master通信，故sentinel认为该节点down掉，选举
            slave为master
	
	2.slave也设置密码：config set requirepass 123
		2.1 由于无法和master&slave通信，故认为都down掉了，sentinel会重复的进行选举
			
	3. sentinel在线修改配置：sentinel set ${mastername} auth-pass 123
	4. 强制刷新：sentinel flushconfig(redis版本>=3.0.2支持)
	5. slave此时并未完成master的密码配置，slave日志会有同步失败的日志
        5.1 为slave设置链接master的密码：config set masterauth 123, slave full resync from master(master会做一次全量复制)
        5.2 对master和slave执行config rewrite完成配置持久化
	````
	**结论**：	
	sentinel模式下的master&slave，能够在线支持密码修改，并持久化配置，客户端auth之后可以正常使用，测试通过
	另外：由于sentinel模式下，master<->slave可能相互转换，所以，master和slave均需要配置requirepass和masterauth
	
	- cluster集群
    ````
	1. 在线对maste添加密码：config set requirepass 123
        1.1 slave变现为从master断开
        1.2 redis cluster并未认为被设置密码的master down调，这点与sentinel表现不
            同，估计gossip协议忽略了redis的密码 
        1.3 对slave设置链接master密码：config set masterauth passwd123 , 
            slave full resync from master
        1.4 对slave设置密码：config set requirepass 123
        1.5 对master和slave进行持久化配置：config rewrite		
    ````
	**结论**：cluster模式能够在线支持密码修改，并ß持久化配置，客户端auth之后可以正常使用，测试通过,另外：由于cluster模式下，master<->slave可能相互转换，所以，master和slave均需要配置requirepass和masterauth.

### 四、cachecloud后台设置

- 创建应用,默认密码


- 修改密码
    <img src="http://i0.itc.cn/20170814/aac_a309d856_c48c_5526_6e61_478f0382b321_2.png" width="100%"/>

    - `更新`按钮:redis密码设置
    - `校验`按钮:用于校验redis实例密码是否都设置成功

