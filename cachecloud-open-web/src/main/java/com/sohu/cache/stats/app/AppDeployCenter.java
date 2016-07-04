package com.sohu.cache.stats.app;

import com.sohu.cache.constant.DataFormatCheckResult;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.redis.ReshardProcess;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * app相关发布操作
 * Created by yijunzhang on 14-10-20.
 */
public interface AppDeployCenter {

    /**
     * 新建应用
     *
     * @param appDesc
     * @param appUser
     * @param memSize
     */
    public boolean createApp(AppDesc appDesc, AppUser appUser, String memSize);

    /**
     * 为应用分配资源
     *
     * @param appAuditId
     * @param nodeInfoList <br/>格式=masterIp:空间:slaveIp
     * @param auditUser
     * @return
     */
    public boolean allocateResourceApp(Long appAuditId, List<String> nodeInfoList, AppUser auditUser);


    /**
     * 为应用分配的资源格式检测
     * @param appAuditId
     * @param appDeployText
     * @return
     */
    public DataFormatCheckResult checkAppDeployDetail(Long appAuditId, String appDeployText);

    /**
     * 下线应用
     *
     * @param appId
     * @return
     */
    public boolean offLineApp(Long appId);

    /**
     * 修改应用下节点配置
     *
     * @param appId
     * @param appAuditId
     * @param key
     * @param value
     * @return
     */
    public boolean modifyAppConfig(Long appId, Long appAuditId, String key, String value);

    /**
     * 垂直扩展
     *
     * @param appId
     * @param appAuditId
     * @param memory 单位MB
     * @return
     */
    public boolean verticalExpansion(Long appId, Long appAuditId, int memory);

    /**
     * 水平扩展(幂等操作)
     *
     * @param appId
     * @param host
     * @param por
     * @param appAuditId
     * @return
     */
    public boolean horizontalExpansion(Long appId, String host, int port, Long appAuditId);

    /**
     * 添加cluster一个主(从)节点
     *
     * @param appId
     * @param masterHost
     * @param slaveHost 从节点可为空
     * @param memory
     * @return
     */
    public boolean addAppClusterSharding(Long appId, String masterHost, String slaveHost, int memory);

    /**
     * 下线集群节点
     *
     * @param appId
     * @param host
     * @param port
     * @return
     */
    public boolean offLineClusterNode(Long appId, String host, int port);

    /**
     * 获取当前水平扩展进度列表
     *
     * @return
     */
    public ConcurrentMap<Long, ReshardProcess> getHorizontalProcess();
    
    
    /**
     * 清理应用数据
     * @param appId
     * @param appUser
     * @return
     */
    public boolean cleanAppData(long appId, AppUser appUser);

}
