package com.sohu.cache.dao;

import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineMemStatInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 机器相关的操作
 * <p>
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
     * 通过ip&版本安装情况机器信息
     *
     * @param ipLike
     * @param versionStr
     * @return
     */
    public List<MachineInfo> getMachineInfoByCondition(@Param("ipLike") String ipLike, @Param("useType") int useType, @Param("type") int type, @Param("versionStr") String versionStr, @Param("k8sType") int k8sType, @Param("realip") String realip);

    /**
     * 通过ip模糊查找机器
     * @param ipLike
     * @param realIpLike
     * @return
     */
    public List<MachineInfo> getMachineListByCondition(@Param("ipLike") String ipLike, @Param("realIpLike") String realIpLike);

    /**
     * @Description: 通过room&useType查询机器信息
     * @Author: caoru
     * @CreateDate: 2018/10/12 11:39
     */
    List<MachineMemStatInfo> getMachineMemStatInfoByCondition(@Param("room") String room, @Param("useType") int useType);

    List<MachineMemStatInfo> getMachineMemStatInfoByIpList(@Param("list") List list);

    /**
     * 通过ip模糊查询机器信息
     *
     * @param ipLike
     * @return
     */
    public List<MachineInfo> getMachineInfoByLikeIp(@Param("ipLike") String ipLike);

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
     * 根据ip更新物理机ip
     *
     * @param real_ip
     * @param ip
     * @return
     */
    int updateMachineRealIpByIp(@Param("real_ip") String real_ip, @Param("ip") String ip);

    /**
     * k8s容器：ip+k8s_type
     *
     * @param ip
     * @return
     */
    MachineInfo existk8sMachine(@Param("ip") String ip);

    /**
     * 获取k8s机器的信息
     *
     * @return
     */
    List<MachineInfo> getK8sMachineList();

    /**
     * 获取k8s容器的信息
     *
     * @return
     */
    List<MachineInfo> getMachineList();

    /**
     * 通过type查询机器列表
     *
     * @param type
     * @return
     */
    public List<MachineInfo> getMachineInfoByType(@Param("type") int type);

    /**
     * <p>
     * Description:获取所有机器的内存统计情况
     * </p>
     *
     * @param
     * @return
     * @author chenshi
     * @version 1.0
     * @date 2017/8/14
     */
    public Map<String, Object> getTotalMachineMem();

    /**
     * <p>
     * Description: 获取机房分布情况
     * </p>
     *
     * @param
     * @return
     * @author chenshi
     * @version 1.0
     * @date 2019/2/27
     */
    public List<Map<String, Object>> getRoomStat();

    /**
     * 更新机器分配状态
     *
     * @param ip
     * @param status
     */
    public void updateMachineAllocate(@Param("ip") String ip, @Param("status") int status);

}
