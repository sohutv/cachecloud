package com.sohu.cache.stats.app;

import com.sohu.cache.constant.ImportAppResult;
import com.sohu.cache.entity.AppDesc;

/**
 * 导入应用
 * @author leifu
 * @Date 2016-4-16
 * @Time 下午3:42:49
 */
public interface ImportAppCenter {

    /**
     * 检查应用和实例
     * 
     * @param appDesc
     * @param appInstanceInfo
     * @return
     */
    ImportAppResult check(AppDesc appDesc, String appInstanceInfo);

    /**
     * 导入应用和相关实例
     * 
     * @param appDesc
     * @param appInstanceInfo
     * @return
     */
    boolean importAppAndInstance(AppDesc appDesc, String appInstanceInfo);
}
