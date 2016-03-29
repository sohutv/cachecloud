package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppClientExceptionStat;
import com.sohu.cache.entity.ClientInstanceException;
import com.sohu.cache.web.util.Page;

/**
 * 客户端异常dao
 * 
 * @author leifu
 * @Date 2015年1月20日
 * @Time 上午11:50:06
 */
public interface AppClientExceptionStatDao {

    /**
     * 保存上报异常
     * 
     * @param appClientExceptionStat
     */
    void save(AppClientExceptionStat appClientExceptionStat);

    /**
     * 获取客户端异常列表
     * 
     * @param appId 应用id
     * @param startTime 开始收集时间
     * @param endTime 结束收集时间
     * @param type 异常类型(ClientExceptionType)
     * @param clientIp 客户端ip
     * @return
     */
    List<AppClientExceptionStat> getAppExceptionList(@Param("appId") Long appId, @Param("startTime") long startTime,
            @Param("endTime") long endTime, @Param("type") int type, @Param("clientIp") String clientIp, @Param("page") Page page);

    /**
     * 获取客户端异常个数
     * 
     * @param appId 应用id
     * @param startTime 开始收集时间
     * @param endTime 结束收集时间
     * @param type 异常类型(ClientExceptionType)
     * @param clientIp 客户端ip
     * @return
     */
    int getAppExceptionCount(@Param("appId") Long appId, @Param("startTime") long startTime,
            @Param("endTime") long endTime, @Param("type") int type, @Param("clientIp") String clientIp);

    /**
     * 大于collectTime后实例的异常统计
     * @param ip
     * @param collectTime
     * @return
     */
    List<ClientInstanceException> getInstanceExceptionStat(@Param("ip") String ip, @Param("collectTime") long collectTime);

    /**
     * 批量保存
     * @param appClientExceptionStatList
     * @return
     */
    int batchSave(@Param("appClientExceptionStatList") List<AppClientExceptionStat> appClientExceptionStatList);

}
