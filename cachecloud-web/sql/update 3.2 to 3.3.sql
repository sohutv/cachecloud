ALTER TABLE instance_config ADD value_type TINYINT(4) DEFAULT 0 NOT NULL COMMENT '取值类型（0：默认值 config_value；1：从主节点拷贝）';

ALTER TABLE app_client_statistic_gather ADD used_disk BIGINT(20) DEFAULT 0 NULL COMMENT '磁盘占用byte';
ALTER TABLE app_client_statistic_gather ADD server_cmd_count bigint(20) DEFAULT 0 NOT NULL COMMENT 'server端统计的命令调用次数';

ALTER TABLE instance_statistics ADD used_disk bigint(255) DEFAULT 0 NOT NULL COMMENT '已使用磁盘，单位byte';

ALTER TABLE app_minute_statistics ADD used_disk bigint(20) DEFAULT 0 NOT NULL COMMENT '磁盘占用（字节）';

ALTER TABLE app_hour_statistics ADD used_disk bigint(20) DEFAULT 0 NOT NULL COMMENT '磁盘占用（字节）';

ALTER TABLE machine_statistics ADD disk_total varchar(120) NULL COMMENT '机器分配磁盘，单位MB';
ALTER TABLE machine_statistics ADD disk_available varchar(120) NULL COMMENT '机器空闲磁盘，单位MB';
ALTER TABLE machine_statistics ADD disk_usage_ratio varchar(15) NULL COMMENT '机器磁盘使用率，百分比（无需乘100）';

ALTER TABLE app_daily ADD avg_used_disk BIGINT(20) NOT NULL COMMENT '平均磁盘使用量';
ALTER TABLE app_daily ADD max_used_disk BIGINT(20) NOT NULL COMMENT '最大磁盘使用量';

ALTER TABLE app_desc ADD persistence_type TINYINT(4) DEFAULT 0 NOT NULL COMMENT '持久化类型（0：常规；1：主aof自动刷盘；从常规；2：主关闭aof，从常规）';

ALTER TABLE app_user ADD biz_id BIGINT(20) DEFAULT NULL COMMENT '所属业务组id（app_biz）';

ALTER TABLE instance_alert_configs ADD app_type TINYINT(4) DEFAULT 0 NOT NULL COMMENT '应用类型(0：redis;)';
ALTER TABLE instance_alert_configs DROP KEY uniq_index;
ALTER TABLE instance_alert_configs ADD CONSTRAINT uniq_index UNIQUE KEY (`type`,instance_id,alert_config,compare_type,app_type);

--
-- Table structure for table `app_biz`
--

CREATE TABLE `app_biz` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(64) NOT NULL COMMENT '业务组名称',
    `biz_desc` varchar(255) NOT NULL COMMENT '业务组描述',
    PRIMARY KEY (`id`),
    UNIQUE KEY `bidx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 COMMENT='业务组表';

--
-- Table structure for table `app_capacity_monitor`
--
CREATE TABLE `app_capacity_monitor` (
                                        `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                                        `app_id` bigint(20) NOT NULL COMMENT '应用id',
                                        `sharding_master_num` int(10) NOT NULL DEFAULT '0' COMMENT '主分片数',
                                        `mem` bigint(20) NOT NULL COMMENT '应用初始内存(字节)',
                                        `cur_mem` bigint(20) NOT NULL COMMENT '应用当前内存(字节)',
                                        `mem_used` bigint(20) NOT NULL DEFAULT '0' COMMENT '应用已使用内存(字节)',
                                        `mem_used_history` bigint(20) DEFAULT '0' COMMENT '应用已使用内存（历史最大值）',
                                        `sharding_mem` bigint(20) NOT NULL COMMENT '应用分片初始内存(字节)',
                                        `cur_sharding_mem` bigint(20) NOT NULL COMMENT '应用分片当前内存(字节)',
                                        `sharding_mem_used` bigint(20) NOT NULL DEFAULT '0' COMMENT '分片已使用内存（最大值）',
                                        `expand_mem_percent` tinyint(4) NOT NULL COMMENT '应用扩容内存使用百分比',
                                        `expand_ratio` tinyint(4) NOT NULL COMMENT '扩容比率',
                                        `expand_ratio_total` int(10) NOT NULL COMMENT '当日最大扩容比率（超出不可扩容）',
                                        `is_expand` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否可扩容：0否；1是',
                                        `is_reduce` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否可缩容: 0否，1是',
                                        `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                        `expand_time` datetime DEFAULT NULL COMMENT '上次扩容时间',
                                        `schedule_status` tinyint(4) DEFAULT '0' COMMENT '计划状态：0：无意义；1：待缩容；2：待扩容',
                                        `schedule_time` date DEFAULT NULL COMMENT '计划处理时间',
                                        `reduce_ratio_min` tinyint(4) NOT NULL DEFAULT '40' COMMENT '缩容内存使用率最小值',
                                        `reduce_ratio_max` tinyint(4) NOT NULL DEFAULT '60' COMMENT '缩容内存使用率最大值',
                                        `expand_count` int(10) NOT NULL DEFAULT '0' COMMENT '当日自动扩容次数',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=596 DEFAULT CHARSET=utf8 COMMENT='app应用容量监控';




------------------------------- add for redis 6.2 and 7.2----------------------------------------------------------
# below two insert sql are related, note the ids related, system_resource.id and instance_config.version_id
# 注意两个表之间的id相关联，system_resource表id字段 和 instance_config表version_id字段

BEGIN;
INSERT INTO `system_resource` VALUES
      (51, 'redis-6.2.4', 'redis-6.2.4 资源包', 3, '2023-02-09 10:24:16', '/redis', 'http://download.redis.io/releases/redis-6.2.4.tar.gz', 1, 1, 'admin', 3507, NULL, 0),
      (62, 'redis-7.2.4', 'redis 7.2.4 资源包', 3, '2024-01-18 15:26:02', '/redis', 'http://download.redis.io/releases/redis-7.2.4.tar.gz', 1, 1, 'admin', 11687, NULL, 700);
COMMIT;

BEGIN;
INSERT INTO instance_config (id, config_key, config_value, info, update_time, `type`, status, version_id, refresh, value_type) VALUES
    (868, 'cluster-enabled', 'yes', '是否开启集群模式', '2021-06-09 10:12:50', 2, 1, 51, 0, 0)
    ,(869, 'cluster-node-timeout', '15000', '集群节点超时时间,默认15秒', '2021-06-09 10:12:50', 2, 1, 51, 0, 1)
    ,(870, 'cluster-migration-barrier', '3', '从节点自动迁移至少需要的可用节点数,默认1个', '2021-06-09 10:12:50', 2, 1, 51, 0, 0)
    ,(871, 'cluster-config-file', 'nodes-%d.conf', '集群配置文件名称,格式:nodes-{port}.conf', '2021-06-09 10:12:50', 2, 1, 51, 0, 0)
    ,(872, 'cluster-require-full-coverage', 'no', '节点部分失败期间,其他节点是否继续工作', '2021-06-09 10:12:50', 2, 1, 51, 0, 1)
    ,(873, 'port', '%d', 'sentinel实例端口', '2021-06-09 10:12:50', 5, 1, 51, 0, 0)
    ,(874, 'dir', '%s', '工作目录', '2021-06-09 10:12:50', 5, 1, 51, 0, 0)
    ,(875, 'sentinel monitor', '%s %s %d 1', 'master名称定义和最少参与监控的sentinel数,格式:masterName ip port num', '2021-06-09 10:12:50', 5, 1, 51, 0, 0)
    ,(876, 'sentinel down-after-milliseconds', '%s 20000', 'Sentinel判定服务器断线的毫秒数', '2021-06-09 10:12:50', 5, 1, 51, 0, 0)
    ,(877, 'sentinel failover-timeout', '%s 180000', '故障迁移超时时间,默认:3分钟', '2021-06-09 10:12:50', 5, 1, 51, 0, 0)
    ,(878, 'sentinel parallel-syncs', '%s 1', '在执行故障转移时,最多有多少个从服务器同时对新的主服务器进行同步,默认:1', '2021-06-09 10:12:50', 5, 1, 51, 0, 0)
    ,(879, 'daemonize', 'no', '是否守护进程', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(880, 'tcp-backlog', '511', 'TCP连接完成队列', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(881, 'timeout', '0', '客户端闲置多少秒后关闭连接,默认为0,永不关闭', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(882, 'tcp-keepalive', '60', '检测客户端是否健康周期,默认关闭', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(883, 'loglevel', 'notice', '日志级别', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(884, 'databases', '16', '可用的数据库数，默认值为16个,默认数据库为0', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(885, 'dir', '%s', 'redis工作目录', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(886, 'stop-writes-on-bgsave-error', 'no', 'bgsave出错了不停写', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(887, 'repl-timeout', '60', 'master批量数据传输时间或者ping回复时间间隔,默认:60秒', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(888, 'repl-disable-tcp-nodelay', 'no', '是否禁用socket的NO_DELAY,默认关闭，影响主从延迟', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(889, 'repl-backlog-size', '10M', '复制缓存区,默认:1mb,配置为:10Mb', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(890, 'repl-backlog-ttl', '7200', 'master在没有从节点的情况下释放BACKLOG的时间多久:默认:3600,配置为:7200', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(891, 'lua-time-limit', '5000', 'Lua脚本最长的执行时间，单位为毫秒', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(892, 'slowlog-log-slower-than', '10000', '慢查询被记录的阀值,默认10毫秒', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(893, 'slowlog-max-len', '128', '最多记录慢查询的条数', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(894, 'hash-max-ziplist-entries', '512', 'hash数据结构优化参数', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(895, 'hash-max-ziplist-value', '64', 'hash数据结构优化参数', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(896, 'list-max-ziplist-entries', '512', 'list数据结构优化参数', '2021-06-09 10:12:50', 6, 0, 51, 0, 1)
    ,(897, 'list-max-ziplist-value', '64', 'list数据结构优化参数', '2021-06-09 10:12:50', 6, 0, 51, 0, 1)
    ,(898, 'set-max-intset-entries', '512', 'set数据结构优化参数', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(899, 'zset-max-ziplist-entries', '128', 'zset数据结构优化参数', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(900, 'zset-max-ziplist-value', '64', 'zset数据结构优化参数', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(901, 'activerehashing', 'yes', '是否激活重置哈希,默认:yes', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(902, 'client-output-buffer-limit normal', '0 0 0', '客户端输出缓冲区限制(客户端)', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(903, 'client-output-buffer-limit pubsub', '32mb 8mb 60', '客户端输出缓冲区限制(发布订阅)', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(904, 'hz', '10', '执行后台task数量,默认:10', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(905, 'port', '%d', '端口', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(906, 'maxmemory', '%dmb', '当前实例最大可用内存', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(907, 'maxmemory-policy', 'volatile-lfu', '内存不够时,淘汰策略,默认:volatile-lfu', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(908, 'appendonly', 'yes', '开启append only持久化模式', '2021-06-09 10:12:50', 6, 1, 51, 0, 1)
    ,(909, 'appendfsync', 'everysec', '默认:aof每秒同步一次', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(910, 'appendfilename', 'appendonly-%d.aof', 'aof文件名称,默认:appendonly-{port}.aof', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(911, 'dbfilename', 'dump-%d.rdb', 'RDB文件默认名称,默认dump-{port}.rdb', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(912, 'aof-rewrite-incremental-fsync', 'yes', 'aof rewrite过程中,是否采取增量文件同步策略,默认:yes', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(913, 'no-appendfsync-on-rewrite', 'yes', '是否在后台aof文件rewrite期间调用fsync,默认调用,修改为yes,防止可能fsync阻塞,但可能丢失rewrite期间的数据', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(914, 'auto-aof-rewrite-min-size', '64m', '触发rewrite的aof文件最小阀值,默认64m', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(915, 'auto-aof-rewrite-percentage', '%d', 'Redis重写aof文件的比例条件,默认从100开始,统一机器下不同实例按4%递减', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(916, 'maxclients', '10000', '客户端最大连接数', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(917, 'protected-mode', 'yes', '开启保护模式', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(918, 'bind', '0.0.0.0', '默认客户端都可连接', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(919, 'list-max-ziplist-size', '-2', '8Kb对象以内采用ziplist', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(920, 'list-compress-depth', '0', '压缩方式，0:不压缩', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(921, 'always-show-logo', 'yes', 'redis启动是否显示logo', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(922, 'lazyfree-lazy-eviction', 'yes', '在被动淘汰键时，是否采用lazy free机制,默认:no', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(923, 'lazyfree-lazy-expire', 'yes', 'TTL的键过期是否采用lazyfree机制 默认值:no', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(924, 'lazyfree-lazy-server-del', 'yes', '隐式的DEL键(rename)是否采用lazyfree机制 默认值:no', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(925, 'aof-use-rdb-preamble', 'yes', '是否开启混合持久化,默认值 no 不开启', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(926, 'protected-mode', 'no', '关闭sentinel保护模式', '2021-06-09 10:12:50', 5, 1, 51, 0, 0)
    ,(927, 'activedefrag', 'no', '碎片整理开启', '2022-06-07 10:16:26', 6, 1, 51, 0, 1)
    ,(928, 'active-defrag-threshold-lower', '10', '碎片率达到百分之多少开启整理', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(929, 'active-defrag-threshold-upper', '100', '碎片率小余多少百分比开启整理', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(930, 'active-defrag-ignore-bytes', '300mb', '内存碎片达到多少兆开启碎片', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(931, 'active-defrag-cycle-min', '10', '碎片整理最小cpu百分比', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(932, 'active-defrag-cycle-max', '30', '碎片整理最大cpu百分比', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(933, 'active-defrag-max-scan-fields', '1000', '内存碎片处理set/hash/zset/list 中的最大数量的项', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(934, 'replica-serve-stale-data', 'yes', '从节点与master断连或复制命令响应：yes 继续响应 no:相关命令返回异常信息', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(935, 'cluster-replica-validity-factor', '10', '从节点延迟有效性判断因子,默认10秒', '2021-06-09 10:12:50', 2, 1, 51, 0, 0)
    ,(936, 'replica-priority', '100', '从节点的优先级,影响sentinel/cluster晋升master操作,0永远不晋升', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(937, 'replica-read-only', 'yes', '从节点是否只读: yes 只读', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(938, 'replica-lazy-flush', 'yes', '从节点发起全量复制,是否采用flushall async清理老数据 默认值 no', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(939, 'client-output-buffer-limit replica', '512mb 256mb 60', '客户端输出缓冲区限制', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(940, 'replica-ignore-maxmemory', 'yes', '从节点是否开启最大内存，避免一些过大缓冲区导致oom', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(941, 'stream-node-max-bytes', '4096', 'stream数据结构优化参数', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(942, 'stream-node-max-entries', '100', 'stream数据结构优化参数', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(943, 'dynamic-hz', 'yes', '自适应平衡空闲CPU的使用率和响应', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(944, 'rdb-save-incremental-fsync', 'yes', 'rdb同步刷盘是否采用增量fsync，每32MB执行一次fsync', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(945, 'repl-ping-replica-period', '10', '指定从节点定期ping master的周期,默认:10秒', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(946, 'latency-monitor-threshold', '30', '延迟事件阀值，单位ms', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(947, 'repl-diskless-load', 'on-empty-db', '完全安全的情况下才使用无磁盘加载', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(948, 'tracking-table-max-keys', '1000000', '无效表键的最大填充数量', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(949, 'rdb-del-sync-files', 'yes', '默认:no 不删除rdb文件,删除实例中复制使用的不持久的RDB文件', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(950, 'lazyfree-lazy-user-del', 'yes', '默认值no,设置del操作命令同unlink一致', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(951, 'io-threads', '1', '读写io线程数量', '2021-06-09 15:22:48', 6, 1, 51, 0, 0)
    ,(952, 'io-threads-do-reads', 'no', '开启io读线程', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(953, 'jemalloc-bg-thread', 'yes', '启用Jemalloc后台线程清理', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(954, 'server_cpulist', '0-7:2', '设置redis服务器/io线程的cpu使用权重', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(955, 'bio_cpulist', '1,3', '设置bio线程的cpu使用权重', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(956, 'aof_rewrite_cpulist', '8-11', '设置aof重写子进程的cpu使用权重', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(957, 'bgsave_cpulist', '1,10-11', '设置bgsave子进程的cpu使用权重', '2021-06-09 10:12:50', 6, 1, 51, 0, 0)
    ,(959, 'save', '', '关闭同步操作', '2021-07-01 10:31:11', 6, 1, 51, 0, 0)
    ,(1579, 'enable-module-command', 'yes', '是否支持module命令', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1582, 'save', '', '关闭同步操作', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1584, 'cluster-enabled', 'yes', '是否开启集群模式', '2024-01-17 10:23:00', 2, 1, 62, 0, 0)
    ,(1585, 'cluster-node-timeout', '15000', '集群节点超时时间,默认15秒', '2024-01-17 10:23:00', 2, 1, 62, 0, 1)
    ,(1586, 'cluster-migration-barrier', '3', '从节点自动迁移至少需要的可用节点数,默认1个', '2024-01-17 10:23:00', 2, 1, 62, 0, 0)
    ,(1587, 'cluster-config-file', 'nodes-%d.conf', '集群配置文件名称,格式:nodes-{port}.conf', '2024-01-17 10:23:00', 2, 1, 62, 0, 0)
    ,(1588, 'cluster-require-full-coverage', 'no', '节点部分失败期间,其他节点是否继续工作', '2024-01-17 10:23:00', 2, 1, 62, 0, 1)
    ,(1589, 'port', '%d', 'sentinel实例端口', '2024-01-17 10:23:00', 5, 1, 62, 0, 0)
    ,(1590, 'dir', '%s', '工作目录', '2024-01-17 10:23:00', 5, 1, 62, 0, 0)
    ,(1591, 'sentinel monitor', '%s %s %d 1', 'master名称定义和最少参与监控的sentinel数,格式:masterName ip port num', '2024-01-17 10:23:00', 5, 1, 62, 0, 0)
    ,(1592, 'sentinel down-after-milliseconds', '%s 20000', 'Sentinel判定服务器断线的毫秒数', '2024-01-17 10:23:00', 5, 1, 62, 0, 0)
    ,(1593, 'sentinel failover-timeout', '%s 180000', '故障迁移超时时间,默认:3分钟', '2024-01-17 10:23:00', 5, 1, 62, 0, 0)
    ,(1594, 'sentinel parallel-syncs', '%s 1', '在执行故障转移时,最多有多少个从服务器同时对新的主服务器进行同步,默认:1', '2024-01-17 10:23:00', 5, 1, 62, 0, 0)
    ,(1595, 'daemonize', 'no', '是否守护进程', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1596, 'tcp-backlog', '511', 'TCP连接完成队列', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1597, 'timeout', '0', '客户端闲置多少秒后关闭连接,默认为0,永不关闭', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1598, 'tcp-keepalive', '60', '检测客户端是否健康周期,默认关闭', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1599, 'loglevel', 'notice', '日志级别', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1600, 'databases', '16', '可用的数据库数，默认值为16个,默认数据库为0', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1601, 'dir', '%s', 'redis工作目录', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1602, 'stop-writes-on-bgsave-error', 'no', 'bgsave出错了不停写', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1603, 'repl-timeout', '60', 'master批量数据传输时间或者ping回复时间间隔,默认:60秒', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1604, 'repl-disable-tcp-nodelay', 'no', '是否禁用socket的NO_DELAY,默认关闭，影响主从延迟', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1605, 'repl-backlog-size', '10M', '复制缓存区,默认:1mb,配置为:10Mb', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1606, 'repl-backlog-ttl', '7200', 'master在没有从节点的情况下释放BACKLOG的时间多久:默认:3600,配置为:7200', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1607, 'lua-time-limit', '5000', 'Lua脚本最长的执行时间，单位为毫秒', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1608, 'slowlog-log-slower-than', '10000', '慢查询被记录的阀值,默认10毫秒', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1609, 'slowlog-max-len', '128', '最多记录慢查询的条数', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1610, 'hash-max-ziplist-entries', '512', 'hash数据结构优化参数', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1611, 'hash-max-ziplist-value', '64', 'hash数据结构优化参数', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1612, 'list-max-ziplist-entries', '512', 'list数据结构优化参数', '2024-01-17 10:23:00', 6, 0, 62, 0, 1)
    ,(1613, 'list-max-ziplist-value', '64', 'list数据结构优化参数', '2024-01-17 10:23:00', 6, 0, 62, 0, 1)
    ,(1614, 'set-max-intset-entries', '512', 'set数据结构优化参数', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1615, 'zset-max-ziplist-entries', '128', 'zset数据结构优化参数', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1616, 'zset-max-ziplist-value', '64', 'zset数据结构优化参数', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1617, 'activerehashing', 'yes', '是否激活重置哈希,默认:yes', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1618, 'client-output-buffer-limit normal', '0 0 0', '客户端输出缓冲区限制(客户端)', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1619, 'client-output-buffer-limit pubsub', '32mb 8mb 60', '客户端输出缓冲区限制(发布订阅)', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1620, 'hz', '10', '执行后台task数量,默认:10', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1621, 'port', '%d', '端口', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1622, 'maxmemory', '%dmb', '当前实例最大可用内存', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1623, 'maxmemory-policy', 'volatile-lfu', '内存不够时,淘汰策略,默认:volatile-lfu', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1624, 'appendonly', 'yes', '开启append only持久化模式', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1625, 'appendfsync', 'everysec', '默认:aof每秒同步一次', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1626, 'appendfilename', 'appendonly-%d.aof', 'aof文件名称,默认:appendonly-{port}.aof', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1627, 'dbfilename', 'dump-%d.rdb', 'RDB文件默认名称,默认dump-{port}.rdb', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1628, 'aof-rewrite-incremental-fsync', 'yes', 'aof rewrite过程中,是否采取增量文件同步策略,默认:yes', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1629, 'no-appendfsync-on-rewrite', 'yes', '是否在后台aof文件rewrite期间调用fsync,默认调用,修改为yes,防止可能fsync阻塞,但可能丢失rewrite期间的数据', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1630, 'auto-aof-rewrite-min-size', '64m', '触发rewrite的aof文件最小阀值,默认64m', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1631, 'auto-aof-rewrite-percentage', '%d', 'Redis重写aof文件的比例条件,默认从100开始,统一机器下不同实例按4%递减', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1632, 'maxclients', '10000', '客户端最大连接数', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1633, 'protected-mode', 'no', '开启保护模式', '2024-06-05 11:55:57', 6, 1, 62, 0, 0)
    ,(1634, 'bind', '0.0.0.0 -::*', '默认客户端都可连接', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1635, 'list-max-ziplist-size', '-2', '8Kb对象以内采用ziplist', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1636, 'list-compress-depth', '0', '压缩方式，0:不压缩', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1637, 'always-show-logo', 'yes', 'redis启动是否显示logo', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1638, 'lazyfree-lazy-eviction', 'yes', '在被动淘汰键时，是否采用lazy free机制,默认:no', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1639, 'lazyfree-lazy-expire', 'yes', 'TTL的键过期是否采用lazyfree机制 默认值:no', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1640, 'lazyfree-lazy-server-del', 'yes', '隐式的DEL键(rename)是否采用lazyfree机制 默认值:no', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1641, 'aof-use-rdb-preamble', 'yes', '是否开启混合持久化,默认值 no 不开启', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1642, 'protected-mode', 'no', '关闭sentinel保护模式', '2024-01-17 10:23:00', 5, 1, 62, 0, 0)
    ,(1643, 'activedefrag', 'no', '碎片整理开启', '2024-01-17 10:23:00', 6, 1, 62, 0, 1)
    ,(1644, 'active-defrag-threshold-lower', '10', '碎片率达到百分之多少开启整理', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1645, 'active-defrag-threshold-upper', '100', '碎片率小余多少百分比开启整理', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1646, 'active-defrag-ignore-bytes', '300mb', '内存碎片达到多少兆开启碎片', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1647, 'active-defrag-cycle-min', '10', '碎片整理最小cpu百分比', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1648, 'active-defrag-cycle-max', '30', '碎片整理最大cpu百分比', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1649, 'active-defrag-max-scan-fields', '1000', '内存碎片处理set/hash/zset/list 中的最大数量的项', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1650, 'replica-serve-stale-data', 'yes', '从节点与master断连或复制命令响应：yes 继续响应 no:相关命令返回异常信息', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1651, 'cluster-replica-validity-factor', '10', '从节点延迟有效性判断因子,默认10秒', '2024-01-17 10:23:00', 2, 1, 62, 0, 0)
    ,(1652, 'replica-priority', '100', '从节点的优先级,影响sentinel/cluster晋升master操作,0永远不晋升', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1653, 'replica-read-only', 'yes', '从节点是否只读: yes 只读', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1654, 'replica-lazy-flush', 'yes', '从节点发起全量复制,是否采用flushall async清理老数据 默认值 no', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1655, 'client-output-buffer-limit replica', '512mb 256mb 60', '客户端输出缓冲区限制', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1656, 'replica-ignore-maxmemory', 'yes', '从节点是否开启最大内存，避免一些过大缓冲区导致oom', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1657, 'stream-node-max-bytes', '4096', 'stream数据结构优化参数', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1658, 'stream-node-max-entries', '100', 'stream数据结构优化参数', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1659, 'dynamic-hz', 'yes', '自适应平衡空闲CPU的使用率和响应', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1660, 'rdb-save-incremental-fsync', 'yes', 'rdb同步刷盘是否采用增量fsync，每32MB执行一次fsync', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1661, 'repl-ping-replica-period', '10', '指定从节点定期ping master的周期,默认:10秒', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1662, 'latency-monitor-threshold', '30', '延迟事件阀值，单位ms', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1663, 'repl-diskless-load', 'on-empty-db', '完全安全的情况下才使用无磁盘加载', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1664, 'tracking-table-max-keys', '1000000', '无效表键的最大填充数量', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1665, 'rdb-del-sync-files', 'yes', '默认:no 不删除rdb文件,删除实例中复制使用的不持久的RDB文件', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1666, 'lazyfree-lazy-user-del', 'yes', '默认值no,设置del操作命令同unlink一致', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1667, 'io-threads', '1', '读写io线程数量', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1668, 'io-threads-do-reads', 'no', '开启io读线程', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1669, 'jemalloc-bg-thread', 'yes', '启用Jemalloc后台线程清理', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1670, 'server_cpulist', '0-7:2', '设置redis服务器/io线程的cpu使用权重', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1671, 'bio_cpulist', '1,3', '设置bio线程的cpu使用权重', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1672, 'aof_rewrite_cpulist', '8-11', '设置aof重写子进程的cpu使用权重', '2024-01-17 10:23:00', 6, 1, 62, 0, 0)
    ,(1673, 'bgsave_cpulist', '1,10-11', '设置bgsave子进程的cpu使用权重', '2024-01-17 10:23:00', 6, 1, 62, 0, 0);