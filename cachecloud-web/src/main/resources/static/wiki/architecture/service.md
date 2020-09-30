## CacheCloud服务架构

<a name="cc7-1"/>

### 一、类型架构
- 1.standalone类型架构

<img src="http://i0.itc.cn/20160126/3084_a31f490d_64e3_3e43_6b99_18edbb45d8bc_1.png"/>

用于可穿透业务场景，如后端有DB存储，脱机影响不大的应用。

- 2.sentinel类型架构

<img src="http://i2.itc.cn/20160126/3084_0e82352a_0037_592e_89b3_29bee971bb71_1.png"/>

用于高可用需求场景,可用于高可用Cache,存储等场景。
内存/QPS受限于单机。

- 3.cluster类型架构

<img src="http://i0.itc.cn/20160126/3084_e7ab6ad2_359b_d617_c255_6d56b25e2cd9_1.png"/>

用于高可用需求场景,可用于大数据量高可用Cache/存储等场景。
内存/QPS不受限于单机，可受益于分布式集群高扩展性

<a name="cc7-2"/>

### 二、伸缩架构

- 1.垂直伸缩架构

<img src="http://i1.itc.cn/20160126/3084_dbf1c47c_d145_1145_cebb_92aa6bb7fef5_1.png"/>

通过统一调整每个实例可用内存量做到垂直拓展，受限于机器物理内存资源.
适用于所有redis类型应用

- 2.水平(sentinel)伸缩架构

<img src="http://i1.itc.cn/20160126/3084_93deddfb_fd46_21db_a1cf_46256ffd09e3_1.png"/>

通过在线切换主从关系和实例所属机器实现扩容。
适用于sentinel应用,在物理资源不够用/换掉故障机器时使用.

- 3.水平(cluster)伸缩架构

<img src="http://i1.itc.cn/20160126/3084_b974e379_96cb_edad_2e8a_ecf0821c3020_1.png"/>

通过动态加减实例并在线迁移数据实现伸缩性。
适用于redis-cluster应用,伸缩性最灵活但是速度最慢。