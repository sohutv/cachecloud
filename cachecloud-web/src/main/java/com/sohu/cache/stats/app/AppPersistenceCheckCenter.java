package com.sohu.cache.stats.app;

/**
 * Description: 应用持久化配置检查修正
 * @author zengyizhao
 * @version 1.0
 * @date 2022/11/3
 */
public interface AppPersistenceCheckCenter {

    /**
     * 发送所有应用日报
     */
    void checkAndFixAppPersistence();
    
}
