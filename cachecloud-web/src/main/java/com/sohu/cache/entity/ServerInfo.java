package com.sohu.cache.entity;

import lombok.Data;

/**
 * 服务器信息
 */
@Data
public class ServerInfo {
	private String ip;
	private String host;
	//逻辑cpu个数
	private int cpus;
	//nmon版本
	private String nmon;
	//cpu型号
	private String cpuModel;
	//内核版本
	private String kernel;
	//发行版本
	private String dist;
	//ulimit
	private String ulimit;
	// gcc version
	private String gcc;
}
