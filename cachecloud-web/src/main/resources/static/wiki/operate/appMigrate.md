## 应用迁移步骤

<a name="step1"/>

### 1. 进入CacheCloud后台

在CacheCloud后台的“应用运维”页面，选择要进行迁移的应用，点击“应用迁移”。 这种迁移方式不会更换应用的appId，通过主从节点的failover实现，是对客户端无感知迁移。

<img src="../../img/operate/appMigrate/app-migrate0.png" width="100%"/>

<a name="step2"/>

### 2. 使用迁移工具进行迁移

应用迁移整体包含八个主要步骤：

+ (1) 应用信息：查看源应用的实例IP，角色和Redis版本等信息，选择迁移的目标机房和迁移机器；

<img src="../../img/operate/appMigrate/app-migrate1.png" width="100%"/>

+ (2) 应用迁移计划：这一步主要查看节点的变更信息，新增实例（Slave）的IP和端口号，点击继续进行新老Salve节点的替换；

<img src="../../img/operate/appMigrate/app-migrate2.png" width="100%"/>

+ (3) 新老Salve节点替换: 替换完成之后查看最新实例信息，点击继续，进行主从切换；

<img src="../../img/operate/appMigrate/app-migrate3.png" width="100%"/>

+ (4) 主从Failover: 完成主从节点切换，点击继续，添加新的Slave；

<img src="../../img/operate/appMigrate/app-migrate4.png" width="100%"/>

+ (5) 添加Slave: 添加新的Slave；

<img src="../../img/operate/appMigrate/app-migrate5.png" width="100%"/>

+ (6) 新实例状态检测：检查新实例的连接状态，是否异常，点击继续，下线老的Slave；

<img src="../../img/operate/appMigrate/app-migrate6.png" width="100%"/>

+ (7) 下线Slave: 下线老的Slave；

<img src="../../img/operate/appMigrate/app-migrate7.png" width="100%"/>

+ (8) 迁移完成: 完成迁移。

<img src="../../img/operate/appMigrate/app-migrate8.png" width="100%"/>
