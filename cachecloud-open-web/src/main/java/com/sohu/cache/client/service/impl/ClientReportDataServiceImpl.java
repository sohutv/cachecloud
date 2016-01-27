package com.sohu.cache.client.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.client.service.ClientReportDataService;
import com.sohu.cache.client.service.ClientReportDataExecuteService;
import com.sohu.tv.jedis.stat.constant.ClientReportConstant;
import com.sohu.tv.jedis.stat.enums.ClientCollectDataTypeEnum;
import com.sohu.tv.jedis.stat.model.ClientReportBean;

/**
 * cachecloud客户端数据统一处理
 * 
 * @author leifu
 * @Date 2015年1月19日
 * @Time 上午10:02:01
 */
public class ClientReportDataServiceImpl implements ClientReportDataService {
    public static final String CLIENT_REPORT_POOL ="client-report-pool";
    
    private AsyncService asyncService;
    
    private ClientReportDataExecuteService clientReportDataSizeService;
    
	private final Logger logger = LoggerFactory.getLogger(ClientReportDataServiceImpl.class);

    /**
	 * 上报数据类型对应不同的处理逻辑
	 */
	private Map<ClientCollectDataTypeEnum, ClientReportDataExecuteService> clientReportServiceMap;

	public void init() {
        asyncService.assemblePool(getThreadPoolKey(), AsyncThreadPoolFactory.CLIENT_REPORT_THREAD_POOL);
    }
    
    private String getThreadPoolKey() {
        return CLIENT_REPORT_POOL;
    }
	
	@Override
	public boolean deal(ClientReportBean clientReportBean) {
		try {
			// 上报的数据
			final String clientIp = clientReportBean.getClientIp();
			final long collectTime = clientReportBean.getCollectTime();
			final long reportTime = clientReportBean.getReportTimeStamp();
			final List<Map<String, Object>> datas = clientReportBean.getDatas();
			final Map<String, Object> otherInfo = clientReportBean.getOtherInfo();
			if (datas == null || datas.isEmpty()) {
				logger.warn("datas field {} is empty", clientReportBean);
				return false;
			}
			String key = getThreadPoolKey() + "_" + clientIp;
			asyncService.submitFuture(getThreadPoolKey(), new KeyCallable<Boolean>(key) {
                @Override
                public Boolean execute() {
                    // 根据不同的数据类型映射不同处理逻辑
                    for (Map<String, Object> map : datas) {
                        Integer clientDataType = MapUtils.getInteger(map, ClientReportConstant.CLIENT_DATA_TYPE, -1);
                        ClientCollectDataTypeEnum clientCollectDataTypeEnum = ClientCollectDataTypeEnum.MAP.get(clientDataType);
                        if (clientCollectDataTypeEnum != null) {
                            clientReportServiceMap.get(clientCollectDataTypeEnum).execute(clientIp, collectTime, reportTime, map);
                        }
                    }
                    // 处理其他信息
                    if (MapUtils.isNotEmpty(otherInfo)) {
                        clientReportDataSizeService.execute(clientIp, collectTime, reportTime, otherInfo);
                    }
                    return true;
                }
            });
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	public void setClientReportServiceMap(Map<ClientCollectDataTypeEnum, ClientReportDataExecuteService> clientReportServiceMap) {
		this.clientReportServiceMap = clientReportServiceMap;
	}

    public void setAsyncService(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    public void setClientReportDataSizeService(ClientReportDataExecuteService clientReportDataSizeService) {
        this.clientReportDataSizeService = clientReportDataSizeService;
    }

}
