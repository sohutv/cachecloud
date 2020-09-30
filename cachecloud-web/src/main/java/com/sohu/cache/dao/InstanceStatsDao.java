package com.sohu.cache.dao;

import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.entity.MachineInstanceStat;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 实例统计相关DAO
 */
public interface InstanceStatsDao {

    public void updateInstanceStats(InstanceStats instanceStats);

    public InstanceStats getInstanceStatsByHost(@Param("ip") String ip, @Param("port") long port);

    public InstanceStats getInstanceStatsByInsId(@Param("id") long id);

    public List<InstanceStats> getInstanceStatsByAppId(@Param("appId") long appId);

    public List<InstanceStats> getInstanceStats();

    public List<InstanceStats> getInstanceStatsByIp(@Param("ip") String ip);

    Map getMachineMemByIp(@Param("ip") String ip);

    /**
     * <p>
     * Description:获取所有的memory
     * </p>
     * @author chenshi
     * @version 1.0
     * @date 2017/8/14
     */
    public Map<String, Object> getTotalMem();

    /**
     * <p>
     * Description:获取有效redis实例 master使用的memory
     * </p>
     * @author chenshi
     * @version 1.0
     * @date 2017/8/14
     */
    public Map<String, Object> getTotalAppMem(List<String> instancelist);

    /**
     * <p>
     * Description: 获取实例统计信息
     * </p>
     * @author chenshi
     * @version 1.0
     * @date 2019/2/22
     */
    public List<MachineInstanceStat> getMachineInstanceStatList();
}
