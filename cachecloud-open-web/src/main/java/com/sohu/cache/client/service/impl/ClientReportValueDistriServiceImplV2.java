package com.sohu.cache.client.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.ClientReportInstanceService;
import com.sohu.cache.client.service.ClientReportValueDistriService;
import com.sohu.cache.dao.AppClientValueStatDao;
import com.sohu.cache.entity.AppClientValueDistriSimple;
import com.sohu.cache.entity.AppClientValueDistriStatTotal;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;
import com.sohu.tv.jedis.stat.enums.ClientCollectDataTypeEnum;
import com.sohu.tv.jedis.stat.enums.ValueSizeDistriEnum;
import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * 客户端上报值分布serviceV2
 * 
 * @author leifu
 * @Date 2016年5月5日
 * @Time 上午10:23:00
 */
public class ClientReportValueDistriServiceImplV2 implements ClientReportValueDistriService {

    private final Logger logger = LoggerFactory.getLogger(ClientReportValueDistriServiceImplV2.class);

    public static Set<String> excludeCommands = new HashSet<String>();
    static {
        excludeCommands.add("ping");
        excludeCommands.add("quit");
    }
    
    /**
     * 客户端统计值分布数据操作
     */
    private AppClientValueStatDao appClientValueStatDao;

    /**
     * host:port与instanceInfo简单缓存
     */
    private ClientReportInstanceService clientReportInstanceService;

    @Override
    public List<AppClientValueDistriSimple> getAppValueDistriList(long appId, long startTime, long endTime) {
        try {
            return appClientValueStatDao.getAppValueDistriList(appId, startTime, endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void batchSave(ClientReportBean clientReportBean) {
        try {
            // 1.client上报
            final long collectTime = clientReportBean.getCollectTime();
            final long reportTime = clientReportBean.getReportTimeStamp();
            final List<Map<String, Object>> datas = clientReportBean.getDatas();
            if (datas == null || datas.isEmpty()) {
                logger.warn("datas field {} is empty", clientReportBean);
                return;
            }

            // 3.解析
            for (Map<String, Object> map : datas) {
                Integer clientDataType = MapUtils.getInteger(map, ClientReportConstant.CLIENT_DATA_TYPE, -1);
                ClientCollectDataTypeEnum clientCollectDataTypeEnum = ClientCollectDataTypeEnum.MAP.get(clientDataType);
                if (clientCollectDataTypeEnum == null) {
                    continue;
                }
                if (ClientCollectDataTypeEnum.VALUE_LENGTH_DISTRI_TYPE.equals(clientCollectDataTypeEnum)) {
                    AppClientValueDistriStatTotal appClientValueDistriStat = generate(collectTime, reportTime, map);
                    if (appClientValueDistriStat != null) {
                        appClientValueStatDao.save(appClientValueDistriStat);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private AppClientValueDistriStatTotal generate(long collectTime, long reportTime, Map<String, Object> map) {
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
            return null;
        }
        if (excludeCommands.contains(command)) {
            return null;
        }

        // 实例host:port
        String hostPort = MapUtils.getString(map, ClientReportConstant.VALUE_HOST_PORT, "");
        if (StringUtils.isEmpty(hostPort)) {
            logger.warn("hostPort is empty", hostPort);
            return null;
        }
        int index = hostPort.indexOf(":");
        if (index <= 0) {
            logger.warn("hostPort {} format is wrong", hostPort);
            return null;
        }
        String host = hostPort.substring(0, index);
        int port = NumberUtils.toInt(hostPort.substring(index + 1));

        // 实例信息
        InstanceInfo instanceInfo = clientReportInstanceService.getInstanceInfoByHostPort(host, port);
        if (instanceInfo == null) {
            // logger.warn("instanceInfo is empty, host is {}, port is {}",
            // host, port);
            return null;
        }

        AppClientValueDistriStatTotal stat = new AppClientValueDistriStatTotal();
        stat.setAppId(instanceInfo.getAppId());
        stat.setCollectTime(collectTime);
        stat.setUpdateTime(new Date());
        stat.setCommand(command);
        stat.setDistributeType(valueSizeDistriEnum.getType());
        stat.setCount(count);

        return stat;
    }

    @Override
    public int deleteBeforeCollectTime(long collectTime) {
        try {
            return appClientValueStatDao.deleteBeforeCollectTime(collectTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    public void setClientReportInstanceService(ClientReportInstanceService clientReportInstanceService) {
        this.clientReportInstanceService = clientReportInstanceService;
    }

    public void setAppClientValueStatDao(AppClientValueStatDao appClientValueStatDao) {
        this.appClientValueStatDao = appClientValueStatDao;
    }

}
