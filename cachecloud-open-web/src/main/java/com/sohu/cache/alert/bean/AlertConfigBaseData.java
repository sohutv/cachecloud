package com.sohu.cache.alert.bean;

import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.StandardStats;

/**
 * 报警基础数据
 * @author leifu
 * @Date 2017年6月16日
 * @Time 下午2:19:10
 */
public class AlertConfigBaseData {
    /**
     * 基准数据
     */
    private StandardStats standardStats;
    
    /**
     * 实例信息
     */
    private InstanceInfo instanceInfo;

    public StandardStats getStandardStats() {
        return standardStats;
    }

    public void setStandardStats(StandardStats standardStats) {
        this.standardStats = standardStats;
    }

    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }

    public void setInstanceInfo(InstanceInfo instanceInfo) {
        this.instanceInfo = instanceInfo;
    }
    
}
