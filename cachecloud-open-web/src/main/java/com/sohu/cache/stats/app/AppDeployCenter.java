package com.sohu.cache.stats.app;

import com.sohu.cache.constant.DataFormatCheckResult;
import com.sohu.cache.constant.HorizontalResult;
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
     * 检测水平扩容节点
     * @param appAuditId
     * @param masterSizeSlave
     * @return
     */
    public DataFormatCheckResult checkHorizontalNodes(Long appAuditId, String masterSizeSlave);
    
    /**
     * 检查水平扩容的格式
     * @param appId
     * @param appAuditId
     * @param sourceId
     * @param targetId
     * @param startSlot
     * @param endSlot
     * @param migrateType
     * @return
     */
    public HorizontalResult checkHorizontal(long appId, long appAuditId, long sourceId, long targetId, int startSlot,
            int endSlot, int migrateType);
    
    
    /**
     * 开始水平扩容
     * @param appId
     * @param appAuditId
     * @param sourceId
     * @param targetId
     * @param startSlot
     * @param endSlot
     * @param migrateType
     * @return
     */
    public HorizontalResult startHorizontal(long appId, long appAuditId, long sourceId, long targetId, int startSlot,
            int endSlot, int migrateType);
    
    /**
     * 添加cluster一个主(从)节点
     *
     * @param appId
     * @param masterHost
     * @param slaveHost 从节点可为空
     * @param memory
     * @return
     */
    public boolean addHorizontalNodes(Long appId, String masterHost, String slaveHost, int memory);
    

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
