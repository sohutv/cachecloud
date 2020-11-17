### 资源说明

由于github不能上传过大资源包，这里提供临时的下载地址：[资源下载](http://47.97.112.178/redis-ecs/)

### 1. 准备Redis二进制资源包： 

- 1) 资源下载: [下载Redis资源包](http://download.redis.io/releases)；
- 2) 在宿主/容器环境安装(make)redis资源包，对make编译完成资源重新打包redis-*.*.*-make.tar.gz；
- 3) 当前资源编译环境：redis3/4/5的make包编译要求操作系统环境：CentOS release 6.9 (Final) gcc version 4.8.5，redis6的make包编译要求操作系统环境：CentOS release 6.9 (Final) gcc version 7.3.0 (提示：如果使用的centos操作系统版本较高，可以对redis进行重新编译，当前make资源包不一定能够允许成功)
        
### 2. 文件说明：

- 1) cachecloud-web.war: 可直接启动cachecloud中台，详情参考： [快速接入](./cachecloud-web/src/main/resources/static/wiki/quickstart/index.md)
	
		nohup java -jar -Dspring.profiles.active=open cachecloud-web.war &

- 2) script脚本：
	- cachecloud-init.sh: 初始化宿主环境(物理机/虚拟机)系统变量，Redis机器相关资源等，执行：sh cachecloud-init.sh [username];
	- redis-install.sh: 安装单个redis版本, 执行: sh redis-install.sh [username] [redisTarGz]；
	- redisShake-install.sh: 安装redis-shake-v2.0.3，执行：sh redisShake-install.sh [username]；
	- ssh-keygen.sh: 创建公钥私钥，执行：sh ssh-keygen.sh [username]；

- 3) redis-*.*.*-make.tar.gz:
	- Redis各大版本的make资源包（已经编译好的包），如果需要到其他版本可自行添加。(提示：资源编译需要注意当前机器操作系统和gcc版本)

- 4）相关Redis工具包: 
	- redis-shake-v2.0.3.tar.gz：redis数据迁移工具包；
	- redis-full-check-1.4.4.tar.gz：redis数据校验包 ；
	- 如需升级版本可在官方下载最新 [Redis-shake](https://github.com/alibaba/RedisShake/releases) & [Redis-full-check](https://github.com/alibaba/RedisFullCheck/releases)  。
  
### 3. 部署流程:
   
  - 1) 使用root登录目标服务器;
  - 2) 将cachecloud-init.sh脚本拷贝到目标服务器当前用户目录下/opt ;
  - 3) 执行 sh cachecloud-init.sh [username]，默认username="cachecloud-open"；
  - 4) 设置密码;
  - 5) 一路安装直到成功；
  - 6) 通过cachecloud后台可以对当前机器进行Redis实例部署。
