package com.sohu.cache.stats.instance;

import com.sohu.cache.entity.InstanceCommandStats;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;

import java.util.List;
import java.util.Map;

/**
 * 实例统计方法
 * Created by yijunzhang on 14-9-17.
 */
public interface InstanceStatsCenter {

    /**
     * 根据实例id获取实例的静态信息
     *
     * @param instanceId 实例id
     * @return 实例信息对象
     */
    InstanceInfo getInstanceInfo(long instanceId);

    /**
     * 根据实例id获取实例的实施统计&是否在线&redis实时统计信息
     *
     * @param instanceId 实例id
     * @return 实例实时状态信息
     */
    InstanceStats getInstanceStats(long instanceId);

    /**
     * 根据实例id，起止时间，命令名称获取实例对应命令的执行次数曲线
     *
     * @param instanceId  实例id
     * @param beginTime   起始时间 闭区间 yyyyMMddHHmm
     * @param endTime     结束时间 闭区间 yyyyMMddHHmm
     * @param commandName 命令名称
     * @return 实例命令执行对象的列表
     */
    List<InstanceCommandStats> getCommandStatsList(Long instanceId, long beginTime, long endTime, String commandName);

    /**
     * 根据appId，起止时间，统计指标名称 获取实例对应指标统计列表
     *
     * @param appId  实例id
     * @param beginTime   起始时间 闭区间 yyyyMMddHHmm
     * @param endTime     结束时间 闭区间 yyyyMMddHHmm
     * @param commands 命令名称
     * @return 应用下 所有实例 命令执行对象的列表
     * <br/>
     * Map<Integer, Map<String, List<InstanceCommandStats>>> : Map<InstanceId,Map<CommandName,List<InstanceCommandStats>>>
     */
    Map<Integer, Map<String, List<InstanceCommandStats>>> getStandardStatsList(Long appId, long beginTime, long endTime,
            List<String> commands);

    /**
     * 在实例上执行命令
     *
     * @param host
     * @param port
     * @param command
     * @return
     */
    public String executeCommand(String host, int port, String command);

    /**
     * 在实例上执行命令
     *
     * @param instanceId
     * @param command
     * @return
     */
    public String executeCommand(Long instanceId, String command);

    /**
     * 获取所有统计信息
     * @return
     */
    public List<InstanceStats> getInstanceStats();

    /**
     * 按照机器获取实例列表
     * @param ip
     * @return
     */
    List<InstanceStats> getInstanceStats(String ip);

    /**
     * 将一个对象（如统计信息）保存到mysql里
     *
     * @param infoMap 统计信息对象
     * @param clusterInfoMap clusterinfo统计信息
     * @param ip      ip
     * @param port    port
     * @param dbType
     * @return 成功保存返回true，否则返回false
     */
    public boolean saveStandardStats(Map<String, Object> infoMap, Map<String, Object> clusterInfoMap, String ip, int port, String dbType);

    /**
     * 根据收集时间查询某一个实例或机器的统计信息
     *
     * @param collectTime 时间点，格式：yyyyMMddHHmm
     * @param ip          ip
     * @param port        port
     * @param dbType
     * @return 该时间点对应的统计信息
     */
    public Map<String, Object> queryStandardInfoMap(long collectTime, String ip, int port, String dbType);

    /**
     * 查询一段时间内，实例或机器的统计信息的列表，如实例或机器一天中每分钟的统计数据
     *
     * @param beginTime 起始时间，格式：yyyyMMddHHmm
     * @param endTime   结束时间，格式：yyyyMMddHHmm
     * @param ip        ip
     * @param port      port
     * @param dbType
     * @return 该时间区间内，对应的统计信息的列表
     */
    public List<Map<String, Object>> queryDiffMapList(long beginTime, long endTime, String ip, int port, String dbType);

    /**
     * 清理{day}之前的数据
     *
     * @param day
     */
    public void cleanUpStandardStats(int day);

}
