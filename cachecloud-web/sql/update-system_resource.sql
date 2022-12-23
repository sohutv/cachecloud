-- system_resource change
ALTER TABLE system_resource ADD order_num int(6) NOT NULL DEFAULT '0' COMMENT '排序';
