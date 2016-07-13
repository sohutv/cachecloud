package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.ServerInfo;
import com.sohu.cache.entity.ServerStatus;
import com.sohu.cache.server.data.Server;

/**
 * 服务器状态信息持久化
 */
public interface ServerStatusDao {

	/**
	 * 查询服务器基本信息
	 * @param ip
	 * @return @ServerInfo
	 */
	public ServerInfo queryServerInfo(@Param("ip") String ip);
	
	/**
	 * 保存服务器发行版信息
	 * @param ip
	 * @param dist from /etc/issue
	 */
	public void saveServerInfo(@Param("ip") String ip, @Param("dist") String dist);
	
	/**
	 * 删除服务器信息
	 * @param ip
	 * @return 删除的数量
	 */
	public Integer deleteServerInfo(@Param("ip") String ip);
	
	/**
	 * 保存/更新服务器信息
	 * @param server
	 * @return 影响的行数
	 */
	public Integer saveAndUpdateServerInfo(@Param("server")Server server);
	
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerStatus(@Param("ip") String ip, 
			@Param("cdate") String date);
	
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerOverview(@Param("ip") String ip, 
			@Param("cdate") String date);
	
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerCpu(@Param("ip") String ip, 
			@Param("cdate") String date);
	
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerNet(@Param("ip") String ip, 
			@Param("cdate") String date);
	
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
	public List<ServerStatus> queryServerDisk(@Param("ip") String ip, 
			@Param("cdate") String date);
	
	/**
	 * 保存服务器状态
	 * @param Server
	 */
	public void saveServerStat(@Param("server") Server server);
}
