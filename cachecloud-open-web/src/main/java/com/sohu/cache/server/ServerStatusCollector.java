package com.sohu.cache.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.server.data.OSInfo;
import com.sohu.cache.server.data.Server;
import com.sohu.cache.server.nmon.NMONService;
import com.sohu.cache.ssh.SSHTemplate;
import com.sohu.cache.ssh.SSHTemplate.DefaultLineProcessor;
import com.sohu.cache.ssh.SSHTemplate.Result;
import com.sohu.cache.ssh.SSHTemplate.SSHCallback;
import com.sohu.cache.ssh.SSHTemplate.SSHSession;
import com.sohu.cache.web.service.ServerDataService;

/**
 * 服务器状态监控服务
 */
public class ServerStatusCollector {
	private static final Logger logger = LoggerFactory.getLogger(ServerStatusCollector.class);

	//获取监控结果
	public static final String COLLECT_SERVER_STATUS = 
			  "[ -e \""+NMONService.SOCK_LOG+"\" ] && /bin/cat " + NMONService.SOCK_LOG + " >> " + NMONService.NMON_LOG
			+ ";[ -e \""+NMONService.ULIMIT_LOG+"\" ] && /bin/cat " + NMONService.ULIMIT_LOG + " >> " + NMONService.NMON_LOG
			+ ";/bin/mv " + NMONService.NMON_LOG + " " + NMONService.NMON_OLD_LOG
			+ ";[ $? -eq 0 ] && /bin/cat " + NMONService.NMON_OLD_LOG;
	
	//nmon服务
	private NMONService nmonService;
	//ssh 模板类
	private SSHTemplate sshTemplate;
	//持久化
	private ServerDataService serverDataService;
	
	private AsyncService asyncService;
	
	public void init() {
		asyncService.assemblePool(AsyncThreadPoolFactory.MACHINE_POOL, 
				AsyncThreadPoolFactory.MACHINE_THREAD_POOL);
	}
	
	//异步执行任务
	public void asyncFetchServerStatus(final String ip) {
		String key = "collect-server-"+ip;
		asyncService.submitFuture(AsyncThreadPoolFactory.MACHINE_POOL, new KeyCallable<Boolean>(key) {
            public Boolean execute() {
                try {
                	fetchServerStatus(ip);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        });
	}
	
	/**
	 * 抓取服务器状态
	 * @param ip
	 */
	public void fetchServerStatus(final String ip) {
		try {
			sshTemplate.execute(ip, new SSHCallback() {
				public Result call(SSHSession session) {
					//尝试收集服务器运行状况
					collectServerStatus(ip, session);
					//启动nmon收集服务器运行状况
					OSInfo info = nmonService.start(ip, session);
					saveServerStatus(ip, info);
					return null;
				}
			});
		} catch (Exception e) {
			logger.error("fetchServerStatus "+ip+" err", e);
		}
	}
	
	/**
	 * 收集系统状况
	 * @param ip
	 * @param session
	 */
	private void collectServerStatus(String ip, SSHSession session) {
		final Server server = new Server();
		server.setIp(ip);
		Result result = session.executeCommand(COLLECT_SERVER_STATUS, new DefaultLineProcessor() {
			public void process(String line, int lineNum) throws Exception {
				server.parse(line, null);
			}
		});
		if(!result.isSuccess()) {
			logger.error("collect " + ip + " err:" + result.getResult(), result.getExcetion());
		}
		//保存服务器静态信息
		serverDataService.saveAndUpdateServerInfo(server);
		//保存服务器状况信息
		serverDataService.saveServerStat(server);
	}
	
	/**
	 * 保存服务器dist信息
	 * @param ip
	 * @param OSInfo
	 */
	private void saveServerStatus(String ip, OSInfo osInfo) {
		if(osInfo == null) {
			return;
		}
		serverDataService.saveServerInfo(ip, osInfo.getIssue());
	}
	
	public void setNmonService(NMONService nmonService) {
		this.nmonService = nmonService;
	}
	public void setSshTemplate(SSHTemplate sshTemplate) {
		this.sshTemplate = sshTemplate;
	}
	public void setServerDataService(ServerDataService serverDataService) {
		this.serverDataService = serverDataService;
	}
	public void setAsyncService(AsyncService asyncService) {
		this.asyncService = asyncService;
	}
}
