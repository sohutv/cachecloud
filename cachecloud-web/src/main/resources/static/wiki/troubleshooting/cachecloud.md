## 关于Cachecloud平台使用问题

##### 目录

1. [数据库时间问题](#Q1)
2. [平台本身需要的redis问题](#Q2)
3. [用户登录注册问题](#Q3)
4. [现有redis导入问题](#Q4)

<a name="Q1"/>
### 1.数据库时间问题
CacheCloud版本2 使用mysql-connector-java 8, 与数据库版本不匹配时，时区显示可能有问题。建议在配置jdbcUrl时指定时区serverTimezone=Asia/Shanghai，如下：

    cachecloud:
	  primary:
	    url: jdbc:mysql://129.0.0.1:3306/redis_open?useUnicode=true&characterEncoding=UTF8&autoReconnect=true&connectTimeout=3000&socketTimeout=10000&serverTimezone=Asia/Shanghai

<a name="Q2"/>
### 2.平台本身需要的redis问题
cachecloud-web工程配置文件中可为平台配置一个redis实例信息，用于存储任务流的日志。这一项不是必须的，可在项目启动后再创建配置。

	cachecloud:
	  redis: #配置cachecloud-web需要的redis，用户存储任务流log，可稍后配置
	    main:
	      host: ${ip}
	      port: ${port}
	      password: ${pwd}

<a name="Q3"/>
### 3.用户登录注册问题
CacheCloud版本2.2 将支持用户自主提交&修改密码功能。

<a name="Q4"/>
### 4.现有redis导入问题
CacheCloud版本1 支持简单的原生redis导入功能，但仅限制于导入redis的应用信息查询，节点监控等功能。

CacheCloud版本2.1 新提供“应用导入功能”，实现全方位的redis实例托管，可参考：["应用导入"](../../wiki/function/operation-import)。