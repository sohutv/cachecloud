package com.sohu.cache.stats.instance;

import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.task.constant.MachineSyncEnum;

import java.util.List;

/**
 * Created by yijunzhang on 14-11-26.
 */
public interface InstanceDeployCenter {

    /**
     * 启动已经存在的实例
     * @param appId
     * @param instanceId
     * @return
     */
    boolean startExistInstance(long appId, int instanceId);

    /**
     * 启动已存在的实例，无需进行redis资源包校验
     * @param appId
     * @param instanceId
     * @return
     */
    boolean startExistInstanceWithoutResourceCheck(long appId, int instanceId);

    /**
     * 下线已经存在的实例
     * @param appId
     * @param instanceId
     * @return
     */
    boolean shutdownExistInstance(long appId, int instanceId);

    /**
     * cluster forget
     * @param appId
     * @param instanceId
     * @return
     */
    boolean forgetInstance(long appId, int instanceId);

    /**
     * 清理（cluster forget）集群内所有fail节点
     * @param appId
     * @param instanceId
     * @return
     */
    boolean clearFailInstances(long appId);
    
    /**
     * 展示实例最近的日志
     * @param instanceId
     * @param maxLineNum
     * @return
     */
    String showInstanceRecentLog(int instanceId, int maxLineNum);

    /**
     * 修改实例配置
     * @param appId
     * @param appAuditId
     * @param host
     * @param port
     * @param instanceConfigKey
     * @param instanceConfigValue
     * @return
     */
    boolean modifyInstanceConfig(long appId, Long appAuditId, String host, int port, String instanceConfigKey,
            String instanceConfigValue);

    /**
     * 检测pod是否有被调度其他宿主机
     * @param ip
     */
    MachineSyncEnum podChangeStatus(String ip);

    /**
     * 检测pod是否有心跳停止实例&启动
     * @return
     */
    List<InstanceAlertValueResult> checkAndStartExceptionInstance(String ip, Boolean isAlert);

}
