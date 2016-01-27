package com.sohu.cache.stats.machine;

import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.MachineInfo;

import java.util.List;

/**
 * 基于机器进行数据统计的接口
 *
 * Created by lingguo on 14-6-27.
 */
public interface MachineStatsCenter {

    /**
     * 通过ip查询机器的配置等信息
     *
     * @param ip
     * @return
     */
    public MachineInfo getMachineInfoByIp(final String ip);

    /**
     * 查询该机器上的所有实例的信息
     *
     * @param ip
     * @return
     */
    public List<InstanceInfo> getInstInfoOfMachine(final String ip);

}
