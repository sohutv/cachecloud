package com.sohu.cache.stats.app;

import com.sohu.cache.constant.AppTopology;
import com.sohu.cache.constant.TimeDimensionalityEnum;
import com.sohu.cache.entity.*;
import com.sohu.cache.web.vo.AppDetailVO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * app级别的查询、统计和汇总
 * <p>
 * Created by lingguo on 14-6-26.
 */
public interface AppStatsCenter {

    /**
     * 通过时间区间查询app的分钟统计数据
     *
     * @param appId
     * @param beginTime 时间，格式：yyyyMMddHHmm
     * @param endTime   时间，格式：yyyyMMddHHmm
     * @return
     */
    public List<AppStats> getAppStatsListByMinuteTime(final long appId, long beginTime, long endTime);

    /**
     * 通过时间区间查询app的分钟统计数据
     *
     * @param appId
     * @param beginTime 时间，格式：yyyyMMddHHmm
     * @param endTime   时间，格式：yyyyMMddHHmm
     * @return
     */
    public List<AppStats> getAppStatsList(final long appId, long beginTime, long endTime, TimeDimensionalityEnum timeDimensionalityEnum);

    /**
     * 查询一天中应用的命令执行次数的top5
     *
     * @param appId 应用id
     * @param begin 日期格式：yyyyMMddHHmm
     * @param end   日期格式：yyyyMMddHHmm
     * @return
     */
    public List<AppCommandStats> getTop5AppCommandStatsList(final long appId, long begin, long end);

    /**
     * 查询一天中应用的命令执行次数的top5
     *
     * @param appId 应用id
     * @param begin 日期格式：yyyyMMddHHmm
     * @param end   日期格式：yyyyMMddHHmm
     * @return
     */
    public List<AppCommandStats> getTopLimitAppCommandStatsList(final long appId, long begin, long end, int limit);

    /**
     * 查询应用的配置和节点信息
     *
     * @param appId
     * @return
     */
    public Map<AppTopology, Object> queryAppTopology(final long appId);


    /**
     * 查询应用指定时间段，指定命令名的结果集合
     *
     * @param appId       应用id
     * @param beginTime   时间，格式：yyyyMMddHHmm
     * @param endTime     时间，格式：yyyyMMddHHmm
     * @param commandName 命令名
     * @return
     */
    public List<AppCommandStats> getCommandStatsListV2(long appId, long beginTime, long endTime, TimeDimensionalityEnum timeDimensionalityEnum, String commandName);

    /**
     * 查询应用指定时间段，指定命令名的结果集合
     *
     * @param appId       应用id
     * @param beginTime   时间，格式：yyyyMMddHHmm
     * @param endTime     时间，格式：yyyyMMddHHmm
     * @param commandName 命令名
     * @return
     */
    public List<AppCommandStats> getCommandStatsList(long appId, long beginTime, long endTime, String commandName);

    /**
     * 查询应用指定时间段，指定命令名的结果集合
     *
     * @param appId     应用id
     * @param beginTime 时间，格式：yyyyMMddHHmm
     * @param endTime   时间，格式：yyyyMMddHHmm
     * @return
     */
    public List<AppCommandStats> getCommandStatsList(long appId, long beginTime, long endTime);

    /**
     * 查询应用指定时间段，指定命令名的结果集合
     *
     * @param appId     应用id
     * @param beginTime 时间，格式：yyyyMMddHHmm
     * @param endTime   时间，格式：yyyyMMddHHmm
     * @return
     */
    public List<AppCommandStats> getCommandStatsListV2(long appId, long beginTime, long endTime, TimeDimensionalityEnum timeDimensionalityEnum);

    /**
     * 查询应用指定命令的峰值
     *
     * @param appId       应用id
     * @param beginTime   时间，格式：yyyyMMddHHmm
     * @param endTime     时间，格式：yyyyMMddHHmm
     * @param commandName 命令名
     * @return
     */
    public AppCommandStats getCommandClimax(long appId, Long beginTime, Long endTime, String commandName);

    /**
     * 获取应用详细信息
     *
     * @param appId
     * @return
     */
    public AppDetailVO getAppDetail(long appId);

    /**
     * 通过所有在线的应用详细信息
     * @return
     */
    Map<Long,AppDetailVO> getOnlineAppDetails();

    /**
     * 获取所有在线应用的应用客户端连接总数
     * @return
     */
    List<AppClientStatisticGather> getOnlineAppConnClients();

    

    /**
     * 获取应用命令调用次数分布
     *
     * @param appId
     * @param beginTime
     * @param endTime
     * @return
     */
    public List<AppCommandGroup> getAppCommandGroup(long appId, Long beginTime, Long endTime);

    /**
     * 在appId级别执行命令
     *
     * @param appId
     * @param command
     * @return
     */
    public String executeCommand(long appId, String command);


    /**
     * 按照appId获取实例所有慢查询日志
     *
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<InstanceSlowLog> getInstanceSlowLogByAppId(long appId, Date startDate, Date endDate);


    Map<String, List<Map<String, Object>>> getAppLatencyStats(long appId, long startTime, long endTime);

    Map<String, Long> getAppLatencyStatsGroupByInstance(long appId, long startTime, long endTime);

    Map<String, List<Map<String, Object>>> getAppLatencyInfo(long appId, long startTime, long endTime);

    List<InstanceSlowLog> getByInstanceExecuteTime(long instanceId, String executeDate);

    /**
     * 按照appId获取每个实例慢查询个数
     *
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<String, Long> getInstanceSlowLogCountMapByAppId(Long appId, Date startDate, Date endDate);

    /**
     * <p>
     * Description:获取总览相关统计数
     * </p>
     *
     * @param
     * @return
     * @author chenshi
     * @version 1.0
     * @date 2017/8/14
     */
    public Map<String, Object> getAppTotalStat();

}
