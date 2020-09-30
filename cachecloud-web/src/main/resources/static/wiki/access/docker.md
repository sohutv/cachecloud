## 一、Redis docker部署步骤

### 1. 准备Redis二进制资源包： 

  - 1). 资源下载: [下载Redis资源包](http://download.redis.io/releases)
  - 2). 下载所需要用到Redis版本资源: 例如: [redis-3.0.7](http://download.redis.io/releases/redis-3.0.7.tar.gz)、[redis-3.2.12](http://download.redis.io/releases/redis-3.2.12.tar.gz)、[redis-4.0.14](http://download.redis.io/releases/redis-4.0.14.tar.gz)、[redis-5.0.7](http://download.redis.io/releases/redis-5.0.7.tar.gz)
  - 3). 在宿主/容器环境安装(make)redis资源包，对make编译完成资源重新打包redis-${大版本}.${小版本}.${增量版本}-make.tar.gz
        
### 2. 文件说明：

```
   1).cachecloud-init-docker.sh: 宿主环境(物理机/虚拟机)初始化系统环境变量;
   2).cachecloud-env.sh: cachecloud安装初始化脚本,如果是docker环境 默认会指定初始化;
   3).supervisord.conf: 进程管理工具，可指定容器默认启动的后台守护进程,用于管理sshd以及容器初始化的过程;
   4).authorized_keys: 默认为空，构建镜像会自动生成公钥写入到文件
   5).build.sh: 编译dockerfile创建镜像;
```

### 3. dockerfile说明

````
FROM centos:7.5.1804
RUN \
    /usr/bin/yum -y install wget make openssh-clients openssh-server;\
    mkdir -p /opt/cachecloud /home/cachecloud/.ssh
ADD redis-3.0.7-make.tar.gz /opt/cachecloud
ADD redis-3.2.12-make.tar.gz /opt/cachecloud
ADD redis-4.0.14-make.tar.gz /opt/cachecloud
ADD redis-5.0.6-make.tar.gz /opt/cachecloud
ADD cachecloud-env.sh /opt/cachecloud
ADD cachecloud-init-docker.sh /opt/cachecloud
ADD profile /etc
ADD supervisord.conf /etc
ADD public/authorized_keys /home/cachecloud/.ssh
RUN \
    source /etc/profile;\
    chmod +x /opt/cachecloud/*;\
    chown 600 /home/cachecloud/.ssh/authorized_keys;\
    sh /opt/cachecloud/cachecloud-init-docker.sh;\
    sh /opt/cachecloud/cachecloud-env.sh;\
    make -C /opt/cachecloud/redis-5.0.6 install;\
    ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key ;\
    ssh-keygen -t rsa -f /etc/ssh/ssh_host_ecdsa_key ;\
    ssh-keygen -t rsa -f /etc/ssh/ssh_host_ed25519_key ;\
    ssh-keygen -t rsa -f /etc/ssh/ssh_host_ecdsa_key ;\
    echo "StrictModes no" >> /etc/ssh/sshd_config;\
    yum -y install python-setuptools;\
    easy_install supervisor
EXPOSE 22
CMD /bin/bash
````
构建说明：

1).基于centos7.5操作系统构建镜像；

2).工具包安装: sshd工具/supervisord进程管理工具等；

3).Redis二进制资源包解压安装/基础目录/用户权限；

4).build.sh: 编译dockerfile创建镜像;

### 4. 构建镜像:  sh build.sh

```
    前提: 当前环境需要安装docker环境
    1). 查看镜像
        $ docker images
        
        REPOSITORY            TAG                 IMAGE ID            CREATED             SIZE
        redis                 202002111559        c1f0da6454ab        3 minutes ago       1.08GB

    2). 以交互模式启动一个容器
        $ docker run -i -t redis:202002111559 /bin/bash
    
        [root@d5e8923560bd cachecloud]# tree -L 1
        .
        |-- cachecloud-env.sh
        |-- cachecloud-init-docker.sh
        |-- conf
        |-- data
        |-- logs
        |-- redis           
        |-- redis-3.2.12
        |-- redis-4.0.14
        `-- redis-5.0.6 
```

## 二、Cachecloud docker部署步骤


## 三、迁移工具部署



