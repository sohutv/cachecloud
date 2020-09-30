package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * 数据库连接池统计
 * @author fulei
 * @date 2018年8月17日
 * @time 上午11:40:05
 */
@Data
public class DbPoolStat {

    private long id;
	
	/**
	 * 进程ip
	 */
	private String ip;
	
	/**
	 * 进程port
	 */
	private int port;
	
	private String dbPoolName;
	
	private long collectTime;
	
	private Date collectDate;
	
	private int maxSize;
	
	private int minSize;
	
	private int busySize;
	
	private int idleSize;
	
	private int totalSize;
	
	private Date createTime;
	
	private Date updateTime;

	public Date getCreateTime() {
		return (Date) createTime.clone();
	}

	public void setCreateTime(Date createTime) {
		this.createTime = (Date) createTime.clone();
	}

	public Date getUpdateTime() {
		return (Date) updateTime.clone();
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = (Date) updateTime.clone();
	}

	public Date getCollectDate() {
		return (Date) collectDate.clone();
	}

	public void setCollectDate(Date collectDate) {
		this.collectDate =  (Date) collectDate.clone();
	}
}