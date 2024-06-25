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

