package com.sohu.cache.client.service;

import java.util.Map;

/**
 * 
 * @author leifu
 * @Date 2015-1-31
 * @Time 下午9:01:52
 */
public interface ClientReportDataExecuteService {
	
	/**
	 * 处理各种统计数据
	 * @param clientIp
	 * @param collectTime
	 * @param reportTime
	 * @param map
	 */
    public void execute(String clientIp, long collectTime, long reportTime, Map<String,Object> map);

}
