package com.sohu.cache.stats.app.impl;

import com.sohu.cache.constant.AppTopology;
import com.sohu.cache.constant.TimeDimensionalityEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.AppStatsDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceStatsDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.vo.AppDetailVO;
import com.sohu.cache.web.service.UserService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.text.DecimalFormat;
import java.util.*;

/**
 * 基于app的统计信息的接口：包括app详情、app配置以及基于app的统计
 * @author leifu
 * @Date 2015年3月2日
 * @Time 下午1:50:09
 */
public class AppStatsCenterImpl implements AppStatsCenter {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AppDao appDao;
    
    private InstanceDao instanceDao;

    private AppStatsDao appStatsDao;

    private InstanceStatsDao instanceStatsDao;

    private RedisCenter redisCenter;

    private UserService userService;
    
    private final static String COLLECT_DATE_FORMAT = "yyyyMMddHHmm";

    
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
            } else if(TimeDimensionalityEnum.HOUR.equals(timeDimensionalityEnum)) {
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
                    Boolean isMaster = redisCenter.isMaster(instance.getIp(), instance.getPort());
                    if (isMaster == null) {
                        continue;
                    }
                    if (isMaster) {
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
        } else if(TimeDimensionalityEnum.HOUR.equals(timeDimensionalityEnum)) {
            return appStatsDao.getAppCommandStatsListByHourWithCommand(appId, beginTime, endTime, commandName);
        }
        return Collections.emptyList();
    }

    @Override
    public List<AppCommandStats> getCommandStatsListV2(long appId, long beginTime, long endTime, TimeDimensionalityEnum timeDimensionalityEnum) {
        if (TimeDimensionalityEnum.MINUTE.equals(timeDimensionalityEnum)) {
            return appStatsDao.getAppAllCommandStatsListByMinute(appId, beginTime, endTime);
        } else if(TimeDimensionalityEnum.HOUR.equals(timeDimensionalityEnum)) {
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
        List<InstanceStats> instanceStatsList = instanceStatsDao.getInstanceStatsByAppId(appId);
        if(instanceStatsList != null && instanceStatsList.size() > 0){
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

        List<AppUser> userList = userService.getByAppId(appId);
        if (userList != null && userList.size() > 0) {
            resultVO.setAppUsers(userList);
        }
        resultVO.setMachineNum(machines.size());
        if (allMaxMemory == 0L) {
            resultVO.setMemUsePercent(0.0D);
        } else {
            double percent = 100 * (double) allUsedMemory / (allMaxMemory);
            DecimalFormat df = new DecimalFormat("##.##");
            resultVO.setMemUsePercent(Double.parseDouble(df.format(percent)));
        }

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

    public void setAppDao(AppDao appDao) {
        this.appDao = appDao;
    }

    public void setAppStatsDao(AppStatsDao appStatsDao) {
        this.appStatsDao = appStatsDao;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setInstanceStatsDao(InstanceStatsDao instanceStatsDao) {
        this.instanceStatsDao = instanceStatsDao;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }



}
