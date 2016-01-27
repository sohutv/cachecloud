package com.sohu.cache.memcached.impl;

import com.google.code.yanf4j.config.Configuration;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.constant.MemcachedAccumulation;
import com.sohu.cache.constant.MemcachedCommand;
import com.sohu.cache.constant.MemcachedConstant;
import com.sohu.cache.constant.MemcachedStats;
import com.sohu.cache.dao.AppStatsDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceStatsDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.memcached.MemcachedCenter;
import com.sohu.cache.memcached.enums.MemcachedReadOnlyCommandEnum;
import com.sohu.cache.schedule.SchedulerCenter;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.IdempotentConfirmer;
import com.sohu.cache.util.ObjectConvert;
import com.sohu.cache.util.ScheduleUtil;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * memcached控制的接口实现
 * <p/>
 * User: lingguo
 * Date: 14-6-11
 * Time: 下午5:03
 */
public class MemcachedCenterImpl implements MemcachedCenter {
    private final Logger logger = LoggerFactory.getLogger(MemcachedCenterImpl.class);

    private SchedulerCenter schedulerCenter;

    private AsyncService asyncService;

    private InstanceDao instanceDao;

    private AppStatsDao appStatsDao;

    private InstanceStatsDao instanceStatsDao;

    private InstanceStatsCenter instanceStatsCenter;

    private MachineCenter machineCenter;

    private MachineDao machineDao;

    private final Lock lock = new ReentrantLock();

    private static ConcurrentMap<String, MemcachedClient> memcachedClientMap = new ConcurrentHashMap<String, MemcachedClient>();

    private final long CONNECTION_TIMEOUT = 60000;      // 连接超时：1 minutes
    private final int CONNECTION_POOL_SIZE = 1;         // 线程池大小

    private final String CMD_PREFIX = "cmd_";           // memcached统计中命令key的前缀
    private final String EMPTY_STRING = "";             // 空串

    public void init() {
        System.setProperty("xmemcached.selector.pool.size", "1");
    }

    /**
     * 1. 为当前实例创建trigger，并生效；
     * 2. 为当前实例维护客户端列表；
     *
     * @param appId 应用id
     * @param ip    ip
     * @param port  port
     * @return 部署成功返回true，否则返回false；
     */
    @Override
    public boolean deployMemcachedCollection(long appId, String ip, int port) {
        Assert.isTrue(appId > 0);
        Assert.hasText(ip);
        Assert.isTrue(port > 0);

        maintainMemcachedClientMap(ip, port);

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put(ConstUtils.HOST_KEY, ip);
        dataMap.put(ConstUtils.PORT_KEY, port);
        dataMap.put(ConstUtils.APP_KEY, appId);

        JobKey jobKey = JobKey.jobKey(ConstUtils.MEMCACHED_JOB_NAME, ConstUtils.MEMCACHED_JOB_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(ObjectConvert.linkIpAndPort(ip, port), ConstUtils.MEMCACHED_TRIGGER_GROUP + appId);

        return schedulerCenter.deployJobByCron(jobKey, triggerKey, dataMap, ScheduleUtil.getMinuteCronByAppId(appId),
                false);
    }

    @Override
    public boolean unDeployMemcachedCollection(long appId, String host, int port) {
        Assert.isTrue(appId > 0);
        Assert.hasText(host);
        Assert.isTrue(port > 0);
        TriggerKey triggerKey = TriggerKey.triggerKey(ObjectConvert.linkIpAndPort(host, port), ConstUtils.MEMCACHED_TRIGGER_GROUP + appId);
        Trigger trigger = schedulerCenter.getTrigger(triggerKey);
        if (trigger == null) {
            return true;
        }
        return schedulerCenter.unscheduleJob(triggerKey);
    }

    /**
     * 标识当前实例的future key
     *
     * @param appId
     * @param collectTime
     * @param host
     * @param port
     * @return
     */
    public String buildFutureKey(long appId, long collectTime, String host, int port) {
        StringBuilder keyBuilder = new StringBuilder("memcached-");
        keyBuilder.append(collectTime + "-");
        keyBuilder.append(appId + "-");
        keyBuilder.append(host + ":" + port);
        return keyBuilder.toString();
    }

    private class MemcachedKeyCallable extends KeyCallable<Boolean> {
        private final long appId;
        private final long collectTime;
        private final String host;
        private final int port;
        private Map<String, Object> infoMap;    // 当前分钟的统计信息

        public MemcachedKeyCallable(long appId, long collectTime, String host, int port, Map<String, Object> infoMap) {
            super(buildFutureKey(appId, collectTime, host, port));
            this.appId = appId;
            this.collectTime = collectTime;
            this.host = host;
            this.port = port;
            this.infoMap = infoMap;
        }


        @Override
        public Boolean execute() {
            // 查询上一分钟的统计信息
            long lastMin = ScheduleUtil.getLastCollectTime(collectTime);
            Map<String, Object> lastInfoMap = instanceStatsCenter.queryStandardInfoMap(lastMin, host, port, ConstUtils.MEMCACHED);

            // 计算当前分钟和上一分钟的统计值，作为差值放到本次统计结果中，存到mongodb中。
            Map<String, Object> currentOrganizedMap = organizeInfoMap(infoMap);
            Map<String, Object> lastOrganizedMap = organizeInfoMap(lastInfoMap);
            //上一次统计指标为空,取消差值计算
            Map<String, Object> diffInfoMap;
            if (!MapUtils.isEmpty(lastOrganizedMap)) {
                diffInfoMap = calInfoMapDiff(currentOrganizedMap, lastOrganizedMap);
                infoMap.put(MemcachedConstant.Diff.getValue(), diffInfoMap);
            } else {
                logger.error("[memcached-lastInfoMap] : lastCollectTime = {} appId={} host:port = {}:{} is null", lastMin, appId, host, port);
                diffInfoMap = new HashMap<String, Object>();
            }
            // mongodb中存储的是info得到的所有信息，以及与上一分钟相比的差值diff
            instanceStatsCenter.saveStandardStats(infoMap, host, port, ConstUtils.MEMCACHED);

            InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(host, port);

            // 更新实例的主要指标信息，存入db
            InstanceStats instanceStats = getInstanceStats(appId, host, port, infoMap);
            if (instanceStats != null && instanceStatsDao != null) {
                if (instanceInfo.getParentId() != 0) {          // 实例为从
                    instanceStats.setRole((byte) 2);
                }
                instanceStatsDao.updateInstanceStats(instanceStats);
            }

            // 如果该实例是主，则将命令的分钟统计和需要累加的统计指标入库
            if (instanceInfo.getParentId() == 0) {
                long allCommandCount = 0L;
                // 基于命令执行次数的统计，大于0才保存
                List<AppCommandStats> commandStatsList = getAppCommandStats(appId, collectTime, diffInfoMap);
                for (AppCommandStats commandStats : commandStatsList) {
                    if (commandStats.getCommandCount() > 0) {
                        allCommandCount += commandStats.getCommandCount();
                        try {
                            appStatsDao.mergeMinuteCommandStatus(commandStats);
                            appStatsDao.mergeHourCommandStatus(commandStats);
                        } catch (Exception e) {
                            logger.error(e.getMessage() + appId, e);
                        }
                    }
                }

                // 基于应用的指标累加
                AppStats appStats = getAppStats(appId, collectTime, diffInfoMap, infoMap);
                try {
                    appStats.setCommandCount(allCommandCount);
                    appStatsDao.mergeMinuteAppStats(appStats);
                    appStatsDao.mergeHourAppStats(appStats);
                } catch (Exception e) {
                    logger.error(e.getMessage() + appId, e);
                }
            }
            return true;
        }
    }

    /**
     * 收集当前实例的统计信息，存入mongodb
     *
     * @param appId
     * @param collectTime
     * @param host
     * @param port
     * @return
     */
    @Override
    public Map<String, Object> collectMemcachedInfo(long appId, long collectTime, String host, int port) {
        Assert.isTrue(appId > 0);
        Assert.hasText(host);
        Assert.isTrue(port > 0);
        InstanceInfo instanceInfo = instanceDao.getInstByIpAndPort(host, port);
        //不存在实例/实例异常/下线
        if (instanceInfo == null) {
            return null;
        }
        String hostAndPort = ObjectConvert.linkIpAndPort(host, port);
        MemcachedClient memcachedClient = maintainMemcachedClientMap(host, port);
        if (memcachedClient == null) {
            logger.error("get and create memcached client error, instance: {}", hostAndPort);
            return null;
        }

        // 获取当前分钟统计信息
        Map<String, Object> currentInfoMap = this.getInfoStats(host, port);
        if (currentInfoMap == null || currentInfoMap.isEmpty()) {
            logger.error("memcached currentInfoMap is null, instance: {}", hostAndPort);
            return currentInfoMap;
        }
        currentInfoMap.put(ConstUtils.COLLECT_TIME, collectTime);
        boolean isOk = asyncService.submitFuture(new MemcachedKeyCallable(appId, collectTime, host, port, currentInfoMap));
        if (!isOk) {
            logger.error("submitFuture failed,appId:{},collectTime:{},host:{},ip:{}", new Object[]{appId, collectTime, host, port});
        }

        return currentInfoMap;
    }

    @Override
    public Map<String, Object> getInfoStats(final String host, final int port) {
        // 获取当前分钟统计信息
        final Map<String, Object> currentInfoMap = new LinkedHashMap<String, Object>();
        // 幂等操作防止异常(例如：超时)
        boolean isRun = new IdempotentConfirmer() {
            private int timeOutFactor = 1;
            @Override
            public boolean execute() {
                MemcachedClient memcachedClient = maintainMemcachedClientMap(host, port);
                if (memcachedClient == null) {
                    logger.error("get and create memcached client error, {}:{}", host, port);
                    return false;
                }
                //逐次增加超时时间
                memcachedClient.setOpTimeout(MemcachedClient.DEFAULT_OP_TIMEOUT * (timeOutFactor++));
                try {
                    Map<InetSocketAddress, Map<String, String>> statsMap = memcachedClient.getStats();
                    Map<InetSocketAddress, Map<String, String>> itemsMap = memcachedClient
                            .getStatsByItem(MemcachedConstant.Items.getValue());
                    Map<InetSocketAddress, Map<String, String>> slabsMap = memcachedClient
                            .getStatsByItem(MemcachedConstant.Slabs.getValue());

                    if (statsMap == null || statsMap.keySet().isEmpty()) {
                        logger.warn("statsMap for {}:{} is empty, return.", host, port);
                        return false;
                    }
                    InetSocketAddress address = statsMap.keySet().iterator().next();
                    currentInfoMap.put(MemcachedConstant.Stats.getValue(), statsMap.get(address));
                    currentInfoMap.put(MemcachedConstant.Items.getValue(), itemsMap.get(address));
                    currentInfoMap.put(MemcachedConstant.Slabs.getValue(), slabsMap.get(address));
                    return true;
                } catch (Exception e) {
                    logger.warn("get memcache-stats for {}:{} errormsg:{}", host, port, e.getMessage());
                    currentInfoMap.clear();
                    return false;
                }
            }
        }.run();
        if (isRun) {
            return currentInfoMap;
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean isRun(final String ip, final int port) {
        boolean isRun = new IdempotentConfirmer() {
            private int timeOutFactor = 1;
            @Override
            public boolean execute() {
                try {
                    MemcachedClient memcachedClient = maintainMemcachedClientMap(ip, port);
                    //逐次增加超时时间
                    Map<InetSocketAddress, Map<String, String>> stats = memcachedClient.getStats(MemcachedClient.DEFAULT_OP_TIMEOUT * (timeOutFactor++));
                    return stats != null && stats.size() > 0;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return false;
                }
            }
        }.run();
        if (!isRun) {
            memcachedClientMap.remove(ObjectConvert.linkIpAndPort(ip, port));
        }
        return isRun;
    }

    @Override
    public String executeCommand(AppDesc appDesc, String command) {
        long appId = appDesc.getAppId();
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
        if (instanceList == null || instanceList.isEmpty()) {
            return "应用没有运行的实例";
        }
        String host = null;
        int port = 0;
        for (InstanceInfo instanceInfo : instanceList) {
            InstanceStats instanceStats = instanceStatsDao.getInstanceStatsByHost(instanceInfo.getIp(), instanceInfo.getPort());
            if (instanceInfo != null) {
                if (instanceStats == null || instanceStats.getRole() == 1) {
                    host = instanceInfo.getIp();
                    port = instanceInfo.getPort();
                    break;
                }
            }
        }
        if (host == null || port == 0) {
            return "not found instance";
        }
        return executeCommand(host, port, command);
    }

    @Override
    public String executeCommand(String host, int port, String command) {
        if (!MemcachedReadOnlyCommandEnum.contains(command)) {
            return "only support read-only and safe command";
        }
        command = command.trim() + "\r\n";
        String exeucuteShell = "printf \"%s\" | nc %s %d";
        try {
            exeucuteShell = String.format(exeucuteShell, command, host, port);
            logger.warn("memcacheExecuteShell:{}", exeucuteShell);
            return machineCenter.executeShell(host, exeucuteShell);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "error:" + e.getMessage();
        }
    }

    /**
     * 从统计结果中提取感兴趣的命令和统计指标
     *
     * @param infoMap
     * @return
     */
    public static Map<String, Object> organizeInfoMap(Map<String, Object> infoMap) {
        Map<String, Object> accMap = new HashMap<String, Object>();
        if (MapUtils.isEmpty(infoMap)) {
            return accMap;
        }

        Map<String, Object> statsMap = (Map<String, Object>) infoMap.get(MemcachedConstant.Stats.getValue());
        // 从stats中得到命令的执行次数
        for (MemcachedCommand command : MemcachedCommand.values()) {
            accMap.put(command.getValue(), MapUtils.getLong(statsMap, command.getValue(), 0L));
        }
        // 从stats中得到累加指标的值
        for (MemcachedAccumulation acc : MemcachedAccumulation.values()) {
            accMap.put(acc.getValue(), MapUtils.getLong(statsMap, acc.getValue(), 0L));
        }

        // 额外计算命令的执行次数，如cmd_incr不存在，但可以由incr_hits和incr_misses的和得到
        accMap.put(MemcachedCommand.CmdIncr.getValue(), MapUtils.getLong(statsMap, MemcachedConstant.IncrHits.getValue(),
                0L) + MapUtils.getLong(statsMap, MemcachedConstant.IncrMisses.getValue(), 0L));
        accMap.put(MemcachedCommand.CmdDecr.getValue(), MapUtils.getLong(statsMap, MemcachedConstant.DecrHits.getValue(),
                0L) + MapUtils.getLong(statsMap, MemcachedConstant.DecrMisses.getValue(), 0L));
        accMap.put(MemcachedCommand.CmdCas.getValue(), MapUtils.getLong(statsMap, MemcachedConstant.CasHits.getValue(),
                0L) + MapUtils.getLong(statsMap, MemcachedConstant.CasMisses.getValue(), 0L));
        return accMap;
    }

    /**
     * 根据统计指标计算两个map的差值
     *
     * @param currentInfoMap
     * @param lastInfoMap
     * @return
     */
    public static Map<String, Object> calInfoMapDiff(Map<String, Object> currentInfoMap, Map<String, Object> lastInfoMap) {
        if (MapUtils.isEmpty(currentInfoMap) || MapUtils.isEmpty(lastInfoMap)) {
            return currentInfoMap;
        }
        Map<String, Object> diffInfoMap = new HashMap<String, Object>();
        // 计算命令差值
        for (MemcachedCommand command : MemcachedCommand.values()) {
            long currentValue = MapUtils.getLongValue(currentInfoMap, command.getValue(), 0);
            long lastValue = MapUtils.getLongValue(lastInfoMap, command.getValue(), 0);
            if (currentValue > lastValue) {
                diffInfoMap.put(command.getValue(), currentValue - lastValue);
            }
        }
        // 计算累加指标的差值
        for (MemcachedAccumulation acc : MemcachedAccumulation.values()) {
            long currentValue = MapUtils.getLongValue(currentInfoMap, acc.getValue(), 0);
            long lastValue = MapUtils.getLongValue(lastInfoMap, acc.getValue(), 0);
            if (currentValue > lastValue) {
                diffInfoMap.put(acc.getValue(), currentValue - lastValue);
            }
        }
        return diffInfoMap;
    }

    /**
     * 从统计的差值里，填充基于应用的累加统计指标
     *
     * @param appId
     * @param collectTime
     * @param diffMap
     * @return
     */
    public AppStats getAppStats(long appId, long collectTime, Map<String, Object> diffMap, Map<String,
            Object> infoMap) {
        AppStats appStats = new AppStats();
        appStats.setAppId(appId);
        appStats.setCollectTime(collectTime);
        appStats.setModifyTime(new Date());
        // 填充基于应用的分钟差值数据
        appStats.setEvictedKeys(MapUtils.getLongValue(diffMap, MemcachedAccumulation.Evictions.getValue(), 0));
        appStats.setExpiredKeys(MapUtils.getLongValue(diffMap, MemcachedAccumulation.Expires.getValue(), 0));
        appStats.setHits(MapUtils.getLongValue(diffMap, MemcachedAccumulation.GetHits.getValue(), 0));
        appStats.setMisses(MapUtils.getLongValue(diffMap, MemcachedAccumulation.GetMisses.getValue(), 0));
        // 填充基于应用的全局指标的数据，如已用内存、当前item数量以及当前的连接数
        Map<String, Object> statsMap = (Map<String, Object>) infoMap.get(MemcachedConstant.Stats.getValue());
        appStats.setUsedMemory(MapUtils.getLongValue(statsMap, MemcachedStats.USED_MEMORY.getValue(), 0));
        appStats.setObjectSize(MapUtils.getLongValue(statsMap, MemcachedStats.CURR_ITEMS.getValue(), 0));
        appStats.setConnectedClients(MapUtils.getIntValue(statsMap, MemcachedStats.CURR_CONNECTIONS.getValue(), 0));
        return appStats;
    }

    /**
     * 从统计的差值里填充命令执行次数
     *
     * @param appId
     * @param collectTime
     * @param infoMap
     * @return
     */
    public List<AppCommandStats> getAppCommandStats(long appId, long collectTime, Map<String, Object> infoMap) {
        List<AppCommandStats> commandStatsList = new ArrayList<AppCommandStats>();
        for (MemcachedCommand acc : MemcachedCommand.values()) {
            AppCommandStats commandStats = new AppCommandStats();
            commandStats.setAppId(appId);
            commandStats.setCollectTime(collectTime);
            commandStats.setModifyTime(new Date());
            commandStats.setCommandName(acc.getValue().replace(CMD_PREFIX, EMPTY_STRING));
            commandStats.setCommandCount(MapUtils.getLongValue(infoMap, acc.getValue(), 0));
            commandStatsList.add(commandStats);
        }
        return commandStatsList;
    }

    /**
     * 从memcached统计信息的stats中提取出一些主要的指标信息
     *
     * @param appId
     * @param ip
     * @param port
     * @param infoMap 该实例对应的统计信息
     * @return InstanceStats对象
     */
    public InstanceStats getInstanceStats(long appId, String ip, int port, Map<String, Object> infoMap) {
        if (infoMap == null) {
            logger.warn("infoMap is null, appId: {}, ip: {}, port: {}", appId, ip, port);
            return null;
        }
        Map<String, Object> statsMap = (Map<String, Object>) infoMap.get(MemcachedConstant.Stats.getValue());

        InstanceStats instanceStats = new InstanceStats();
        instanceStats.setAppId(appId);
        InstanceInfo curInst = instanceDao.getInstByIpAndPort(ip, port);
        if (curInst != null) {
            instanceStats.setHostId(curInst.getHostId());
            instanceStats.setInstId(curInst.getId());
        }
        instanceStats.setIp(ip);
        instanceStats.setPort(port);
        instanceStats.setRole((byte) 1);     // 默认是主
        instanceStats.setMaxMemory(MapUtils.getLongValue(statsMap, MemcachedStats.MAX_MEMORY.getValue(), 0));
        instanceStats.setUsedMemory(MapUtils.getLongValue(statsMap, MemcachedStats.USED_MEMORY.getValue(), 0));
        instanceStats.setHits(MapUtils.getLongValue(statsMap, MemcachedStats.HITS.getValue(), 0));
        instanceStats.setMisses(MapUtils.getLongValue(statsMap, MemcachedStats.MISSES.getValue(), 0));
        instanceStats.setCurrItems(MapUtils.getLongValue(statsMap, MemcachedStats.CURR_ITEMS.getValue(), 0));
        instanceStats.setCurrConnections(MapUtils.getIntValue(statsMap, MemcachedStats.CURR_CONNECTIONS.getValue(),
                0));
        instanceStats.setModifyTime(new Timestamp(System.currentTimeMillis()));
        return instanceStats;
    }

    /**
     * 维护memcached客户端列表，如果实例对应的客户端不存在，创建并更新列表，
     * 如果存在，直接返回即可。
     *
     * @param host
     * @param port
     * @return
     */
    private MemcachedClient maintainMemcachedClientMap(String host, int port) {
        String hostAndPort = ObjectConvert.linkIpAndPort(host, port);
        MemcachedClient memcachedClient = memcachedClientMap.get(hostAndPort);
        if (memcachedClient == null) {
            lock.lock();
            try {
                memcachedClient = memcachedClientMap.get(hostAndPort);
                if (memcachedClient == null) {
                    XMemcachedClientBuilder clientBuilder = new XMemcachedClientBuilder(AddrUtil.getAddresses(hostAndPort));
                    clientBuilder.setSessionLocator(new KetamaMemcachedSessionLocator());
                    Configuration configuration = clientBuilder.getConfiguration();
                    configuration.setStatisticsServer(false);        // 关闭客户端统计

                    clientBuilder.setConnectTimeout(CONNECTION_TIMEOUT);
                    clientBuilder.setConnectionPoolSize(CONNECTION_POOL_SIZE);      // 设置连接池
                    clientBuilder.setEnableHealSession(false);
                    memcachedClient = clientBuilder.build();
                    //关闭心跳检查
                    memcachedClient.setEnableHeartBeat(false);
                    //关闭session重连
                    memcachedClient.setEnableHealSession(false);
                    memcachedClientMap.put(hostAndPort, memcachedClient);
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }
        return memcachedClient;
    }

    @Override
    public boolean deployMemcached(final long appId, final String host, final int maxMemory) {
        /** start memcached instance */
        String startMemcachedShell = "memcached -l %s -p %d -m %d &> /opt/cachecloud/logs/memcached-%d.log &";
        int port = machineCenter.getAvailablePort(host, ConstUtils.CACHE_TYPE_MEMCACHED);
        startMemcachedShell = String.format(startMemcachedShell, host, port, maxMemory, appId);
        machineCenter.executeShell(host, startMemcachedShell);

        /** check if the instance is ok */
        String checkMemcachedShell = "printf \"%s\" | nc %s %d";
        String checkCommand = "stats slabs \n";
        checkMemcachedShell = String.format(checkMemcachedShell, checkCommand, host, port);
        String checkResponse = machineCenter.executeShell(host, checkMemcachedShell);
        if (!checkResponse.contains("active_slabs")) {
            logger.error("start memcached instance error, host: {}, port: {}, appId: {}", host, port, appId);
            return false;
        }

        /** save instance to db */
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setAppId(appId);
        instanceInfo.setCmd(startMemcachedShell);
        instanceInfo.setIp(host);
        instanceInfo.setPort(port);
        instanceInfo.setMem(maxMemory);
        instanceInfo.setType(ConstUtils.CACHE_TYPE_MEMCACHED);
        instanceInfo.setHostId(machineDao.getMachineInfoByIp(host).getId());
        instanceInfo.setParentId(0);    // always master
        instanceInfo.setStatus(1);      // enable
        instanceDao.saveInstance(instanceInfo);

        /** deploy trigger for the instance */
        boolean deployResult = deployMemcachedCollection(appId, host, port);
        if (!deployResult) {
            logger.warn("deploy trigger for instance: {}:{}-{} failed.", host, port, appId);
        }

        return true;
    }

    @Override
    public boolean shutdown(String host, int port) {
        Assert.isTrue(StringUtils.isNotBlank(host));
        Assert.isTrue(port > 0);
        boolean isRun = isRun(host, port);
        if (!isRun) {
            return true;
        }
        String closeShell = String.format("kill `ps -ef | grep mem | grep %s | grep %d  | awk '{print $2}'`", host, port);
        //暂时解决老memcache用root启动的问题。
        logger.warn("login machine command: ssh root@{}", host);
        logger.warn("memcache shutdown command: {}", closeShell);
        machineCenter.executeShell(host, closeShell);
        return true;
    }

    public void destory() {
        for (MemcachedClient client : memcachedClientMap.values()) {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }
    }
    
    @Override
    public boolean cleanAppData(AppDesc appDesc, AppUser appUser) {
        return false;
    }

    public void setSchedulerCenter(SchedulerCenter schedulerCenter) {
        this.schedulerCenter = schedulerCenter;
    }

    public void setInstanceStatsCenter(InstanceStatsCenter instanceStatsCenter) {
        this.instanceStatsCenter = instanceStatsCenter;
    }

    public void setAsyncService(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setAppStatsDao(AppStatsDao appStatsDao) {
        this.appStatsDao = appStatsDao;
    }

    public void setInstanceStatsDao(InstanceStatsDao instanceStatsDao) {
        this.instanceStatsDao = instanceStatsDao;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public void setMachineDao(MachineDao machineDao) {
        this.machineDao = machineDao;
    }
}
