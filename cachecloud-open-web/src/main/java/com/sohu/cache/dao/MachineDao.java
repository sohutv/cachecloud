package com.sohu.cache.dao;

import com.sohu.cache.entity.MachineInfo;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 机器相关的操作
 *
 * User: lingguo
 * Date: 14-6-12
 * Time: 下午2:33
 */
public interface MachineDao {

    /**
     * 返回所有可用的机器资源
     *
     * @return
     */
    public List<MachineInfo> getAllMachines();

    /**
     * 通过ip查询机器信息
     *
     * @param ip
     * @return
     */
    public MachineInfo getMachineInfoByIp(@Param("ip") String ip);
    
    /**
     * 通过ip模糊查询机器信息
     * @param ipLike
     * @return
     */
    public List<MachineInfo> getMachineInfoByLikeIp(@Param("ipLike")String ipLike);


    /**
     * 保存一条机器信息
     *
     * @param machineInfo
     */
    public void saveMachineInfo(MachineInfo machineInfo);

    /**
     * 根据ip删除一台机器的信息；
     *
     * @param ip
     */
    public void removeMachineInfoByIp(@Param("ip") String ip);
    
    /**
     * 通过type查询机器列表
     * @param type
     * @return
     */
    public List<MachineInfo> getMachineInfoByType(@Param("type") int type);
    
    /**
     * 更新机器type
     * @return
     */
    public int updateMachineType(@Param("id") long id, @Param("type") int type);

}
