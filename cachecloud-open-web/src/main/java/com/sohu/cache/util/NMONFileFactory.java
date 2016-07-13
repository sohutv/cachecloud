package com.sohu.cache.util;

import java.io.File;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.server.data.OS;
/**
 * nmon文件存储工厂
 */
public class NMONFileFactory {
	private static final Logger logger = LoggerFactory.getLogger(NMONFileFactory.class);
	public static final String NMON_PATH = "/nmon";
	public static final String NMON_DIR_PATH = "nmon.dir";
	public static final String FILE = "file";
	//nmon文件存储 key为OSType_ProcessorArchitecture_DistributionType
	private static final Map<String, File> nmonFileMap = new HashMap<String, File>();
	
	static {
		init();
	}
	
	/**
	 * 初始化nmon文件
	 */
	private static void init() {
		try {
			String path = System.getProperty(NMON_DIR_PATH);
			if(path == null) {
				String classpath = null;
				try {
					CodeSource codeSource = NMONFileFactory.class.getProtectionDomain().getCodeSource();
					classpath = codeSource.getLocation().getPath();
		        	if(classpath.startsWith(FILE)) {
		        		//like that: file:/opt/app/cachecloud/cachecloud-web-1.0-SNAPSHOT.war!/WEB-INF/classes!/
		        		classpath = classpath.substring(FILE.length()+1);
		        	}
		        	if(new File(classpath).isDirectory()) {
		        		path = classpath+"../.."+NMON_PATH;
		        	} else {
		        		//like that: /opt/app/cachecloud/cachecloud-web-1.0-SNAPSHOT.war!/WEB-INF/classes!/
		        		String[] tmp = classpath.split("!/", 2);
		        		path = tmp[0].substring(0, tmp[0].lastIndexOf("/"))+NMON_PATH;
		        	}
		        } catch (Exception e) {
		            logger.error(classpath, e);
		        }
			}
	        File nmonDir = new File(path);
			if(!nmonDir.exists()) {
				logger.error("{} path not exist", nmonDir.getAbsolutePath());
				return;
			}
			//获取操作系统目录
			File[] osDirs = nmonDir.listFiles();
			if(osDirs == null) {
				logger.error("{} not contains OS folders", nmonDir.getAbsolutePath());
				return;
			}
			for(File osDir : osDirs) {
				//获取处理器架构目录
				File[] archFiles = osDir.listFiles();
				if(archFiles == null) {
					logger.info("{} not contains architecture folders", osDir.getName());
					continue;
				}
				for(File archDir : archFiles) {
					//获取nmon文件目录
					File[] nmonFiles = archDir.listFiles();
					if(nmonFiles == null) {
						logger.info("{} not contains nomon files", archDir.getName());
						continue;
					}
					for(File nmonFile : nmonFiles) {
						nmonFileMap.put(osDir.getName() + "_" + archDir.getName() 
								+ "_" + nmonFile.getName() , nmonFile);
					}
					logger.info("init {} {} nmon file size="+nmonFiles.length, 
							osDir.getName(), archDir.getName());
				}
			}
			logger.info("init {} finished, os size={}", nmonDir.getAbsolutePath(), osDirs.length);
		} catch (Exception e) {
			logger.error("init nmon factory", e);
		}
	}
	
	/**
	 * 根据OS信息获取对应版本的NMON文件
	 * @param os
	 * @return File
	 */
	public static File getNMONFile(OS os) {
		String key = os.getOsType().getValue() 
				+ "_" + os.getProcessorArchitecture().getValue()
				+ "_" + os.getDistributionType().getNmonName()
				+ os.getDistributionVersion().getValue();
		return nmonFileMap.get(key);
	}
}
