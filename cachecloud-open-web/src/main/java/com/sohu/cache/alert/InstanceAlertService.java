package com.sohu.cache.alert;

import com.sohu.cache.entity.InstanceFault;

import java.util.List;

/**
 * 实例报警检测
 * @author leifu
 * @Date 2014年12月16日
 * @Time 下午1:56:35
 */
public interface InstanceAlertService {
    
    /**
     * 实例故障列表
     *
     * @param instId
     * @return
     */
    List<InstanceFault> getListByInstId(int instId);

}
