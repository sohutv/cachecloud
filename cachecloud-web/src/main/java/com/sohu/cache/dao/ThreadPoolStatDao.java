package com.sohu.cache.dao;

import com.sohu.cache.entity.ThreadPoolStat;
import org.apache.ibatis.annotations.Param;

/**
 * @author fulei
 * @date 2018年8月11日
 * @time 上午11:22:18
 */
public interface ThreadPoolStatDao {

	/**
     * @param threadPoolStat
	 * @return
	 */
	int save(ThreadPoolStat threadPoolStat);

	/**
	 * 获取上一次任务数量
	 * @param ip
	 * @param port
	 * @param threadPoolName
	 * @return
	 */
	Long getLastTaskCount(@Param("ip") String ip, @Param("port") int port, @Param("threadPoolName") String threadPoolName);
	
}