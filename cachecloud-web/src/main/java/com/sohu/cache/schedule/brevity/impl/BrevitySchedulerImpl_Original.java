package com.sohu.cache.schedule.brevity.impl;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicLongMap;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.entity.BrevityScheduleTask;
import com.sohu.cache.machine.MachineCenter;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yijunzhang
 */
@ConditionalOnProperty(name = "cachecloud.redis.enable", havingValue = "false", matchIfMissing = true)
@Component
@Slf4j
public class BrevitySchedulerImpl_Original implements BrevityScheduler {
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

    public static final AtomicLongMap<String> BREVITY_SCHEDULER_MAP = AtomicLongMap.create();

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
        List<Map<String, Object>> addList = shouldAddMachinesOfType(scheduleType.getType());
        if (CollectionUtils.isNotEmpty(addList)) {
            for (Map<String, Object> map : addList) {
                BrevityScheduleTask task = BrevityScheduleTask.builder()
                        .type(scheduleType.getType())
                        .version(getVersion())
                        .host(MapUtils.getString(map, "host"))
                        .port(MapUtils.getIntValue(map, "port", 0))
                        .createTime(new Date())
                        .build();
                int result = insertBrevityScheduler(task);
                if (result > 0) {
                    log.warn("shouldAddMachinesOfType: task={}", task);
                }
            }
        }
        List<Map<String, Object>> removeList = shouldRemoveMachinesOfType(scheduleType.getType());
        if (CollectionUtils.isNotEmpty(removeList)) {
            for (Map<String, Object> map : removeList) {
                int id = MapUtils.getIntValue(map, "id");
                int result = delBrevityScheduler(id);
                if (result > 0) {
                    log.warn("shouldRemoveMachinesOfType: type={} id={}", scheduleType, id);
                }
            }
        }

    }

    private void maintainNodes(BrevityScheduleType scheduleType) {
        List<Map<String, Object>> addList = shouldAddNodesOfType(scheduleType.getType());
        if (CollectionUtils.isNotEmpty(addList)) {
            for (Map<String, Object> map : addList) {
                BrevityScheduleTask task = BrevityScheduleTask.builder()
                        .type(scheduleType.getType())
                        .version(getVersion())
                        .host(MapUtils.getString(map, "host"))
                        .port(MapUtils.getIntValue(map, "port"))
                        .createTime(new Date())
                        .build();
                int result = insertBrevityScheduler(task);
                if (result > 0) {
                    log.warn("shouldAddNodesOfType: task={}", task);
                }
            }
        }

        List<Map<String, Object>> removeList = shouldRemoveNodesOfType(scheduleType.getType());
        if (CollectionUtils.isNotEmpty(removeList)) {
            for (Map<String, Object> map : removeList) {
                int id = MapUtils.getIntValue(map, "id");
                int result = delBrevityScheduler(id);
                if (result > 0) {
                    log.warn("shouldRemoveNodesOfType: type={} id={}", scheduleType, id);
                }
            }
        }
    }

    private long getVersion() {
        long version = Long.parseLong(DateUtil.formatDate(new Date(), _yyyyMMddHHmm));
        return ScheduleUtil.getLastCollectTime(version);
    }

    private int insertBrevityScheduler(BrevityScheduleTask task) {
        String sql = "insert into brevity_schedule_resources(type,version,host,port,create_time)"
                + " values(?,?,?,?,?)";
        return jdbcTemplate.update(sql, task.getType(), task.getVersion(), task.getHost(),
                task.getPort(), task.getCreateTime());
    }

    private int delBrevityScheduler(int id) {
        String sql = "delete from  brevity_schedule_resources where id = ?";
        return jdbcTemplate.update(sql, id);
    }

    //查询应该被添加的节点
    private List<Map<String, Object>> shouldAddNodesOfType(int type) {
        String sql = "select t1.ip host,t1.port port"
                + " from instance_info  t1"
                + " left join brevity_schedule_resources t2 on (t2.type=? and t1.ip=t2.host and t1.port=t2.port)\n"
                + " where t1.status != 2 and t1.type in (2,6) and t2.id is null";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, type);
        return list;
    }

    //查询应该被删除的节点
    private List<Map<String, Object>> shouldRemoveNodesOfType(int type) {
        String sql = "select t2.id id"
                + " from brevity_schedule_resources t2"
                + " left join instance_info  t1 on (t1.status != 2 and t1.ip=t2.host and t1.port=t2.port)"
                + " where t2.type=? and t1.ip is null";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, type);
        return list;
    }

    //查询应该被添加的机器
    private List<Map<String, Object>> shouldAddMachinesOfType(int type) {
        String sql = "select t1.ip host"
                + " from machine_info t1"
                + " left join brevity_schedule_resources t2 on (t2.type=? and t1.ip=t2.host)"
                + " where t1.available = 1 and t2.id is null";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, type);
        return list;
    }

    //查询应该被删除的机器
    private List<Map<String, Object>> shouldRemoveMachinesOfType(int type) {
        String sql = "select t2.id id"
                + " from brevity_schedule_resources t2"
                + " left join machine_info t1 on (t1.available = 1 and t1.ip=t2.host)"
                + " where t2.type=? and t1.ip is null";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, type);
        return list;
    }

    private List<Integer> getTaskIds(int type, long version) {
        String sql = "select id from brevity_schedule_resources "
                + "where type=" + type + " and version < " + version;
        List<Integer> ids = jdbcTemplate.queryForList(sql, Integer.class);
        return ids;
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
                List<Integer> taskIds = getTaskIds(type, currentVersion);
                if (CollectionUtils.isEmpty(taskIds)) {
                    log.info("dispatcherTasks-empty: type={} version={}", type, currentVersion);
                    continue;
                }
                //乱序
                Collections.shuffle(taskIds);
                long dispatcherSize = 0;
                List<Integer> subTaskIds = new ArrayList<>();
                for (int index = 0; index < taskIds.size(); index++) {
                    if (index > 0 && index % BATCH == 0) {
                        log.debug("dispatcherTasks-subTaskIds: index={} size={}", index, subTaskIds.size());
                        // copy for async
                        asyncSubTaskIds(Lists.newArrayList(subTaskIds), currentVersion, scheduleType);
                        dispatcherSize += subTaskIds.size();
                        subTaskIds.clear();
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
                    subTaskIds.add(taskIds.get(index));
                }
                if (subTaskIds.size() > 0) {
                    dispatcherSize += subTaskIds.size();
                    asyncSubTaskIds(Lists.newArrayList(subTaskIds), currentVersion, scheduleType);
                }
                log.warn("dispatcherTasks-dispatched: size={} type={} version={}", dispatcherSize, type,
                        currentVersion);
            }
        }
    }

    public Map<String, Object> getNodeInfoByLockId(int lockId) {
        String sql = "select t2.app_id appId,t2.ip host,t2.port port"
                + " from brevity_schedule_resources t1"
                + " inner join instance_info t2 on (t1.host=t2.ip and t1.port=t2.port)"
                + " where t1.id = " + lockId;
        return jdbcTemplate.queryForMap(sql);
    }

    public Map<String, Object> getMachineInfoByLockId(int lockId) {
        String sql = "select t2.id hostId,t2.ip host"
                + " from brevity_schedule_resources t1"
                + " inner join machine_info t2 on t1.host=t2.ip"
                + " where t1.id = " + lockId;
        return jdbcTemplate.queryForMap(sql);
    }

    private void asyncSubTaskIds(final List<Integer> subTaskIds, final long currentVersion,
                                 final BrevityScheduleType scheduleType) {
        asyncService.submitFuture(AsyncThreadPoolFactory.BREVITY_SCHEDULER_POOL,
                new KeyCallable<Integer>("version:" + currentVersion) {
                    @Override
                    public Integer execute() {
                        //执行分组
                        int[] batchUpdates = batchUpdate(subTaskIds, currentVersion);
                        List<Integer> lockIds = Lists.newArrayList();
                        for (int i = 0; i < subTaskIds.size(); i++) {
                            int updated = batchUpdates[i];
                            //获得行锁
                            if (updated > 0) {
                                lockIds.add(subTaskIds.get(i));
                                BREVITY_SCHEDULER_MAP.incrementAndGet(scheduleType.getType() + "-" + currentVersion);
                            }
                        }
                        for (Integer lockId : lockIds) {
                            if (scheduleType == BrevityScheduleType.REDIS_INFO) {
                                Map<String, Object> map = getNodeInfoByLockId(lockId);
                                long appId = MapUtils.getLong(map, "appId");
                                String host = MapUtils.getString(map, "host");
                                int port = MapUtils.getIntValue(map, "port");
                                redisCenter.collectRedisInfo(appId, currentVersion, host, port);
                            } else if (scheduleType == BrevityScheduleType.REDIS_SLOWLOG) {
                                Map<String, Object> map = getNodeInfoByLockId(lockId);
                                long appId = MapUtils.getLong(map, "appId");
                                String host = MapUtils.getString(map, "host");
                                int port = MapUtils.getIntValue(map, "port");
                                redisCenter.collectRedisSlowLog(appId, currentVersion, host, port);
                            } else if (scheduleType == BrevityScheduleType.MACHINE_INFO) {
                                Map<String, Object> map = getMachineInfoByLockId(lockId);
                                long hostId = MapUtils.getLong(map, "hostId");
                                String host = MapUtils.getString(map, "host");
                                machineCenter.asyncCollectMachineInfo(hostId, currentVersion, host);
                            } else if (scheduleType == BrevityScheduleType.MACHINE_MONITOR) {
                                Map<String, Object> map = getMachineInfoByLockId(lockId);
                                long hostId = MapUtils.getLong(map, "hostId");
                                String host = MapUtils.getString(map, "host");
                                machineCenter.asyncMonitorMachineStats(hostId, host);
                            } else if (scheduleType == BrevityScheduleType.MACHINE_NMON) {
                                Map<String, Object> map = getMachineInfoByLockId(lockId);
                                String host = MapUtils.getString(map, "host");
                                serverStatusCollector.asyncFetchServerStatus(host);
                            } else if (scheduleType == BrevityScheduleType.REDIS_LATENCY) {
                                Map<String, Object> map = getNodeInfoByLockId(lockId);
                                long appId = MapUtils.getLong(map, "appId");
                                String host = MapUtils.getString(map, "host");
                                int port = MapUtils.getIntValue(map, "port");
                                redisCenter.collectRedisLatencyInfo(appId, currentVersion, host, port);
                            }
                        }
                        return lockIds.size();
                    }
                });
    }
}
