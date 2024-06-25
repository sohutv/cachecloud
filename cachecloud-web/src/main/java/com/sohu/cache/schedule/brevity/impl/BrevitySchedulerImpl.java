package com.sohu.cache.schedule.brevity.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicLongMap;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.entity.BrevityScheduleTask;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.schedule.brevity.BrevityScheduleType;
import com.sohu.cache.schedule.brevity.BrevityScheduler;
import com.sohu.cache.server.ServerStatusCollector;
import com.sohu.cache.util.ScheduleUtil;
import com.sohu.cache.web.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by zengyizhao
 */
@ConditionalOnProperty(name = "cachecloud.redis.enable", havingValue = "true")
@Component
@Slf4j
public class BrevitySchedulerImpl implements BrevityScheduler {
    private static final String _yyyyMMddHHmm = "yyyyMMddHHmm";
    private static final int BATCH = 100;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Lazy
    @Autowired
    private RedisCenter redisCenter;

    @Autowired
    @Lazy
    private MachineCenter machineCenter;

    @Autowired
    private ServerStatusCollector serverStatusCollector;

    @Autowired
    private AssistRedisService assistRedisService;

    public static final AtomicLongMap<String> BREVITY_SCHEDULER_MAP = AtomicLongMap.create();

    public static final String REDIS_KEY_PREFIX = "cc:brevity:schedule:";

    public static final String REDIS_LOCK_KEY_PREFIX = "cc:brevity:schedule:lock:";

    public static final int REDIS_LOCK_EXPIRE_SECONDS = 90;

    @PostConstruct
    private void init() {
        asyncService.assemblePool(AsyncThreadPoolFactory.BREVITY_SCHEDULER_POOL,
                AsyncThreadPoolFactory.BREVITY_SCHEDULER_ASYNC_THREAD_POOL);
    }

    @Override
    public void maintainTasks() {
        try {
            BrevityScheduleType redisInfoType = BrevityScheduleType.REDIS_INFO;
            maintainNodes(redisInfoType);

            BrevityScheduleType redisSlowlogType = BrevityScheduleType.REDIS_SLOWLOG;
            maintainNodes(redisSlowlogType);

            BrevityScheduleType redisLatencyType = BrevityScheduleType.REDIS_LATENCY;
            maintainNodes(redisLatencyType);

            BrevityScheduleType machineInfoType = BrevityScheduleType.MACHINE_INFO;
            maintainMachines(machineInfoType);

            BrevityScheduleType machineMonitorType = BrevityScheduleType.MACHINE_MONITOR;
            maintainMachines(machineMonitorType);

            BrevityScheduleType machineNmonType = BrevityScheduleType.MACHINE_NMON;
            maintainMachines(machineNmonType);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void maintainMachines(BrevityScheduleType scheduleType) {
        List<BrevityScheduleTask> originalNodes = this.getOriginalMachinesOfType(scheduleType.getType());
        List<BrevityScheduleTask> latestNodes = this.getLatestMachinesOfType(scheduleType.getType());
        List<BrevityScheduleTask> addList = this.shouldAddMachinesOfType(originalNodes, latestNodes);
        if (CollectionUtils.isNotEmpty(addList)) {
            long version = getVersion();
            Date date = new Date();
            for (BrevityScheduleTask scheduleTask : addList) {
                scheduleTask.setType(scheduleType.getType());
                scheduleTask.setVersion(version);
                scheduleTask.setCreateTime(date);
                boolean insertRst = insertBrevityScheduler(scheduleTask);
                if (insertRst) {
                    log.warn("shouldAddMachinesOfType: task={}", scheduleTask);
                }
            }
        }
        List<BrevityScheduleTask> removeList = this.shouldRemoveMachinesOfType(originalNodes, latestNodes);
        if (CollectionUtils.isNotEmpty(removeList)) {
            for (BrevityScheduleTask  scheduleTask : removeList) {
                Long result = delBrevityScheduler(scheduleTask);
                if (result != null && result > 0) {
                    log.warn("shouldRemoveMachinesOfType: type={} task={}", scheduleType, scheduleTask);
                }
            }
        }
    }

    private void maintainNodes(BrevityScheduleType scheduleType) {
        List<BrevityScheduleTask> originalNodes = this.getOriginalNodesOfType(scheduleType.getType());
        List<BrevityScheduleTask> latestNodes = this.getLatestNodesOfType(scheduleType.getType());
        List<BrevityScheduleTask> addList = this.shouldAddNodesOfType(originalNodes, latestNodes);
        if (CollectionUtils.isNotEmpty(addList)) {
            long version = getVersion();
            Date date = new Date();
            for (BrevityScheduleTask scheduleTask : addList) {
                scheduleTask.setType(scheduleType.getType());
                scheduleTask.setVersion(version);
                scheduleTask.setCreateTime(date);
                boolean insertRst = insertBrevityScheduler(scheduleTask);
                if (insertRst) {
                    log.warn("shouldAddNodesOfType: task={}", scheduleTask);
                }
            }
        }
        List<BrevityScheduleTask> removeList = this.shouldRemoveNodesOfType(originalNodes, latestNodes);
        if (CollectionUtils.isNotEmpty(removeList)) {
            for (BrevityScheduleTask  scheduleTask : removeList) {
                Long result = delBrevityScheduler(scheduleTask);
                if (result != null && result > 0) {
                    log.warn("shouldRemoveNodesOfType: type={} task={}", scheduleType, scheduleTask);
                }
            }
        }
    }

    private long getVersion() {
        long version = Long.parseLong(DateUtil.formatDate(new Date(), _yyyyMMddHHmm));
        return ScheduleUtil.getLastCollectTime(version);
    }

    private boolean insertBrevityScheduler(BrevityScheduleTask task) {
        return assistRedisService.hset(REDIS_KEY_PREFIX + task.getType(), task.getKeyField(), JSONObject.toJSONString(task));
    }

    private Long delBrevityScheduler(BrevityScheduleTask  task) {
        return assistRedisService.hdel(REDIS_KEY_PREFIX + task.getType(), task.getKeyField());
    }

    //查询原有节点
    private List<BrevityScheduleTask> getOriginalNodesOfType(int type) {
        Map<String, String> originalMap = assistRedisService.hgetAll(REDIS_KEY_PREFIX + type);
        Collection<String> originalNodes = originalMap.values();
        List<BrevityScheduleTask> originalNodeList = originalNodes.stream()
                .map(originalNode -> JSONObject.parseObject(originalNode, BrevityScheduleTask.class))
                .collect(Collectors.toList());
        return originalNodeList;
    }

    //查询最新的节点
    private List<BrevityScheduleTask> getLatestNodesOfType(int type) {
        String sql = "select t1.ip host,t1.port port, t1.type instanceType"
                + " from instance_info  t1"
                + " where t1.status != 2 and t1.type in (2,6,11,12)";
        List<BrevityScheduleTask> list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(BrevityScheduleTask.class));
        return list;
    }

    //查询应该被添加的节点
    private List<BrevityScheduleTask> shouldAddNodesOfType(List<BrevityScheduleTask> originalNodeList,
                                                           List<BrevityScheduleTask> latestNodeList) {
        List<BrevityScheduleTask> shouldAdds = new ArrayList<>();
        Set<String> originals = originalNodeList.stream()
                .map(original -> original.getHostPort()).collect(Collectors.toSet());
        latestNodeList.forEach(latest -> {
            if(!originals.contains(latest.getHostPort())){
                shouldAdds.add(latest);
            }
        });
        return shouldAdds;
    }


    //查询应该被删除的节点
    private List<BrevityScheduleTask> shouldRemoveNodesOfType(List<BrevityScheduleTask> originalNodeList,
                                                              List<BrevityScheduleTask> latestNodeList) {
        List<BrevityScheduleTask> shouldRemoves = new ArrayList<>();
        Set<String> latests = latestNodeList.stream()
                .map(latest -> latest.getHostPort()).collect(Collectors.toSet());
        originalNodeList.forEach(original -> {
            if(!latests.contains(original.getHostPort())){
                shouldRemoves.add(original);
            }
        });
        return shouldRemoves;
    }

    //查询原有节点
    private List<BrevityScheduleTask> getOriginalMachinesOfType(int type) {
        Map<String, String> originalMap = assistRedisService.hgetAll(REDIS_KEY_PREFIX + type);
        Collection<String> originalMachines = originalMap.values();
        List<BrevityScheduleTask> originalMachineList = originalMachines.stream()
                .map(originalMachine -> JSONObject.parseObject(originalMachine, BrevityScheduleTask.class))
                .collect(Collectors.toList());
        return originalMachineList;
    }

    //查询最新的节点
    private List<BrevityScheduleTask> getLatestMachinesOfType(int type) {
        String sql = "select t1.ip host"
                + " from machine_info t1"
                + " where t1.available = 1";
        List<BrevityScheduleTask> list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(BrevityScheduleTask.class));
        return list;
    }

    //查询应该被添加的机器
    private List<BrevityScheduleTask> shouldAddMachinesOfType(
            List<BrevityScheduleTask> originalMachineList, List<BrevityScheduleTask> latestMachineList) {
        List<BrevityScheduleTask> shouldAdds = new ArrayList<>();
        Set<String> originals = originalMachineList.stream()
                .map(original -> original.getHost()).collect(Collectors.toSet());
        latestMachineList.forEach(latest -> {
            if(!originals.contains(latest.getHost())){
                shouldAdds.add(latest);
            }
        });
        return shouldAdds;
    }

    //查询应该被删除的机器
    private List<BrevityScheduleTask> shouldRemoveMachinesOfType(
            List<BrevityScheduleTask> originalMachineList, List<BrevityScheduleTask> latestMachineList) {
        List<BrevityScheduleTask> shouldRemoves = new ArrayList<>();
        Set<String> latests = latestMachineList.stream()
                .map(latest -> latest.getHost()).collect(Collectors.toSet());
        originalMachineList.forEach(original -> {
            if(!latests.contains(original.getHost())){
                shouldRemoves.add(original);
            }
        });
        return shouldRemoves;
    }

    private List<BrevityScheduleTask> getTasks(int type, long version) {
        Map<String, String> originalMap = assistRedisService.hgetAll(REDIS_KEY_PREFIX + type);
        Collection<String> tasks = originalMap.values();
        List<BrevityScheduleTask> scheduleTasks = tasks.stream().map(task -> JSONObject.parseObject(task, BrevityScheduleTask.class))
                .filter(brevityScheduleTask -> brevityScheduleTask.getVersion() < version)
                .collect(Collectors.toList());
        return scheduleTasks;
    }

    private int[] batchUpdate(List<Integer> ids, long version) {
        String sql = "update brevity_schedule_resources set version = ? where id = ? and version < ?";
        List<Object[]> batchArgs = Lists.newArrayListWithCapacity(ids.size());
        for (Integer id : ids) {
            Object[] args = new Object[3];
            args[0] = version;
            args[1] = id;
            args[2] = version;
            batchArgs.add(args);
        }
        //primary-key+version 竞争
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void dispatcherTasks() {
        log.warn("dispatcherTasks:brevity_scheduler_map={}", BREVITY_SCHEDULER_MAP);
        BREVITY_SCHEDULER_MAP.clear();

        long currentVersion = getVersion();
        for (BrevityScheduleType scheduleType : BrevityScheduleType.values()) {
            int minutes = scheduleType.getMinutes();
            int type = scheduleType.getType();
            log.debug("dispatcherTasks: type={} version={}", type, currentVersion);
            // 以minutes作为分段，整除代表可以被执行
            if (currentVersion % minutes == 0) {
                List<BrevityScheduleTask> tasks = getTasks(type, currentVersion);
                if (CollectionUtils.isEmpty(tasks)) {
                    log.info("dispatcherTasks-empty: type={} version={}", type, currentVersion);
                    continue;
                }
                //乱序
                Collections.shuffle(tasks);
                long dispatcherSize = 0;
                List<BrevityScheduleTask> subTasks = new ArrayList<>();
                for (int index = 0; index < tasks.size(); index++) {
                    if (index > 0 && index % BATCH == 0) {
                        log.debug("dispatcherTasks-subTaskIds: index={} size={}", index, subTasks.size());
                        // copy for async
                        asyncSubTaskIds(Lists.newArrayList(subTasks), currentVersion, scheduleType);
                        dispatcherSize += subTasks.size();
                        subTasks.clear();
                        // if machine collect ,per 100 task sleep serve times
                        if (scheduleType.getType() == BrevityScheduleType.MACHINE_INFO.getType() ||
                                scheduleType.getType() == BrevityScheduleType.MACHINE_MONITOR.getType() ||
                                scheduleType.getType() == BrevityScheduleType.MACHINE_NMON.getType()) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(5000l);
                                log.warn("machine task sleep {} ms", 5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    subTasks.add(tasks.get(index));
                }
                if (subTasks.size() > 0) {
                    dispatcherSize += subTasks.size();
                    asyncSubTaskIds(Lists.newArrayList(subTasks), currentVersion, scheduleType);
                }
                log.warn("dispatcherTasks-dispatched: size={} type={} version={}", dispatcherSize, type,
                        currentVersion);
            }
        }
    }

    public Map<String, Object> getNodeInfoByTask(BrevityScheduleTask task) {
        String sql = "select t2.app_id appId,t2.ip host,t2.port port"
                + " from instance_info t2 "
                + " where t2.status != 2 "
                + " and t2.ip = '" + task.getHost()
                + "' and t2.port = " + task.getPort();
        return jdbcTemplate.queryForMap(sql);
    }

    public Map<String, Object> getMachineInfoByTask(BrevityScheduleTask task) {
        String sql = "select t2.id hostId,t2.ip host"
                + " from machine_info t2 "
                + " where t2.available = 1 "
                + " and t2.ip = '" + task.getHost()
                + "'";
        return jdbcTemplate.queryForMap(sql);
    }

    private void asyncSubTaskIds(final List<BrevityScheduleTask> subTasks, final long currentVersion,
                                 final BrevityScheduleType scheduleType) {
        asyncService.submitFuture(AsyncThreadPoolFactory.BREVITY_SCHEDULER_POOL,
                new KeyCallable<Integer>("version:" + currentVersion) {
                    @Override
                    public Integer execute() {
                        //执行分组
                        List<BrevityScheduleTask> toUnlockTasks = Lists.newArrayList();
                        List<BrevityScheduleTask> lockTasks = Lists.newArrayList();
                        try{
                            for (int i = 0; i < subTasks.size(); i++) {
                                boolean updated = assistRedisService
                                        .setNEX(REDIS_LOCK_KEY_PREFIX + subTasks.get(i).getKeyField(), String.valueOf(subTasks.get(i).getVersion()), REDIS_LOCK_EXPIRE_SECONDS);
                                //获得行锁
                                if (updated) {
                                    toUnlockTasks.add(subTasks.get(i));
                                    String latestTaskStr = assistRedisService.hget(REDIS_KEY_PREFIX + scheduleType.getType(), subTasks.get(i).getKeyField());
                                    BrevityScheduleTask brevityScheduleTask = JSONObject.parseObject(latestTaskStr, BrevityScheduleTask.class);
                                    if (brevityScheduleTask.getVersion() != null && brevityScheduleTask.getVersion() >= currentVersion) {
                                        continue;
                                    }
                                    subTasks.get(i).setVersion(currentVersion);
                                    boolean hset = assistRedisService.hset(REDIS_KEY_PREFIX + scheduleType.getType(), subTasks.get(i).getKeyField(), JSONObject.toJSONString(subTasks.get(i)));
                                    if(hset){
                                        lockTasks.add(subTasks.get(i));
                                        BREVITY_SCHEDULER_MAP.incrementAndGet(scheduleType.getType() + "-" + currentVersion);
                                    }
                                }
                            }
                            for (BrevityScheduleTask task : lockTasks) {
                                if (scheduleType == BrevityScheduleType.REDIS_INFO) {
                                    Map<String, Object> map = getNodeInfoByTask(task);
                                    long appId = MapUtils.getLong(map, "appId");
                                    String host = MapUtils.getString(map, "host");
                                    int port = MapUtils.getIntValue(map, "port");
                                    Integer instanceType = MapUtils.getInteger(map, "instanceType");
                                    redisCenter.collectRedisInfo(appId, currentVersion, host, port);
                                } else if (scheduleType == BrevityScheduleType.REDIS_SLOWLOG) {
                                    Map<String, Object> map = getNodeInfoByTask(task);
                                    long appId = MapUtils.getLong(map, "appId");
                                    String host = MapUtils.getString(map, "host");
                                    int port = MapUtils.getIntValue(map, "port");
                                    Integer instanceType = MapUtils.getInteger(map, "instanceType");
                                    redisCenter.collectRedisSlowLog(appId, currentVersion, host, port);
                                } else if (scheduleType == BrevityScheduleType.MACHINE_INFO) {
                                    Map<String, Object> map = getMachineInfoByTask(task);
                                    long hostId = MapUtils.getLong(map, "hostId");
                                    String host = MapUtils.getString(map, "host");
                                    machineCenter.asyncCollectMachineInfo(hostId, currentVersion, host);
                                } else if (scheduleType == BrevityScheduleType.MACHINE_MONITOR) {
                                    Map<String, Object> map = getMachineInfoByTask(task);
                                    long hostId = MapUtils.getLong(map, "hostId");
                                    String host = MapUtils.getString(map, "host");
                                    machineCenter.asyncMonitorMachineStats(hostId, host);
                                } else if (scheduleType == BrevityScheduleType.MACHINE_NMON) {
                                    Map<String, Object> map = getMachineInfoByTask(task);
                                    String host = MapUtils.getString(map, "host");
                                    serverStatusCollector.asyncFetchServerStatus(host);
                                } else if (scheduleType == BrevityScheduleType.REDIS_LATENCY) {
                                    Map<String, Object> map = getNodeInfoByTask(task);
                                    long appId = MapUtils.getLong(map, "appId");
                                    String host = MapUtils.getString(map, "host");
                                    int port = MapUtils.getIntValue(map, "port");
                                    Integer instanceType = MapUtils.getInteger(map, "instanceType");
                                    redisCenter.collectRedisLatencyInfo(appId, currentVersion, host, port);
                                }
                            }

                        } finally {
                            List<String> keys = new ArrayList<>();
                            toUnlockTasks.forEach(task -> keys.add(REDIS_LOCK_KEY_PREFIX + task.getKeyField()));
                            String[] keyArray = new String[keys.size()];
                            assistRedisService.delMulti(keys.toArray(keyArray));
                        }
                        log.warn("dispatcherTasks asyncSubTask this batch end time: {}, scheduleType:{}, provideSize:{}, handleSize:{}"
                                , System.currentTimeMillis(), scheduleType.getInfo(), subTasks.size(), lockTasks.size());
                        return lockTasks.size();
                    }
                });
    }
}
