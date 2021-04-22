package com.sohu.cache.machine;

import com.sohu.cache.constant.MachineInfoEnum.TypeEnum;
import com.sohu.cache.entity.*;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.web.enums.MachineMemoryDistriEnum;
import com.sohu.cache.web.vo.MachineStatsVo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 基于host的操作
 * User: lingguo
 */
public interface MachineCenter {

    /**
     * 收集host的状态信息
     *
     * @param hostId      机器id
     * @param collectTime 收集时间
     * @param ip          ip
     * @return 机器的信息
     */
    public Map<String, Object> collectMachineInfo(final long hostId, final long collectTime, final String ip);

    /**
     * 异步收集host的状态信息
     *
     * @param hostId      机器id
     * @param collectTime 收集时间
     * @param ip          ip
     */
    public void asyncCollectMachineInfo(final long hostId, final long collectTime, final String ip);

    /**
     * 监控机器的状态信息，向上层汇报或者报警
     *
     * @param hostId 机器id
     * @param ip     ip
     * @return
     */
    public void monitorMachineStats(final long hostId, final String ip);

    /**
     * 异步监控机器的状态信息，向上层汇报或者报警
     *
     * @param hostId 机器id
     * @param ip     ip
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

    String executeShell(final String ip, String shell, Integer timeout);

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
     *
     * @param ipLike
     * @return
     */
    public List<MachineStats> getMachineStats(String ipLike);

    /**
     * 获取机器列表
     *
     * @param ipLike
     * @param versionId
     * @param isInstall
     */
    public List<MachineStats> getMachineStats(String ipLike, Integer useType,Integer type, Integer versionId, Integer isInstall,Integer k8sType,String realip);

    /**
     * 获取全部机器列表
     *
     * @return
     */
    public List<MachineStats> getAllMachineStats();

    List<MachineMemStatInfo> getAllValidMachineMem(List<String> excludeMachineList, String room, Integer useType);

    List<MachineMemStatInfo> getValidMachineMemByIpList(List<String> ipList);

    /**
     * 根据ip获取机器信息
     *
     * @param ip
     * @return
     */
    public MachineInfo getMachineInfoByIp(String ip);


    MachineStats getMachineMemoryDetail(String ip);

    /**
     * 获取一台机器的所有实例
     *
     * @param ip
     * @return
     */
    List<InstanceInfo> getMachineInstanceInfo(String ip);


    /**
     * 获取一台机器的所有实例统计信息
     *
     * @param ip
     * @return
     */
    List<InstanceStats> getMachineInstanceStatsByIp(String ip);

    /**
     * 获取指定机器某个redis端口的最近日志
     *
     * @param maxLineNum
     * @return
     */
    String showInstanceRecentLog(InstanceInfo instanceInfo, int maxLineNum);

    /**
     * 根据机器类型获取机器列表
     *
     * @param typeEnum
     * @return
     */
    List<MachineInfo> getMachineInfoByType(TypeEnum typeEnum);

    /**
     * 获取机器下实例数map
     *
     * @return
     */
    public Map<String, Integer> getMachineInstanceCountMap();

    public Map<String,MachineInfo> getK8sMachineMap();

    /**
     * <p>
     * Description: 获取有效机房
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/10/16
     */
    public List<MachineRoom> getEffectiveRoom();

    List<MachineRoom> getAllRoom();

    /**
     * 机器使用内存分布
     * @return
     */
    public Map<MachineMemoryDistriEnum, Integer> getMaxMemoryDistribute();

    /**
     * 机器最大内存分布
     * @return
     */
    public Map<MachineMemoryDistriEnum, Integer> getUsedMemoryDistribute();

    /**
     * 获取机器性能统计分布
     * @return
     */
    public List<MachineStatsVo> getmachineStatsVoList();

    /**
	 * @param appId
	 * @param port
	 * @param instanceTypeEnum
	 * @return
	 */
	public String getInstanceRemoteBasePath(long appId, int port, InstanceTypeEnum instanceTypeEnum);

    /**
     * 获取机器的部署路径
     *  1.物理机/虚机/docker: /opt/cachecloud/conf  /opt/cachecloud/data /opt/cachecloud/logs
     *  1.k8s容器: /opt/cachecloud/conf/${host}/  /opt/cachecloud/data/${host}/ /opt/cachecloud/logs/${host}
     * @param host 机器ip
     * @return
     */
    public String getMachineRelativeDir(String host,int dirType);

    /**
     * 是否k8s机器
     * @param host 机器ip
     * @return  true：是  false：否
     */
    public Boolean isK8sMachine(String host);

    public Map<String,Object> getAllMachineEnv(Date searchDate,int type);

    public Map<String,Object> getExceptionMachineEnv(Date searchDate);

    /**
     * 获取机器列表的第一台机器资源
     * @return
     */
    public String getFirstMachineIp();

    public  List<MachineStats>  checkMachineModule(List<MachineStats> machineStatsList);

}
