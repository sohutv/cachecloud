package com.sohu.cache.client.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.client.service.ClientReportExceptionService;
import com.sohu.cache.client.service.ClientReportInstanceService;
import com.sohu.cache.dao.AppClientExceptionStatDao;
import com.sohu.cache.dao.AppClientVersionDao;
import com.sohu.cache.entity.AppClientExceptionStat;
import com.sohu.cache.entity.AppClientVersion;
import com.sohu.cache.entity.ClientInstanceException;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.web.util.Page;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;
import com.sohu.tv.jedis.stat.enums.ClientCollectDataTypeEnum;
import com.sohu.tv.jedis.stat.enums.ClientExceptionType;
import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * 客户端上报异常service
 * 
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:32
 */
public class ClientReportExceptionServiceImpl implements ClientReportExceptionService {

    private final Logger logger = LoggerFactory.getLogger(ClientReportExceptionServiceImpl.class);

    /**
     * 客户端异常操作
     */
    private AppClientExceptionStatDao appClientExceptionStatDao;

    /**
     * host:port与instanceInfo简单缓存
     */
    private ClientReportInstanceService clientReportInstanceService;
    
    /**
     * 客户端ip,版本查询
     */
    private AppClientVersionDao appClientVersionDao;

    

    @Override
    public List<AppClientExceptionStat> getAppExceptionList(Long appId, long startTime, long endTime, int type,
            String clientIp, Page page) {
        try {
            return appClientExceptionStatDao.getAppExceptionList(appId, startTime, endTime, type, clientIp, page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public int getAppExceptionCount(Long appId, long startTime, long endTime, int type, String clientIp) {
        try {
            return appClientExceptionStatDao.getAppExceptionCount(appId, startTime, endTime, type, clientIp);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public List<ClientInstanceException> getInstanceExceptionStat(String ip, long collectTime) {
        try {
            return appClientExceptionStatDao.getInstanceExceptionStat(ip, collectTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void batchSave(ClientReportBean clientReportBean) {
        try {
            // 1.client上报
            final String clientIp = clientReportBean.getClientIp();
            final long collectTime = clientReportBean.getCollectTime();
            final long reportTime = clientReportBean.getReportTimeStamp();
            final List<Map<String, Object>> datas = clientReportBean.getDatas();
            if (datas == null || datas.isEmpty()) {
                logger.warn("datas field {} is empty", clientReportBean);
                return;
            }

            // 2.结果集
            List<AppClientExceptionStat> appClientExceptionStatList = new ArrayList<AppClientExceptionStat>();

            // 3.解析
            for (Map<String, Object> map : datas) {
                Integer clientDataType = MapUtils.getInteger(map, ClientReportConstant.CLIENT_DATA_TYPE, -1);
                ClientCollectDataTypeEnum clientCollectDataTypeEnum = ClientCollectDataTypeEnum.MAP.get(clientDataType);
                if (clientCollectDataTypeEnum == null) {
                    continue;
                }
                if (ClientCollectDataTypeEnum.EXCEPTION_TYPE.equals(clientCollectDataTypeEnum)) {
                    AppClientExceptionStat appClientExceptionStat = generate(clientIp, collectTime, reportTime, map);
                    if (appClientExceptionStat != null) {
                        appClientExceptionStatList.add(appClientExceptionStat);
                    }
                }
            }
            
            // 4.批量保存
            if (CollectionUtils.isNotEmpty(appClientExceptionStatList)) {
                appClientExceptionStatDao.batchSave(appClientExceptionStatList);
            }
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    private AppClientExceptionStat generate(String clientIp, long collectTime, long reportTime, Map<String, Object> map) {

        // 异常信息
        String exceptionClass = MapUtils.getString(map, ClientReportConstant.EXCEPTION_CLASS, "");
        Long exceptionCount = MapUtils.getLong(map, ClientReportConstant.EXCEPTION_COUNT, 0L);
        int exceptionType = MapUtils.getInteger(map, ClientReportConstant.EXCEPTION_TYPE, ClientExceptionType.REDIS_TYPE.getType());

        String host = null;
        Integer port = null;
        Integer instanceId = null;
        long appId;
        if (ClientExceptionType.REDIS_TYPE.getType() == exceptionType) {
            // 实例host:port
            String hostPort = MapUtils.getString(map, ClientReportConstant.EXCEPTION_HOST_PORT, "");
            if (StringUtils.isEmpty(hostPort)) {
                logger.warn("hostPort is empty", hostPort);
                return null;
            }
            int index = hostPort.indexOf(":");
            if (index <= 0) {
                logger.warn("hostPort {} format is wrong", hostPort);
                return null;
            }
            host = hostPort.substring(0, index);
            port = NumberUtils.toInt(hostPort.substring(index + 1));

            // 实例信息
            InstanceInfo instanceInfo = clientReportInstanceService.getInstanceInfoByHostPort(host, port);
            if (instanceInfo == null) {
//                logger.warn("instanceInfo is empty, host is {}, port is {}", host, port);
                return null;
            }
            // 实例id
            instanceId = instanceInfo.getId();
            // 应用id
            appId = instanceInfo.getAppId();
        } else {
            List<AppClientVersion> appClientVersion = appClientVersionDao.getByClientIp(clientIp);
            if (CollectionUtils.isNotEmpty(appClientVersion)) {
                appId = appClientVersion.get(0).getAppId();
            } else {
                appId = 0;
            }
        }

        // 组装AppClientExceptionStat
        AppClientExceptionStat stat = new AppClientExceptionStat();
        stat.setAppId(appId);
        stat.setClientIp(clientIp);
        stat.setReportTime(new Date(reportTime));
        stat.setCollectTime(collectTime);
        stat.setCreateTime(new Date());
        stat.setExceptionClass(exceptionClass);
        stat.setExceptionCount(exceptionCount);
        stat.setInstanceHost(host);
        stat.setInstancePort(port);
        stat.setInstanceId(instanceId);
        stat.setType(exceptionType);

        return stat;
    }

    public void setAppClientExceptionStatDao(AppClientExceptionStatDao appClientExceptionStatDao) {
        this.appClientExceptionStatDao = appClientExceptionStatDao;
    }

    public void setAppClientVersionDao(AppClientVersionDao appClientVersionDao) {
        this.appClientVersionDao = appClientVersionDao;
    }

    public void setClientReportInstanceService(ClientReportInstanceService clientReportInstanceService) {
        this.clientReportInstanceService = clientReportInstanceService;
    }

}
