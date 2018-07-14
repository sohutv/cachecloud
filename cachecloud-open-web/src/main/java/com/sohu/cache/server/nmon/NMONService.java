package com.sohu.cache.server.nmon;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.server.data.OS;
import com.sohu.cache.server.data.OSInfo;
import com.sohu.cache.ssh.SSHTemplate.DefaultLineProcessor;
import com.sohu.cache.ssh.SSHTemplate.Result;
import com.sohu.cache.ssh.SSHTemplate.SSHSession;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.NMONFileFactory;
import com.sohu.cache.util.OSFactory;
/**
 * 服务器监控脚本服务(nmon识别和监控)
 */
public class NMONService {
	private static final Logger logger = LoggerFactory.getLogger(NMONService.class);
	//获取系统版本位数命令
	public static final String OS_INFO_CMD = "/bin/uname -a; /bin/cat /etc/issue";
	//nmon路径
	public static final String NMON = "nmon";
	//nmon输出的结果文件
	public static final String NMON_LOG = "/tmp/nmon.log";
	//nmon输出的老结果文件
	public static final String NMON_OLD_LOG = "/tmp/nmon.old.log";
	//tcp输出的结果文件
	public static final String SOCK_LOG = "/tmp/sock.log";
	//ulimit输出的结果文件
	public static final String ULIMIT_LOG = "/tmp/ulimit.log";
	
	/**
	 * 启动nmon收集系统状况
	 * @param ip
	 * @param session
	 * @return @OSInfo 收集到的操作系统信息
	 */
	public OSInfo start(String ip, SSHSession session) {
		Result startCollectResult = session.executeCommand(getStartServerCollect());
		if(!startCollectResult.isSuccess()) {
			logger.error("start nmon "+ip+" err:"+startCollectResult.getResult(), 
					startCollectResult.getExcetion());
			//执行命令没有发生异常，则nmon可能不存在或有问题
			if(startCollectResult.getExcetion() == null) {
				//尝试处理出错信息
				return initNmon(ip, session);
			}
		}
		return null;
	}
	
	
	/**
	 * 尝试修复启动失败的错误
	 * @param ip
	 * @param session
	 */
	private OSInfo initNmon(String ip, SSHSession session) {
		//获取nmon版本
		String version = getNMONVersion(ip, session);
		//获取操作系统原始信息
		OSInfo osInfo = getOSInfo(ip, session);
		OS os = null;
		//nmon文件不存在，需要根据操作系统识别是否支持
		if(null == version) {
			logger.warn("{} not exist {}", ip, getNmonFile());
			//将原始信息转换为可识别的操作系统
			os = OSFactory.getOS(osInfo);
		} else {
			//nmon存在，但是版本有问题，此时不应该再判断系统信息了，直接用默认的  
			logger.warn("{} {} version err:"+version, ip, getNmonFile());
			os = OSFactory.getDefaultOS(osInfo);
		}
		if(os == null) {
			logger.error("unkonw os info={}", osInfo);
			return null;
		}
		//获取nmon文件
		File nmonFile = NMONFileFactory.getNMONFile(os);
		if(nmonFile == null) {
			logger.warn("{} no corresponding nmon file", os);
			nmonFile = NMONFileFactory.getNMONFile(OSFactory.getDefaultOS(osInfo));
		}
		//将nmon文件传输至服务器
		sendNMONToServer(ip, session, nmonFile);
		
		return osInfo;
	}
	
	/**
	 * 获取nmon文件版本
	 * @param ip
	 * @param session
	 * @return 存在返回版本，不存在返回null, 执行错误返回异常
	 */
	private String getNMONVersion(String ip, SSHSession session) {
		Result nmonVersionResult = session.executeCommand(getNmonVersion());
		if(nmonVersionResult.isSuccess()) {
			return nmonVersionResult.getResult();
		} else {
			logger.error(getNmonVersion()+" err:"+nmonVersionResult.getResult(), nmonVersionResult.getExcetion());
		}
		return null;
	}
	
	/**
	 * 获取操作系统信息
	 * @param ip
	 * @param session
	 * @return OSInfo
	 */
	private OSInfo getOSInfo(String ip, SSHSession session) {
		final OSInfo osInfo = new OSInfo();
		session.executeCommand(OS_INFO_CMD, new DefaultLineProcessor() {
			public void process(String line, int lineNum) throws Exception {
				switch(lineNum) {
				case 1:
					osInfo.setUname(line);
					break;
				case 2:
					osInfo.setIssue(line);
				}
			}
		});
		return osInfo;
	}
	
	/**
	 * 将nmon文件scp到服务器上
	 * @param ip
	 * @param session
	 * @param nmonFile
	 */
	private void sendNMONToServer(String ip, SSHSession session, File nmonFile) {
		Result mkResult = session.executeCommand(getMkNmonDir());
		if(!mkResult.isSuccess()) {
			logger.error("mkdir err:"+mkResult.getResult(), mkResult.getExcetion());
			return;
		}
		Result scpRst = session.scpToFile(nmonFile.getAbsolutePath(), NMON, getMmonDir());
		if(scpRst.isSuccess()) {
			logger.info("scp {} to {} success", nmonFile.getAbsolutePath(), ip);
		} else {
			logger.error("scp to "+ip+" err", scpRst.getExcetion());
		}
	}
	
	/**
	 * nmon监控启动
	 * @return
	 */
	private String getStartServerCollect() {
		return getNmonFile() +" -F " + NMON_LOG + " -s0 -c1;" +
				"/bin/grep TCP /proc/net/sockstat > " + SOCK_LOG + 
				";ulimit -n -u > " + ULIMIT_LOG;
	}
	
	private String getMmonDir() {
		return ConstUtils.NMON_DIR;
	}
	
	private String getNmonFile() {
		return getMmonDir() + "/" + NMON;
	}
	
	private String getMkNmonDir() {
		return "/bin/mkdir -p " + getMmonDir();
	}
	
	private String getNmonVersion() {
		return "[ -e \""+ getNmonFile() +"\" ] && "+ getNmonFile() +" -V";
	}
	
}
