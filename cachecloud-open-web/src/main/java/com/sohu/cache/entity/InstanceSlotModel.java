package com.sohu.cache.entity;

import java.util.List;

/**
 * 实例slot
 * 
 * @author leifu
 * @Date 2016年12月4日
 * @Time 下午2:30:29
 */
public class InstanceSlotModel {
	/**
	 * slot分布，例如： 0-4096 或者0-8 9-4096
	 */
	private List<String> slotDistributeList;

	/**
	 * slot列表
	 */
	private List<Integer> slotList;
	
	/**
	 * ip
	 */
	private String host;
	
	/**
	 * 端口
	 */
	private int port;

	public List<String> getSlotDistributeList() {
		return slotDistributeList;
	}

	public void setSlotDistributeList(List<String> slotDistributeList) {
		this.slotDistributeList = slotDistributeList;
	}

	public List<Integer> getSlotList() {
		return slotList;
	}

	public void setSlotList(List<Integer> slotList) {
		this.slotList = slotList;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "InstanceSlotModel [slotDistributeList=" + slotDistributeList + ", slotList=" + slotList + ", host="
				+ host + ", port=" + port + "]";
	}
	
}
