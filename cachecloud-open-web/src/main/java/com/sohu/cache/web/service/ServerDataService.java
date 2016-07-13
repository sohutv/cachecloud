package com.sohu.cache.web.service;

import java.util.List;

import com.sohu.cache.entity.ServerInfo;
import com.sohu.cache.entity.ServerStatus;
import com.sohu.cache.server.data.Server;

public interface ServerDataService {
	/**
	 * 查询服务器基本信息
	 * @param ip
	 * @return @ServerInfo
	 */
	public ServerInfo queryServerInfo(String ip);
	
	/**
	 * 保存服务器发行版信息
	 * @param ip
	 * @param dist from /etc/issue
	 */
	public void saveServerInfo(String ip, String dist);
	
	/**
	 * 保存/更新服务器信息
	 * @param server
	 * @return 影响的行数
	 */
	public Integer saveAndUpdateServerInfo(Server server);
	
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerStatus(String ip, String date);
	
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerOverview(String ip, String date);
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerCpu(String ip, String date);
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerNet(String ip, String date);
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerDisk(String ip, String date);
	
	/**
	 * 保存服务器状态
	 */
	public void saveServerStat(Server server);
}
