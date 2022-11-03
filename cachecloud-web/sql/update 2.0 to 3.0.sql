CREATE TABLE `app_alert_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `visible_type` int(1) NOT NULL COMMENT '可见类型（0：均可见；1：仅管理员可见；）',
  `important_level` int(1) NOT NULL COMMENT '重要类型（0：一般；1：重要；2：紧急）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `app_id` bigint(20) DEFAULT NULL COMMENT 'app id',
  `instance_id` bigint(20) DEFAULT NULL COMMENT '实例id',
  `ip` varchar(16) COLLATE utf8_bin DEFAULT NULL COMMENT '机器ip',
  `port` int(10) DEFAULT NULL COMMENT '端口号',
  `title` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '报警标题',
  `content` varchar(500) COLLATE utf8_bin NOT NULL COMMENT '报警内容',
  PRIMARY KEY (`id`),
  KEY `app_id` (`app_id`),
  KEY `ip` (`ip`),
  KEY `idx_inst_id` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='报警记录表';

CREATE TABLE `config_restart_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL COMMENT '应用id',
  `app_name` varchar(36) NOT NULL COMMENT '应用名称',
  `operate_type` char(1) NOT NULL COMMENT '操作类型（0:滚动重启，1:修改配置强制重启；2：修改配置）',
  `param` varchar(2000) NOT NULL COMMENT '初始化任务参数(json):不变',
  `status` tinyint(4) NOT NULL COMMENT '状态：0等待，1运行，2成功，3失败，4配置修改待重启',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `log` longtext COMMENT '日志信息',
  `user_name` varchar(64) DEFAULT NULL COMMENT '操作人员姓名',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `instances` varchar(1000) DEFAULT NULL COMMENT '涉及实例id列表的json格式',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='重启记录表';

CREATE TABLE `module_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `git_url` varchar(255) NOT NULL DEFAULT '' COMMENT 'git resource',
  `info` varchar(128) DEFAULT NULL COMMENT '模块信息说明',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0:无效 1:有效',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Redis模块信息表';

CREATE TABLE `module_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `module_id` int(11) NOT NULL,
  `version_id` int(11) NOT NULL COMMENT '关联版本号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `so_path` varchar(255) DEFAULT NULL COMMENT '编译后so库的地址',
  `tag` varchar(64) NOT NULL COMMENT '模块版本号',
  `status` int(255) NOT NULL DEFAULT '0' COMMENT '是否可用(关联so地址)：0 不可用 1：可用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Redis模块版本管理表';

CREATE TABLE `app_import` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) DEFAULT NULL COMMENT '目标应用id',
  `instance_info` text COMMENT '源redis实例信息',
  `redis_password` varchar(200) DEFAULT NULL COMMENT '源redis密码',
  `status` int(11) DEFAULT NULL COMMENT '迁移状态：PREPARE(0, "准备", "应用导入-未开始"),     START(1, "进行中...", "应用导入-开始"),     ERROR(2, "error", "应用导入-出错"),     VERSION_BUILD_START(11, "进行中...", "新建redis版本-进行中"),     VERSION_BUILD_ERROR(12, "error", "新建redis版本-出错"),     VERSION_BUILD_END(20, "成功", "新建redis版本-完成"),     APP_BUILD_INIT(21, "准备就绪", "新建redis应用-准备就绪"),     APP_BUILD_START(22, "进行中...", "新建redis应用-进行中"),     APP_BUILD_ERROR(23, "error", "新建redis应用-出错"),     APP_BUILD_END(30, "成功", "新建redis应用-完成"),     MIGRATE_INIT(31, "准备就绪", "数据迁移-准备就绪"),     MIGRATE_START(32, "进行中...", "数据迁移-进行中"),     MIGRATE_ERROR(33, "error", "数据迁移-出错"),     MIGRATE_END(3, "成功", "应用导入-成功")',
  `step` int(11) DEFAULT NULL COMMENT '导入阶段',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `migrate_id` bigint(20) DEFAULT NULL COMMENT '数据迁移id',
  `mem_size` int(11) DEFAULT NULL COMMENT '目标应用内存大小，单位G',
  `redis_version_name` varchar(20) DEFAULT NULL COMMENT '目标应用redis版本，格式：redis-x.x.x',
  `app_build_task_id` bigint(20) DEFAULT NULL COMMENT '目标应用部署任务id',
  `source_type` int(11) DEFAULT NULL COMMENT '源redis类型：7:cluster, 6:sentinel, 5:standalone',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- instance_alert_configs change
ALTER TABLE instance_alert_configs ADD important_level TINYINT(4) DEFAULT 0 NOT NULL COMMENT '重要程度（0：一般；1：重要；2：紧急）';

-- app_user change
ALTER TABLE app_user ADD password varchar(64) NULL COMMENT '密码';
ALTER TABLE app_user ADD register_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '注册时间';
ALTER TABLE app_user ADD purpose varchar(255) NULL COMMENT '使用目的';
ALTER TABLE app_user ADD company varchar(255) NULL COMMENT '公司名称';

-- module_info change
ALTER TABLE module_info ADD CONSTRAINT `NAMEKEY` UNIQUE KEY (name);

-- app_desc change
ALTER TABLE app_desc ADD custom_password varchar(255) DEFAULT NULL COMMENT '自定义密码';

-- redis_module_config definition

CREATE TABLE `redis_module_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(128) NOT NULL COMMENT '配置名',
  `config_value` varchar(512) NOT NULL COMMENT '配置值',
  `info` varchar(512) NOT NULL COMMENT '配置说明',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `type` mediumint(9) NOT NULL COMMENT '类型：2.cluster节点特殊配置, 5:sentinel节点配置, 6:redis普通节点',
  `status` tinyint(4) NOT NULL COMMENT '1有效,0无效',
  `version_id` int(11) NOT NULL COMMENT 'Module version版本表主键id',
  `refresh` tinyint(4) DEFAULT '0' COMMENT '是否可重置：0不可，1可重置',
  `module_id` int(11) NOT NULL DEFAULT '7' COMMENT 'Module 信息表id',
  `config_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '配置类型，0：加载和运行配置；1：加载时配置；2：运行时配置',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_configkey_type_version_id` (`config_key`,`type`,`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='redis模块配置表';


-- app_to_module definition

CREATE TABLE `app_to_module` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` bigint(20) NOT NULL COMMENT '应用id',
  `module_id` int(11) NOT NULL COMMENT '模块info id',
  `module_version_id` int(11) NOT NULL COMMENT '模块版本id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_to_module_un` (`app_id`,`module_id`,`module_version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='应用与模块关系表';

ALTER TABLE machine_info ADD dis_type tinyint(4) DEFAULT 0 NOT NULL COMMENT '操作系统发行版本，0:centos;1:ubuntu';

