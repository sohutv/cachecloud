package com.sohu.cache.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.server.data.OS;
import com.sohu.cache.server.data.OSInfo;
import com.sohu.cache.server.data.OSInfo.DistributionType;
import com.sohu.cache.server.data.OSInfo.DistributionVersion;
import com.sohu.cache.server.data.OSInfo.OSType;
import com.sohu.cache.server.data.OSInfo.ProcessorArchitecture;

/**
 * 根据操作系统原生信息解析出OS
 */
public class OSFactory {
	private static final Logger logger = LoggerFactory.getLogger(OSFactory.class);
	//获取发行版本号的主版本和次版本
	private static final Pattern VERSION_PATTERN = Pattern.compile("([1-9]+(\\.[0-9]+)?)");
	
	public static OS getDefaultOS(OSInfo osInfo) {
		String uname = osInfo.getUname();
		//无法获取系统位数
		if(uname == null) {
			return null;
		}
		uname = uname.toLowerCase();
		ProcessorArchitecture defaultArch = ProcessorArchitecture.X86_64;
		//其次获取操作系统位数
		if(!uname.contains(defaultArch.getValue())) {
			defaultArch = ProcessorArchitecture.X86;
		}
		return new OS(OSType.LINUX, DistributionType.LINUX_OLD, 
				DistributionVersion.DEFAULT, defaultArch);
	}
	
	/**
	 * 采用uname -a信息和/etc/issue解析出目前能够支持的操作系统
	 * @param osInfo
	 * @return OS
	 */
	public static OS getOS(OSInfo osInfo) {
		String uname = osInfo.getUname();
		String issue = osInfo.getIssue();
		OSType osType = OSType.LINUX;
		ProcessorArchitecture defaultArch = ProcessorArchitecture.X86_64;
		DistributionType defaultDist = DistributionType.LINUX_OLD;
		DistributionVersion version = DistributionVersion.DEFAULT;
		
		//无法获取系统类型，位数 版本，采用默认
		if(uname == null || issue == null) {
			OS os = new OS(osType, defaultDist, version, defaultArch);
			return os;
		}
		
		uname = uname.toLowerCase();
		//首先获取操作系统类型
		if(!uname.contains(OSType.LINUX.getValue())) {
			logger.error("os={} is temporarily not supported", uname);
			return null;
		}
		//其次获取操作系统位数
		if(!uname.contains(defaultArch.getValue())) {
			defaultArch = ProcessorArchitecture.X86;
		}
		//再次解析操作系统发行版本
		issue = issue.toLowerCase();
		
		DistributionType findType = DistributionType.findByContains(issue);
		//没有找到匹配的版本，使用默认
		if(findType == null) {
			logger.warn("dist cannot matched, {}", issue);
			OS os = new OS(osType, defaultDist, version, defaultArch);
			return os;
		}
		
		//最后解析版本号
		Matcher matcher = VERSION_PATTERN.matcher(issue);
		//没有版本好用默认的
		if(!matcher.find()) {
			logger.warn("version not matched, {}", issue);
			OS os = new OS(osType, defaultDist, version, defaultArch);
			return os;
		}
		String ver = matcher.group();
		ver = ver.replaceAll("\\.", "");
		logger.info("version matched, {} - {}", ver, issue);
		DistributionVersion versionResult = findVersion(findType.getVersions(), ver);
		//没有具体的版本能匹配上
		if(versionResult == null) {
			logger.info("version {} not found, {}", ver);
			OS os = new OS(osType, defaultDist, version, defaultArch);
			return os;
		}
		
		OS os = new OS(osType, findType, versionResult, defaultArch);
		logger.info("find OS={}", os);
		return os;
	}
	
	private static DistributionVersion findVersion(DistributionVersion[] versions, String target) {
		for(DistributionVersion dv : versions) {
			if(dv.getValue().equals(target)){
				return dv;
			} 
		}
		return null;
	}
}
