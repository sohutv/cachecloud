package com.sohu.cache.machine;

import com.sohu.cache.entity.MachineInfo;

import java.util.List;

/**
 * 对机器的操作，包括：
 *  - 增添/移除机器；
 *  - 批量增添/移除机器；
 *  - 将初始化脚本推送到机器上；
 *
 * @author: lingguo
 * @time: 2014/8/29 14:31
 */
public interface MachineDeployCenter {

    /**
     * 增加一台机器，并初始化，包括：
     *  - 将机器信息保存入库；
     *  - 加入定时监控任务中；
     *
     * @param machineInfo
     */
    public boolean addMachine(MachineInfo machineInfo);

    /**
     * 批量增添机器
     *
     * @param machineInfoList
     */
    public boolean addAllMachines(List<MachineInfo> machineInfoList);

    /**
     * 移除一台机器
     *
     * @param machineInfo
     */
    public boolean removeMachine(MachineInfo machineInfo);

    /**
     * 批量移除机器
     *
     * @param machineInfoList
     */
    public boolean removeAllMachines(List<MachineInfo> machineInfoList);

    /**
     * 为一个主从实例选择合适的机器
     *
     * @param maxMemory     实例的内存
     * @param pairHostId    主从不能在同一台机器上：如果当前实例为主，则pairId为对应从实例的hostId；
     *                      如果当前实例为从，则pairId为对应主实例的hostId；如果是第一次，则pairHostId为null；
     * @param excludeHostIds  需要排除的机器列表，会尽可能地排除；
     * @param groupId       分组id
     * @return              实例的hostId，如果没有可用的机器，返回null；
     */
    public Long chooseBestMachine(long maxMemory, Long pairHostId, List<Long> excludeHostIds, int groupId);
}
