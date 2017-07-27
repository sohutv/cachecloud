package com.sohu.cache.stats.instance.impl;

import com.sohu.cache.constant.RedisConstant;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceStatsDao;
import com.sohu.cache.entity.InstanceCommandStats;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.entity.StandardStats;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.util.DateUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by yijunzhang on 14-9-17.
 */
public class InstanceStatsCenterImpl implements InstanceStatsCenter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private InstanceDao instanceDao;

    private InstanceStatsDao instanceStatsDao;

    private RedisCenter redisCenter;
    
    @Override
    public InstanceInfo getInstanceInfo(long instanceId) {
        return instanceDao.getInstanceInfoById(instanceId);
    }

    @Override
    public InstanceStats getInstanceStats(long instanceId) {
        InstanceStats instanceStats = instanceStatsDao.getInstanceStatsByInsId(instanceId);
        if (instanceStats == null) {
            logger.error("instanceStats id={} is null", instanceId);
            return null;
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        int type = instanceInfo.getType();
        boolean isRun = redisCenter.isRun(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
        instanceStats.setRun(isRun);
        if (isRun) {
            Map<String, Object> infoMap = getInfoMap(instanceInfo.getAppId(), type, instanceInfo.getIp(), instanceInfo.getPort());
            instanceStats.setInfoMap(infoMap);
            if (infoMap == null || infoMap.isEmpty()) {
                instanceStats.setRun(false);
            }
        }
        return instanceStats;
    }

    private Map<String, Object> getInfoMap(long appId, int type, String ip, int port) {
        Map<RedisConstant, Map<String, Object>> infoMap = redisCenter.getInfoStats(appId, ip, port);
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        if (infoMap != null) {
            for (RedisConstant redisConstant : infoMap.keySet()) {
                resultMap.put(redisConstant.getValue(), infoMap.get(redisConstant));
            }
        }
        return resultMap;
    }

    @Override
    public List<InstanceCommandStats> getCommandStatsList(Long instanceId, long beginTime, long endTime,
            String commandName) {
        if (instanceId == null) {
            return Collections.emptyList();
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        List<InstanceCommandStats> resultList = new ArrayList<InstanceCommandStats>();
        String ip = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        int type = instanceInfo.getType();
        List<Map<String, Object>> objectList = this.queryDiffMapList(beginTime, endTime, ip, port, ConstUtils.REDIS);;
        if (objectList != null) {
            for (Map<String, Object> map : objectList) {
                InstanceCommandStats stats = parseCommand(instanceId, commandName, map, true, type);
                if (stats != null) {
                    resultList.add(stats);
                }
            }
        }

        return resultList;
    }

    @Override
    public Map<Integer, Map<String, List<InstanceCommandStats>>> getStandardStatsList(Long appId, long beginTime,
            long endTime, List<String> commands) {
        if (appId == null) {
            return Collections.emptyMap();
        }
        List<InstanceInfo> list = instanceDao.getInstListByAppId(appId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, Map<String, List<InstanceCommandStats>>> resultMap = new LinkedHashMap<Integer, Map<String, List<InstanceCommandStats>>>();
        for (InstanceInfo instance : list) {
            if (instance.isOffline()) {
                continue;
            }
            int instanceId = instance.getId();
            String ip = instance.getIp();
            int port = instance.getPort();
            int type = instance.getType();
            Boolean isMaster = redisCenter.isMaster(appId, ip, port);
            if (BooleanUtils.isNotTrue(isMaster)){
                continue;
            }
            List<Map<String, Object>> objectList = this.queryDiffMapList(beginTime, endTime, ip, port, ConstUtils.REDIS);;
            if (objectList != null) {
                Map<String, List<InstanceCommandStats>> commandMap = new LinkedHashMap<String, List<InstanceCommandStats>>();
                for (String commandName : commands) {
                    List<InstanceCommandStats> resultList = new ArrayList<InstanceCommandStats>(objectList.size());
                    for (Map<String, Object> map : objectList) {
                        InstanceCommandStats stats = parseCommand(instanceId, commandName, map, false, type);
                        if (stats != null) {
                            resultList.add(stats);
                        }
                    }
                    commandMap.put(commandName, resultList);
                }
                resultMap.put(instanceId, commandMap);
            }
        }
        return resultMap;
    }

    private InstanceCommandStats parseCommand(long instanceId, String command,
            Map<String, Object> commandMap, boolean isCommand, int type) {
        Long collectTime = MapUtils.getLong(commandMap, ConstUtils.COLLECT_TIME, null);
        if (collectTime == null) {
            return null;
        }
        Long count;
        if (isCommand) {
            count = MapUtils.getLong(commandMap, "cmdstat_" + command.toLowerCase(), null);
        } else {
            count = MapUtils.getLong(commandMap, command.toLowerCase(), null);
        }
        if (count == null) {
            return null;
        }
        InstanceCommandStats stats = new InstanceCommandStats();
        stats.setCommandCount(count);
        stats.setCommandName(command);
        stats.setCollectTime(collectTime);
        stats.setCreateTime(DateUtil.getDateByFormat(String.valueOf(collectTime), "yyyyMMddHHmm"));
        stats.setModifyTime(DateUtil.getDateByFormat(String.valueOf(collectTime), "yyyyMMddHHmm"));
        stats.setInstanceId(instanceId);

        return stats;
    }

    @Override
    public String executeCommand(String host, int port, String command) {
        if (StringUtils.isBlank(host) || port == 0) {
            return "host or port is null";
        }
        InstanceInfo instanceInfo = instanceDao.getAllInstByIpAndPort(host, port);
        if (instanceInfo == null) {
            return "instance not exist";
        }
        if (TypeUtil.isRedisType(instanceInfo.getType())) {
            return redisCenter.executeCommand(instanceInfo.getAppId(), host, port, command);
        }
        return "not support type";
    }

    @Override
    public String executeCommand(Long instanceId, String command) {
        InstanceInfo instanceInfo = getInstanceInfo(instanceId);
        return executeCommand(instanceInfo.getIp(), instanceInfo.getPort(), command);
    }

    @Override
    public List<InstanceStats> getInstanceStats() {
        return instanceStatsDao.getInstanceStats();
    }

    @Override
    public List<InstanceStats> getInstanceStats(String ip) {
        List<InstanceStats> instanceStatsList = instanceStatsDao.getInstanceStatsByIp(ip);
        return instanceStatsList;
    }

    @Override
    public boolean saveStandardStats(Map<String, Object> infoMap, Map<String, Object> clusterInfoMap, String ip, int port, String dbType) {
        Assert.isTrue(infoMap != null && infoMap.size() > 0);
        Assert.isTrue(StringUtils.isNotBlank(ip));
        Assert.isTrue(port > 0);
        Assert.isTrue(infoMap.containsKey(ConstUtils.COLLECT_TIME), ConstUtils.COLLECT_TIME + " not in infoMap");
        long collectTime = MapUtils.getLong(infoMap, ConstUtils.COLLECT_TIME);
        StandardStats ss = new StandardStats();
        ss.setCollectTime(collectTime);
        ss.setIp(ip);
        ss.setPort(port);
        ss.setDbType(dbType);
        if (infoMap.containsKey(RedisConstant.DIFF.getValue())) {
            Map<String, Object> diffMap = (Map<String, Object>) infoMap.get(RedisConstant.DIFF.getValue());
            ss.setDiffMap(diffMap);
            infoMap.remove(RedisConstant.DIFF.getValue());
        } else {
            ss.setDiffMap(new HashMap<String, Object>(0));
        }
        ss.setInfoMap(infoMap);
        ss.setClusterInfoMap(clusterInfoMap);

        int mergeCount = instanceStatsDao.mergeStandardStats(ss);
        return mergeCount > 0;
    }

    @Override
    public Map<String, Object> queryStandardInfoMap(long collectTime, String ip, int port, String dbType) {
        Assert.isTrue(StringUtils.isNotBlank(ip));
        Assert.isTrue(port > 0);
        Assert.isTrue(collectTime > 0);
        StandardStats ss = instanceStatsDao.getStandardStats(collectTime, ip, port, dbType);
        if (ss != null) {
            Map<String, Object> infoMap = ss.getInfoMap();
            Map<String, Object> diffMap = ss.getDiffMap();
            infoMap.put(RedisConstant.DIFF.getValue(), diffMap);
            return infoMap;
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public List<Map<String, Object>> queryDiffMapList(long beginTime, long endTime, String ip, int port,
            String dbType) {
        Assert.isTrue(StringUtils.isNotBlank(ip));
        Assert.isTrue(port > 0);
        Assert.isTrue(beginTime > 0);
        Assert.isTrue(endTime > 0);
        List<StandardStats> list = instanceStatsDao.getDiffJsonList(beginTime, endTime, ip, port, dbType);
        if (list == null || list.isEmpty()) {
            return new ArrayList<Map<String, Object>>(0);
        }
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>(list.size());
        for (StandardStats ss : list) {
            Map<String, Object> diffMap = ss.getDiffMap();
            diffMap.put(ConstUtils.COLLECT_TIME, ss.getCollectTime());
            resultList.add(diffMap);
        }
        return resultList;
    }

    @Override
    public void cleanUpStandardStats(int day) {
        try {
            SimpleDateFormat minSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat hourSdf = new SimpleDateFormat("yyyy-MM-dd HH");
            // 基准时间
            Date baseTime = DateUtils.addDays(hourSdf.parse(minSdf.format(new Date())), 0 - day);
            Date startTime = null;          //删除开始时间
            Date endTime = null;            //删除结束时间
            int mins = 24 * 60 * (day - 1); //天换算分钟数,保留一天数据
            int perMin = 10;                //每10分钟区间做一次删除
            long beginTime = System.currentTimeMillis();
            for (int count = 1; count <= mins / perMin; count++) {
                startTime = DateUtils.addMinutes(baseTime, perMin * (count - 1));
                endTime = DateUtils.addMinutes(baseTime, perMin * count);
                long startMills = System.currentTimeMillis();
                instanceStatsDao.deleteStandardStatsByScanTime(startTime, endTime);
                logger.warn("execute delete task cost：{} ms ,time :{},{}", System.currentTimeMillis() - startMills, minSdf.format(startTime), minSdf.format(endTime));
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            logger.info("cleanUpStandardStats total costTime =" + (System.currentTimeMillis() - beginTime) / 1000 + " s");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setInstanceStatsDao(InstanceStatsDao instanceStatsDao) {
        this.instanceStatsDao = instanceStatsDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }
}
