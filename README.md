##<a name="index"/>目录&nbsp;&nbsp;(具体细节[wiki文档](https://github.com/sohutv/cachecloud/wiki "Cachecloud Wiki")、[page](http://sohutv.github.io/cachecloud "Cachecloud page")、[视频教程](http://my.tv.sohu.com/pl/9100280/index.shtml "Cachecloud video")、QQ群：534429768)
* [一、CacheCloud是做什么的](#cc1)
* [二、CacheCloud提供哪些功能](#cc2)
* [三、CacheCloud解决什么问题](#cc3)
* [四、CacheCloud提供的价值](#cc4) 
* [五、CacheCloud在搜狐的规模](#cc5)
* [六、CacheCloud环境需求](#cc6)
* [七、CacheCloud快速开始](#cc7)
    * [1.初始化数据库](#cc7-1)
    * [2.CacheCloud项目配置](#cc7-2)
    * [3.启动cachecloud系统](#cc7-3)
    * [4.添加机器](#cc7-4)

<a name="cc1"/>
## 一、CacheCloud是做什么的
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CacheCloud提供一个Redis云管理平台：实现多种类型(**Redis Standalone**、**Redis Sentinel**、**Redis Cluster**)自动部署、解决Redis实例碎片化现象、提供完善统计、监控、运维功能、减少运维成本和误操作，提高机器的利用率，提供灵活的伸缩性，提供方便的接入客户端。



<img src="http://i3.itc.cn/20160125/3084_5393fb5d_7350_f249_9e37_c0d06d00b908_1.png">

<a name="cc2"/>
## 二、CacheCloud提供哪些功能
+  **监控统计：**	提供了机器、应用、实例下各个维度数据的监控和统计界面。
+  **一键开启：**	Redis Standalone、Redis Sentinel、Redis Cluster三种类型的应用，无需手动配置初始化。
+  **Failover：**	支持哨兵,集群的高可用模式。
+  **伸缩：**	    提供完善的垂直和水平在线伸缩功能。
+  **完善运维：**    提供自动运维和简化运维操作功能，避免纯手工运维出错。
+  **方便的客户端：**方便快捷的客户端接入。
+  **元数据管理：**    提供机器、应用、实例、用户信息管理。
+  **流程化：**      提供申请，运维，伸缩，修改等完善的处理流程 

<a name="cc3"/>
## 三、CacheCloud解决什么问题 ###
<img src="http://i3.itc.cn/20160125/3084_e6f2f51c_54cf_4081_450f_c69998e74d01_1.png">
####1.部署成本
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Redis多机(Redis-Sentinel, Redis-Cluster)部署和配置相对比较复杂，较容易出错。

	例如：100个redis数据节点组成的redis-cluster集群，如果单纯手工安装，既耗时又容易出错。
####2.实例碎片化
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;作为一个Redis管理员(可以看做redis DBA)需要帮助开发者管理上百个Redis-Cluster集群，分布在数百台机器上，人工维护成本很高，需要自动化运维工具。
####3. 监控、统计和管理不完善
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;一些开源的Redis监控和管理工具，例如：RedisLive(Python)、Redis Commander(Node.js)，Redmon(Ruby)无论从功能的全面性(例如配置管理，支持Redis-Cluster等等)、扩展性很难满足需求。
####4. 运维成本
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Redis的使用者需要维护各自的Redis，但是用户可能更加善于使用Redis实现各种功能，但是没有足够的精力和经验维护Redis。
Redis的开发人员如同使用Mysql一样，不需要运维Mysql服务器，同样使用Redis服务，不要自己运维Redis，Redis由一些在Redis运维方面更有经验的人来维护（保证高可用，高扩展性），使得开发者更加关注于Redis使用本身。
####5. 伸缩性
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本产品支持Redis最新的Redis-Sentinel、Redis-Cluster集群机构，既满足Redis高可用性、又能满足Redis的可扩展性，具有较强的容量和性能伸缩能力。
####6. 经济成本
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机器利用率低，各个项目组的Redis较为分散的部署在各自服务器上，造成了大量闲置资源没有有效利用。 
####7. 版本不统一 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;各个项目的Redis使用各种不同的版本，不便于管理和交互。

<a name="cc4"/>
## 四、CacheCloud提供的价值 ###
+  规模化自动运维：	降低运维成本，降低人为操作出错率。
+  自由伸缩：	    提供灵活的伸缩性，应用扩容/收缩成本降低，机器资源得到重复利用。
+  团队提升，开源贡献:提升云产品开发设计经验,自己作为开发者和使用者，Eating your own dog food。

<a name="cc5"/>
## 五、CacheCloud在搜狐的规模 ###
+  每天100+亿次命令调用
+  2T+的内存空间
+  800+个Redis实例
+  100+台机器

<a name="cc6"/>
## 六、CacheCloud环境需求 ###
+  Java 7
+  Maven 3
+  MySQL
+  Redis 3

<a name="cc7"/>
## 七、CacheCloud快速开始 ###

<a name="cc7-1"/>
####1、初始化数据库
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;导入项目中cachecloud.sql初始化库表结构。默认插入admin超级管理员

<a name="cc7-2"/>
####2、CacheCloud项目配置
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;使用了maven作为项目构建的工具，提供了 local.properties和online.properties两套配置作为测试、线上的隔离。
属性配置说明：
	

| 属性名 | 说明  | 示例 |
| :-------------------------- |:----------------------------- | :----------------------------------------:|
| cachecloud.db.url      | mysql驱动url     | jdbc:mysql://127.0.0.1:3306/cache-cloud |
| cachecloud.db.user     | mysql用户名      |  admin |
| cachecloud.db.password | mysql密码        |  admin | 
| cachecloud.machine.username | 服务器用户名,用于ssh        | ${your machine username} | 
| cachecloud.machine.password | 服务器密码,用于ssh        |  ${your machine password} | 
| web.port | spring-boot内嵌tomcat启动端口        | 8080  | 		
		
		
		
<a name="cc7-3"/>
####3、启动cachecloud系统

#####(1). 本地启动:
+  在cachecloud根目录下运行
```Java        
mvn clean compile install -Plocal
```
+  在cachecloud-web模块下运行
```Java        
mvn spring-boot:run
```

#####(2). 生产环境
+  在cachecloud根目录下运行
```Java        
mvn clean compile install -Ponline
```
+  拷贝war包(cachecloud-open-web/target/cachecloud-open-web-1.0-SNAPSHOT.war)到/opt/cachecloud-web下
+  拷贝配置文件(cachecloud-open-web/src/main/resources/cachecloud-web.conf)到/opt/cachecloud-web下，并改名为cachecloud-open-web-1.0-SNAPSHOT.conf（spring-boot要求，否则配置不生效）
+  启动
```Java
sudo ln -s /opt/cachecloud-web/cachecloud-web-1.0-SNAPSHOT.war /etc/init.d/cachecloud-web
/etc/init.d/cachecloud-web start 
```        
        
        
#####(3). 登录确认

#####(a) 访问：http://127.0.0.1:9999
(9999是tomcat的端口号，具体要参考第三节中的online.properties和local.properties中的web.port)
#####(b) 如果访问正常，请使用用户名:admin、密码:admin访问系统，跳转到应用列表下：
<img src="http://i1.itc.cn/20160304/3084_b7374fe0_1136_79a9_6de7_699599da7345_1.png">

<a name="cc7-4"/>
####4、添加机器
#####(1). 运行脚本:
cachecloud项目中的cachecloud-init.sh脚本是用来初始化服务器的cachecloud环境，主要工作如下：

+  **(a). 创建cachecloud项目用户**：因为cachecloud项目的部分功能(redis启动、服务器监控)是通过ssh完成的，所以这里的用户和密码要和项目中的相对应，具体详见第三节。

+  **(b). 创建cachecloud项目的工作目录、数据目录、配置目录、日志目录、redis安装目录、临时目录等等。**(/opt/cachecloud/data、/opt/cachecloud/conf、/opt/cachecloud/logs、/opt/cachecloud/redis、/tmp/cachecloud)

+  **(c). 安装最新的release版本的Redis**

#####(2). 脚本执行
+  (a). 使用root登录目标服务器。
+  (b). 将cachecloud-init.sh脚本拷贝到目标服务器当前用户目录下。
+  (c). 执行 sh cachecloud-init.sh ${yourusername}
+  (d). 两次确认密码
+  (e). 一路安装直到成功。

#####(3). 建议和警告 
+  (a). 请在root用户下执行初始化脚本，因为初始化脚本涉及到了用户的创建等较高的权限。
+  (b). 出于安全的考虑，所选的机器最好不要有外网IP地址。
+  (c). 用户名和密码最好不要用cachecloud, 密码尽可能复杂。
+  (d). 机器的ssh端口最好是22。
+  (e). 请确保/opt/有足够的硬盘空间，因为/opt/cachecloud/data要存储RDB和AOF的持久化文件，如果硬盘过小，会造成持久化失败。（如果硬盘确实很小，建议建立一个软链接到/opt/cachecloud/data,且保证软链接的目录也是username用户，一定要保证/opt/cachecloud的目录结构）
+  (f). 脚本中目前使用的是redis-3.0.6，如有需要请自行替换，建议使用3.0 release以后的版本。
    
#####(4). 添加机器 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;进入管理员界面(http://ip:port/manage/total/list)，进入机器管理，点击添加机器，添加机器信息是开通应用的基础。
<img src="http://i2.itc.cn/20160127/3084_c9d9d17b_4e86_a17f_5442_cf9cc08c68f3_1.jpg"/>


 
