package com.sohu.cache.stats.instance;

/**
 * Created by yijunzhang on 14-11-26.
 */
public interface InstanceDeployCenter {

    /**
     * 启动已经存在的实例
     * @param instanceId
     * @return
     */
    boolean startExistInstance(int instanceId);

    /**
     * 下线已经存在的实例
     * @param instanceId
     * @return
     */
    boolean shutdownExistInstance(int instanceId);
    
    
    /**
     * 展示实例最近的日志
     * @param instanceId
     * @param maxLineNum
     * @return
     */
    String showInstanceRecentLog(int instanceId, int maxLineNum);

    /**
     * 修改实例配置
     * @param appAuditId
     * @param host
     * @param port
     * @param instanceConfigKey
     * @param instanceConfigValue
     * @return
     */
    boolean modifyInstanceConfig(Long appAuditId, String host, int port, String instanceConfigKey,
            String instanceConfigValue);

}
