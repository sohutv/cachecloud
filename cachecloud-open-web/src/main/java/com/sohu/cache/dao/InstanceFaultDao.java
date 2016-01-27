package com.sohu.cache.dao;

import com.sohu.cache.entity.InstanceFault;

import java.util.List;

/**
 * Created by yijunzhang on 14-12-29.
 */
public interface InstanceFaultDao {

    /**
     * 添加InstanceFault实例
     *
     * @return
     */
    int insert(InstanceFault instanceFault);

    /**
     * 实例故障列表
     *
     * @param instId
     * @return
     */
    List<InstanceFault> getListByInstId(int instId);

    /**
     * 应用故障列表
     *
     * @param appId
     * @return
     */
    List<InstanceFault> getListByAppId(long appId);


}
