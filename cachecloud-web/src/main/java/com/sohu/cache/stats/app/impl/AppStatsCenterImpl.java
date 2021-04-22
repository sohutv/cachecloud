package com.sohu.cache.stats.app.impl;

import com.google.common.collect.Maps;
import com.sohu.cache.constant.AppTopology;
import com.sohu.cache.constant.TimeDimensionalityEnum;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.PandectUtil;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.MachineMemoryDistriEnum;
import com.sohu.cache.web.enums.StatEnum;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.vo.AppDetailVO;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于app的统计信息的接口：包括app详情、app配置以及基于app的统计
 *
 * @author leifu
 * @Date 2015年3月2日
 * @Time 下午1:50:09
 */
@Service("appStatsCenter")
public class AppStatsCenterImpl implements AppStatsCenter {

    private final static String COLLECT_DATE_FORMAT = "yyyyMMddHHmm";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private AppDao appDao;
    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private MachineDao machineDao;
    @Autowired
    private AppStatsDao appStatsDao;
    @Autowired
    private InstanceLatencyHistoryDao instanceLatencyHistoryDao;
    @Autowired
    private InstanceSlowLogDao instanceSlowLogDao;
    @Autowired
    private InstanceStatsDao instanceStatsDao;
    @Autowired
    @Lazy
    private RedisCenter redisCenter;
    @Autowired
    private UserService userService;
    @Autowired
    @Lazy
    private MachineCenter machineCenter;
    @Autowired
    private ResourceDao resourceDao;

    @Override
    public List<AppStats> getAppStatsListByMinuteTime(long appId, long beginTime, long endTime) {
        Assert.isTrue(appId > 0);
        Assert.isTrue(beginTime > 0 && endTime > 0);

        List<AppStats> appStatsList = null;
        try {
            appStatsList = appStatsDao.getAppStatsList(appId, new TimeDimensionality(beginTime, endTime, COLLECT_DATE_FORMAT));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return appStatsList;
    }

    /**
     * 通过时间区间查询app的分钟统计数据
     *
     * @param appId
     * @param beginTime 时间，格式：yyyyMMddHHmm
     * @param endTime   时间，格式：yyyyMMddHHmm
     * @return
     */
    @Override
    public List<AppStats> getAppStatsList(final long appId, long beginTime, long endTime, TimeDimensionalityEnum timeDimensionalityEnum) {
        Assert.isTrue(appId > 0);
        Assert.isTrue(beginTime > 0 && endTime > 0);

        List<AppStats> appStatsList = null;
        try {
            if (TimeDimensionalityEnum.MINUTE.equals(timeDimensionalityEnum)) {
                appStatsList = appStatsDao.getAppStatsByMinute(appId, beginTime, endTime);
            } else if (TimeDimensionalityEnum.HOUR.equals(timeDimensionalityEnum)) {
                appStatsList = appStatsDao.getAppStatsByHour(appId, beginTime, endTime);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return appStatsList;
    }

    @Override
    public List<AppCommandStats> getTop5AppCommandStatsList(final long appId, long begin, long end) {
        Assert.isTrue(appId > 0);
        Assert.isTrue(begin > 0L);
        Assert.isTrue(end > 0L);

        List<AppCommandStats> topAppCmdList = null;
        try {
            topAppCmdList = appStatsDao.getTopAppCommandGroupSum(appId, new TimeDimensionality(begin, end, COLLECT_DATE_FORMAT), 5);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return topAppCmdList;
    }

    @Override
    public List<AppCommandStats> getTopLimitAppCommandStatsList(long appId, long begin, long end, int limit) {
        Assert.isTrue(appId > 0);
        Assert.isTrue(begin > 0L);
        Assert.isTrue(end > 0L);

        List<AppCommandStats> topAppCmdList = null;
        try {
            topAppCmdList = appStatsDao.getTopAppCommandStatsList(appId, new TimeDimensionality(begin, end, COLLECT_DATE_FORMAT), limit);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return topAppCmdList;
    }

    /**
     * 查询应用的配置和节点信息
     *
     * @param appId
     * @return
     */
    @Override
    public Map<AppTopology, Object> queryAppTopology(final long appId) {
        Assert.isTrue(appId > 0);

        Map<AppTopology, Object> appTopologyMap = new HashMap<AppTopology, Object>();
        AppDesc appDesc = null;
        double totalMemory = 0.0;
        Set<Long> machineSet = new HashSet<Long>();
        int masterCount = 0;
        int slaveCount = 0;

        List<InstanceInfo> instanceInfoList = null;
        try {
            appDesc = appDao.getAppDescById(appId);
            instanceInfoList = instanceDao.getInstListByAppId(appId);
            if (appDesc == null || instanceInfoList == null || instanceInfoList.isEmpty()) {
                logger.error("get app and it's instances error， appId = {}", appId);
                return null;
            }
            if (appDesc.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                for (InstanceInfo instance : instanceInfoList) {
                    machineSet.add(instance.getHostId());
                    totalMemory += instance.getMem();
                    BooleanEnum isMaster = redisCenter.isMaster(appId, instance.getIp(), instance.getPort());
                    if (isMaster == BooleanEnum.OTHER) {
                        continue;
                    }
                    if (isMaster == BooleanEnum.TRUE) {
                        masterCount++;
                    } else {
                        slaveCount++;
                    }
                }
            }
            appTopologyMap.put(AppTopology.TOTAL_MEMORY, totalMemory / ConstUtils._1024);
            appTopologyMap.put(AppTopology.MACHINE_COUNT, machineSet.size());
            appTopologyMap.put(AppTopology.MASTER_COUNT, masterCount);
            appTopologyMap.put(AppTopology.SLAVE_COUNT, slaveCount);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return appTopologyMap;
    }

    /**
     * 查询应用指定时间段，指定命令名的结果集合
     *
     * @param appId       应用id
     * @param beginTime   时间，格式：yyyyMMddHHmm
     * @param endTime     时间，格式：yyyyMMddHHmm
     * @param commandName 命令名
     * @return
     */
    @Override
    public List<AppCommandStats> getCommandStatsList(long appId, long beginTime, long endTime, String commandName) {
        return appStatsDao.getAppCommandStatsList(appId, commandName, new TimeDimensionality(beginTime, endTime, COLLECT_DATE_FORMAT));
    }

    /**
     * 查询应用指定时间段，指定命令名的结果集合
     *
     * @param appId     应用id
     * @param beginTime 时间，格式：yyyyMMddHHmm
     * @param endTime   时间，格式：yyyyMMddHHmm
     * @return
     */
    @Override
    public List<AppCommandStats> getCommandStatsList(long appId, long beginTime, long endTime) {
        return appStatsDao.getAppAllCommandStatsList(appId, new TimeDimensionality(beginTime, endTime, COLLECT_DATE_FORMAT));
    }

    @Override
    public List<AppCommandStats> getCommandStatsListV2(long appId, long beginTime, long endTime, TimeDimensionalityEnum timeDimensionalityEnum, String commandName) {
        if (TimeDimensionalityEnum.MINUTE.equals(timeDimensionalityEnum)) {
            return appStatsDao.getAppCommandStatsListByMinuteWithCommand(appId, beginTime, endTime, commandName);
        } else if (TimeDimensionalityEnum.HOUR.equals(timeDimensionalityEnum)) {
            return appStatsDao.getAppCommandStatsListByHourWithCommand(appId, beginTime, endTime, commandName);
        }
        return Collections.emptyList();
    }

    @Override
    public List<AppCommandStats> getCommandStatsListV2(long appId, long beginTime, long endTime, TimeDimensionalityEnum timeDimensionalityEnum) {
        if (TimeDimensionalityEnum.MINUTE.equals(timeDimensionalityEnum)) {
            return appStatsDao.getAppAllCommandStatsListByMinute(appId, beginTime, endTime);
        } else if (TimeDimensionalityEnum.HOUR.equals(timeDimensionalityEnum)) {
            return appStatsDao.getAppAllCommandStatsListByHour(appId, beginTime, endTime);
        }
        return Collections.emptyList();
    }


    /**
     * 查询应用指定命令的峰值
     *
     * @param appId       应用id
     * @param beginTime   时间，格式：yyyyMMddHHmm
     * @param endTime     时间，格式：yyyyMMddHHmm
     * @param commandName 命令名
     * @return
     */
    @Override
    public AppCommandStats getCommandClimax(long appId, Long beginTime, Long endTime, String commandName) {
        TimeDimensionality td = new TimeDimensionality(beginTime, endTime, COLLECT_DATE_FORMAT);
        AppCommandStats appCommandStats = appStatsDao.getCommandClimaxCount(appId, commandName, td);
        if (appCommandStats == null) {
            return null;
        }
        appCommandStats.setCommandName(commandName);
        AppCommandStats appCommandStatsTemp = appStatsDao.getCommandClimaxCreateTime(appId, commandName, appCommandStats.getCommandCount(), td);
        if (appCommandStatsTemp != null) {
            appCommandStats.setCreateTime(appCommandStatsTemp.getCreateTime());
        }
        return appCommandStats;
    }

    /**
     * 获取应用命令调用次数分布
     *
     * @param appId
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public List<AppCommandGroup> getAppCommandGroup(long appId, Long beginTime, Long endTime) {
        return appStatsDao.getAppCommandGroup(appId, new TimeDimensionality(beginTime, endTime, COLLECT_DATE_FORMAT));
    }

    /**
     * 获取应用详细信息
     */
    @Override
    public AppDetailVO getAppDetail(long appId) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return null;
        }
        //1.获取Redis版本名称
        SystemResource redisResource = resourceDao.getResourceById(appDesc.getVersionId());
        if (redisResource != null) {
            appDesc.setVersionName(redisResource.getName());
            //2.判断版本是否可以升级
            List<SystemResource> versionList = resourceDao.getResourceList(ResourceEnum.REDIS.getValue());
            for (SystemResource version : versionList) {
                try {
                    // 大版本同一版本且大于当前版本号
                    int versionTag = Integer.parseInt(version.getName().replaceAll("redis-","").replaceAll("\\.",""));
                    int currentTag = Integer.parseInt(redisResource.getName().replaceAll("redis-","").replaceAll("\\.",""));
                    // 支持小版本号升级
                    String versionStr = redisResource.getName().substring(0, redisResource.getName().lastIndexOf("."));
                    if (version.getName().indexOf(versionStr) > -1 && versionTag > currentTag) {
                        appDesc.setIsVersionUpgrade(1);
                        break;
                    }
                } catch (Exception e) {
                    logger.error("parse version:{} {} exception : {}", redisResource.getName(), version.getName(), e.getMessage(), e);
                    appDesc.setIsVersionUpgrade(0);
                }
            }
        } else {
            appDesc.setIsVersionUpgrade(0);
        }

        AppDetailVO resultVO = new AppDetailVO();
        resultVO.setAppDesc(appDesc);
        Set<String> machines = new HashSet<String>();
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);

        if (instanceList == null || instanceList.isEmpty()) {
            return resultVO;
        }
        long hits = 0L;
        long miss = 0L;
        long allUsedMemory = 0L;
        long allMaxMemory = 0L;
        double highestMemFragRatio = 0D;        //碎片率最大值
        long instId = 0L;           //碎片率最大值的实例id
        List<InstanceStats> instanceStatsList = instanceStatsDao.getInstanceStatsByAppId(appId);
        if (instanceStatsList != null && instanceStatsList.size() > 0) {
            Map<Long, InstanceStats> instanceStatMap = new HashMap<Long, InstanceStats>();
            for (InstanceStats stats : instanceStatsList) {
                instanceStatMap.put(stats.getInstId(), stats);
            }

            for (InstanceInfo instanceInfo : instanceList) {
                if (instanceInfo.isOffline()) {
                    continue;
                }
                machines.add(instanceInfo.getIp());
                InstanceStats instanceStats = instanceStatMap.get(Long.valueOf(instanceInfo.getId()));
                if (instanceStats == null) {
                    continue;
                }
                boolean isMaster = isMaster(instanceStats);

                long usedMemory = instanceStats.getUsedMemory();
                long usedMemoryMB = usedMemory / 1024 / 1024;

                allUsedMemory += usedMemory;
                allMaxMemory += instanceStats.getMaxMemory();

                hits += instanceStats.getHits();
                miss += instanceStats.getMisses();
                //碎片率最大值
                double memFragRatio = instanceStats.getMemFragmentationRatio();
                if (memFragRatio > highestMemFragRatio) {
                    highestMemFragRatio = memFragRatio;
                    instId = instanceStats.getInstId();
                }
                if (isMaster) {
                    resultVO.setMem(resultVO.getMem() + instanceInfo.getMem());
                    resultVO.setCurrentMem(resultVO.getCurrentMem() + usedMemoryMB);
                    resultVO.setCurrentObjNum(resultVO.getCurrentObjNum() + instanceStats.getCurrItems());
                    resultVO.setMasterNum(resultVO.getMasterNum() + 1);
                    //按instanceStats计算conn
                    resultVO.setConn(resultVO.getConn() + instanceStats.getCurrConnections());
                } else {
                    resultVO.setSlaveNum(resultVO.getSlaveNum() + 1);
                }
            }
        }
        // 授权用户
        List<AppUser> userList = userService.getByAppId(appId);
        if (userList != null && userList.size() > 0) {
            resultVO.setAppUsers(userList);
        }
        // 报警用户
        List<AppUser> alertUsedrList = userService.getAlertByAppId(appId);
        if (alertUsedrList != null && alertUsedrList.size() > 0) {
            resultVO.setAlertUsers(alertUsedrList);
        }

        resultVO.setMachineNum(machines.size());
        if (allMaxMemory == 0L) {
            resultVO.setMemUsePercent(0.0D);
        } else {
            double percent = 100 * (double) allUsedMemory / (allMaxMemory);
            DecimalFormat df = new DecimalFormat("##.##");
            resultVO.setMemUsePercent(Double.parseDouble(df.format(percent)));
        }
        //最大碎片率及对应实例Id
        resultVO.setHighestMemFragRatio(highestMemFragRatio);
        resultVO.setInstIdWithHighestMemFragRatio(instId);

        if (miss == 0L) {
            if (hits > 0) {
                resultVO.setHitPercent(100.0D);
            } else {
                resultVO.setHitPercent(0.0D);
            }
        } else {
            double percent = 100 * (double) hits / (hits + miss);
            DecimalFormat df = new DecimalFormat("##.##");
            resultVO.setHitPercent(Double.parseDouble(df.format(percent)));
        }

        return resultVO;
    }

    @Override
    public Map<Long, AppDetailVO> getOnlineAppDetails() {
        List<AppDesc> appDescList = appDao.getOnlineApps();
        Map<Long, AppDetailVO> appDetailVOMap = appDescList.stream()
                .map(appDesc -> getAppDetail(appDesc.getAppId()))
                .collect(Collectors.toMap(appDetail -> appDetail.getAppDesc().getAppId(), appDetail -> appDetail));
        return appDetailVOMap;
    }

    @Override
    public List<AppClientStatisticGather> getOnlineAppConnClients() {
        List<AppClientStatisticGather> result = new ArrayList<>();
        List<AppDesc> appDescList = appDao.getOnlineApps();
        appDescList.forEach(appDesc -> {
            AppClientStatisticGather gather = new AppClientStatisticGather();
            long appId = appDesc.getAppId();
            List<Map<String, Object>> addrInstanceList = redisCenter.getAppClientList(appId, 0);
            int totalConnectedClients = 0;
            for (Map<String, Object> addrInstance : addrInstanceList) {
                totalConnectedClients += MapUtils.getIntValue(addrInstance, "size", 0);
            }

            gather.setGatherTime(DateUtil.formatYYYY_MM_dd(new Date()));
            gather.setAppId(appId);
            gather.setConnectedClients(totalConnectedClients);
            result.add(gather);
        });
        return result;
    }

    private boolean isMaster(InstanceStats instanceStats) {
        return instanceStats.getRole() == 1 ? true : false;
    }

    @Override
    public String executeCommand(long appId, String command) {
        if (StringUtils.isBlank(command)) {
            return "命令不能为空";
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return "app not found";
        }
        if (TypeUtil.isRedisType(appDesc.getType())) {
            return redisCenter.executeCommand(appDesc, command);
        }
        return "not support app";
    }

    @Override
    public Map<String, Long> getInstanceSlowLogCountMapByAppId(Long appId, Date startDate, Date endDate) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return Collections.emptyMap();
        }
        if (TypeUtil.isRedisType(appDesc.getType())) {
            return redisCenter.getInstanceSlowLogCountMapByAppId(appId, startDate, endDate);
        }
        return Collections.emptyMap();
    }

    @Override
    public List<InstanceSlowLog> getInstanceSlowLogByAppId(long appId, Date startDate, Date endDate) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return Collections.emptyList();
        }
        if (TypeUtil.isRedisType(appDesc.getType())) {
            return redisCenter.getInstanceSlowLogByAppId(appId, startDate, endDate);
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<Map<String, Object>>> getAppLatencyStats(long appId, long startTime, long endTime) {
        try {
            List<Map<String, Object>> appLatencyInfoList = instanceLatencyHistoryDao.getAppLatencyStats(appId, startTime, endTime);
            Map<String, List<Map<String, Object>>> appLatencyInfoMap = Maps.newHashMap();

            appLatencyInfoList.stream().forEach(appLatencyInfo -> {
                String event = MapUtils.getString(appLatencyInfo, "event");
                ArrayList appEventLatency = (ArrayList) MapUtils.getObject(appLatencyInfoMap, event);
                if (CollectionUtils.isEmpty(appEventLatency)) {
                    appEventLatency = Lists.newArrayList();
                    appLatencyInfoMap.put(event, appEventLatency);
                }
                appEventLatency.add(appLatencyInfo);
            });
            return appLatencyInfoMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, Long> getAppLatencyStatsGroupByInstance(long appId, long startTime, long endTime) {
        try {
            List<Map<String, Object>> appLatencyInfoList = instanceLatencyHistoryDao.getAppLatencyStatsGroupByInstance(appId, startTime, endTime);
            Map<String, Long> appInstanceLatencyStats = appLatencyInfoList.stream()
                    .collect(Collectors.toMap(latencyInfo -> MapUtils.getString(latencyInfo, "host_port"), latencyInfo -> MapUtils.getLong(latencyInfo, "count")));
            return appInstanceLatencyStats;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }


    @Override
    public Map<String, List<Map<String, Object>>> getAppLatencyInfo(long appId, long startTime, long endTime) {
        try {
            List<Map<String, Object>> appLatencyInfoList = instanceLatencyHistoryDao.getAppLatencyInfo(appId, startTime, endTime, "");
            Map<String, List<Map<String, Object>>> appLatencyInfoMap = Maps.newHashMap();

            appLatencyInfoList.stream().forEach(appLatencyInfo -> {
                String host_port = MapUtils.getString(appLatencyInfo, "host_port");
                ArrayList appEventLatency = (ArrayList) MapUtils.getObject(appLatencyInfoMap, host_port);
                if (CollectionUtils.isEmpty(appEventLatency)) {
                    appEventLatency = Lists.newArrayList();
                    appLatencyInfoMap.put(host_port, appEventLatency);
                }
                appEventLatency.add(appLatencyInfo);
            });
            return appLatencyInfoMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public List<InstanceSlowLog> getByInstanceExecuteTime(long instanceId, String executeDate) {
        try {
            return instanceSlowLogDao.getByInstanceExecuteTime(instanceId, executeDate);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getAppTotalStat() {

        Map<String, Object> totalMap = new HashMap<String, Object>();

        /**
         * 1.从mysql获取
         * 2.从jvm获取
         */
        if (PandectUtil.getFromMysql() || MapUtils.isEmpty(PandectUtil.getPandectMap())) {
            // 1.基础统计
            // 1.1 在线应用数
            int appCount = appDao.getAllAppCount(null);
            totalMap.put(StatEnum.TOTAL_EFFETIVE_APP.value(), appCount);
            // 1.2 机器数量
            List<MachineInfo> machineInfoList = machineDao.getAllMachines();
            totalMap.put(StatEnum.TOTAL_MACHINE_NUM.value(), machineInfoList.size());
            // 1.3 获取实例数量
            List<InstanceInfo> allInsts = instanceDao.getAllInsts();
            totalMap.put(StatEnum.TOTAL_INSTANCE_NUM.value(), allInsts.size());

            // 2.获取redis版本分布情况
            List<ParamCount> redisDistribute = new ArrayList<ParamCount>();
            List<Map<String, Integer>> appVersionStats = appDao.getVersionStat();
            Map<Integer, String> versionMap = new HashMap<Integer, String>();
            List<SystemResource> versionList = resourceDao.getResourceList(ResourceEnum.REDIS.getValue());
            if (!CollectionUtils.isEmpty(versionList)) {
                for (SystemResource  version : versionList) {
                    versionMap.put(version.getId(), version.getName());
                }
            }
            if (!CollectionUtils.isEmpty(appVersionStats)) {
                for (Map<String, Integer> appVersion : appVersionStats) {
                    Integer version_id = MapUtils.getInteger(appVersion, "version_id");
                    Integer num = MapUtils.getInteger(appVersion, "num");
                    if (versionMap.containsKey(version_id)) {
                        ParamCount paramCount = new ParamCount(versionMap.get(version_id), num, "");
                        redisDistribute.add(paramCount);
                    }
                }
            }
            totalMap.put(StatEnum.REDIS_VERSION_DISTRIBUTE.value(), JSONArray.fromObject(redisDistribute));
            totalMap.put(StatEnum.REDIS_VERSION_COUNT.value(), versionList.size());

            // 4.获取机器内存分配/机器内存使用分布
            // 4.1 机器内存使用分布
            Map<MachineMemoryDistriEnum, Integer> machineMemoryDistributeMap = machineCenter.getUsedMemoryDistribute();
            List<ParamCount> machineMemoryDistributeList = new ArrayList<ParamCount>();
            for (Map.Entry<MachineMemoryDistriEnum, Integer> entry : machineMemoryDistributeMap.entrySet()) {
                ParamCount paramCount = new ParamCount(entry.getKey().getInfo(), entry.getValue(), "");
                machineMemoryDistributeList.add(paramCount);
            }
            totalMap.put(StatEnum.MACHINE_USEDMEMORY_DISTRIBUTE.value(), JSONArray.fromObject(machineMemoryDistributeList));
            // 4.2 机器内存分配分布
            Map<MachineMemoryDistriEnum, Integer> maxMemoryDistributeMap = machineCenter.getMaxMemoryDistribute();
            List<ParamCount> maxMemoryDistributeList = new ArrayList<ParamCount>();
            for (Map.Entry<MachineMemoryDistriEnum, Integer> entry : maxMemoryDistributeMap.entrySet()) {
                ParamCount paramCount = new ParamCount(entry.getKey().getInfo(), entry.getValue(), "");
                maxMemoryDistributeList.add(paramCount);
            }
            totalMap.put(StatEnum.MACHINE_MAXMEMORY_DISTRIBUTE.value(), JSONArray.fromObject(maxMemoryDistributeList));
            // 4.3 机房分布
            List<Map<String, Object>> roomStat = machineDao.getRoomStat();
            List<ParamCount> roomDistribute = new ArrayList<ParamCount>();
            if (roomStat != null && roomStat.size() > 0) {
                for (Map<String, Object> room : roomStat) {
                    ParamCount paramCount = new ParamCount(MapUtils.getString(room, "name"), MapUtils.getInteger(room, "num"), "");
                    roomDistribute.add(paramCount);
                }
            }
            totalMap.put(StatEnum.MACHIEN_ROOM_DISTRIBUTE.value(), JSONArray.fromObject(roomDistribute));

            // 5.最后一次从数据库获取时间
            totalMap.put(PandectUtil.KEY_LASTTIME, System.currentTimeMillis());
            // 6.暂存到jvm
            PandectUtil.setPandectMap(totalMap);
        } else {
            totalMap.putAll(PandectUtil.getPandectMap());
        }
        return totalMap;
    }

}
