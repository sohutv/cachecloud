package com.sohu.cache.alert;

import com.sohu.cache.entity.InstanceFault;

import java.util.List;

/**
 * 应用报警
 * @author leifu
 * @Date 2014年12月16日
 * @Time 下午2:51:51
 */
public interface AppAlertService {
	
    /**
     * 应用故障列表
     *
     * @param appId
     * @return
     */
    List<InstanceFault> getListByAppId(long appId);
}
