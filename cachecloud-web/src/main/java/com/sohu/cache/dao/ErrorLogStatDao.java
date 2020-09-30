package com.sohu.cache.dao;

import com.sohu.cache.entity.ErrorLogStat;
import org.apache.ibatis.annotations.Param;

/**
 * @author fulei
 */
public interface ErrorLogStatDao {

	/**
	 * @param errorLogStat
	 * @return
	 */
	int save(ErrorLogStat errorLogStat);
	
	
	/**
	 * 获取上一次错误数量
	 * @param ip
	 * @param port
	 * @param className
	 * @return
	 */
	Long getLastErrorCount(@Param("ip") String ip, @Param("port") int port, @Param("className") String className);
}
