package com.sohu.cache.machine;

import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineRelation;
import com.sohu.cache.web.enums.SuccessEnum;

import java.util.List;

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

    /**
     * 记录pod每次变更虚ip和宿主机关系
     */
    public void updateMachineRelation(int id, Long taskid, int is_sync);

    /**
     *  查询容器 pod每次变更节点
     */
    public List<MachineRelation> getMachineRelationList(String containerIp);

    /**
     * 检测是同步任务状态
     * @param containerIp
     * @param sourceIp
     * @return true:有同步中任务  false:没有同步中任务
     */
    public SuccessEnum checkMachineSyncStatus(String containerIp, String sourceIp, int is_sync);

}
