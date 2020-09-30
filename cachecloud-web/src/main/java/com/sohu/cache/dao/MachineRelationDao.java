package com.sohu.cache.dao;

import com.sohu.cache.entity.MachineRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by chenshi on 2019/5/21.
 */
public interface MachineRelationDao {

    /**
     * 保存pod实例变更状态
     *
     * @param machineRelation
     */
    public void saveOrUpdateMachineRelation(MachineRelation machineRelation);

    /**
     * 更新任务taskid
     */
    public void updateMachineRelation(@Param("id") int id, @Param("taskid") long taskid, @Param("is_sync") long is_sync);

    /**
     * 获取容器的trace记录
     *
     * @param ip
     */
    public List<MachineRelation> getRelationList(@Param("ip") String ip);

    /**
     * 获取pod online的容器记录
     * @param ip
     * @param status
     */
    public List<MachineRelation> getOnlinePodList(@Param("ip") String ip,@Param("status") int status);

    /**
     * 获取容器的trace记录
     *
     * @param ip
     * @param real_ip
     */
    public List<MachineRelation> getUnSyncRelationList(@Param("ip") String ip, @Param("real_ip") String real_ip);

    /**
     * 更新机器同步状态
     *
     * @param id
     * @param is_sync
     */
    public void updateMachineSyncStatus(@Param("id") int id, @Param("is_sync") int is_sync);

    /**
     * 检测任务的状态
     * @param ip
     * @param real_ip
     * @param is_sync
     * @return
     */
    public List<MachineRelation> getMachineSyncStatus(@Param("ip") String ip, @Param("real_ip") String real_ip, @Param("is_sync") long is_sync);

}
