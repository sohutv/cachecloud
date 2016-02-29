package com.sohu.cache.client.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.ClientReportValueDistriService;
import com.sohu.cache.dao.AppClientValueDistriStatDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppClientValueDistriSimple;
import com.sohu.cache.entity.AppClientValueDistriStat;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;
import com.sohu.tv.jedis.stat.enums.ValueSizeDistriEnum;

/**
 * 客户端上报值分布service
 * 
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:32
 */
public class ClientReportValueDistriServiceImpl implements
		ClientReportValueDistriService {

	private final Logger logger = LoggerFactory.getLogger(ClientReportValueDistriServiceImpl.class);

	/**
	 * 客户端统计值分布数据操作
	 */
	private AppClientValueDistriStatDao appClientValueDistriStatDao;

	/**
	 * 实例操作
	 */
	private InstanceDao instanceDao;

	@Override
	public void execute(String clientIp, long collectTime, long reportTime,
			Map<String, Object> map) {
		String valueDistri = MapUtils.getString(map, ClientReportConstant.VALUE_DISTRI, "");
		ValueSizeDistriEnum valueSizeDistriEnum = ValueSizeDistriEnum.getByValue(valueDistri);
		if (valueSizeDistriEnum == null) {
			logger.warn("valueDistri {} is wrong, not in enums {}", valueDistri, ValueSizeDistriEnum.values());
		}

		// 次数
		Integer count = MapUtils.getInteger(map, ClientReportConstant.VALUE_COUNT, 0);

		// 命令
		String command = MapUtils.getString(map, ClientReportConstant.VALUE_COMMAND, "");
		if (StringUtils.isBlank(command)) {
			logger.warn("command is empty!");
			return;
		}

		// 实例host:port
		String hostPort = MapUtils.getString(map, ClientReportConstant.VALUE_HOST_PORT, "");
		if (StringUtils.isEmpty(hostPort)) {
			logger.warn("hostPort is empty", hostPort);
			return;
		}
		int index = hostPort.indexOf(":");
		if (index <= 0) {
			logger.warn("hostPort {} format is wrong", hostPort);
			return;
		}
		String host = hostPort.substring(0, index);
		int port = NumberUtils.toInt(hostPort.substring(index + 1));

		// 实例信息
		InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(host, port);
		if (instanceInfo == null) {
			//logger.warn("instanceInfo is empty, host is {}, port is {}", host, port);
			return;
		}
		long appId = instanceInfo.getAppId();

		AppClientValueDistriStat stat = new AppClientValueDistriStat();
		stat.setAppId(appId);
		stat.setClientIp(clientIp);
		stat.setReportTime(new Date(reportTime));
		stat.setCollectTime(collectTime);
		stat.setCreateTime(new Date());
		stat.setCommand(command);
		stat.setDistributeValue(valueDistri);
		stat.setDistributeType(valueSizeDistriEnum.getType());
		stat.setCount(count);
		stat.setInstanceHost(host);
		stat.setInstancePort(port);
		stat.setInstanceId(instanceInfo.getId());

		appClientValueDistriStatDao.save(stat);
	}

	@Override
	public List<AppClientValueDistriSimple> getAppValueDistriList(long appId, long startTime, long endTime) {
	    try {
            return appClientValueDistriStatDao.getAppValueDistriList(appId, startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
	}


	public void setInstanceDao(InstanceDao instanceDao) {
		this.instanceDao = instanceDao;
	}

	public void setAppClientValueDistriStatDao(AppClientValueDistriStatDao appClientValueDistriStatDao) {
		this.appClientValueDistriStatDao = appClientValueDistriStatDao;
	}

}
