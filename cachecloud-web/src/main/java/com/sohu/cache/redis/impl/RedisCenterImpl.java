package com.sohu.cache.redis.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.constant.*;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.*;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.protocol.RedisProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.enums.RedisInfoEnum;
import com.sohu.cache.redis.enums.RedisReadOnlyCommandEnum;
import com.sohu.cache.redis.util.*;
import com.sohu.cache.report.ReportDataComponent;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.util.*;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.ClientTypeEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.ModuleService;
import com.sohu.cache.web.service.WebClientComponent;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.vo.RedisSlowLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Module;
import redis.clients.jedis.exceptions.JedisAskDataException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisMovedDataException;
import redis.clients.jedis.util.SafeEncoder;
import redis.clients.jedis.util.Slowlog;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by yijunzhang on 14-6-10.
 */
@Service("redisCenter")
public class RedisCenterImpl implements RedisCenter {
    public static final int REDIS_DEFAULT_TIME = 1000;
    public static final String REDIS_SLOWLOG_POOL = "redis-slowlog-pool";
    private static final int COUNT = 1000;
    private static List<RedisInfoEnum> otherNeedCalDifRedisInfoEnumList = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Lock lock = new ReentrantLock();
    @Autowired
    private AppStatsDao appStatsDao;
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private InstanceStatsDao instanceStatsDao;
    @Autowired
    private InstanceStatsCenter instanceStatsCenter;
    @Autowired
    @Lazy
    private MachineCenter machineCenter;
    private volatile Map<String, JedisPool> jedisPoolMap = new HashMap<String, JedisPool>();
    @Autowired
    private AppDao appDao;
    @Autowired
    private AppAuditLogDao appAuditLogDao;
    @Autowired
    private AppService appService;
    @Autowired
    private InstanceSlowLogDao instanceSlowLogDao;
    @Autowired
    private InstanceLatencyHistoryDao instanceLatencyHistoryDao;
    @Autowired
    private WebClientComponent webClientComponent;
    @Autowired
    SSHService sshService;
    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ReportDataComponent reportDataComponent;

    @PostConstruct
    public void init() {
        asyncService.assemblePool(getThreadPoolKey(), AsyncThreadPoolFactory.REDIS_SLOWLOG_THREAD_POOL);
        otherNeedCalDifRedisInfoEnumList.add(RedisInfoEnum.mem_fragmentation_ratio);
    }


    private JedisPool maintainJedisPool(String host, int port, String password) {
        String hostAndPort = ObjectConvert.linkIpAndPort(host, port);
        JedisPool jedisPool = jedisPoolMap.get(hostAndPort);
        if (jedisPool == null) {
            lock.lock();
            try {
                //double check
                jedisPool = jedisPoolMap.get(hostAndPort);
                if (jedisPool == null) {
                    try {
                        if (StringUtils.isNotBlank(password)) {
                            jedisPool = new JedisPool(new GenericObjectPoolConfig(), host, port,
                                    Protocol.DEFAULT_TIMEOUT, password);
                        } else {
                            jedisPool = new JedisPool(new GenericObjectPoolConfig(), host, port,
                                    Protocol.DEFAULT_TIMEOUT);
                        }
                        jedisPoolMap.put(hostAndPort, jedisPool);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    } finally {

                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return jedisPool;
    }

    private String buildFutureKey(long appId, long collectTime, String host, int port) {
        StringBuilder keyBuffer = new StringBuilder("redis-");
        keyBuffer.append(collectTime);
        keyBuffer.append("-");
        keyBuffer.append(appId);
        keyBuffer.append("-");
        keyBuffer.append(host + ":" + port);
        return keyBuffer.toString();
    }

    @Override
    public List<InstanceSlowLog> collectRedisSlowLog(long appId, long collectTime, String host, int port) {
        Assert.isTrue(appId > 0);
        Assert.hasText(host);
        Assert.isTrue(port > 0);
        InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(host, port);
        //不存在实例/实例异常/下线
        if (instanceInfo == null) {
            return null;
        }
        if (TypeUtil.isRedisSentinel(instanceInfo.getType())) {
            //忽略sentinel redis实例
            return null;
        }
        // 从redis中获取慢查询日志
        List<RedisSlowLog> redisLowLogList = getRedisSlowLogs(appId, host, port, 100);
        if (CollectionUtils.isEmpty(redisLowLogList)) {
            return Collections.emptyList();
        }

        // transfer
        final List<InstanceSlowLog> instanceSlowLogList = new ArrayList<InstanceSlowLog>();
        for (RedisSlowLog redisSlowLog : redisLowLogList) {
            InstanceSlowLog instanceSlowLog = transferRedisSlowLogToInstance(redisSlowLog, instanceInfo);
            if (instanceSlowLog == null) {
                continue;
            }
            instanceSlowLogList.add(instanceSlowLog);
        }

        if (CollectionUtils.isEmpty(instanceSlowLogList)) {
            return Collections.emptyList();
        }

        //处理
        String key = getThreadPoolKey() + "_" + host + "_" + port;
        boolean isOk = asyncService.submitFuture(getThreadPoolKey(), new KeyCallable<Boolean>(key) {
            @Override
            public Boolean execute() {
                try {
                    instanceSlowLogDao.batchSave(instanceSlowLogList);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        });
        if (!isOk) {
            logger.error("slowlog submitFuture failed,appId:{},collectTime:{},host:{},port:{}", appId, collectTime,
                    host, port);
        }
        return instanceSlowLogList;
    }

    private InstanceSlowLog transferRedisSlowLogToInstance(RedisSlowLog redisSlowLog, InstanceInfo instanceInfo) {
        if (redisSlowLog == null) {
            return null;
        }
        String command = redisSlowLog.getCommand();
        long executionTime = redisSlowLog.getExecutionTime();
        //如果command=BGREWRITEAOF并且小于50毫秒,则忽略
        if (command.equalsIgnoreCase("BGREWRITEAOF") && executionTime < 50000) {
            return null;
        }
        InstanceSlowLog instanceSlowLog = new InstanceSlowLog();
        instanceSlowLog.setAppId(instanceInfo.getAppId());
        instanceSlowLog.setCommand(redisSlowLog.getCommand());
        instanceSlowLog.setCostTime((int) redisSlowLog.getExecutionTime());
        instanceSlowLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
        instanceSlowLog.setExecuteTime(new Timestamp(redisSlowLog.getDate().getTime()));
        instanceSlowLog.setInstanceId(instanceInfo.getId());
        instanceSlowLog.setIp(instanceInfo.getIp());
        instanceSlowLog.setPort(instanceInfo.getPort());
        instanceSlowLog.setSlowLogId(redisSlowLog.getId());

        return instanceSlowLog;
    }

    private String getThreadPoolKey() {
        return REDIS_SLOWLOG_POOL;
    }

    @Override
    public Map<RedisConstant, Map<String, Object>> collectRedisInfo(long appId, long collectTime, String host,
                                                                    int port) {
        long start = System.currentTimeMillis();

        InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(host, port);
        //不存在实例/实例异常/下线
        if (instanceInfo == null) {
            return null;
        }
        if (TypeUtil.isRedisSentinel(instanceInfo.getType())) {
            //忽略sentinel redis实例
            return null;
        }
        Map<RedisConstant, Map<String, Object>> infoMap = this.getInfoStats(appId, host, port);
        if (infoMap == null || infoMap.isEmpty()) {
            logger.error("appId:{},collectTime:{},host:{},port:{} cost={} ms redis infoMap is null",
                    new Object[]{appId, collectTime, host, port, (System.currentTimeMillis() - start)});
            return infoMap;
        }

        //上报数据
        Map<String, Object> redisInfoMap = new HashMap<>();
        redisInfoMap.put("instanceInfo", instanceInfo);
        redisInfoMap.put("collectTime", collectTime);
        redisInfoMap.put("info", infoMap);
        reportDataComponent.reportRedisInfoData(redisInfoMap);

        // cluster info统计
        Map<String, Object> clusterInfoMap = getClusterInfoStats(appId, instanceInfo);

        boolean isOk = asyncService
                .submitFuture(new RedisKeyCallable(appId, collectTime, host, port, infoMap, clusterInfoMap));
        if (!isOk) {
            logger.error("submitFuture failed,appId:{},collectTime:{},host:{},port:{} cost={} ms",
                    new Object[]{appId, collectTime, host, port, (System.currentTimeMillis() - start)});
        }
        return infoMap;
    }

    @Override
    public Map<RedisConstant, Map<String, Object>> getInfoStats(final long appId, final String host, final int port) {
        Map<RedisConstant, Map<String, Object>> infoMap = null;
        final StringBuilder infoBuilder = new StringBuilder();
        try {
            boolean isOk = new IdempotentConfirmer() {
                private int timeOutFactor = 1;

                @Override
                public boolean execute() {
                    Jedis jedis = null;
                    try {
                        jedis = getJedis(appId, host, port);
                        jedis.getClient().setConnectionTimeout(REDIS_DEFAULT_TIME * (timeOutFactor++));
                        jedis.getClient().setSoTimeout(REDIS_DEFAULT_TIME * (timeOutFactor++));
                        String info = jedis.info("all");
                        infoBuilder.append(info);
                        return StringUtils.isNotBlank(info);
                    } catch (Exception e) {
                        logger.warn("{}:{}, redis-getInfoStats errorMsg:{}", host, port, e.getMessage());
                        return false;
                    } finally {
                        if (jedis != null)
                            jedis.close();
                    }
                }
            }.run();
            if (!isOk) {
                return infoMap;
            }
            infoMap = processRedisStats(infoBuilder.toString());
        } catch (Exception e) {
            logger.error(e.getMessage() + " {}:{}", host, port, e);
        }
        if (infoMap == null || infoMap.isEmpty()) {
            logger.error("host:{},port:{} redis infoMap is null", host, port);
            return infoMap;
        }
        return infoMap;
    }

    @Override
    public Map<String, Object> getClusterInfoStats(final long appId, final String host, final int port) {
        InstanceInfo instanceInfo = instanceDao.getAllInstByIpAndPort(host, port);
        return getClusterInfoStats(appId, instanceInfo);
    }

    @Override
    public Map<String, Object> getClusterInfoStats(final long appId, final InstanceInfo instanceInfo) {
        long startTime = System.currentTimeMillis();
        if (instanceInfo == null) {
            logger.warn("getClusterInfoStats instanceInfo is null");
            return Collections.emptyMap();
        }
        if (!TypeUtil.isRedisCluster(instanceInfo.getType())) {
            return Collections.emptyMap();
        }
        final String host = instanceInfo.getIp();
        final int port = instanceInfo.getPort();
        Map<String, Object> clusterInfoMap = null;
        final StringBuilder infoBuilder = new StringBuilder();
        try {
            boolean isOk = new IdempotentConfirmer() {
                private int timeOutFactor = 1;

                @Override
                public boolean execute() {
                    Jedis jedis = null;
                    try {
                        jedis = getJedis(appId, host, port);
                        jedis.getClient().setConnectionTimeout(REDIS_DEFAULT_TIME * (timeOutFactor++));
                        jedis.getClient().setSoTimeout(REDIS_DEFAULT_TIME * (timeOutFactor++));
                        String clusterInfo = jedis.clusterInfo();
                        infoBuilder.append(clusterInfo);
                        return StringUtils.isNotBlank(clusterInfo);
                    } catch (Exception e) {
                        logger.warn("{}:{}, redis-getInfoStats errorMsg:{}", host, port, e.getMessage());
                        return false;
                    } finally {
                        if (jedis != null)
                            jedis.close();
                    }
                }
            }.run();
            if (!isOk) {
                return clusterInfoMap;
            }
            clusterInfoMap = processClusterInfoStats(infoBuilder.toString());
        } catch (Exception e) {
            logger.error(e.getMessage() + " {}:{}", host, port, e);
        }
        if (MapUtils.isEmpty(clusterInfoMap)) {
            logger.error("{}:{} redis clusterInfoMap is null", host, port);
            return Collections.emptyMap();
        }
        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > 1000) {
            logger.warn("{}:{} cluster info cost time {} ms", host, port, costTime);
        }
        return clusterInfoMap;
    }

    private void fillAccumulationMap(Map<RedisConstant, Map<String, Object>> infoMap,
                                     Table<RedisConstant, String, Long> table) {
        if (table == null || table.isEmpty()) {
            return;
        }
        Map<String, Object> accMap = infoMap.get(RedisConstant.DIFF);
        if (accMap == null) {
            accMap = new LinkedHashMap<String, Object>();
            infoMap.put(RedisConstant.DIFF, accMap);
        }
        for (RedisConstant constant : table.rowKeySet()) {
            Map<String, Long> rowMap = table.row(constant);
            accMap.putAll(rowMap);
        }
    }

    private void fillDoubleAccumulationMap(Map<RedisConstant, Map<String, Object>> infoMap,
                                           Table<RedisConstant, String, Double> table) {
        if (table == null || table.isEmpty()) {
            return;
        }
        Map<String, Object> accMap = infoMap.get(RedisConstant.DIFF);
        if (accMap == null) {
            accMap = new LinkedHashMap<String, Object>();
            infoMap.put(RedisConstant.DIFF, accMap);
        }
        for (RedisConstant constant : table.rowKeySet()) {
            Map<String, Double> rowMap = table.row(constant);
            accMap.putAll(rowMap);
        }
    }

    /**
     * 内存碎片率统计最新值，不计算差值
     */
    private void fillMemFragRatioMap(Map<RedisConstant, Map<String, Object>> infoMap) {

        Map<String, Double> currentMap = new LinkedHashMap<>();
        RedisInfoEnum acc = RedisInfoEnum.mem_fragmentation_ratio;
        Double count = getDoubleCount(infoMap, acc.getRedisConstant(), acc.getValue());
        if (count != null) {
            currentMap.put(acc.getValue(), count);
        }
        //DecimalFormat df = new DecimalFormat("##.##");
        Map<String, Object> accMap = infoMap.get(RedisConstant.DIFF);
        if (accMap == null) {
            accMap = new LinkedHashMap<>();
            infoMap.put(RedisConstant.DIFF, accMap);
        }
        accMap.putAll(currentMap);
    }

    /**
     * 获取累加参数值
     *
     * @param currentInfoMap
     * @return 累加差值map
     */
    private Table<RedisConstant, String, Long> getAccumulationDiff(
            Map<RedisConstant, Map<String, Object>> currentInfoMap,
            Map<String, Object> lastInfoMap) {
        //没有上一次统计快照，忽略差值统计
        if (lastInfoMap == null || lastInfoMap.isEmpty()) {
            return HashBasedTable.create();
        }
        Map<RedisInfoEnum, Long> currentMap = new LinkedHashMap<RedisInfoEnum, Long>();
        for (RedisInfoEnum acc : RedisInfoEnum.getNeedCalDifRedisInfoEnumList()) {
            Long count = getCommonCount(currentInfoMap, acc.getRedisConstant(), acc.getValue());
            if (count != null) {
                currentMap.put(acc, count);
            }
        }
        Map<RedisInfoEnum, Long> lastMap = new LinkedHashMap<RedisInfoEnum, Long>();
        for (RedisInfoEnum acc : RedisInfoEnum.getNeedCalDifRedisInfoEnumList()) {
            Long lastCount = getCommonCount(lastInfoMap, acc.getRedisConstant(), acc.getValue());
            if (lastCount != null) {
                lastMap.put(acc, lastCount);
            }
        }
        Table<RedisConstant, String, Long> resultTable = HashBasedTable.create();
        for (RedisInfoEnum key : currentMap.keySet()) {
            Long value = MapUtils.getLong(currentMap, key, null);
            Long lastValue = MapUtils.getLong(lastMap, key, null);
            if (value == null || lastValue == null) {
                //忽略
                continue;
            }
            long diff = 0L;
            if (value > lastValue) {
                diff = value - lastValue;
            }
            resultTable.put(key.getRedisConstant(), key.getValue(), diff);
        }
        return resultTable;
    }

    /**
     * 获取累加参数值
     *
     * @param currentInfoMap
     * @return 累加差值map
     */
    private Table<RedisConstant, String, Double> getDoubleAccumulationDiff(
            Map<RedisConstant, Map<String, Object>> currentInfoMap,
            Map<String, Object> lastInfoMap) {
        //没有上一次统计快照，忽略差值统计
        if (lastInfoMap == null || lastInfoMap.isEmpty()) {
            return HashBasedTable.create();
        }
        Map<RedisInfoEnum, Double> currentMap = new LinkedHashMap<RedisInfoEnum, Double>();
        for (RedisInfoEnum acc : otherNeedCalDifRedisInfoEnumList) {
            Double count = getDoubleCount(currentInfoMap, acc.getRedisConstant(), acc.getValue());
            if (count != null) {
                currentMap.put(acc, count);
            }
        }
        Map<RedisInfoEnum, Double> lastMap = new LinkedHashMap<RedisInfoEnum, Double>();
        for (RedisInfoEnum acc : otherNeedCalDifRedisInfoEnumList) {
            Double lastCount = getDoubleCount(lastInfoMap, acc.getRedisConstant(), acc.getValue());
            if (lastCount != null) {
                lastMap.put(acc, lastCount);
            }
        }
        DecimalFormat df = new DecimalFormat("##.##");
        Table<RedisConstant, String, Double> resultTable = HashBasedTable.create();
        for (RedisInfoEnum key : currentMap.keySet()) {
            Double value = MapUtils.getDouble(currentMap, key, null);
            Double lastValue = MapUtils.getDouble(lastMap, key, null);
            if (value == null || lastValue == null) {
                //忽略
                continue;
            }
            double diff = 0D;
            if (value > lastValue) {
                diff = value - lastValue;
            }
            diff = Double.parseDouble(df.format(diff));
            resultTable.put(key.getRedisConstant(), key.getValue(), diff);
        }
        return resultTable;
    }

    /**
     * 获取命令差值统计
     *
     * @param currentInfoMap
     * @param lastInfoMap
     * @return 命令统计
     */
    private Table<RedisConstant, String, Long> getCommandsDiff(Map<RedisConstant, Map<String, Object>> currentInfoMap,
                                                               Map<String, Object> lastInfoMap) {
        //没有上一次统计快照，忽略差值统计
        if (lastInfoMap == null || lastInfoMap.isEmpty()) {
            return HashBasedTable.create();
        }
        Map<String, Object> map = currentInfoMap.get(RedisConstant.Commandstats);
        Map<String, Long> currentMap = transferLongMap(map);
        Map<String, Object> lastObjectMap;
        if (lastInfoMap.get(RedisConstant.Commandstats.getValue()) == null) {
            lastObjectMap = new HashMap<String, Object>();
        } else {
            lastObjectMap = (Map<String, Object>) lastInfoMap.get(RedisConstant.Commandstats.getValue());
        }
        Map<String, Long> lastMap = transferLongMap(lastObjectMap);

        Table<RedisConstant, String, Long> resultTable = HashBasedTable.create();
        for (String command : currentMap.keySet()) {
            long lastCount = MapUtils.getLong(lastMap, command, 0L);
            long currentCount = MapUtils.getLong(currentMap, command, 0L);
            if (currentCount > lastCount) {
                resultTable.put(RedisConstant.Commandstats, command, currentCount - lastCount);
            }
        }
        return resultTable;
    }

    private AppStats getAppStats(long appId, long collectTime, Table<RedisConstant, String, Long> table,
                                 Map<RedisConstant, Map<String, Object>> infoMap) {
        AppStats appStats = new AppStats();
        appStats.setAppId(appId);
        appStats.setCollectTime(collectTime);
        appStats.setModifyTime(new Date());
        appStats.setUsedMemory(
                MapUtils.getLong(infoMap.get(RedisConstant.Memory), RedisInfoEnum.used_memory.getValue(), 0L));
        appStats.setUsedMemoryRss(
                MapUtils.getLong(infoMap.get(RedisConstant.Memory), RedisInfoEnum.used_memory_rss.getValue(), 0L));
        appStats.setHits(MapUtils.getLong(table.row(RedisConstant.Stats), RedisInfoEnum.keyspace_hits.getValue(), 0L));
        appStats.setMisses(
                MapUtils.getLong(table.row(RedisConstant.Stats), RedisInfoEnum.keyspace_misses.getValue(), 0L));
        appStats.setEvictedKeys(
                MapUtils.getLong(table.row(RedisConstant.Stats), RedisInfoEnum.evicted_keys.getValue(), 0L));
        appStats.setExpiredKeys(
                MapUtils.getLong(table.row(RedisConstant.Stats), RedisInfoEnum.expired_keys.getValue(), 0L));
        appStats.setNetInputByte(
                MapUtils.getLong(table.row(RedisConstant.Stats), RedisInfoEnum.total_net_input_bytes.getValue(), 0L));
        appStats.setNetOutputByte(
                MapUtils.getLong(table.row(RedisConstant.Stats), RedisInfoEnum.total_net_output_bytes.getValue(), 0L));

        appStats.setConnectedClients(MapUtils.getIntValue(infoMap.get(RedisConstant.Clients),
                RedisInfoEnum.connected_clients.getValue(), 0));
        appStats.setObjectSize(getObjectSize(infoMap));

        appStats.setCpuSys(MapUtils.getLongValue(table.row(RedisConstant.CPU), RedisInfoEnum.used_cpu_sys.getValue(),
                0));
        appStats.setCpuUser(
                MapUtils.getLongValue(table.row(RedisConstant.CPU), RedisInfoEnum.used_cpu_user.getValue(),
                        0));
        appStats.setCpuSysChildren(
                MapUtils.getLongValue(table.row(RedisConstant.CPU), RedisInfoEnum.used_cpu_sys_children.getValue(),
                        0));
        appStats.setCpuUserChildren(
                MapUtils.getLongValue(table.row(RedisConstant.CPU), RedisInfoEnum.used_cpu_user_children.getValue(),
                        0));
        logger.debug("appStats={} table={}", appStats, table);
        return appStats;
    }

    private long getObjectSize(Map<RedisConstant, Map<String, Object>> currentInfoMap) {
        Map<String, Object> sizeMap = currentInfoMap.get(RedisConstant.Keyspace);
        if (sizeMap == null || sizeMap.isEmpty()) {
            return 0L;
        }
        long result = 0L;
        Map<String, Long> longSizeMap = transferLongMap(sizeMap);

        for (Map.Entry<String, Long> entry : longSizeMap.entrySet()) {
            result += entry.getValue();
        }
        return result;
    }

    private Long getCommonCount(Map<?, ?> infoMap, RedisConstant redisConstant, String commond) {
        Object constantObject =
                infoMap.get(redisConstant) == null ? infoMap.get(redisConstant.getValue()) : infoMap.get(redisConstant);
        if (constantObject != null && (constantObject instanceof Map)) {
            Map constantMap = (Map) constantObject;
            if (constantMap.get(commond) == null) {
                return null;
            }
            return MapUtils.getLongValue(constantMap, commond);
        }
        return null;
    }

    private Double getDoubleCount(Map<?, ?> infoMap, RedisConstant redisConstant, String commond) {
        Object constantObject =
                infoMap.get(redisConstant) == null ? infoMap.get(redisConstant.getValue()) : infoMap.get(redisConstant);
        if (constantObject != null && (constantObject instanceof Map)) {
            Map constantMap = (Map) constantObject;
            return MapUtils.getDoubleValue(constantMap, commond);
        }
        return null;
    }

    /**
     * 转换redis 命令行统计结果
     *
     * @param commandMap
     * @return
     */
    private Map<String, Long> transferLongMap(Map<String, Object> commandMap) {
        Map<String, Long> resultMap = new HashMap<String, Long>();
        if (commandMap == null || commandMap.isEmpty()) {
            return resultMap;
        }
        for (Map.Entry<String, Object> entry : commandMap.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            String key = entry.getKey();
            String value = entry.getValue().toString();
            String[] stats = value.split(",");
            if (stats.length == 0) {
                continue;
            }
            String[] calls = stats[0].split("=");
            if (calls == null || calls.length < 2) {
                continue;
            }
            long callCount = Long.parseLong(calls[1]);
            resultMap.put(key, callCount);
        }
        return resultMap;
    }

    private List<AppCommandStats> getCommandStatsList(long appId, long collectTime,
                                                      Table<RedisConstant, String, Long> table) {
        Map<String, Long> commandMap = table.row(RedisConstant.Commandstats);
        List<AppCommandStats> list = new ArrayList<AppCommandStats>();
        if (commandMap == null) {
            return list;
        }
        for (String key : commandMap.keySet()) {
            String commandName = key.replace("cmdstat_", "");
            long callCount = MapUtils.getLong(commandMap, key, 0L);
            if (callCount == 0L) {
                continue;
            }
            AppCommandStats commandStats = new AppCommandStats();
            commandStats.setAppId(appId);
            commandStats.setCollectTime(collectTime);
            commandStats.setCommandName(commandName);
            commandStats.setCommandCount(callCount);
            commandStats.setModifyTime(new Date());
            list.add(commandStats);
        }
        return list;
    }

    /**
     * 处理clusterinfo统计信息
     *
     * @param clusterInfo
     * @return
     */
    private Map<String, Object> processClusterInfoStats(String clusterInfo) {
        Map<String, Object> clusterInfoMap = new HashMap<String, Object>();
        String[] lines = clusterInfo.split("\r\n");
        for (String line : lines) {
            String[] pair = line.split(":");
            if (pair.length == 2) {
                clusterInfoMap.put(pair[0], pair[1]);
            }
        }
        return clusterInfoMap;
    }

    /**
     * 处理redis统计信息
     *
     * @param statResult 统计结果串
     */
    private Map<RedisConstant, Map<String, Object>> processRedisStats(String statResult) {
        Map<RedisConstant, Map<String, Object>> redisStatMap = new HashMap<RedisConstant, Map<String, Object>>();
        String[] data = statResult.split("\r\n");
        String key;
        int i = 0;
        int length = data.length;
        while (i < length) {
            if (data[i].contains("#")) {
                int index = data[i].indexOf('#');
                key = data[i].substring(index + 1);
                ++i;
                RedisConstant redisConstant = RedisConstant.value(key.trim());
                if (redisConstant == null) {
                    continue;
                }
                Map<String, Object> sectionMap = new LinkedHashMap<String, Object>();
                while (i < length && data[i].contains(":")) {
                    String[] pair = StringUtils.splitByWholeSeparator(data[i], ":");
                    sectionMap.put(pair[0], pair[1]);
                    i++;
                }
                redisStatMap.put(redisConstant, sectionMap);
            } else {
                i++;
            }
        }
        return redisStatMap;
    }

    /**
     * 根据infoMap的结果判断实例的主从
     *
     * @param infoMap
     * @return
     */
    private BooleanEnum hasSlaves(Map<RedisConstant, Map<String, Object>> infoMap) {
        Map<String, Object> replicationMap = infoMap.get(RedisConstant.Replication);
        if (MapUtils.isEmpty(replicationMap)) {
            return BooleanEnum.OTHER;
        }
        for (Entry<String, Object> entry : replicationMap.entrySet()) {
            String key = entry.getKey();
            //判断一个即可
            if (key != null && key.contains("slave0")) {
                return BooleanEnum.TRUE;
            }
        }
        return BooleanEnum.FALSE;
    }

    /**
     * 根据infoMap的结果判断实例的主从
     *
     * @param infoMap
     * @return
     */
    private BooleanEnum isMaster(Map<RedisConstant, Map<String, Object>> infoMap) {
        Map<String, Object> map = infoMap.get(RedisConstant.Replication);
        if (map == null || map.get(RedisInfoEnum.role.getValue()) == null) {
            //return null;
            return BooleanEnum.OTHER;
        }
        if (String.valueOf(map.get(RedisInfoEnum.role.getValue())).equals("master")) {
            //return true;
            return BooleanEnum.TRUE;
        }
        //return false;
        return BooleanEnum.FALSE;
    }

    /**
     * 根据ip和port判断某一个实例当前是主还是从
     *
     * @param ip   ip
     * @param port port
     * @return 主返回true， 从返回false；
     */
    @Override
    public BooleanEnum isMaster(long appId, String ip, int port) {
        Jedis jedis = null;
        try {
            jedis = getJedis(appId, ip, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
            String info = jedis.info("all");
            Map<RedisConstant, Map<String, Object>> infoMap = processRedisStats(info);
            return isMaster(infoMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BooleanEnum.OTHER;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    /**
     * 根据infoMap的结果判断实例的主从
     *
     * @param infoMap
     * @return
     */
    private BooleanEnum isSlaveAndPointedMasterUp(Map<RedisConstant, Map<String, Object>> infoMap, InstanceInfo masterInstance) {
        if(masterInstance == null){
            return BooleanEnum.FALSE;
        }
        Map<String, Object> map = infoMap.get(RedisConstant.Replication);
        if (map == null || map.get(RedisInfoEnum.role.getValue()) == null) {
            //return null;
            return BooleanEnum.FALSE;
        }
        if (String.valueOf(map.get(RedisInfoEnum.role.getValue())).equals("slave")
                && (String.valueOf(map.get(RedisInfoEnum.master_link_status.getValue())).equals("up"))
                && (String.valueOf(map.get(RedisInfoEnum.master_host.getValue())).equals(masterInstance.getIp()))
                && (String.valueOf(map.get(RedisInfoEnum.master_port.getValue())).equals(String.valueOf(masterInstance.getPort())))
        ){
            return BooleanEnum.TRUE;
        }
        return BooleanEnum.FALSE;
    }

    /**
     * 判断实例是否为从节点，并且与主节点连接有效
     * @param appDesc
     * @param slaveInstance
     * @param masterInstance
     * @return
     */
    @Override
    public BooleanEnum isSlaveAndPointedMasterUp(AppDesc appDesc, InstanceInfo slaveInstance, InstanceInfo masterInstance){
        Jedis jedis = null;
        try {
            jedis = getJedis(slaveInstance.getIp(), slaveInstance.getPort(), appDesc.getAppPassword());
            String info = jedis.info("all");
            Map<RedisConstant, Map<String, Object>> infoMap = processRedisStats(info);
            return isSlaveAndPointedMasterUp(infoMap, masterInstance);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BooleanEnum.FALSE;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public long getDbSize(long appId, String ip, int port) {
        Jedis jedis = getJedis(appId, ip, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
        try {
            return jedis.dbSize();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        } finally {
            jedis.close();
        }
    }

    /**
     * @Description: scan key
     * @Author: caoru
     * @CreateDate: 2018/11/13 16:08
     */
    @Async
    public Future<List<String>> findInstancePatternKeys(long appId, String ip, int port, String pattern) {
        List<String> list = new ArrayList<String>();
        Jedis jedis = getJedis(appId, ip, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
        try {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams params = new ScanParams().match(pattern).count(COUNT);
            do {
                ScanResult<String> result = jedis.scan(cursor, params);
                list.addAll(result.getResult());
                cursor = result.getCursor();
            } while (!"0".equals(cursor));
            return new AsyncResult<List<String>>(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new AsyncResult<List<String>>(list);
        } finally {
            jedis.close();
        }
    }

    /**
     * @Description: 查询单实例的big key
     * @Author: caoru
     * @CreateDate: 2018/11/13 16:08
     */
    public List<String> findInstanceBigKey(long appId, String ip, int port, long startBytes, long endBytes) {
        List<String> list = new ArrayList<String>();
        Jedis jedis = getJedis(appId, ip, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
        try {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams params = new ScanParams().count(COUNT);
            do {
                ScanResult<String> result = jedis.scan(cursor, params);
                for (String key : result.getResult()) {
                    String keyType = jedis.type(key);
                    if ("string".equals(keyType)) {
                        long len = jedis.strlen(key);
                        if (len > startBytes && len < endBytes) {
                            list.add(key);
                        }
                    } else {
                        String debugRes = jedis.debug(DebugParams.OBJECT(key));
                        long serializedlength = 0;
                        for (String param : Arrays.asList(debugRes.split(" "))) {
                            if (param.startsWith("serializedlength")) {
                                serializedlength = Long.parseLong(param.split(":")[1]);
                            }
                        }
                        if (serializedlength > startBytes && serializedlength < endBytes) {
                            list.add(key);
                        }
                    }
                }
                cursor = result.getCursor();
            } while (!"0".equals(cursor));
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return list;
        } finally {
            jedis.close();
        }
    }

    /**
     * @Description: 查询应用的big key
     * @Author: caoru
     * @CreateDate: 2018/11/13 16:08
     */
    public List<String> findClusterBigKey(long appId, long startBytes, long endBytes) {
        List<String> list = new ArrayList<String>();
        List<InstanceInfo> allMasterInstance = getAllHealthyInstanceInfo(appId);
        for (InstanceInfo masterInstance : allMasterInstance) {
            String ip = masterInstance.getIp();
            int port = masterInstance.getPort();
            List<String> res = findInstanceBigKey(appId, ip, port, startBytes, endBytes);
            list.addAll(res);
        }
        return list;
    }

    /**
     * @Description: 查询单实例的idle key
     * @Author: caoru
     * @CreateDate: 2018/11/13 16:08
     */
    public List<String> findInstanceIdleKeys(long appId, String ip, int port, long idleDays) {
        List<String> list = new ArrayList<String>();
        Jedis jedis = getJedis(appId, ip, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
        try {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams params = new ScanParams().count(COUNT);
            do {
                ScanResult<String> result = jedis.scan(cursor, params);
                for (String key : result.getResult()) {
                    String debugRes = jedis.debug(DebugParams.OBJECT(key));
                    long lruSecondsIdle = 0;
                    for (String param : Arrays.asList(debugRes.split(" "))) {
                        if (param.startsWith("lru_seconds_idle")) {
                            lruSecondsIdle = Long.parseLong(param.split(":")[1]);
                        }
                    }
                    if (lruSecondsIdle > idleDays * 3600 * 24) {
                        list.add(key);
                    }
                }
                cursor = result.getCursor();
            } while (!"0".equals(cursor));
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return list;
        } finally {
            jedis.close();
        }
    }

    /**
     * @Description: 查询应用的idle key
     * @Author: caoru
     * @CreateDate: 2018/11/13 16:08
     */
    public List<String> findClusterIdleKeys(long appId, long idleDays) {
        List<String> list = new ArrayList<String>();
        List<InstanceInfo> allMasterInstance = getAllHealthyInstanceInfo(appId);
        for (InstanceInfo masterInstance : allMasterInstance) {
            String ip = masterInstance.getIp();
            int port = masterInstance.getPort();
            List<String> res = findInstanceIdleKeys(appId, ip, port, idleDays);
            list.addAll(res);
        }
        return list;
    }

    /**
     * @Description: 查询单实例匹配的pattern
     * @Author: caoru
     * @CreateDate: 2018/11/13 16:08
     */
    public void delInstancePatternKeys(long appId, String ip, int port, String pattern) {
        Jedis jedis = getJedis(appId, ip, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
        try {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams params = new ScanParams().match(pattern).count(COUNT);
            do {
                ScanResult<String> result = jedis.scan(cursor, params);
                for (String key : result.getResult()) {
                    jedis.del(key);
                }
                cursor = result.getCursor();
            } while (!"0".equals(cursor));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }
    }

    /**
     * @Description: 查询应用的匹配的pattern
     * @Author: caoru
     * @CreateDate: 2018/11/13 16:08
     */
    public void delClusterPatternKey(long appId, String pattern) {
        List<InstanceInfo> allMasterInstance = getAllHealthyInstanceInfo(appId);
        for (InstanceInfo masterInstance : allMasterInstance) {
            String ip = masterInstance.getIp();
            int port = masterInstance.getPort();
            delInstancePatternKeys(appId, ip, port, pattern);
        }
    }

    /**
     * 根据ip和port判断redis实例当前是否有从节点
     *
     * @param ip   ip
     * @param port port
     * @return 主返回true，从返回false；
     */
    public BooleanEnum hasSlaves(long appId, String ip, int port) {
        Jedis jedis = getJedis(appId, ip, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
        try {
            String info = jedis.info("all");
            Map<RedisConstant, Map<String, Object>> infoMap = processRedisStats(info);
            return hasSlaves(infoMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            jedis.close();
        }
    }

    @Override
    public HostAndPort getMaster(String ip, int port, String password) {
        JedisPool jedisPool = maintainJedisPool(ip, port, password);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String info = jedis.info(RedisConstant.Replication.getValue());
            Map<RedisConstant, Map<String, Object>> infoMap = processRedisStats(info);
            Map<String, Object> map = infoMap.get(RedisConstant.Replication);
            if (map == null) {
                return null;
            }
            String masterHost = MapUtils.getString(map, RedisInfoEnum.master_host.getValue(), null);
            int masterPort = MapUtils.getInteger(map, RedisInfoEnum.master_port.getValue(), 0);
            if (StringUtils.isNotBlank(masterHost) && masterPort > 0) {
                return new HostAndPort(masterHost, masterPort);
            }
            return null;
        } catch (Exception e) {
            logger.error("{}:{} getMaster failed {}", ip, port, e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    public HostAndPort getSlave0(String ip, int port, String password) {
        JedisPool jedisPool = maintainJedisPool(ip, port, password);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String info = jedis.info(RedisConstant.Replication.getValue());
            Map<RedisConstant, Map<String, Object>> infoMap = processRedisStats(info);
            Map<String, Object> map = infoMap.get(RedisConstant.Replication);
            if (map == null) {
                return null;
            }
            String slaveInfo = MapUtils.getString(map, "slave0");
            String slaveHost = "";
            int slavePort = 0;
            if (!StringUtil.isBlank(slaveInfo)) {
                for (String slave0 : slaveInfo.split(",")) {
                    if (slave0.indexOf("ip") > -1) {
                        slaveHost = slave0.replaceAll("ip=", "");
                    }
                    if (slave0.indexOf("port") > -1) {
                        slavePort = Integer.parseInt(slave0.replaceAll("port=", ""));
                    }
                }
            }
            if (StringUtils.isNotBlank(slaveHost) && slavePort > 0) {
                return new HostAndPort(slaveHost, slavePort);
            }
            return null;
        } catch (Exception e) {
            logger.error("{}:{} getMaster failed {}", ip, port, e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public boolean isRun(final String ip, final int port, final int retryTimes) {
        return isRun(ip, port, null, retryTimes);
    }

    public boolean isRun(final String ip, final int port, final String password, final int retryTimes) {
        boolean isRun = new IdempotentConfirmer(retryTimes) {
            private int timeOutFactor = 1;

            @Override
            public boolean execute() {
                Jedis jedis = null;
                try {
                    jedis = getJedis(ip, port, password);
                    jedis.getClient().setConnectionTimeout(Protocol.DEFAULT_TIMEOUT * (timeOutFactor++));
                    jedis.getClient().setSoTimeout(Protocol.DEFAULT_TIMEOUT * (timeOutFactor++));
                    String pong = jedis.ping();
                    return pong != null && pong.equalsIgnoreCase("PONG");
                } catch (JedisDataException e) {
                    String message = e.getMessage();
                    logger.warn(e.getMessage());
                    if (StringUtils.isNotBlank(message) && message.startsWith("LOADING")) {
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    logger.warn("{}:{} error message is {} ", ip, port, e.getMessage());
                    return false;
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }
        }.run();
        return isRun;
    }

    @Override
    public boolean isRun(final String ip, final int port) {
        return isRun(ip, port, null);
    }

    @Override
    public boolean isRun(final long appId, final String ip, final int port) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        return isRun(ip, port, appDesc.getAppPassword());
    }

    @Override
    public boolean isRun(final String ip, final int port, final String password) {
        boolean isRun = new IdempotentConfirmer() {
            private int timeOutFactor = 1;

            @Override
            public boolean execute() {
                Jedis jedis = null;
                try {
                    jedis = getJedis(ip, port, password);
                    jedis.getClient().setConnectionTimeout(Protocol.DEFAULT_TIMEOUT * (timeOutFactor++));
                    jedis.getClient().setSoTimeout(Protocol.DEFAULT_TIMEOUT * (timeOutFactor++));
                    String pong = jedis.ping();
                    return pong != null && pong.equalsIgnoreCase("PONG");
                } catch (JedisDataException e) {
                    String message = e.getMessage();
                    logger.warn(e.getMessage());
                    if (StringUtils.isNotBlank(message) && message.startsWith("LOADING")) {
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    logger.warn("{}:{} error count={} message is {} ", ip, port, timeOutFactor, e.getMessage());
                    return false;
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }
        }.run();
        return isRun;
    }

    @Override
    public boolean shutdown(long appId, String ip, int port) {
        boolean isRun = isRun(appId, ip, port);
        if (!isRun) {
            return true;
        }
        final Jedis jedis = getJedis(appId, ip, port);
        try {
            //关闭实例节点
            boolean isShutdown = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    jedis.shutdown();
                    return true;
                }
            }.run();
            if (!isShutdown) {
                logger.error("{}:{} redis not shutdown!", ip, port);
            }
            return isShutdown;
        } finally {
            jedis.close();
        }
    }

    @Override
    public boolean forget(long appId, String ip, int port, String nodeId) {
        boolean isRun = isRun(appId, ip, port);     //todo: 除了isRun，是否还需要其他判断条件
        if (!isRun) {
            return true;
        }

        boolean isForget = new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String response = null;
                Jedis jedis = null;
                try {
                    jedis = getJedis(appId, ip, port);
                    response = jedis.clusterForget(nodeId);
                } catch (JedisDataException jde) {
                    //处于handshake状态的节点会抛异常：ERR Unknown node 92e90269c5f86a663a692c5bcf766ecdda80aa9e
                    logger.error(jde.getMessage(), jde);
                    response = "OK";
                } catch (Exception e) {
                    logger.error("appId {} instance {}:{}  forget instance {} error!", appId, ip, port, nodeId, e);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
                return response != null && response.equalsIgnoreCase("OK");
            }
        }.run();
        return isForget;
    }

    @Override
    public boolean shutdown(String ip, int port) {
        boolean isRun = isRun(ip, port);
        if (!isRun) {
            return true;
        }
        final Jedis jedis = getJedis(ip, port);
        try {
            //关闭实例节点
            boolean isShutdown = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    jedis.shutdown();
                    return true;
                }
            }.run();
            if (!isShutdown) {
                logger.error("{}:{} redis not shutdown!", ip, port);
            }
            return isShutdown;
        } finally {
            jedis.close();
        }
    }

    @Override
    public boolean checkShutdownSuccess(InstanceInfo instanceInfo){
        if(instanceInfo == null){
            return false;
        }
        //关闭节点后，判断配置文件句柄是否释放
        boolean executeFlag = false;
        int tryTimes = 3;
        long sleepTime = 2L;
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        StringBuilder command = new StringBuilder();
        command.append("ps -ef | grep redis | grep redis-server | grep :").append(port).append("  | grep -v \"grep\"");
        while(tryTimes-- > 0){
            try{
                String execute = SSHUtil.execute(host, command.toString());
                if(StringUtils.isEmpty(execute)){
                    executeFlag = true;
                    break;
                }
                logger.info(String.format("check Instance shutdown not success, will one more time, appId:%s, instance:%s, command:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), command));
                TimeUnit.SECONDS.sleep(sleepTime);
            }catch (Exception e){
                logger.error(String.format("check Instance shutdown error, appId:%s, instance:%s, command:%s, error: ", instanceInfo.getAppId(), instanceInfo.getHostPort(), command), e);
            }
        }
        return executeFlag;
    }

    @Override
    public String getClusterMyId(long appId, String ip, int port) {
        final Jedis jedis = getJedis(appId, ip, port);
        try {
            return jedis.clusterMyId();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        } finally {
            jedis.close();
        }
    }

    @Override
    public String getClusterNodes(long appId, String ip, int port) {
        final Jedis jedis = getJedis(appId, ip, port);
        try {
            return jedis.clusterNodes();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        } finally {
            jedis.close();
        }
    }

    /**
     * 返回当前实例的一些关键指标
     *
     * @param appId
     * @param ip
     * @param port
     * @param infoMap
     * @return
     */
    public InstanceStats getInstanceStats(long appId, String ip, int port,
                                          Map<RedisConstant, Map<String, Object>> infoMap) {
        if (infoMap == null) {
            return null;
        }
        // 查询最大内存限制
        Long maxMemory = this.getRedisMaxMemory(appId, ip, port);
        /**
         * 将实例的一些关键指标返回
         */
        InstanceStats instanceStats = new InstanceStats();
        instanceStats.setAppId(appId);
        InstanceInfo curInst = instanceDao.getInstByIpAndPort(ip, port);
        if (curInst != null) {
            instanceStats.setHostId(curInst.getHostId());
            instanceStats.setInstId(curInst.getId());
        } else {
            logger.error("redis={}:{} not found", ip, port);
            return null;
        }
        instanceStats.setIp(ip);
        instanceStats.setPort(port);
        if (maxMemory != null) {
            instanceStats.setMaxMemory(maxMemory);
        }
        instanceStats.setUsedMemory(
                MapUtils.getLongValue(infoMap.get(RedisConstant.Memory), RedisInfoEnum.used_memory.getValue(), 0));
        instanceStats.setHits(
                MapUtils.getLongValue(infoMap.get(RedisConstant.Stats), RedisInfoEnum.keyspace_hits.getValue(), 0));
        instanceStats.setMisses(
                MapUtils.getLongValue(infoMap.get(RedisConstant.Stats), RedisInfoEnum.keyspace_misses.getValue(), 0));
        instanceStats.setCurrConnections(
                MapUtils.getIntValue(infoMap.get(RedisConstant.Clients), RedisInfoEnum.connected_clients.getValue(),
                        0));
        instanceStats.setCurrItems(getObjectSize(infoMap));
        instanceStats.setRole((byte) 1);
        if (MapUtils.getString(infoMap.get(RedisConstant.Replication), RedisInfoEnum.role.getValue()).equals("slave")) {
            instanceStats.setRole((byte) 2);
        }
        instanceStats.setModifyTime(new Timestamp(System.currentTimeMillis()));
        instanceStats.setMemFragmentationRatio(MapUtils.getDoubleValue(infoMap.get(RedisConstant.Memory),
                RedisInfoEnum.mem_fragmentation_ratio.getValue(), 0.0));
        instanceStats.setAofDelayedFsync(
                MapUtils.getIntValue(infoMap.get(RedisConstant.Persistence), RedisInfoEnum.aof_delayed_fsync.getValue(),
                        0));
        return instanceStats;
    }

    @Override
    public Long getRedisMaxMemory(final long appId, final String ip, final int port) {
        final String key = "maxmemory";
        final Map<String, Long> resultMap = new HashMap<String, Long>();
        boolean isSuccess = new IdempotentConfirmer() {
            private int timeOutFactor = 1;

            @Override
            public boolean execute() {
                Jedis jedis = null;
                try {
                    jedis = getJedis(appId, ip, port);
                    jedis.getClient().setConnectionTimeout(REDIS_DEFAULT_TIME * (timeOutFactor++));
                    jedis.getClient().setSoTimeout(REDIS_DEFAULT_TIME * (timeOutFactor++));
                    List<String> maxMemoryList = jedis.configGet(key); // 返回结果：list中是2个字符串，如："maxmemory",
                    // "4096000000"
                    if (maxMemoryList != null && maxMemoryList.size() >= 2) {
                        resultMap.put(key, Long.valueOf(maxMemoryList.get(1)));
                    }
                    return MapUtils.isNotEmpty(resultMap);
                } catch (Exception e) {
                    logger.warn("{}:{} errorMsg: {}", ip, port, e.getMessage());
                    return false;
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }
        }.run();
        if (isSuccess) {
            return MapUtils.getLong(resultMap, key);
        } else {
            logger.error("{}:{} getMaxMemory failed!", ip, port);
            return null;
        }
    }

    @Override
    public String executeCommand(AppDesc appDesc, String command, String userName) {
        //非测试应用只能执行白名单里面的命令
        if (AppDescEnum.AppTest.NOT_TEST.getValue() == appDesc.getIsTest()) {
            if (!RedisReadOnlyCommandEnum.contains(command)) {
                return "online app only support read-only and safe command";
            }
        }
        int type = appDesc.getType();
        long appId = appDesc.getAppId();
        String password = appDesc.getAppPassword();
        if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            JedisSentinelPool jedisSentinelPool = getJedisSentinelPool(appDesc);
            if (jedisSentinelPool == null) {
                return "sentinel can not execute ";
            }
            Jedis jedis = null;
            try {
                jedis = jedisSentinelPool.getResource();
                String host = jedis.getClient().getHost();
                int port = jedis.getClient().getPort();
                return executeCommand(appId, host, port, command);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return "运行出错:" + e.getMessage();
            } finally {
                if (jedis != null)
                    jedis.close();
                jedisSentinelPool.destroy();
            }
        } else if (type == ConstUtils.CACHE_REDIS_STANDALONE) {
            List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
            if (instanceList == null || instanceList.isEmpty()) {
                return "应用没有运行的实例";
            }
            String host = null;
            int port = 0;
            for (InstanceInfo instanceInfo : instanceList) {
                host = instanceInfo.getIp();
                port = instanceInfo.getPort();
                break;
            }
            try {
                return executeCommand(appId, host, port, command);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return "运行出错:" + e.getMessage();
            }
        } else if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
            if (instanceList == null || instanceList.isEmpty()) {
                return "应用没有运行的实例";
            }
            Set<HostAndPort> clusterHosts = new LinkedHashSet<HostAndPort>();
            for (InstanceInfo instance : instanceList) {
                if (instance != null && instance.isOnline()) {
                    clusterHosts.add(new HostAndPort(instance.getIp(), instance.getPort()));
                }
            }
            if (clusterHosts.isEmpty()) {
                return "no run instance";
            }
            String commandKey = getCommandKey(command);
            if(StringUtils.isEmpty(commandKey)){
                logger.error(String.format("executeCommand with empty commandKey, appDesc is : %s, command is: %s, user is : %s", appDesc.getAppId(), command, userName));
            }
            for (HostAndPort hostAndPort : clusterHosts) {
                HostAndPort rightHostAndPort = null;
                if(commandKey != null){
                    rightHostAndPort = getClusterRightHostAndPort(hostAndPort.getHost(), hostAndPort.getPort(),
                            password, command, commandKey);
                }else{
                    rightHostAndPort = hostAndPort;
                }
                if (rightHostAndPort != null) {
                    try {
                        return executeCommand(appId, rightHostAndPort.getHost(), rightHostAndPort.getPort(), command);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        return "运行出错:" + e.getMessage();
                    }
                }
            }

        }
        return "不支持应用类型";
    }

    /**
     * 获取key对应的节点
     *
     * @param host
     * @param port
     * @param password
     * @param command
     * @param key
     * @return
     */
    private HostAndPort getClusterRightHostAndPort(String host, int port, String password, String command, String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis(host, port, password);
            jedis.type(key);
            return new HostAndPort(host, port);
        } catch (JedisMovedDataException e) {
            return e.getTargetNode();
        } catch (JedisAskDataException e) {
            return e.getTargetNode();
        } catch (Exception e) {
            logger.error("command {} is error", command, e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private String getCommandKey(String command) {
        String[] array = StringUtils.trim(command).split("\\s+");
        if (array.length > 1) {
            return array[1];
        } else {
            return null;
        }
    }

    @Override
    public String executeCommand(long appId, String host, int port, String command) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return "not exist appId";
        }
        //非测试应用只能执行白名单里面的命令
        if (AppDescEnum.AppTest.NOT_TEST.getValue() == appDesc.getIsTest()) {
            if (!RedisReadOnlyCommandEnum.contains(command)) {
                return "online app only support read-only and safe command ";
            }
        }
        String password = appDesc.getAppPassword();
        String shell = RedisProtocol.getExecuteCommandShell(host, port, password, command);
        //记录客户端发送日志
        logger.warn("executeRedisShell={}", shell);
        return machineCenter.executeShell(host, shell);
    }

    @Override
    public String executeAdminCommand(long appId, String host, int port, String command, Integer timeout) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return "not exist appId";
        }
        String password = appDesc.getAppPassword();
        String shell = RedisProtocol.getExecuteAdminCommandShell(host, port, password, command);
        //记录客户端发送日志
        logger.warn("executeRedisShell={}", shell);
        return machineCenter.executeShell(host, shell, timeout);
    }

    @Override
    public JedisSentinelPool getJedisSentinelPool(AppDesc appDesc) {
        if (appDesc == null) {
            logger.error("appDesc is null");
            return null;
        }
        if (appDesc.getType() != ConstUtils.CACHE_REDIS_SENTINEL) {
            logger.error("type={} is not sentinel", appDesc.getType());
            return null;
        }
        long appId = appDesc.getAppId();
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        instanceInfos = instanceInfos.stream().filter(instanceInfo -> instanceInfo.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus()).collect(Collectors.toList());

        String masterName = null;
        for (Iterator<InstanceInfo> i = instanceInfos.iterator(); i.hasNext(); ) {
            InstanceInfo instanceInfo = i.next();
            if (instanceInfo.getType() != ConstUtils.CACHE_REDIS_SENTINEL) {
                i.remove();
                continue;
            }
            if (masterName == null && StringUtils.isNotBlank(instanceInfo.getCmd())) {
                masterName = instanceInfo.getCmd();
            }
        }
        Set<String> sentinels = new HashSet<String>();
        for (InstanceInfo instanceInfo : instanceInfos) {
            sentinels.add(instanceInfo.getIp() + ":" + instanceInfo.getPort());
        }
        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(masterName, sentinels);
        return jedisSentinelPool;
    }

    @Override
    public Map<String, String> getRedisConfigList(int instanceId) {
        if (instanceId <= 0) {
            return Collections.emptyMap();
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        if (instanceInfo == null) {
            return Collections.emptyMap();
        }
        if (TypeUtil.isRedisType(instanceInfo.getType())) {
            Jedis jedis = null;
            try {
                jedis = getJedis(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(),
                        REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
                List<String> configs = jedis.configGet("*");
                Map<String, String> configMap = new LinkedHashMap<String, String>();
                for (int i = 0; i < configs.size(); i += 2) {
                    if (i < configs.size()) {
                        String key = configs.get(i);
                        String value = configs.get(i + 1);
                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        configMap.put(key, value);
                    }
                }
                return configMap;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        return Collections.emptyMap();
    }

    @Override
    public List<RedisSlowLog> getRedisSlowLogs(int instanceId, int maxCount) {
        if (instanceId <= 0) {
            return Collections.emptyList();
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        if (instanceInfo == null) {
            return Collections.emptyList();
        }
        if (TypeUtil.isRedisType(instanceInfo.getType())) {
            return getRedisSlowLogs(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), maxCount);
        }
        return Collections.emptyList();
    }

    private List<RedisSlowLog> getRedisSlowLogs(long appId, String host, int port, int maxCount) {
        Jedis jedis = null;
        try {
            jedis = getJedis(appId, host, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
            List<RedisSlowLog> resultList = new ArrayList<RedisSlowLog>();
            List<Slowlog> slowlogs = null;
            if (maxCount > 0) {
                slowlogs = jedis.slowlogGet(maxCount);
            } else {
                slowlogs = jedis.slowlogGet();
            }
            if (slowlogs != null && slowlogs.size() > 0) {
                for (Slowlog sl : slowlogs) {
                    RedisSlowLog rs = new RedisSlowLog();
                    rs.setId(sl.getId());
                    rs.setExecutionTime(sl.getExecutionTime());
                    long time = sl.getTimeStamp() * 1000L;
                    rs.setDate(new Date(time));
                    rs.setTimeStamp(DateUtil.formatYYYYMMddHHMMSS(new Date(time)));
                    rs.setCommand(StringUtils.join(sl.getArgs(), " "));
                    resultList.add(rs);
                }
            }
            return resultList;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean configRewrite(final long appId, final String host, final int port) {
        return new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                Jedis jedis = getJedis(appId, host, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
                try {
                    String response = jedis.configRewrite();
                    return response != null && response.equalsIgnoreCase("OK");
                } finally {
                    jedis.close();
                }
            }
        }.run();
    }

    @Override
    public boolean cleanAppData(AppDesc appDesc, AppUser appUser) {
        if (appDesc == null) {
            return false;
        }

        long appId = appDesc.getAppId();

        // 线上应用不能清理数据
        if (AppDescEnum.AppTest.IS_TEST.getValue() != appDesc.getIsTest()) {
            logger.error("appId {} profile must be test", appId);
            return false;
        }

        // 必须是redis应用
        if (!TypeUtil.isRedisType(appDesc.getType())) {
            logger.error("appId {} type must be redis", appId);
            return false;
        }

        // 实例验证
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
        if (CollectionUtils.isEmpty(instanceList)) {
            logger.error("appId {} instanceList is empty", appId);
            return false;
        }

        // 开始清除
        for (InstanceInfo instance : instanceList) {
            if (instance.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                continue;
            }
            String host = instance.getIp();
            int port = instance.getPort();
            // master + 非sentinel节点
            BooleanEnum isMater = isMaster(appId, host, port);
            if (isMater == BooleanEnum.TRUE && !TypeUtil.isRedisSentinel(instance.getType())) {
                //异步线程处理
                AsyncThreadPoolFactory.DEFAULT_ASYNC_THREAD_POOL.execute(new Runnable() {
                    @Override
                    public void run() {
                        Jedis jedis = getJedis(appId, host, port);
                        jedis.getClient().setConnectionTimeout(REDIS_DEFAULT_TIME);
                        jedis.getClient().setSoTimeout(60000);
                        try {
                            logger.warn("{}:{} start clear data", host, port);
                            long start = System.currentTimeMillis();
                            String result = jedis.flushAll();
                            logger.warn("{}:{} finish clear data :{}, cost time:{} ms", host, port, result,
                                    (System.currentTimeMillis() - start));
                        } catch (Exception e) {
                            logger.error("clear redis: " + e.getMessage(), e);
                        } finally {
                            jedis.close();
                        }
                    }
                });
            }
        }

        //记录日志
        AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, 0L, AppAuditLogTypeEnum.APP_CLEAN_DATA);
        appAuditLogDao.save(appAuditLog);

        return true;
    }

    @Override
    public boolean isSingleClusterNode(long appId, String host, int port) {
        final Jedis jedis = getJedis(appId, host, port);
        try {
            String clusterNodes = jedis.clusterNodes();
            if (StringUtils.isBlank(clusterNodes)) {
                throw new RuntimeException(host + ":" + port + "clusterNodes is null");
            }
            String[] nodeInfos = clusterNodes.split("\n");
            if (nodeInfos.length == 1) {
                return true;
            }
            return false;
        } finally {
            jedis.close();
        }
    }

    @Override
    public List<String> getClientList(int instanceId) {
        if (instanceId <= 0) {
            return Collections.emptyList();
        }
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        if (instanceInfo == null) {
            return Collections.emptyList();
        }
        if (TypeUtil.isRedisType(instanceInfo.getType())) {
            Jedis jedis = null;
            try {
                jedis = getJedis(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(),
                        REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
                jedis.clientList();
                List<String> resultList = new ArrayList<String>();
                String clientList = jedis.clientList();
                if (StringUtils.isNotBlank(clientList)) {
                    String[] array = clientList.split("\n");
                    resultList.addAll(Arrays.asList(array));
                }
                return resultList;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> formatClientList(List<String> clientList) {
        List<Map<String, Object>> clientMapList = clientList.stream().map(clientInfo -> parseClientInfo(clientInfo)).collect(Collectors.toList());
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        clientMapList.stream().forEach(clientMap -> {
            String addr = MapUtils.getString(clientMap, "addr");
            List<Map<String, Object>> list = result.get(addr);
            if (CollectionUtils.isEmpty(list)) {
                list = new ArrayList<>();
                result.put(addr, list);
            }
            list.add(clientMap);
        });
        List<Map<String, Object>> finalResult = getClientInfoMap(result);

        return finalResult;
    }

    @Override
    public List<Map<String, Object>> getAppClientList(long appId, int condition) {
        Map<String, Map<Integer, Object>> finalResult = new HashMap<>();

        List<InstanceInfo> instanceInfoList = appService.getAppOnlineInstanceInfo(appId);
        instanceInfoList.stream().forEach(instanceInfo -> {
            int instanceId = instanceInfo.getId();
            List<Map<String, Object>> instanceClientList = formatClientList(getClientList(instanceId));

            for (Map<String, Object> map : instanceClientList) {
                String addr = MapUtils.getString(map, "addr");
                Map<Integer, Object> result = MapUtils.getMap(finalResult, addr);
                if (MapUtils.isEmpty(result)) {
                    result = new HashMap<>();
                    finalResult.put(addr, result);
                }
                result.put(instanceId, map);
            }

        });

        return formatAppClientList(finalResult, appId, condition);
    }


    private List<Map<String, Object>> formatAppClientList(Map<String, Map<Integer, Object>> addrClientListMap, long appId, int condition) {
        List<Map<String, Object>> finalResult = new ArrayList<>();
        List<String> ccWebClientList = webClientComponent.getWebClientIps();
        List<String> redisClientList = appService.getAppOnlineInstanceInfo(appId).stream().map(instanceInfo -> instanceInfo.getIp()).collect(Collectors.toList());

        for (String addr : addrClientListMap.keySet()) {
            Map<Integer, Object> instanceClientListMap = addrClientListMap.get(addr);
            Map<String, Object> map = new HashMap<>();
            map.put("addr", addr);

            Set<String> flags = new HashSet<>();
            int size = 0;
            for (Integer instanceId : instanceClientListMap.keySet()) {
                Map<String, Object> clientInfo = (HashMap) instanceClientListMap.get(instanceId);
                Set<String> instanceFlags = (HashSet) clientInfo.get("clientTypeSet");
                flags.addAll(instanceFlags);
                int count = MapUtils.getIntValue(clientInfo, "count");
                size += count;
            }
            map.put("flags", flags);
            map.put("size", size);

            map.put("instanceClientStats", instanceClientListMap);

            switch (condition) {
                case 0:
                    if (!ccWebClientList.contains(addr) && !redisClientList.contains(addr)) {
                        finalResult.add(map);
                    }
                    break;
                case 1:
                    if (ccWebClientList.contains(addr)) {
                        finalResult.add(map);
                    }
                    break;
                case 2:
                    if (redisClientList.contains(addr)) {
                        finalResult.add(map);
                    }
                    break;
                case 3:
                    finalResult.add(map);
                    break;
            }
        }

        return finalResult;
    }

    private List<Map<String, Object>> getClientInfoMap(Map<String, List<Map<String, Object>>> map) {
        List<Map<String, Object>> finalResult = new ArrayList<>();

        for (String addr : map.keySet()) {
            List<Map<String, Object>> clients = map.get(addr);
            Set<String> flagsSet = clients.stream().map(clientInfo ->
                    ClientTypeEnum.Method.getDesc(MapUtils.getString(clientInfo, "flags", "")))
                    .collect(Collectors.toSet());

            Map<String, Object> clientMap = new HashMap<>();
            clientMap.put("addr", addr);
            clientMap.put("clientTypeSet", flagsSet);
            clientMap.put("count", clients.size());
            clientMap.put("clientInfoList", clients);

            finalResult.add(clientMap);
        }

        return finalResult;
    }

    private Map<String, Object> parseClientInfo(String clientInfo) {
        Map<String, Object> clientInfoMap = new HashMap<>();
        String[] tmpArray1 = clientInfo.split(" ");
        if (tmpArray1 != null) {
            for (String tmp : tmpArray1) {
                String[] tmpArray2 = tmp.split("=|:");
                if (tmpArray2.length == 3) {
                    clientInfoMap.put(tmpArray2[0], tmpArray2[1]);
                    clientInfoMap.put("port", tmpArray2[2]);
                } else if (tmpArray2.length == 2) {
                    clientInfoMap.put(tmpArray2[0], tmpArray2[1]);
                }
            }
        }
        return clientInfoMap;
    }

    @Override
    public Map<String, String> getClusterLossSlots(long appId) {
        // 1.从应用中获取一个健康的主节点
        InstanceInfo sourceMasterInstance = getHealthyInstanceInfo(appId);
        if (sourceMasterInstance == null) {
            return Collections.emptyMap();
        }
        // 2. 获取所有slot和节点的对应关系
        Map<Integer, String> slotHostPortMap = getSlotsHostPortMap(appId, sourceMasterInstance.getIp(),
                sourceMasterInstance.getPort());
        // 3. 获取集群中失联的slot
        List<Integer> lossSlotList = getClusterLossSlots(appId, sourceMasterInstance.getIp(),
                sourceMasterInstance.getPort());
        // 3.1 将失联的slot列表组装成Map<String host:port,List<Integer> lossSlotList>
        Map<String, List<Integer>> hostPortSlotMap = new HashMap<String, List<Integer>>();
        if (CollectionUtils.isNotEmpty(lossSlotList)) {
            for (Integer lossSlot : lossSlotList) {
                String key = slotHostPortMap.get(lossSlot);
                if (hostPortSlotMap.containsKey(key)) {
                    hostPortSlotMap.get(key).add(lossSlot);
                } else {
                    List<Integer> list = new ArrayList<Integer>();
                    list.add(lossSlot);
                    hostPortSlotMap.put(key, list);
                }
            }
        }
        // 3.2 hostPortSlotMap组装成Map<String host:port,String startSlot-endSlot>
        Map<String, String> slotSegmentsMap = new HashMap<String, String>();
        for (Entry<String, List<Integer>> entry : hostPortSlotMap.entrySet()) {
            List<Integer> list = entry.getValue();
            List<String> slotSegments = new ArrayList<String>();
            int min = list.get(0);
            int max = min;
            for (int i = 1; i < list.size(); i++) {
                int temp = list.get(i);
                if (temp == max + 1) {
                    max = temp;
                } else {
                    slotSegments.add(String.valueOf(min) + "-" + String.valueOf(max));
                    min = temp;
                    max = temp;
                }
            }
            slotSegments.add(String.valueOf(min) + "-" + String.valueOf(max));
            slotSegmentsMap.put(entry.getKey(), slotSegments.toString());
        }
        return slotSegmentsMap;
    }

    /**
     * 从一个应用中获取一个健康的主节点
     *
     * @param appId
     * @return
     */
    public InstanceInfo getHealthyInstanceInfo(long appId) {
        InstanceInfo sourceMasterInstance = null;
        List<InstanceInfo> appInstanceInfoList = instanceDao.getInstListByAppId(appId);
        if (CollectionUtils.isEmpty(appInstanceInfoList)) {
            logger.error("appId {} has not instances", appId);
            return null;
        }
        for (InstanceInfo instanceInfo : appInstanceInfoList) {
            int instanceType = instanceInfo.getType();
            if (!TypeUtil.isRedisCluster(instanceType)) {
                continue;
            }
            final String host = instanceInfo.getIp();
            final int port = instanceInfo.getPort();
            if (instanceInfo.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                continue;
            }
            boolean isRun = isRun(appId, host, port);
            if (!isRun) {
                logger.warn("{}:{} is not run", host, port);
                continue;
            }
            BooleanEnum isMaster = isMaster(appId, host, port);
            if (isMaster != BooleanEnum.TRUE) {
                logger.warn("{}:{} is not master", host, port);
                continue;
            }
            sourceMasterInstance = instanceInfo;
            break;
        }
        return sourceMasterInstance;
    }

    /**
     * 从一个应用中获取所有健康master节点
     *
     * @param appId
     * @return 应用对应master节点列表
     */
    public List<InstanceInfo> getAllHealthyInstanceInfo(long appId) {
        // return instances
        List<InstanceInfo> allInstance = new ArrayList<InstanceInfo>();
        List<InstanceInfo> appInstanceInfoList = instanceDao.getInstListByAppId(appId);
        if (CollectionUtils.isEmpty(appInstanceInfoList)) {
            logger.error("appId {} has not instances", appId);
            return null;
        }
        for (InstanceInfo instanceInfo : appInstanceInfoList) {
            int instanceType = instanceInfo.getType();
            if (!TypeUtil.isRedisCluster(instanceType)) {
                continue;
            }
            final String host = instanceInfo.getIp();
            final int port = instanceInfo.getPort();
            if (instanceInfo.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                continue;
            }
            boolean isRun = isRun(appId, host, port);
            if (!isRun) {
                logger.warn("{}:{} is not run", host, port);
                continue;
            }
            BooleanEnum isMaster = isMaster(appId, host, port);
            if (isMaster != BooleanEnum.TRUE) {
                logger.warn("{}:{} is not master", host, port);
                continue;
            }
            // add exist redis
            allInstance.add(instanceInfo);
        }
        return allInstance;
    }

    @Override
    public List<InstanceLatencyHistory> collectRedisLatencyInfo(long appId, long collectTime, String host, int port) {
        Assert.isTrue(appId > 0);
        Assert.hasText(host);
        Assert.isTrue(port > 0);
        InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(host, port);
        //不存在实例/实例异常/下线
        if (instanceInfo == null) {
            return null;
        }
        if (TypeUtil.isRedisSentinel(instanceInfo.getType())) {
            //忽略sentinel redis实例
            return null;
        }

        // 从redis中获取延迟信息
        List<InstanceLatencyHistory> latencyHistoryList = getLatencyLatest(instanceInfo.getId(), appId, host, port);
        if (CollectionUtils.isEmpty(latencyHistoryList)) {
            return Collections.emptyList();
        }

        //入库
        String key = getThreadPoolKey() + "_" + host + "_" + port;
        boolean isOk = asyncService.submitFuture(getThreadPoolKey(), new KeyCallable<Boolean>(key) {
            @Override
            public Boolean execute() {
                try {
                    instanceLatencyHistoryDao.batchSave(latencyHistoryList);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        });
        if (!isOk) {
            logger.error("latencyHistory submitFuture failed,appId:{},collectTime:{},host:{},port:{}", appId, collectTime,
                    host, port);
        }
        return latencyHistoryList;
    }

    private List<InstanceLatencyHistory> getLatencyLatest(long instanceId, long appId, String host, int port) {
        Jedis jedis = null;
        try {
            jedis = getJedis(appId, host, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);

            List<InstanceLatencyHistory> resultList = new ArrayList<>();
            List<LatencyItem> latencyItems = JedisUtil.latencyLatest(jedis);
            List<Object> subResultList = null;
            if (CollectionUtils.isNotEmpty(latencyItems)) {
                List<String> eventList = latencyItems.stream().map(latencyItem -> latencyItem.getEvent()).collect(Collectors.toList());

                Pipeline pipeline = jedis.pipelined();
                for (String event : eventList) {
                    PipelineUtil.latencyHistory(pipeline, event);
                    PipelineUtil.latencyReset(pipeline, event);
                }
                subResultList = pipeline.syncAndReturnAll();

                if (CollectionUtils.isNotEmpty(subResultList)) {
                    for (int i = 0; i < subResultList.size(); i++) {
                        Object o = subResultList.get(i);
                        if (o instanceof List) {
                            String event = eventList.get(i / 2);
                            List<Object> latencyHistoryItems = (List<Object>) o;
                            List<InstanceLatencyHistory> instanceLatencyHistoryList = latencyHistoryItems.stream()
                                    .map(data -> {
                                        List<Object> properties = (List<Object>) data;
                                        LatencyHistoryItem latencyHistory = new LatencyHistoryItem(properties);
                                        return new InstanceLatencyHistory(
                                                instanceId, appId, host, port, event,
                                                new Date(latencyHistory.getTimeStamp() * 1000L),
                                                latencyHistory.getExecutionTime());
                                    })
                                    .collect(Collectors.toList());
                            resultList.addAll(instanceLatencyHistoryList);
                        }
                    }
                }
            }
            return resultList;
        } catch (Exception e) {
            logger.error(String.format("appId:%s, host:%s,port:%s,error:%s", appId, host, port, e.getMessage()), e);
            return Collections.emptyList();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * clusterslots命令拼接成Map<Integer slot, String host:port>
     *
     * @param host
     * @param port
     * @return
     */
    private Map<Integer, String> getSlotsHostPortMap(long appId, String host, int port) {
        Map<Integer, String> slotHostPortMap = new HashMap<Integer, String>();
        Jedis jedis = null;
        try {
            jedis = getJedis(appId, host, port);
            List<Object> slots = jedis.clusterSlots();
            for (Object slotInfoObj : slots) {
                List<Object> slotInfo = (List<Object>) slotInfoObj;
                if (slotInfo.size() <= 2) {
                    continue;
                }
                List<Integer> slotNums = getAssignedSlotArray(slotInfo);

                // hostInfos
                List<Object> hostInfos = (List<Object>) slotInfo.get(2);
                if (hostInfos.size() <= 0) {
                    continue;
                }
                HostAndPort targetNode = generateHostAndPort(hostInfos);

                for (Integer slot : slotNums) {
                    slotHostPortMap.put(slot, targetNode.getHost() + ":" + targetNode.getPort());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return slotHostPortMap;
    }

    private HostAndPort generateHostAndPort(List<Object> hostInfos) {
        return new HostAndPort(SafeEncoder.encode((byte[]) hostInfos.get(0)),
                ((Long) hostInfos.get(1)).intValue());
    }

    private List<Integer> getAssignedSlotArray(List<Object> slotInfo) {
        List<Integer> slotNums = new ArrayList<Integer>();
        for (int slot = ((Long) slotInfo.get(0)).intValue(); slot <= ((Long) slotInfo.get(1))
                .intValue(); slot++) {
            slotNums.add(slot);
        }
        return slotNums;
    }

    @Override
    public List<Integer> getClusterLossSlots(long appId, String host, int port) {
        InstanceInfo instanceInfo = instanceDao.getAllInstByIpAndPort(host, port);
        if (instanceInfo == null) {
            logger.warn("{}:{} instanceInfo is null", host, port);
            return Collections.emptyList();
        }
        if (!TypeUtil.isRedisCluster(instanceInfo.getType())) {
            logger.warn("{}:{} is not rediscluster type", host, port);
            return Collections.emptyList();
        }
        List<Integer> clusterLossSlots = new ArrayList<Integer>();
        Jedis jedis = null;
        try {
            jedis = getJedis(appId, host, port, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
            String clusterNodes = jedis.clusterNodes();
            if (StringUtils.isBlank(clusterNodes)) {
                throw new RuntimeException(host + ":" + port + "clusterNodes is null");
            }
            Set<Integer> allSlots = new LinkedHashSet<Integer>();
            for (int i = 0; i <= 16383; i++) {
                allSlots.add(i);
            }

            // 解析
            ClusterNodeInformationParser nodeInfoParser = new ClusterNodeInformationParser();
            for (String nodeInfo : clusterNodes.split("\n")) {
                if (StringUtils.isNotBlank(nodeInfo) && !nodeInfo.contains("disconnected") && !nodeInfo
                        .contains("fail")) {
                    if (nodeInfo.contains("@")) {
                        // redis4.0 兼容集群协议 6397@16397
                        nodeInfo = nodeInfo.replaceAll(nodeInfo.substring(nodeInfo.indexOf("@"),
                                nodeInfo.indexOf("@") + nodeInfo.split("@")[1].indexOf(" ") + 1), "");
                    }
                    ClusterNodeInformation clusterNodeInfo = nodeInfoParser
                            .parse(nodeInfo, new HostAndPort(host, port));
                    List<Integer> availableSlots = clusterNodeInfo.getAvailableSlots();
                    for (Integer slot : availableSlots) {
                        allSlots.remove(slot);
                    }
                }
            }
            clusterLossSlots = new ArrayList<Integer>(allSlots);
        } catch (Exception e) {
            logger.error("getClusterLossSlots: " + e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return clusterLossSlots;
    }

    @Override
    public List<Integer> getInstanceSlots(long appId, String healthHost, int healthPort, String lossSlotsHost,
                                          int lossSlotsPort) {
        InstanceInfo instanceInfo = instanceDao.getAllInstByIpAndPort(healthHost, healthPort);
        if (instanceInfo == null) {
            logger.warn("{}:{} instanceInfo is null", healthHost, healthPort);
            return Collections.emptyList();
        }
        if (!TypeUtil.isRedisCluster(instanceInfo.getType())) {
            logger.warn("{}:{} is not rediscluster type", healthHost, healthPort);
            return Collections.emptyList();
        }
        List<Integer> clusterLossSlots = new ArrayList<Integer>();
        Jedis jedis = null;
        try {
            jedis = getJedis(appId, healthHost, healthPort, REDIS_DEFAULT_TIME, REDIS_DEFAULT_TIME);
            String clusterNodes = jedis.clusterNodes();
            if (StringUtils.isBlank(clusterNodes)) {
                throw new RuntimeException(healthHost + ":" + healthPort + "clusterNodes is null");
            }
            // 解析获取丢失slots
            ClusterNodeInformationParser nodeInfoParser = new ClusterNodeInformationParser();
            for (String nodeInfo : clusterNodes.split("\n")) {
                if (StringUtils.isNotBlank(nodeInfo) && nodeInfo.contains("fail") && nodeInfo
                        .contains(lossSlotsHost + ":" + lossSlotsPort)) {
                    if (nodeInfo.contains("@")) {
                        // redis4.0 兼容集群协议 6397@16397
                        nodeInfo = nodeInfo.replaceAll(nodeInfo.substring(nodeInfo.indexOf("@"),
                                nodeInfo.indexOf("@") + nodeInfo.split("@")[1].indexOf(" ") + 1), "");
                    }
                    ClusterNodeInformation clusterNodeInfo = nodeInfoParser
                            .parse(nodeInfo, new HostAndPort(healthHost, healthPort));
                    clusterLossSlots = clusterNodeInfo.getAvailableSlots();
                }
            }
        } catch (Exception e) {
            logger.error("getClusterLossSlots: " + e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return clusterLossSlots;
    }

    @PreDestroy
    public void destory() {
        for (JedisPool jedisPool : jedisPoolMap.values()) {
            jedisPool.destroy();
        }
    }

    @Override
    public List<InstanceSlowLog> getInstanceSlowLogByAppId(long appId) {
        try {
            return instanceSlowLogDao.getByAppId(appId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<InstanceSlowLog> getInstanceSlowLogByAppId(long appId, Date startDate, Date endDate) {
        try {
            return instanceSlowLogDao.search(appId, startDate, endDate);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Long> getInstanceSlowLogCountMapByAppId(Long appId, Date startDate, Date endDate) {
        try {
            List<Map<String, Object>> list = instanceSlowLogDao
                    .getInstanceSlowLogCountMapByAppId(appId, startDate, endDate);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyMap();
            }
            Map<String, Long> resultMap = new LinkedHashMap<String, Long>();
            for (Map<String, Object> map : list) {
                long count = MapUtils.getLongValue(map, "count");
                String hostPort = MapUtils.getString(map, "hostPort");
                if (StringUtils.isNotBlank(hostPort)) {
                    resultMap.put(hostPort, count);
                }
            }
            return resultMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, InstanceSlotModel> getClusterSlotsMap(long appId) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (!TypeUtil.isRedisCluster(appDesc.getType())) {
            return Collections.emptyMap();
        }
        // 最终结果
        Map<String, InstanceSlotModel> resultMap = new HashMap<String, InstanceSlotModel>();

        // 找到一个运行的节点用来执行cluster slots
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
        String host = null;
        int port = 0;
        for (InstanceInfo instanceInfo : instanceList) {
            // 下线和心跳停止 均跳过
            if (instanceInfo.isOffline() || instanceInfo.getStatus() == InstanceStatusEnum.ERROR_STATUS.getStatus()) {
                continue;
            }
            host = instanceInfo.getIp();
            port = instanceInfo.getPort();
            boolean isRun = isRun(appId, host, port);
            if (isRun) {
                break;
            }
        }
        if (StringUtils.isBlank(host) || port <= 0) {
            return Collections.emptyMap();
        }

        // 获取cluster slots
        List<Object> clusterSlotList = null;
        Jedis jedis = null;
        try {
            jedis = getJedis(appId, host, port);
            clusterSlotList = jedis.clusterSlots();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        if (clusterSlotList == null || clusterSlotList.size() == 0) {
            return Collections.emptyMap();
        }
        //clusterSlotList形如：
        //		[0, 1, [[B@5caf905d, 6380], [[B@27716f4, 6379]]
        //		[3, 4096, [[B@8efb846, 6380], [[B@2a84aee7, 6379]]
        //		[12291, 16383, [[B@a09ee92, 6383], [[B@30f39991, 6382]]
        //		[2, 2, [[B@452b3a41, 6381], [[B@4a574795, 6382]]
        //		[8194, 12290, [[B@f6f4d33, 6381], [[B@23fc625e, 6382]]
        //		[4097, 8193, [[B@3f99bd52, 6380], [[B@4f023edb, 6381]]

        for (Object clusterSlotObj : clusterSlotList) {
            List<Object> slotInfoList = (List<Object>) clusterSlotObj;
            if (slotInfoList.size() <= 2) {
                continue;
            }
            //获取slot的start到end相关
            int startSlot = ((Long) slotInfoList.get(0)).intValue();
            int endSlot = ((Long) slotInfoList.get(1)).intValue();
            String slotDistribute = getStartToEndSlotDistribute(startSlot, endSlot);
            List<Integer> slotList = getStartToEndSlotList(startSlot, endSlot);

            List<Object> masterInfoList = (List<Object>) slotInfoList.get(2);
            String tempHost = SafeEncoder.encode((byte[]) masterInfoList.get(0));
            int tempPort = ((Long) masterInfoList.get(1)).intValue();
            String hostPort = tempHost + ":" + tempPort;
            if (resultMap.containsKey(hostPort)) {
                InstanceSlotModel instanceSlotModel = resultMap.get(hostPort);
                instanceSlotModel.getSlotDistributeList().add(slotDistribute);
                instanceSlotModel.getSlotList().addAll(slotList);
            } else {
                InstanceSlotModel instanceSlotModel = new InstanceSlotModel();
                instanceSlotModel.setHost(tempHost);
                instanceSlotModel.setPort(tempPort);
                List<String> slotDistributeList = new ArrayList<String>();
                slotDistributeList.add(slotDistribute);
                instanceSlotModel.setSlotDistributeList(slotDistributeList);
                instanceSlotModel.setSlotList(slotList);
                resultMap.put(hostPort, instanceSlotModel);
            }
        }
        return resultMap;
    }

    /**
     * 获取slot列表
     *
     * @param startSlot
     * @param endSlot
     * @return
     */
    private List<Integer> getStartToEndSlotList(int startSlot, int endSlot) {
        List<Integer> slotList = new ArrayList<Integer>();
        if (startSlot == endSlot) {
            slotList.add(startSlot);
        } else {
            for (int i = startSlot; i <= endSlot; i++) {
                slotList.add(i);
            }
        }
        return slotList;
    }

    /**
     * 0,4096 0-4096
     * 2,2 2-2
     *
     * @return
     */
    private String getStartToEndSlotDistribute(int startSlot, int endSlot) {
        if (startSlot == endSlot) {
            return String.valueOf(startSlot);
        } else {
            return startSlot + "-" + endSlot;
        }
    }

    @Override
    public String getRedisVersion(long appId, String ip, int port) {
        Map<RedisConstant, Map<String, Object>> infoAllMap = getInfoStats(appId, ip, port);
        if (MapUtils.isEmpty(infoAllMap)) {
            return null;
        }
        Map<String, Object> serverMap = infoAllMap.get(RedisConstant.Server);
        if (MapUtils.isEmpty(serverMap)) {
            return null;
        }
        return MapUtils.getString(serverMap, "redis_version");
    }

    public Boolean getRedisReplicationStatus(long appId, String ip, int port) {

        Map<RedisConstant, Map<String, Object>> infoAllMap = getInfoStats(appId, ip, port);
        if (MapUtils.isEmpty(infoAllMap)) {
            return false;
        }
        Map<String, Object> serverMap = infoAllMap.get(RedisConstant.Replication);
        if (MapUtils.isEmpty(serverMap)) {
            return false;
        }
        /**
         * 主从failover (info replication) slave0 state 状态变化: wait_bgsave -> send_bulk -> online
         * 1.slave0	ip=${ip},port=${port},state=online,offset=413125529634,lag=1
         * 2.master_repl_offset	413125537241
         */
        String slave0 = MapUtils.getString(serverMap, "slave0");
        String master_repl_offset = MapUtils.getString(serverMap, "master_repl_offset");
        String role = MapUtils.getString(serverMap, "role");

        logger.info("salve0 :{} ,master_repl_offset :{}", slave0, master_repl_offset);
        try {
            if (!StringUtils.isEmpty(slave0) && slave0.indexOf("state=online") > -1 && role.equals("master")
                    && !StringUtils.isEmpty(master_repl_offset)) {

                long slave_offset = 0l;
                for (String info : slave0.split(",")) {
                    if (info.indexOf("offset") > -1) {
                        logger.info(" slave offset = {} ", info.replaceAll("offset=", ""));
                        slave_offset = Long.parseLong(info.replaceAll("offset=", ""));
                    }
                }
                // 从偏移量差值 ,load内存数据 offset
                if (slave_offset == 0) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getNodeId(long appId, String ip, int port) {
        final Jedis jedis = getJedis(appId, ip, port);
        try {
            final StringBuilder clusterNodes = new StringBuilder();
            boolean isGetNodes = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    String nodes = jedis.clusterNodes();
                    if (nodes != null && nodes.length() > 0) {
                        clusterNodes.append(nodes);
                        return true;
                    }
                    return false;
                }
            }.run();
            if (!isGetNodes) {
                logger.error("{}:{} clusterNodes failed", jedis.getClient().getHost(), jedis.getClient().getPort());
                return null;
            }
            for (String infoLine : clusterNodes.toString().split("\n")) {
                if (infoLine.contains("myself")) {
                    String nodeId = infoLine.split(" ")[0];
                    return nodeId;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return null;
    }

    @Override
    public Jedis getJedis(long appId, String host, int port) {
        Jedis jedis = getJedis(appId, host, port, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
        return jedis;
    }

    @Override
    public Jedis getJedis(long appId, String host, int port, int connectionTimeout, int soTimeout) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        String password = appDesc.getAppPassword();
        Jedis jedis = getJedis(host, port, connectionTimeout, soTimeout, password);
        return jedis;
    }

    @Override
    public Jedis getJedis(String host, int port, String authPassword) {
        return getJedis(host, port, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT, authPassword);
    }

    @Override
    public Jedis getJedis(String host, int port) {
        return getJedis(host, port, null);
    }

    @Override
    public Jedis getJedis(String host, int port, String password, int connectionTimeout, int soTimeout){
        return getJedis(host, port, connectionTimeout, soTimeout, password);
    }

    private Jedis getJedis(String host, int port, int connectionTimeout, int soTimeout, String authPassword) {
        Jedis jedis = new Jedis(host, port);
        jedis.getClient().setConnectionTimeout(connectionTimeout);
        jedis.getClient().setSoTimeout(soTimeout);
        try {
            if (StringUtils.isBlank(authPassword)) {
                // 保证存活性
                jedis.ping();
            } else {
                AuthUtil.auth(jedis, authPassword);
            }
        } catch (Exception e) {
            //防止加载RBD期间报:JedisDataException: LOADING Redis is loading the dataset in memory错误
            logger.error(e.getMessage());
        }
        return jedis;
    }

    private void fixReadOnlyOfCluster(long appId, Jedis jedis) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return;
        }
    }

    @Override
    public boolean sendDeployRedisRelateCollectionMsg(long appId, String host, int port) {
        return true;
    }

    @Override
    public boolean checkNutCrackerConfIsSame(long appId) {
        List<String> masterNameList = getMasterNameListFromNutCrackerConf(appId);
        if (CollectionUtils.isNotEmpty(masterNameList)) {
            return true;
        }
        return false;
    }

    private List<String> getMasterNameListFromNutCrackerConf(long appId) {
        List<List<String>> appNutCrackerMasterList = getFullInstanceListFromNutCrackerConf(appId);
        if (CollectionUtils.isEmpty(appNutCrackerMasterList)) {
            return Collections.emptyList();
        }
        List<String> nutCrackerMasterList = appNutCrackerMasterList.get(0);
        List<String> masterNameList = new ArrayList<String>();
        for (String nutCrackerMaster : nutCrackerMasterList) {
            String[] arr = nutCrackerMaster.split("\\s+");
            masterNameList.add(arr[arr.length - 1].trim());
        }
        return masterNameList;
    }

    public List<List<String>> getFullInstanceListFromNutCrackerConf(long appId) {
        Map<String, List<String>> appNutCrackerMasterMap = getAppNutCrackerMasterList(appId);
        if (MapUtils.isEmpty(appNutCrackerMasterMap)) {
            logger.error(BaseTask.marker, "appId {} appNutCrackerMasterListMap is empty", appId);
            return Collections.emptyList();
        }
        List<String> ipPortList = new ArrayList<String>();
        List<List<String>> appNutCrackerMasterList = new ArrayList<List<String>>();
        for (Entry<String, List<String>> entry : appNutCrackerMasterMap.entrySet()) {
            ipPortList.add(entry.getKey());
            appNutCrackerMasterList.add(entry.getValue());
        }
        for (int i = 0; i < appNutCrackerMasterList.size() - 1; i++) {
            List<String> appNutCrackerMasterList1 = appNutCrackerMasterList.get(i);
            List<String> appNutCrackerMasterList2 = appNutCrackerMasterList.get(i + 1);
            if (appNutCrackerMasterList1.size() != appNutCrackerMasterList2.size()) {
                logger.error(BaseTask.marker, "{} and {} config size is not same", ipPortList.get(i), ipPortList.get(i + 1));
                return Collections.emptyList();
            }
            for (int j = 0; j < appNutCrackerMasterList1.size(); j++) {
                if (!appNutCrackerMasterList1.get(j).trim().equals(appNutCrackerMasterList2.get(j).trim())) {
                    logger.error(BaseTask.marker, "{} and {} config content is not same", ipPortList.get(i), ipPortList.get(i + 1));
                    return Collections.emptyList();
                }
            }
        }
        return appNutCrackerMasterList;
    }

    /**
     * 获取所有在线的proxy配置
     *
     * @param appId
     * @return
     */
    private Map<String, List<String>> getAppNutCrackerMasterList(long appId) {
        Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
        List<InstanceInfo> instanceInfoList = appService.getAppInstanceByType(appId, InstanceTypeEnum.NUTCRACKER);
        for (InstanceInfo instanceInfo : instanceInfoList) {
            if (!instanceInfo.isOnline()) {
                continue;
            }
            String host = instanceInfo.getIp();
            int port = instanceInfo.getPort();
            String remoteBasePath = machineCenter.getInstanceRemoteBasePath(appId, instanceInfo.getPort(),
                    InstanceTypeEnum.NUTCRACKER);
            String confPath = MachineProtocol.getConfPath(remoteBasePath) + "/" + RedisProtocol.getNutCrackerConfName();
            String commandResult = machineCenter.executeShell(instanceInfo.getIp(), "cat " + confPath);
            if (StringUtils.isBlank(commandResult)) {
                logger.error(BaseTask.marker, "appId {} {}:{} nutcrack conf {} is empty", appId, host, port, confPath);
                return Collections.emptyMap();
            }
            List<String> masterNameList = new ArrayList<String>();
            String[] lines = commandResult.split("\n");
            for (String line : lines) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                if (!line.contains("-")) {
                    continue;
                }
                if (line.split(":").length < 2) {
                    continue;
                }
                masterNameList.add(line);
            }
            resultMap.put(host + ":" + port, masterNameList);
        }
        return resultMap;
    }

    @Override
    public List<InstanceInfo> checkNutCrackerHashIsSame(long appId, boolean isDelete) {
        return null;
    }

    public List<InstanceInfo> checkInstanceModule(long appId) {

        // 实例列表
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        if (!CollectionUtils.isEmpty(instanceList)) {
            //增加实例在线过滤，避免查询已下线实例造成错误
            instanceList = instanceList.stream().filter(instanceInfo -> InstanceStatusEnum.GOOD_STATUS.getStatus() == instanceInfo.getStatus()).collect(Collectors.toList());
            for (InstanceInfo instanceInfo : instanceList) {
                if (!CollectionUtils.isEmpty(instanceList)) {
                    String host = instanceInfo.getIp();
                    int port = instanceInfo.getPort();
                    int type = instanceInfo.getType();
                    Jedis jedis = null;
                    try {
                        if (type == ConstUtils.CACHE_REDIS_STANDALONE || type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                            jedis = getJedis(appId, host, port);
                            List<Module> modules = jedis.moduleList();
                            instanceInfo.setModules(modules);
                            logger.info("checkInstanceModule {}:{} module info :{}", host, port, modules);
                        }
                    } catch (Exception e) {
                        logger.error("checkInstanceModule {}:{} error , message:{}", host, port, e.getMessage(), e);
                    } finally {
                        if (jedis != null) {
                            jedis.close();
                        }
                    }
                }
            }
        }
        return instanceList;
    }

    public Map loadModule(long appId, int versionId) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        int status = SuccessEnum.SUCCESS.value();
        String message = "";

        // 装载模块
        String so_name = "";
        try {
            ModuleVersion moduleVersion = moduleService.getModuleVersionById(versionId);
            // 验证是否存在
            String soPath = moduleVersion.getSoPath();
            so_name = soPath.substring(soPath.lastIndexOf("/") + 1);
            String check_command = String.format("ls -l %s | grep %s | wc -l", ConstUtils.MODULE_BASE_PATH, so_name);
            String download_command = String.format("mkdir -p %s && cd %s && wget %s && chmod +x *.so", ConstUtils.MODULE_BASE_PATH, ConstUtils.MODULE_BASE_PATH, soPath);

            List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
            if (!CollectionUtils.isEmpty(instanceList)) {
                for (InstanceInfo instanceInfo : instanceList) {
                    if (!instanceInfo.isOffline()) {
                        String host = instanceInfo.getIp();
                        int port = instanceInfo.getPort();
                        int type = instanceInfo.getType();
                        // module load path
                        String module_path = ConstUtils.MODULE_BASE_PATH + so_name;
                        //检测并下载组件
                        checkAndDownloadModule(host, check_command, download_command);
                        Jedis jedis = null;
                        try {
                            if (type == ConstUtils.CACHE_REDIS_STANDALONE || type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                                jedis = getJedis(appId, host, port);
                                // 装载模块
                                List<Module> modules = jedis.moduleList();
                                // 未load module
//                                if (!existModule(modules, moduleName)) {
                                String result = jedis.moduleLoad(module_path);
                                logger.info(" {}:{} load module path:{} result:{}", host, port, module_path, result);
                                //写配置文件
                                refreshConfig(appId, host, port, module_path);
//                                }
                            }
                        } catch (Exception e) {
                            logger.error(" {}:{} load module path:{} error , message:{}", host, port, module_path, e.getMessage(), e);
                            status = SuccessEnum.ERROR.value();
                            message += String.format("%s:%s load module:%s error \n", host, port, module_path);
                        } finally {
                            if (jedis != null) {
                                jedis.close();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("appid:{} load moduleName :{} error :{}", appId, so_name, e.getMessage());
            status = SuccessEnum.FAIL.value();
        }

        resultMap.put("status", status);
        resultMap.put("so_name", so_name);
        resultMap.put("message", message);
        return resultMap;
    }

    public Map loadModule(long appId, String moduleName) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        int status = SuccessEnum.SUCCESS.value();
        String message = "";
        try {
            List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
            if (!CollectionUtils.isEmpty(instanceList)) {
                for (InstanceInfo instanceInfo : instanceList) {
                    if (!instanceInfo.isOffline()) {
                        String host = instanceInfo.getIp();
                        int port = instanceInfo.getPort();
                        int type = instanceInfo.getType();
                        String module_path = ConstUtils.MODULE_BASE_PATH + moduleName;
                        Jedis jedis = null;
                        try {
                            if (type == ConstUtils.CACHE_REDIS_STANDALONE || type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                                jedis = getJedis(appId, host, port);
                                List<Module> modules = jedis.moduleList();
                                // 未load module
                                if (!existModule(modules, moduleName)) {
                                    String result = jedis.moduleLoad(module_path);
                                    logger.info(" {}:{} load module path:{} result:{}", host, port, module_path, result);
                                    //写配置文件
                                    refreshConfig(appId, host, port, module_path);
                                }
                            }
                        } catch (Exception e) {
                            logger.error(" {}:{} load module path:{} error , message:{}", host, port, module_path, e.getMessage(), e);
                            status = SuccessEnum.ERROR.value();
                            message += String.format("%s:%s load module:%s error \n", host, port, module_path);
                        } finally {
                            if (jedis != null) {
                                jedis.close();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("appid:{} load moduleName :{} error :{}", appId, moduleName, e.getMessage());
            status = SuccessEnum.FAIL.value();
        }
        resultMap.put("status", status);
        resultMap.put("message", message);
        return resultMap;
    }

    public boolean refreshConfig(long appid, String host, int port, String modulePath) {

        try {
            AppDesc appDesc = appService.getByAppId(appid);
            boolean iscluster = false;
            if (appDesc.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                iscluster = true;
            }
            String configName = RedisProtocol.getConfig(port, iscluster);
            String filePath = MachineProtocol.CONF_DIR + configName;
            if (machineCenter.isK8sMachine(host)) {
                filePath = MachineProtocol.getK8sConfDir(host) + configName;
            }

            String cmd = String.format("echo \"loadmodule %s\" >> %s", modulePath, filePath);

            String result = sshService.execute(host, cmd);
            logger.info("appid:{} {}:{} load module:{} refresh config result:{}", appid, host, port, modulePath, result);

        } catch (Exception e) {
            logger.error("appid:{} {}:{} load module:{} refresh config error :{}", appid, host, port, modulePath, e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean existModule(List<Module> modules, String moduleName) {
        if (!CollectionUtils.isEmpty(modules)) {
            for (Module module : modules) {
                if (module.getName().equals(ConstUtils.MODULE_MAP.get(moduleName))) {
                    logger.info("module:{} alread load in redis!", moduleName);
                    return true;
                }
            }
        }
        return false;
    }

    // 自动下载so
    public void checkAndDownloadModule(String ip, String check_command, String download_command) {
        try {
            String result = sshService.execute(ip, check_command);
            logger.info("checkAndDownloadModule check_command:{} result:{}", check_command, result);
            if ("0".equals(result)) {
                // download to module path
                String download_res = sshService.execute(ip, download_command);
                logger.info("download_command:{} result:{}", download_command, download_res);
            }
        } catch (SSHException e) {
            logger.error("checkAndDownloadModule ip:{} error :{}", ip, e.getMessage(), e);
        }
    }

    public boolean checkAndLoadModule(long appId, String masterHost, int masterPort, String slaveHost, int slavePort) {

        Jedis jedis = null;
        Jedis currentJedis = null;
        try {
            //原redis实例
            jedis = getJedis(appId, masterHost, masterPort);
            //变更redis实例
            currentJedis = getJedis(appId, slaveHost, slavePort);
            List<Module> modules = jedis.moduleList();
            // 未load module
            if (!CollectionUtils.isEmpty(modules)) {
                for (Module module : modules) {
                    String moduleFileName = MapUtils.getString(ConstUtils.MODULE_MAP, module.getName());
                    if (!StringUtils.isEmpty(moduleFileName)) {
                        // 装载redis插件
                        String modulePath = String.format("%s%s", ConstUtils.MODULE_BASE_PATH, moduleFileName);
                        String result = currentJedis.moduleLoad(modulePath);
                        logger.info(" {}:{} load module path:{} result:{}", slaveHost, slavePort, modulePath, result);
                        // 写配置文件
                        refreshConfig(appId, slaveHost, slavePort, modulePath);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(" {}:{} load module error , message:{}", slaveHost, slaveHost, e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
            if (currentJedis != null) {
                currentJedis.close();
            }
        }
        return true;
    }

    public Map unloadModule(long appId, String moduleName) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        int status = SuccessEnum.SUCCESS.value();
        String message = "";
        try {
            List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
            if (!CollectionUtils.isEmpty(instanceList)) {
                for (InstanceInfo instanceInfo : instanceList) {
                    if (!instanceInfo.isOffline()) {
                        String host = instanceInfo.getIp();
                        int port = instanceInfo.getPort();
                        int type = instanceInfo.getType();
                        Jedis jedis = null;
                        try {
                            if (type == ConstUtils.CACHE_REDIS_STANDALONE || type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                                jedis = getJedis(appId, host, port);
                                List<Module> modules = jedis.moduleList();
                                if (!CollectionUtils.isEmpty(modules)) {
                                    for (Module module : modules) {
                                        if (module.getName().equals(moduleName)) {
                                            String result = jedis.moduleUnload(module.getName());
                                            logger.info("checkInstanceModule {}:{} unload module:{} result:{}", host, port, moduleName, result);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error("checkInstanceModule {}:{} unload module:{} error , message:{}", host, port, moduleName, e.getMessage(), e);
                            status = SuccessEnum.ERROR.value();
                            message += String.format("%s:%s unload module:%s error \n", host, port, moduleName);
                        } finally {
                            if (jedis != null) {
                                jedis.close();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("appid:{} unload moduleName :{} error :{}", appId, moduleName, e.getMessage());
            status = SuccessEnum.FAIL.value();
        }
        resultMap.put("status", status);
        resultMap.put("message", message);
        return resultMap;
    }

    private class RedisKeyCallable extends KeyCallable<Boolean> {
        private final long appId;
        private final long collectTime;
        private final String host;
        private final int port;
        private final Map<RedisConstant, Map<String, Object>> infoMap;
        private final Map<String, Object> clusterInfoMap;

        private RedisKeyCallable(long appId, long collectTime, String host, int port,
                                 Map<RedisConstant, Map<String, Object>> infoMap, Map<String, Object> clusterInfoMap) {
            super(buildFutureKey(appId, collectTime, host, port));
            this.appId = appId;
            this.collectTime = collectTime;
            this.host = host;
            this.port = port;
            this.infoMap = infoMap;
            this.clusterInfoMap = clusterInfoMap;
        }

        @Override
        public Boolean execute() {
            //比对currentInfoMap和lastInfoMap,计算差值
            long lastCollectTime = ScheduleUtil.getLastCollectTime(collectTime);
            Map<String, Object> lastInfoMap = instanceStatsCenter
                    .queryStandardInfoMap(lastCollectTime, host, port, ConstUtils.REDIS);

            if (lastInfoMap == null || lastInfoMap.isEmpty()) {
                logger.error("[redis-lastInfoMap] : lastCollectTime = {} appId={} host:port = {}:{} is null",
                        lastCollectTime, appId, host, port);
            }
            //基本统计累加差值
            Table<RedisConstant, String, Long> baseDiffTable = getAccumulationDiff(infoMap, lastInfoMap);
            fillAccumulationMap(infoMap, baseDiffTable);

            //命令累加差值
            Table<RedisConstant, String, Long> commandDiffTable = getCommandsDiff(infoMap, lastInfoMap);
            fillAccumulationMap(infoMap, commandDiffTable);

            //内存碎片率差值计算
            //Table<RedisConstant, String, Double> otherDiffTable = getDoubleAccumulationDiff(infoMap, lastInfoMap);
            //fillDoubleAccumulationMap(infoMap, otherDiffTable);
            fillMemFragRatioMap(infoMap);

            Map<String, Object> currentInfoMap = new LinkedHashMap<String, Object>();
            for (Map.Entry<RedisConstant, Map<String, Object>> entry : infoMap.entrySet()) {
                currentInfoMap.put(entry.getKey().getValue(), entry.getValue());
            }
            currentInfoMap.put(ConstUtils.COLLECT_TIME, collectTime);
            instanceStatsCenter.saveStandardStats(currentInfoMap, clusterInfoMap, host, port, ConstUtils.REDIS);

            // 更新实例在db中的状态
            InstanceStats instanceStats = getInstanceStats(appId, host, port, infoMap);
            if (instanceStats != null) {
                instanceStatsDao.updateInstanceStats(instanceStats);
            }

            BooleanEnum isMaster = isMaster(infoMap);
            if (isMaster == BooleanEnum.TRUE) {
                Table<RedisConstant, String, Long> diffTable = HashBasedTable.create();
                diffTable.putAll(baseDiffTable);
                diffTable.putAll(commandDiffTable);

                long allCommandCount = 0L;
                //更新命令统计
                List<AppCommandStats> commandStatsList = getCommandStatsList(appId, collectTime, diffTable);
                for (AppCommandStats commandStats : commandStatsList) {
                    //排除无效命令且存储有累加的数据
                    if (RedisExcludeCommand.isExcludeCommand(commandStats.getCommandName())
                            || commandStats.getCommandCount() <= 0L) {
                        continue;
                    }
                    allCommandCount += commandStats.getCommandCount();
                    try {
                        // todo 数据库(on duplicate key update)竞争优化
                        appStatsDao.mergeMinuteCommandStatus(commandStats);
                        appStatsDao.mergeHourCommandStatus(commandStats);
                    } catch (Exception e) {
                        logger.error(e.getMessage() + appId, e);
                    }
                }
                //写入app分钟统计
                AppStats appStats = getAppStats(appId, collectTime, diffTable, infoMap);
                try {
                    appStats.setCommandCount(allCommandCount);
                    // todo 数据库(on duplicate key update)竞争优化
                    appStatsDao.mergeMinuteAppStats(appStats);
                    appStatsDao.mergeHourAppStats(appStats);
                } catch (Exception e) {
                    logger.error(e.getMessage() + appId, e);
                }
                logger.debug("collect redis info done, appId: {}, instance: {}:{}, time: {}", appId, host, port,
                        collectTime);
            }

            return true;
        }
    }
}