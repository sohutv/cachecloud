package com.sohu.cache.web.service;

import java.util.List;

import com.sohu.cache.entity.InstanceFault;

/**
 * 故障服务
 * @author leifu
 * @Date 2015-6-6
 * @Time 下午10:03:38
 */
public interface MemFaultService {
    
    /**
     * 获取故障列表
     * @return
     */
    List<InstanceFault> getFaultList();

}
