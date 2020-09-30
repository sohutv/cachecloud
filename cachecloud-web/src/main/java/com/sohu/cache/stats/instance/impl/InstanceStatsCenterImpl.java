package com.sohu.cache.stats.instance.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.constant.RedisConstant;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceStatsDao;
import com.sohu.cache.dao.StandardStatsDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.util.DateUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by yijunzhang on 14-9-17.
 */
@Service("instanceStatsCenter")
public class InstanceStatsCenterImpl implements InstanceStatsCenter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ForkJoinPool forkJoinPool = new ForkJoinPool(20);
    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private InstanceStatsDao instanceStatsDao;
    @Autowired
    private StandardStatsDao standardStatsDao;
    @Autowired
    @Lazy
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
            Map<String, Object> infoMap = getInfoMap(instanceInfo.getAppId(), type, instanceInfo.getIp(),
                    instanceInfo.getPort());
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
            for (Map.Entry<RedisConstant, Map<String, Object>> entry : infoMap.entrySet()) {
                resultMap.put(entry.getKey().getValue(), entry.getValue());
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
        List<Map<String, Object>> objectList = this.queryDiffMapList(beginTime, endTime, ip, port, ConstUtils.REDIS);
        ;
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
            BooleanEnum isMaster = redisCenter.isMaster(appId, ip, port);
            if (isMaster != BooleanEnum.TRUE) {
                continue;
            }
            List<Map<String, Object>> objectList = this
                    .queryDiffMapList(beginTime, endTime, ip, port, ConstUtils.REDIS);

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

    @Override
    public Table<Integer, String, Map<String, List<InstanceMinuteStats>>> getInstanceMinuteStatsList(Long appId, long beginTime,
                                                                                                     long endTime, List<String> commands) {
        if (appId == null) {
            return HashBasedTable.create();
        }
        List<InstanceInfo> list = instanceDao.getInstListByAppId(appId);

        if (list == null || list.isEmpty()) {
            return HashBasedTable.create();
        }
        List<InstanceInfo> onlineMasterList = list.stream()
                .filter(x -> !x.isOffline())
                .filter(x -> BooleanEnum.TRUE == redisCenter.isMaster(appId, x.getIp(), x.getPort()))
                .collect(Collectors.toList());

        long start = System.currentTimeMillis();
        List<ForkJoinTask> taskList = onlineMasterList.stream()
                .map(x -> forkJoinPool.submit(new QueryDiffMapListTask(x, beginTime, endTime, commands, ConstUtils.REDIS)))
                .collect(Collectors.toList());

        Table<Integer, String, Map<String, List<InstanceMinuteStats>>> resultTable = HashBasedTable.create();
        for (ForkJoinTask<Table<Integer, String, Map<String, List<InstanceMinuteStats>>>> task : taskList) {
            try {
                Table<Integer, String, Map<String, List<InstanceMinuteStats>>> instanceTable = task.get(3000, TimeUnit.MILLISECONDS);
                resultTable.putAll(instanceTable);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        logger.warn("get total {} online master instances diffMapList cost sum {} ms", onlineMasterList.size(), (System.currentTimeMillis() - start));

        return resultTable;
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

    private InstanceMinuteStats parseMinuteStats(long instanceId, String command,
                                                 Map<String, Object> commandMap, boolean isCommand, int type) {
        Long collectTime = MapUtils.getLong(commandMap, ConstUtils.COLLECT_TIME, null);
        if (collectTime == null) {
            return null;
        }
        Double count;
        if (isCommand) {
            count = MapUtils.getDouble(commandMap, "cmdstat_" + command.toLowerCase(), null);
        } else {
            count = MapUtils.getDouble(commandMap, command.toLowerCase(), null);
        }
        if (count == null) {
            return null;
        }
        InstanceMinuteStats stats = new InstanceMinuteStats();
        stats.setMemFragmentationRatio(count);
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
    public boolean saveStandardStats(Map<String, Object> infoMap, Map<String, Object> clusterInfoMap, String ip,
                                     int port, String dbType) {
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

        int mergeStandardCount = standardStatsDao.mergeStandardStats(ss);
        int mergeInstanceCount = standardStatsDao.mergeInstanceMinuteStats(ss);
        return mergeStandardCount > 0 && mergeInstanceCount > 0;
    }

    @Override
    public Map<String, Object> queryStandardInfoMap(long collectTime, String ip, int port, String dbType) {
        Assert.isTrue(StringUtils.isNotBlank(ip));
        Assert.isTrue(port > 0);
        Assert.isTrue(collectTime > 0);
        StandardStats ss = standardStatsDao.getStandardStats(collectTime, ip, port, dbType);
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
        List<StandardStats> list = standardStatsDao.getDiffJsonList(beginTime, endTime, ip, port, dbType);
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

    class QueryDiffMapListTask extends KeyCallable<Table<Integer, String, Map<String, List<InstanceMinuteStats>>>> {

        private InstanceInfo instance;
        private long beginTime;
        private long endTime;
        private List<String> commands;
        private String dbType;

        public QueryDiffMapListTask(InstanceInfo instance, long beginTime, long endTime,
                                    List<String> commands, String dbType) {
            super("QueryDiffMapListTask-" + instance.getIp() + ":" + instance.getPort());
            this.instance = instance;
            this.beginTime = beginTime;
            this.endTime = endTime;
            this.commands = commands;
            this.dbType = dbType;
        }

        @Override
        public Table<Integer, String, Map<String, List<InstanceMinuteStats>>> execute() {
            int instanceId = instance.getId();
            String ip = instance.getIp();
            int port = instance.getPort();
            int type = instance.getType();
            List<Map<String, Object>> objectList = queryDiffMapList(beginTime, endTime, ip, port, dbType);
            Table<Integer, String, Map<String, List<InstanceMinuteStats>>> table = HashBasedTable.create();
            if (objectList != null) {
                Map<String, List<InstanceMinuteStats>> commandMap = new LinkedHashMap<>();
                for (String commandName : commands) {
                    List<InstanceMinuteStats> resultList = new ArrayList<>(objectList.size());
                    for (Map<String, Object> map : objectList) {
                        InstanceMinuteStats stats = parseMinuteStats(instanceId, commandName, map, false, type);
                        if (stats != null) {
                            resultList.add(stats);
                        }
                    }
                    commandMap.put(commandName, resultList);
                }
                table.put(instanceId, instance.getHostPort(), commandMap);
            }
            return table;
        }
    }
}
