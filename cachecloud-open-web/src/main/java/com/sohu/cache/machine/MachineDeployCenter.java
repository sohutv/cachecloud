package com.sohu.cache.machine;

import com.sohu.cache.entity.MachineInfo;

/**
 * 机器部署相关
 * @author leifu
 * changed @Date 2016-4-24
 * @Time 下午5:07:30
 */
public interface MachineDeployCenter {

    /**
     * 增加一台机器:入db和开启统计
     *
     * @param machineInfo
     */
    public boolean addMachine(MachineInfo machineInfo);

    /**
     * 移除一台机器：删db数据和关闭统计
     *
     * @param machineInfo
     */
    public boolean removeMachine(MachineInfo machineInfo);

}
