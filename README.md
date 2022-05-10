### 目录
* [一、CacheCloud发展](#cc1)
* [二、CacheCloud是什么](#cc2)
* [三、CacheCloud功能架构](#cc3)
* [四、CacheCloud使用规模](#cc4)
* [五、CacheCloud资源部署VS云厂商](#cc5)  
* [六、ECS服务器试用版本](#cc6) 
* [七、FAQ快速接入](#cc7)
* [八、支持与帮助](#cc8)


<a name="cc1"/>

## 一、CacheCloud发展

[![Stargazers over time](https://starchart.cc/sohutv/cachecloud.svg)](https://starchart.cc/sohutv/cachecloud)

CacheCloud项目是从2016年以来一直在持续迭代开发,**如果你喜欢这个项目[CacheCloud试用版本](http://124.222.77.220:8080/admin/app/list),欢迎Star多多支持.**  

为了给大家带来更好的CacheCloud使用体验,希望您能在issue里提供宝贵建议：https://github.com/sohutv/cachecloud/issues/281

也感谢大家一直对项目支持：

[![Stargazers repo roster for @sohutv/cachecloud](https://reporoster.com/stars/sohutv/cachecloud)](https://github.com/sohutv/cachecloud/stargazers)
[![Forkers repo roster for @sohutv/cachecloud](https://reporoster.com/forks/sohutv/cachecloud)](https://github.com/sohutv/cachecloud/network/members)

<a name="cc2"/>

## 二、CacheCloud是什么

CacheCloud是一个Redis云管理平台：支持Redis多种架构(Standalone、Sentinel、Cluster)高效管理、有效降低大规模redis运维成本，提升资源管控能力和利用率。平台提供快速搭建/迁移，运维管理，弹性伸缩，统计监控，客户端整合接入等功能。

![cachecloud云平台](cachecloud-web/src/main/resources/static/img/readme/cachecloud.png)

<a name="cc3"/>

## 三、CacheCloud功能架构

+ Redis搭建：环境初始化、实例部署安装、类型架构支持；
+ 客户端接入：Java-SDK接入、客户端监控、其他语言接入；
+ 运维管理：宿主环境、资源管理、应用审计、应用运维、应用质量监控、应用拓扑诊断；
+ 弹性伸缩：资源评估、垂直伸缩、水平伸缩、外部接入；
+ 统计监控：指标采集、应用统计、节点统计、机器统计、监控报警、问题诊断；

<img src="cachecloud-web/src/main/resources/static/img/readme/CacheCloud功能架构.png" width="100%"/>

<a name="cc4"/>

## 四、CacheCloud使用规模

+ 400亿+ commands/day
+ 15T+ Memory Total
+ 300+ app Total / 3000+ Instances Total
+ 200+ Machines Total

<a name="cc5"/>

## 五、CacheCloud资源部署VS云厂商

+ Redis Sentinel部署成本:          
<img src="cachecloud-web/src/main/resources/static/img/readme/sentinel-cost.png" width="50%"/>

+ Redis 集群部署成本:
<img src="cachecloud-web/src/main/resources/static/img/readme/cluster-cost.png" width="50%"/>

<a name="cc6"/>

## 六、ECS服务器试用版本
+ CacheCloud后台地址：[地址](http://124.222.77.220:8080/admin/app/list)
+ 新用户注册成功可试用Redis集群
+ 开源版本试用截止时间：2023-02-01，如果大家有空闲公网资源可以贡献，请[联系我们](#cc8)

<a name="cc7"/> 

## 七、FAQ快速接入
+ [快速开始](cachecloud-web/src/main/resources/static/wiki/quickstart/index.md)
+ [客户端接入](cachecloud-web/src/main/resources/static/wiki/access/client.md)

<a name="cc8"/>

## 八、支持与帮助

+ QQ群:

   CacheCloud开发运维 已满
   
   CacheCloud开发运维2群:894022242
   
   CacheCloud开发运维3群:908821300
   
+ 微信群:发布Cachecloud最新动态，帮大家减轻工作负担。

<img src="http://photocdn.tv.sohu.com/img/cachecloud/weixin.jpg" width="40%"/>

<img src="cachecloud-web/src/main/resources/static/img/readme/subcribe.png" width="40%"/>

+ 微信：如果大家有公网资源可以联系我，会加入到开源版本服务资源部署试用，提高大家的用户体验。

<img src="cachecloud-web/src/main/resources/static/img/readme/wechat.png" width="40%"/>

如果你觉得CacheCloud对你有帮助，欢迎Star。
