package com.sohu.cache.web.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.dao.ServerStatusDao;
import com.sohu.cache.entity.ServerInfo;
import com.sohu.cache.entity.ServerStatus;
import com.sohu.cache.server.data.Server;
import com.sohu.cache.web.service.ServerDataService;

public class ServerDataServiceImpl implements ServerDataService {
	private static final Logger logger = LoggerFactory.getLogger(ServerDataServiceImpl.class);
	//持久化接口
	private ServerStatusDao serverStatusDao;
	
	@Override
	public ServerInfo queryServerInfo(String ip) {
		try {
			return serverStatusDao.queryServerInfo(ip);
		} catch (Exception e) {
			logger.error("query err:"+ip, e);
		}
		return null;
	}

	@Override
	public void saveServerInfo(String ip, String dist) {
		if(dist == null) {
			return;
		}
		dist = dist.trim();
		if(dist.length() == 0) {
			return;
		}
		try {
			serverStatusDao.saveServerInfo(ip, dist);
		} catch (Exception e) {
			logger.error("saveServerInfo err:"+ip+" dist="+dist, e);
		}
	}

	public Integer saveAndUpdateServerInfo(Server server) {
		if(server.getHost() == null || server.getNmon() == null || server.getCpus() == 0 || 
		   server.getCpuModel() == null || server.getKernel() == null || server.getUlimit() == null) {
			return null;
		}
		try {
			return serverStatusDao.saveAndUpdateServerInfo(server);
		} catch (Exception e) {
			logger.error("saveAndUpdateServerInfo err server="+server, e);
		}
		return null;
	}

	@Override
	public List<ServerStatus> queryServerStatus(String ip, String date) {
		try {
			return serverStatusDao.queryServerStatus(ip, date);
		} catch (Exception e) {
			logger.error("queryServerStatus err ip="+ip+" date="+date, e);
		}
		return new ArrayList<ServerStatus>(0);
	}

	@Override
	public List<ServerStatus> queryServerOverview(String ip, String date) {
		try {
			return serverStatusDao.queryServerOverview(ip, date);
		} catch (Exception e) {
			logger.error("queryServerOverview err ip="+ip+" date="+date, e);
		}
		return new ArrayList<ServerStatus>(0);
	}

	@Override
	public List<ServerStatus> queryServerCpu(String ip, String date) {
		try {
			return serverStatusDao.queryServerCpu(ip, date);
		} catch (Exception e) {
			logger.error("queryServerCpu err ip="+ip+" date="+date, e);
		}
		return new ArrayList<ServerStatus>(0);
	}

	@Override
	public List<ServerStatus> queryServerNet(String ip, String date) {
		try {
			return serverStatusDao.queryServerNet(ip, date);
		} catch (Exception e) {
			logger.error("queryServerNet err ip="+ip+" date="+date, e);
		}
		return new ArrayList<ServerStatus>(0);
	}

	@Override
	public List<ServerStatus> queryServerDisk(String ip, String date) {
		try {
			return serverStatusDao.queryServerDisk(ip, date);
		} catch (Exception e) {
			logger.error("queryServerDisk err ip="+ip+" date="+date, e);
		}
		return new ArrayList<ServerStatus>(0);
	}

	@Override
	public void saveServerStat(Server server) {
		if(server == null || server.getDateTime() == null) {
			return;
		}
		try {
			serverStatusDao.saveServerStat(server);
		} catch (Exception e) {
			logger.error("saveServerStat err server="+server, e);
		}
	}

	public void setServerStatusDao(ServerStatusDao serverStatusDao) {
		this.serverStatusDao = serverStatusDao;
	}
}
