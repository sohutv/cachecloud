package com.sohu.cache.machine;

import java.util.List;
import java.util.Map;

import com.sohu.cache.constant.MachineInfoEnum.TypeEnum;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineStats;

/**
 * 基于host的操作
 *
 * User: lingguo
 * Date: 14-6-12
 * Time: 上午10:32
 */
public interface MachineCenter {

    /**
     * 为当前host创建trigger，并部署
     *
     * @param hostId    机器id
     * @param ip        ip
     * @return          是否部署成功
     */
    public boolean deployMachineCollection(final long hostId, final String ip);
    
    /**
     * 为当前host删除trigger,取消部署
     *
     * @param hostId    机器id
     * @param ip        ip
     * @return          是否取消部署成功
     */
    public boolean unDeployMachineCollection(final long hostId, final String ip);

    /**
     * 收集host的状态信息
     *
     * @param hostId        机器id
     * @param collectTime   收集时间
     * @param ip            ip
     * @return              机器的信息
     */
    public Map<String, Object> collectMachineInfo(final long hostId, final long collectTime, final String ip);
    
    /**
     * 异步收集host的状态信息
     *
     * @param hostId        机器id
     * @param collectTime   收集时间
     * @param ip            ip
     */
    public void asyncCollectMachineInfo(final long hostId, final long collectTime, final String ip);

    /**
     * 为当前机器的监控删除trigger
     *
     * @param hostId    机器id
     * @param ip    ip
     * @return      取消部署成功返回true， 否则返回false
     */
    public boolean unDeployMachineMonitor(final long hostId, final String ip);
    
    /**
     * 为当前机器的监控创建trigger
     *
     * @param hostId    机器id
     * @param ip    ip
     * @return      部署成功返回true， 否则返回false
     */
    public boolean deployMachineMonitor(final long hostId, final String ip);

    /**
     * 监控机器的状态信息，向上层汇报或者报警
     *
     * @param hostId    机器id
     * @param ip        ip
     * @return
     */
    public void monitorMachineStats(final long hostId, final String ip);
    
    /**
     * 异步监控机器的状态信息，向上层汇报或者报警
     *
     * @param hostId    机器id
     * @param ip        ip
     * @return
     */
    public void asyncMonitorMachineStats(final long hostId, final String ip);

    /**
     * 在主机ip上的端口port上启动一个进程，并check是否启动成功；
     *
     * @param ip    ip
     * @param port  端口
     * @param shell shell命令
     * @return 是否成功
     */
    public boolean startProcessAtPort(String ip, int port, final String shell);

    /**
     * 执行shell命令并获取返回结果
     *
     * @param ip
     * @param shell
     * @return
     */
    public String executeShell(final String ip, String shell);

    /**
     * 根据类型返回机器可用端口
     *
     * @param ip
     * @param type
     * @return
     */
    public Integer getAvailablePort(final String ip, final int type);

    /**
     * 创建远程文件
     *
     * @param host
     * @param fileName
     * @param content
     * @return 是否创建成功
     */
    public String createRemoteFile(final String host, String fileName, List<String> content);


    /**
     * 获取机器列表
     * @param ipLike
     * @return
     */
    public List<MachineStats> getMachineStats(String ipLike);
    
    /**
     * 获取全部机器列表
     * @return
     */
    public List<MachineStats> getAllMachineStats();

    /**
     * 根据ip获取机器信息
     * @param ip
     * @return
     */
    public MachineInfo getMachineInfoByIp(String ip);


    MachineStats getMachineMemoryDetail(String ip);
    
    /**
     * 获取一台机器的所有实例
     * @param ip
     * @return
     */
    List<InstanceInfo> getMachineInstanceInfo(String ip);
    
    
    /**
     * 获取一台机器的所有实例统计信息
     * @param ip
     * @return
     */
    List<InstanceStats> getMachineInstanceStatsByIp(String ip);

    /**
     * 获取指定机器某个redis端口的最近日志
     * @param maxLineNum
     * @return
     */
    String showInstanceRecentLog(InstanceInfo instanceInfo, int maxLineNum);

    /**
     * 根据机器类型获取机器列表
     * @param typeEnum
     * @return
     */
    List<MachineInfo> getMachineInfoByType(TypeEnum typeEnum);
    
    /**
     * 为当前ip创建trigger，并部署
     *
     * @param hostId    机器id
     * @param ip        ip
     * @return          是否部署成功
     */
    public boolean deployServerCollection(long hostId, String ip);
    
    /**
     * 为当前服务器状态收集删除trigger
     * @param hostId    机器id
     * @param ip    ip
     * @return      取消部署成功返回true， 否则返回false
     */
    public boolean unDeployServerCollection(final long hostId, final String ip);

    /**
     * 获取机器下实例数map
     * @return
     */
    public Map<String, Integer> getMachineInstanceCountMap();
    
}
