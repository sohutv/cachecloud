package com.sohu.cache.machine.impl;

import com.google.common.base.Strings;
import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.constant.MachineConstant;
import com.sohu.cache.constant.MachineInfoEnum.TypeEnum;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.*;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.machine.PortGenerator;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.enums.DirEnum;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.*;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.MachineMemoryDistriEnum;
import com.sohu.cache.web.enums.RedisVersionEnum;
import com.sohu.cache.web.vo.MachineStatsVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import redis.clients.jedis.HostAndPort;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 机器接口的实现
 * User: lingguo
 */
@Service("machineCenter")
public class MachineCenterImpl implements MachineCenter {
    private final Logger logger = LoggerFactory.getLogger(MachineCenterImpl.class);
    @Autowired
    private InstanceStatsCenter instanceStatsCenter;
    @Autowired
    private MachineStatsDao machineStatsDao;
    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private InstanceStatsDao instanceStatsDao;
    @Autowired
    private MachineDao machineDao;
    @Autowired
    private RedisCenter redisCenter;
    @Autowired
    private AppDao appDao;
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private MachineRoomDao machineRoomDao;

    /**
     * 邮箱报警
     */
    @Autowired
    private EmailComponent emailComponent;
    @Autowired
    private AsyncService asyncService;

    @PostConstruct
    public void init() {
        asyncService.assemblePool(AsyncThreadPoolFactory.MACHINE_POOL,
                AsyncThreadPoolFactory.MACHINE_THREAD_POOL);
    }

    //异步执行任务
    public void asyncCollectMachineInfo(final long hostId, final long collectTime, final String ip) {
        String key = "collect-machine-" + hostId + "-" + ip + "-" + collectTime;
        asyncService.submitFuture(AsyncThreadPoolFactory.MACHINE_POOL, new KeyCallable<Boolean>(key) {
            public Boolean execute() {
                try {
                    Map<String, Object> map = collectMachineInfo(hostId, collectTime, ip);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        });
    }

    /**
     * 收集当前host的状态信息，保存到mysql；
     * 这里将hostId作为参数传入，mysql中集合名为：ip:hostId
     *
     * @param hostId      机器id
     * @param collectTime 收集时间，格式：yyyyMMddHHmm
     * @param ip          ip
     * @return 机器的统计信息
     */
    @Override
    public Map<String, Object> collectMachineInfo(final long hostId, final long collectTime, final String ip) {
        Map<String, Object> infoMap = new HashMap<String, Object>();
        MachineStats machineStats = null;
        try {
            int sshPort = SSHUtil.getSshPort(ip);
            // todo 合并SSHUTIL 到 SSHService
            machineStats = SSHUtil.getMachineInfo(ip, sshPort, ConstUtils.USERNAME, ConstUtils.PASSWORD);
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(ip);
            machineStats.setHostId(hostId);
            if (machineStats != null && machineStats.validate()) {
                infoMap.put(MachineConstant.Ip.getValue(), machineStats.getIp());
                infoMap.put(MachineConstant.CpuUsage.getValue(), machineStats.getCpuUsage());
                infoMap.put(MachineConstant.MemoryUsageRatio.getValue(), machineStats.getMemoryUsageRatio());
                /**
                 * SSHUtil返回的内存单位为k，由于实例的内存基本存储单位都是byte，所以统一为byte
                 */
                if (machineStats.getMemoryFree() != null) {
                    infoMap.put(MachineConstant.MemoryFree.getValue(),
                            Long.parseLong(machineStats.getMemoryFree()) * ConstUtils._1024);
                } else {
                    infoMap.put(MachineConstant.MemoryFree.getValue(), 0);
                }
                if (machineStats.getMemoryTotal() != null) {
                    infoMap.put(MachineConstant.MemoryTotal.getValue(),
                            Long.parseLong(machineStats.getMemoryTotal()) * ConstUtils._1024);
                } else {
                    infoMap.put(MachineConstant.MemoryTotal.getValue(), 0);
                }

                infoMap.put(MachineConstant.Load.getValue(), machineStats.getLoad());
                infoMap.put(MachineConstant.Traffic.getValue(), machineStats.getTraffic());
                infoMap.put(MachineConstant.DiskUsage.getValue(), machineStats.getDiskUsageMap());
                infoMap.put(ConstUtils.COLLECT_TIME, collectTime);
                instanceStatsCenter.saveStandardStats(infoMap, new HashMap<String, Object>(0), ip, (int) hostId,
                        ConstUtils.MACHINE);
                machineStats.setMemoryFree(Long.parseLong(machineStats.getMemoryFree()) * ConstUtils._1024 + "");
                machineStats.setMemoryTotal(Long.parseLong(machineStats.getMemoryTotal()) * ConstUtils._1024 + "");
                machineStats.setModifyTime(new Date());
                // 获取maxmemory和运行实例总数
                int maxMemory = instanceDao.getMemoryByHost(ip);
                machineStats.setMaxMemory(maxMemory);
                int instanceCount = instanceDao.getInstanceCountByHost(ip);
                machineStats.setInstanceCount(instanceCount);
                // 获取物理机入库内存
                machineStats.setMachineMemory(machineInfo.getMem() * 1024);
                machineStatsDao.mergeMachineStats(machineStats);
                logger.debug("collect machine info done, host: {}, time: {}", ip, collectTime);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("host:{} collectMachineErrorStats=>{}", ip, machineStats);
            logger.error(e.getMessage(), e);
        }
        return infoMap;
    }

    //异步执行任务
    public void asyncMonitorMachineStats(final long hostId, final String ip) {
        String key = "monitor-machine-" + hostId + "-" + ip;
        asyncService.submitFuture(AsyncThreadPoolFactory.MACHINE_POOL, new KeyCallable<Boolean>(key) {
            public Boolean execute() {
                try {
                    monitorMachineStats(hostId, ip);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        });
    }

    /**
     * 监控机器的状态
     *
     * @param hostId 机器id
     * @param ip     ip
     */
    @Override
    public void monitorMachineStats(final long hostId, final String ip) {
        Assert.isTrue(hostId > 0);
        Assert.hasText(ip);

        MachineStats machineStats = machineStatsDao.getMachineStatsByIp(ip);
        if (machineStats == null) {
            logger.warn("machine stats is null, ip: {}, time: {}", ip, new Date());
            return;
        }
        double cpuUsage = ObjectConvert.percentToDouble(machineStats.getCpuUsage(), 0);
        int memTotal = 0;
        double memoryUsage = 0.0;
        try {
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(ip);
            memTotal = machineInfo.getMem() * 1024;
            Map machineMemDetail = instanceStatsDao.getMachineMemByIp(ip);
            long memUsed = MapUtils.getLongValue(machineMemDetail, "usedMem", 0l);
            double memUsedRss = MapUtils.getDoubleValue(machineMemDetail, "usedMemRss", 0d);
            double memAllocRatio = memUsedRss / 1024 / 1024 / memTotal * 100;
            DecimalFormat df = new DecimalFormat("#.00");
            memoryUsage = ObjectConvert.percentToDouble(df.format(memAllocRatio), 0);
        } catch (Exception e) {
            logger.info("get memoryUsage error:{}", e.getMessage());
        }

        double memoryThreshold = ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD;
        /**
         * 当机器的状态超过预设的阀值时，向上汇报或者报警
         */
        StringBuilder alertContent = new StringBuilder();
        // 内存使用率
        if (memoryUsage > memoryThreshold) {
            logger.warn("memoryUsageRatio is above security line, ip: {}, memTotal: {}, memoryUsage: {}%", ip, memTotal, memoryUsage);
            alertContent.append("ip:").append(ip).append(",memTotal(G):").append(memTotal / 1024).append(",memUse(%):").append(memoryUsage);
        }
        // 报警
        if (StringUtils.isNotBlank(alertContent.toString())) {
            String title = "cachecloud机器内存报警:";
            emailComponent.sendMailToAdmin(title, alertContent.toString());
        }
    }

    /**
     * 在主机ip上的端口port上启动一个进程，并check是否启动成功；
     *
     * @param ip    ip
     * @param port  port
     * @param shell shell命令
     * @return 成功返回true，否则返回false；
     */
    @Override
    public boolean startProcessAtPort(final String ip, final int port, final String shell) {
        checkArgument(!Strings.isNullOrEmpty(ip), "invalid ip.");
        checkArgument(port > 0 && port < 65536, "invalid port");
        checkArgument(!Strings.isNullOrEmpty(shell), "invalid shell.");

        boolean success = true;

        try {
            // 执行shell命令，有的是后台执行命令，没有返回值; 如果端口被占用，表示启动成功；
            SSHUtil.execute(ip, shell);
            success = isPortUsed(ip, port);
        } catch (SSHException e) {
            logger.error("execute shell command error, ip: {}, port: {}, shell: {}", ip, port, shell);
            logger.error(e.getMessage(), e);
        }
        return success;
    }

    /**
     * 多次验证是否进程已经启动
     *
     * @param ip
     * @param port
     * @return
     */
    private boolean isPortUsed(final String ip, final int port) {
        boolean isPortUsed = new IdempotentConfirmer() {
            private int sleepTime = 100;

            @Override
            public boolean execute() {
                try {
                    boolean success = SSHUtil.isPortUsed(ip, port);
                    if (!success) {
                        TimeUnit.MILLISECONDS.sleep(sleepTime);
                        sleepTime += 100;
                    }
                    return success;
                } catch (SSHException e) {
                    logger.error(e.getMessage(), e);
                    return false;
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        }.run();
        return isPortUsed;
    }

    /**
     * 执行shell命令，并将结果返回；
     *
     * @param ip    机器ip
     * @param shell shell命令
     * @return 命令的返回值
     */
    @Override
    public String executeShell(final String ip, final String shell) {
        checkArgument(!Strings.isNullOrEmpty(ip), "invalid ip.");
        checkArgument(!Strings.isNullOrEmpty(shell), "invalid shell.");

        String result = null;
        try {
            result = SSHUtil.execute(ip, shell);
        } catch (SSHException e) {
            logger.error("execute shell: {} at ip: {} error.", shell, ip, e);
            result = ConstUtils.INNER_ERROR;
        }

        return result;
    }

    @Override
    public String executeShell(String ip, String shell, Integer timeout) {
        checkArgument(!Strings.isNullOrEmpty(ip), "invalid ip.");
        checkArgument(!Strings.isNullOrEmpty(shell), "invalid shell.");

        String result = null;
        try {
            result = SSHUtil.execute(ip, shell, timeout);
        } catch (SSHException e) {
            logger.error("execute shell: {} at ip: {} error, timeout: {}", shell, ip, timeout, e);
            result = ConstUtils.INNER_ERROR;
        }

        return result;
    }

    /**
     * 获取指定server上的一个可用的端口；type表示cache的类型；
     * PortGenerator是线程安全的；
     *
     * @param ip   目标server；
     * @param type cache类型
     * @return 可用端口，如果为null，则表示发生异常；
     */
    @Override
    public Integer getAvailablePort(final String ip, final int type) {

        Integer availablePort = PortGenerator.getRedisPort(ip);
        // 去实例表中再check一下，该端口是否从来没被使用过
        while (instanceDao.getCountByIpAndPort(ip, availablePort) > 0) {
            availablePort++;
        }
        return availablePort;
    }

    /**
     * 根据content的配置内容创建配置文件，并推送到目标server的约定目录下；
     * 文件内容有更新，会覆写；
     *
     * @param host     要推送到的目标server；
     * @param fileName 配置文件名
     * @param content  配置文件的内容
     * @return 配置文件在远程server上的绝对路径，如果为null则表示失败；
     */
    @Override
    public String createRemoteFile(final String host, String fileName, List<String> content) {
        checkArgument(!Strings.isNullOrEmpty(host), "invalid host.");
        checkArgument(!Strings.isNullOrEmpty(fileName), "invalid fileName.");
        checkArgument(content != null && content.size() > 0, "content is empty.");

        String localAbsolutePath = MachineProtocol.TMP_DIR + fileName;
        File tmpDir = new File(MachineProtocol.TMP_DIR);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                logger.error("cannot create dir:{} directory. ", tmpDir.getAbsolutePath());
            }
        }

        Path path = Paths.get(MachineProtocol.TMP_DIR + fileName);
        String confDir = getMachineRelativeDir(host, DirEnum.CONF_DIR.getValue());
        String remotePath = confDir + fileName;
        /**
         * 将配置文件的内容写到本地
         */
        try {
            BufferedWriter bufferedWriter = Files
                    .newBufferedWriter(path, Charset.forName(MachineProtocol.ENCODING_UTF8));
            try {
                for (String line : content) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            } finally {
                if (bufferedWriter != null)
                    bufferedWriter.close();
            }
        } catch (IOException e) {
            logger.error("write redis config file error, ip: {}, filename: {}, content: {}, e", host, fileName, content,
                    e);
            return null;
        } finally {

        }

        /**
         * 将配置文件推送到目标机器上
         */
        try {
            // k8s资源创建相关目录
            if (isK8sMachine(host)) {
                String mkdirCommand = "mkdir " + MachineProtocol.getK8sConfDir(host) + " " + MachineProtocol.getK8sDataDir(host) + " " + MachineProtocol.getK8sLogDir(host);
                String mkdirResult = SSHUtil.execute(host, mkdirCommand);
                logger.info("execute mkdir :" + mkdirResult);
            }
            SSHUtil.scpFileToRemote(host, localAbsolutePath, confDir);
        } catch (SSHException e) {
            logger.error("scp config file to remote server error: ip: {}, fileName: {}", host, fileName, e);
            return null;
        }

        /**
         * 删除临时文件
         */
        File file = new File(localAbsolutePath);
        if (file.exists()) {
            boolean del = file.delete();
            if (!del) {
                logger.warn("file.delete:{}", del);
            }
        }

        return remotePath;
    }

    @Override
    public List<MachineStats> getMachineStats(String ipLike) {
        List<MachineInfo> machineInfoList = machineDao.getMachineInfoByLikeIp(ipLike);
        List<SystemResource> versionList = resourceDao.getResourceList(ResourceEnum.REDIS.getValue());

        String versionStr = "";//获取Redis所有有效安装版本
        StringBuilder sb = new StringBuilder();
        if (versionList != null && versionList.size() > 0) {
            for (SystemResource version : versionList) {
                sb.append(version.getName()).append("#1;");
            }
            versionStr = sb.toString();
        }
        List<MachineStats> machineStatsList = new ArrayList<MachineStats>();
        for (MachineInfo machineInfo : machineInfoList) {
            String ip = machineInfo.getIp();
            MachineStats machineStats = machineStatsDao.getMachineStatsByIp(ip);
            if (machineStats == null) {
                machineStats = new MachineStats();
            }
            machineStats.setMemoryAllocated(instanceDao.getMemoryByHost(ip));
            machineStats.setInfo(machineInfo);
            machineStats.setIsInstall(1);//设置Redis都已经安装
            // 判断是否已安装完成Redis
            if (!StringUtils.isEmpty(versionStr)) {
                for (String version : versionStr.split(";")) {
                    if (StringUtils.isEmpty(machineInfo.getVersionInstall())) {
                        machineStats.setIsInstall(0);
                    }
                    if (!StringUtils.isEmpty(version) && !StringUtils.isEmpty(machineInfo.getVersionInstall()) && machineInfo.getVersionInstall().indexOf(version) == -1) {
                        machineStats.setIsInstall(0);
                    }
                }
            }
            machineStatsList.add(machineStats);
        }
        return machineStatsList;
    }

    /**
     * 获取机器列表
     *
     * @param ipLike
     * @param versionId
     * @param isInstall
     */
    public List<MachineStats> getMachineStats(String ipLike, Integer useType, Integer type, Integer versionId, Integer isInstall, Integer k8sType, String realip) {

        // 版本安装条件过滤
        String versionName = (versionId == null || versionId == -1) ? "" : resourceDao.getResourceById(versionId).getName();
        String installInfo = (isInstall == null || isInstall == -1) ? "" : String.valueOf(isInstall);
        String versionCondition = "";
        if (!StringUtil.isBlank(versionName) || !StringUtil.isBlank(installInfo)) {
            versionCondition = versionName + "#" + installInfo;
        }
        List<MachineInfo> machineList = machineDao.getMachineInfoByCondition(ipLike, useType == null ? -1 : useType.intValue(), type == null ? -1 : type.intValue(), versionCondition, k8sType == null ? -1 : k8sType.intValue(), realip);

        List<SystemResource> versionList = resourceDao.getResourceList(ResourceEnum.REDIS.getValue());
        //获取Redis所有有效安装版本
        final String versionStr = versionList.stream().map(version -> version.getName() + "#1").collect(Collectors.joining(";"));

        return machineList.parallelStream().map(machineInfo -> {
            String ip = machineInfo.getIp();
            MachineStats machineStats = machineStatsDao.getMachineStatsByIp(ip);
            if (machineStats == null) {
                machineStats = new MachineStats();
                machineStats.setIp(ip);
            }
            machineStats.setMemoryAllocated(instanceDao.getMemoryByHost(ip));
            machineStats.setInfo(machineInfo);
            machineStats.setIsInstall(1);
            // 判断是否已安装完成Redis
            if (!StringUtils.isEmpty(versionStr)) {
                for (String version : versionStr.split(";")) {
                    if (StringUtils.isEmpty(machineInfo.getVersionInstall()) ||
                            (!StringUtils.isEmpty(version) && machineInfo.getVersionInstall().indexOf(version) == -1)) {
                        machineStats.setIsInstall(0);
                    }
                }
            }
            //填充machineMemInfo信息
            MachineMemInfo machineMemInfo = new MachineMemInfo();
            Map memRes = instanceStatsDao.getMachineMemByIp(ip);
            machineMemInfo.setIp(ip);
            machineMemInfo.setApplyMem(MapUtils.getLongValue(memRes, "applyMem", 0l));
            machineMemInfo.setUsedMem(MapUtils.getLongValue(memRes, "usedMem", 0l));
            machineMemInfo.setUsedMemRss(MapUtils.getDoubleValue(memRes, "usedMemRss", 0d));
            machineStats.setMachineMemInfo(machineMemInfo);

            return machineStats;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MachineStats> getAllMachineStats() {
        List<MachineStats> list = machineStatsDao.getAllMachineStats();
        for (MachineStats ms : list) {
            String ip = ms.getIp();
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(ip);
            if (machineInfo == null || machineInfo.isOffline()) {
                continue;
            }

            int memoryHost = instanceDao.getMemoryByHost(ip);
            getMachineMemoryDetail(ms.getIp());

            //获取机器申请和使用内存
            long applyMem = 0;
            long usedMem = 0;
            List<InstanceStats> instanceStats = instanceStatsDao.getInstanceStatsByIp(ip);
            for (InstanceStats instance : instanceStats) {
                applyMem += instance.getMaxMemory();
                usedMem += instance.getUsedMemory();
            }
            MachineMemInfo machineMemInfo = new MachineMemInfo();
            machineMemInfo.setIp(ip);
            machineMemInfo.setApplyMem(applyMem);
            machineMemInfo.setUsedMem(usedMem);
            ms.setMachineMemInfo(machineMemInfo);

            ms.setMemoryAllocated(memoryHost);
            ms.setInfo(machineInfo);
        }
        return list;
    }

    @Override
    public List<MachineMemStatInfo> getAllValidMachineMem(List<String> excludeMachineList, String room,
                                                          Integer useType) {
        List<MachineMemStatInfo> machineMemStatInfoList = machineDao.getMachineMemStatInfoByCondition(room, useType);
        return machineMemStatInfoList.parallelStream().filter(memStatInfo -> !excludeMachineList.contains(memStatInfo.getIp()))
                .map(memStatInfo -> {
                    MachineMemStatInfo memStatInfoNew = memStatInfo;
                    Map memRes = instanceStatsDao.getMachineMemByIp(memStatInfo.getIp());
                    memStatInfoNew.setApplyMem(MapUtils.getLong(memRes, "applyMem", 0l));
                    memStatInfoNew.setUsedMem(MapUtils.getLong(memRes, "usedMem", 0l));
                    memStatInfoNew.setInstanceNum(MapUtils.getIntValue(memRes, "instanceNum", 0));
                    return memStatInfoNew;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MachineMemStatInfo> getValidMachineMemByIpList(List<String> ipList) {
        if (ipList != null) {
            try {
                List<MachineMemStatInfo> memStatInfoList = machineDao.getMachineMemStatInfoByIpList(ipList);
                for (MachineMemStatInfo memStatInfo : memStatInfoList) {
                    Map memRes = instanceStatsDao.getMachineMemByIp(memStatInfo.getIp());
                    memStatInfo.setApplyMem(MapUtils.getLong(memRes, "applyMem", 0l));
                    memStatInfo.setUsedMem(MapUtils.getLong(memRes, "usedMem", 0l));
                    memStatInfo.setInstanceNum(MapUtils.getIntValue(memRes, "instanceNum", 0));
                }
                return memStatInfoList;
            } catch (Exception e) {
                logger.info("getValidMachineMemByIpList error: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    public MachineInfo getMachineInfoByIp(String ip) {
        return machineDao.getMachineInfoByIp(ip);
    }

    @Override
    public MachineStats getMachineMemoryDetail(String ip) {
        long applyMem = 0;
        long usedMem = 0;
        List<InstanceStats> instanceStats = instanceStatsDao.getInstanceStatsByIp(ip);
        for (InstanceStats instance : instanceStats) {
            applyMem += instance.getMaxMemory();
            usedMem += instance.getUsedMemory();
        }

        MachineStats machineStats = machineStatsDao.getMachineStatsByIp(ip);
        MachineInfo machineInfo = machineDao.getMachineInfoByIp(ip);
        // 机器下线 查不到相关信息
        if (machineStats == null) {
            return null;
        }
        // 机器下线 查不到相关信息
        if (machineInfo != null) {
            machineStats.setInfo(machineInfo);
        }
        MachineMemInfo machineMemInfo = new MachineMemInfo();
        machineMemInfo.setIp(ip);
        machineMemInfo.setApplyMem(applyMem);
        machineMemInfo.setUsedMem(usedMem);
        machineStats.setMachineMemInfo(machineMemInfo);

        int memoryHost = instanceDao.getMemoryByHost(ip);
        machineStats.setMemoryAllocated(memoryHost);

        return machineStats;
    }

    public List<InstanceStats> getMachineInstanceStatsByIp(String ip) {
        return instanceStatsDao.getInstanceStatsByIp(ip);
    }

    @Override
    public List<InstanceInfo> getMachineInstanceInfo(String ip) {
        List<InstanceInfo> resultList = instanceDao.getInstListByIp(ip);
        if (resultList != null && resultList.size() > 0) {
            for (InstanceInfo instanceInfo : resultList) {
                int type = instanceInfo.getType();
                if (instanceInfo.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                    continue;
                }
                if (TypeUtil.isRedisType(type)) {
                    if (TypeUtil.isRedisSentinel(type)) {
                        continue;
                    }
                    String host = instanceInfo.getIp();
                    int port = instanceInfo.getPort();
                    long appId = instanceInfo.getAppId();
                    AppDesc appDesc = appDao.getAppDescById(appId);
                    String password = appDesc.getPasswordMd5();
                    BooleanEnum isMaster = redisCenter.isMaster(appId, host, port);
                    instanceInfo.setRoleDesc(isMaster);
                    if (isMaster == BooleanEnum.FALSE) {
                        HostAndPort hap = redisCenter.getMaster(host, port, password);
                        if (hap != null) {
                            instanceInfo.setMasterHost(hap.getHost());
                            instanceInfo.setMasterPort(hap.getPort());
                            for (InstanceInfo innerInfo : resultList) {
                                if (innerInfo.getIp().equals(hap.getHost())
                                        && innerInfo.getPort() == hap.getPort()) {
                                    instanceInfo.setMasterInstanceId(innerInfo.getId());
                                    break;
                                }
                            }
                        }
                    }

                }
            }
        } else {
            return resultList;
        }
        return resultList;
    }

    @Override
    public String showInstanceRecentLog(InstanceInfo instanceInfo, int maxLineNum) {
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        int type = instanceInfo.getType();
        String logType = "";
        if (TypeUtil.isRedisDataType(type)) {
            logType = "redis-";
        } else if (TypeUtil.isRedisSentinel(type)) {
            logType = "redis-sentinel-";
        }

        String remoteFilePath = getMachineRelativeDir(host, DirEnum.LOG_DIR.getValue()) + logType + port + "-*.log";
        StringBuilder command = new StringBuilder();
        command.append("/usr/bin/tail -n").append(maxLineNum).append(" ").append(remoteFilePath);
        try {
            return SSHUtil.execute(host, command.toString());
        } catch (SSHException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public List<MachineInfo> getMachineInfoByType(TypeEnum typeEnum) {
        try {
            return machineDao.getMachineInfoByType(typeEnum.getType());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Integer> getMachineInstanceCountMap() {
        List<Map<String, Object>> mapList = instanceDao.getMachineInstanceCountMap();
        if (CollectionUtils.isEmpty(mapList)) {
            return Collections.emptyMap();
        }

        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        for (Map<String, Object> map : mapList) {
            String ip = MapUtils.getString(map, "ip", "");
            if (StringUtils.isBlank(ip)) {
                continue;
            }
            int count = MapUtils.getIntValue(map, "count");
            resultMap.put(ip, count);
        }
        return resultMap;
    }

    public Map<String, MachineInfo> getK8sMachineMap() {
        Map<String, MachineInfo> k8sMachineMaps = new HashMap<String, MachineInfo>();
        List<MachineInfo> k8sMachineList = machineDao.getK8sMachineList();
        if (!CollectionUtils.isEmpty(k8sMachineList)) {
            for (MachineInfo machineInfo : k8sMachineList) {
                k8sMachineMaps.put(machineInfo.getIp(), machineInfo);
            }
        }
        return k8sMachineMaps;
    }

    public List<RedisVersionStat> getMachineInstallRedisStat(List<SystemResource> resourceList){

        List<MachineInfo> allMachines = machineDao.getAllMachines();
        //1.遍历机器安装情况
        Map<String, Integer> installStats = new HashMap<String, Integer>();
        if (allMachines != null && allMachines.size() > 0) {
            for (MachineInfo machine : allMachines) {
                String version_install = machine.getVersionInstall();
                if (!StringUtils.isEmpty(version_install)) {
                    for (String installinfo : version_install.split(";")) {
                        Integer count = MapUtils.getInteger(installStats, installinfo, 0) + 1;
                        installStats.put(installinfo, count);
                    }
                }
            }
        }

        //2. app stat
        List<Map<String, Integer>> appVersionStats = appDao.getVersionStat();

        Map<String, Integer> appStats = new HashMap<String, Integer>();
        if (appVersionStats != null && appVersionStats.size() > 0) {
            for (Map<String, Integer> appVersion : appVersionStats) {
                appStats.put(MapUtils.getString(appVersion, "version_id"), MapUtils.getInteger(appVersion, "num", 0));
            }
        }
        //3.安装信息写入redisVersionStat
        List<RedisVersionStat> redisVersionStatList = new ArrayList<RedisVersionStat>();
        for (SystemResource redisResource : resourceList) {
            RedisVersionStat redisVersionStat = new RedisVersionStat(redisResource);
            redisVersionStat.setInstallNum(MapUtils.getInteger(installStats,
                    redisResource.getName() + ConstUtils.POUND + RedisVersionEnum.Redis_installed.getValue(), 0));
            redisVersionStat.setUninstallNum(MapUtils.getInteger(installStats,
                    redisResource.getName() + ConstUtils.POUND + RedisVersionEnum.Redis_uninstalled.getValue(), 0));
            redisVersionStat.setInstallExceptionNum(MapUtils.getInteger(installStats,
                    redisResource.getName() + ConstUtils.POUND + RedisVersionEnum.Redis_installException.getValue(), 0));
            redisVersionStat.setTotalMachineNum(allMachines.size());
            if (!CollectionUtils.isEmpty(allMachines)) {
                redisVersionStat.setInstallRatio(redisVersionStat.getInstallNum() * 100 / allMachines.size());
            } else {
                redisVersionStat.setInstallRatio(0);
            }
            redisVersionStat.setAppUsedNum(MapUtils.getIntValue(appStats, redisResource.getId() + ""));
            redisVersionStatList.add(redisVersionStat);
        }
        return redisVersionStatList;
    }

    public List<MachineInfo> getAllEffectiveMachines() {
        return machineDao.getAllMachines();
    }

    public List<MachineRoom> getEffectiveRoom() {
        return machineRoomDao.getEffectiveRoom();
    }

    @Override
    public List<MachineRoom> getAllRoom() {
        return machineRoomDao.getAllRoom();
    }

    @Override
    public Map<MachineMemoryDistriEnum, Integer> getMaxMemoryDistribute() {
        Map<MachineMemoryDistriEnum, Integer> resultMap = new HashMap<MachineMemoryDistriEnum, Integer>();
        List<MachineStats> machineStatsList = machineStatsDao.getAllMachineStats();
        for (MachineStats machineStats : machineStatsList) {
            int percent = 0;
            if (machineStats.getMachineMemory() > 0) {
                percent = machineStats.getMaxMemory() * 100 / machineStats.getMachineMemory();
            }
            MachineMemoryDistriEnum machineMemoryDistriEnum = MachineMemoryDistriEnum.getRightPercentDistri(percent);
            if (resultMap.containsKey(machineMemoryDistriEnum)) {
                resultMap.put(machineMemoryDistriEnum, resultMap.get(machineMemoryDistriEnum) + 1);
            } else {
                resultMap.put(machineMemoryDistriEnum, 1);
            }
        }
        return resultMap;
    }

    @Override
    public Map<MachineMemoryDistriEnum, Integer> getUsedMemoryDistribute() {
        Map<MachineMemoryDistriEnum, Integer> resultMap = new HashMap<MachineMemoryDistriEnum, Integer>();
        //机器自身统计map
        List<MachineStats> machineStatsList = machineStatsDao.getAllMachineStats();
        //机器实例统计map
        List<MachineInstanceStat> machineInstanceStatList = instanceStatsDao.getMachineInstanceStatList();
        Map<String, MachineInstanceStat> machineInstanceStatMap = new HashMap<String, MachineInstanceStat>();
        for (MachineInstanceStat machineInstanceStat : machineInstanceStatList) {
            machineInstanceStatMap.put(machineInstanceStat.getIp(), machineInstanceStat);
        }
        for (MachineStats machineStats : machineStatsList) {
            int machineMemory = machineStats.getMachineMemory();
            long usedMemory = machineInstanceStatMap.containsKey(machineStats.getIp()) ? machineInstanceStatMap.get(machineStats.getIp()).getUsedMemory() / 1024 / 1024 : 0;
            int percent = 0;
            if (machineMemory > 0) {
                percent = (int) (usedMemory * 100 / machineMemory);
            }
            MachineMemoryDistriEnum machineMemoryDistriEnum = MachineMemoryDistriEnum.getRightPercentDistri(percent);
            if (machineMemoryDistriEnum == null) {
                logger.warn("=======ip {} percent {} is not is MachineMemoryDistriEnum========", machineStats.getIp(), percent);
                continue;
            }
            if (resultMap.containsKey(machineMemoryDistriEnum)) {
                resultMap.put(machineMemoryDistriEnum, resultMap.get(machineMemoryDistriEnum) + 1);
            } else {
                resultMap.put(machineMemoryDistriEnum, 1);
            }
        }
        return resultMap;
    }

    @Override
    public List<MachineStatsVo> getmachineStatsVoList() {
        List<MachineInfo> machineInfoList = machineDao.getAllMachines();

        //机器自身统计map
        List<MachineStats> machineStatsList = machineStatsDao.getAllMachineStats();
        Map<String, MachineStats> machineStatsMap = new HashMap<String, MachineStats>();
        for (MachineStats machineStats : machineStatsList) {
            machineStatsMap.put(machineStats.getIp(), machineStats);
        }

        //机器实例统计map
        List<MachineInstanceStat> machineInstanceStatList = instanceStatsDao.getMachineInstanceStatList();
        Map<String, MachineInstanceStat> machineInstanceStatMap = new HashMap<String, MachineInstanceStat>();
        for (MachineInstanceStat machineInstanceStat : machineInstanceStatList) {
            machineInstanceStatMap.put(machineInstanceStat.getIp(), machineInstanceStat);
        }

        Map<String, MachineStatsVo> machineRoomMachineStatsVoMap = new HashMap<String, MachineStatsVo>();
        for (MachineInfo machineInfo : machineInfoList) {
            String ip = machineInfo.getIp();

            //机器统计
            MachineStats machineStats = machineStatsMap.get(ip);
            if (machineStats == null) {
                machineStats = new MachineStats();
            }
            // 单位MB
            long machineMemoryTotal = machineInfo.getMem() * 1024;
            long machineFreeTotal = machineMemoryTotal - machineStats.getMaxMemory();

            //实例统计
            MachineInstanceStat machineInstanceStat = machineInstanceStatMap.get(ip);
            if (machineInstanceStat == null) {
                machineInstanceStat = new MachineInstanceStat();
            }
            long instanceMaxMemory = machineInstanceStat.getMaxMemory();
            long instanceUsedmemory = machineInstanceStat.getUsedMemory();

            String machineRoom = machineInfo.getRoom();
            if (machineRoomMachineStatsVoMap.containsKey(machineRoom)) {
                MachineStatsVo machineStatsVo = machineRoomMachineStatsVoMap.get(machineRoom);
                machineStatsVo.setTotalMachineMem(machineMemoryTotal + machineStatsVo.getTotalMachineMem());
                machineStatsVo.setTotalMachineFreeMem(machineFreeTotal + machineStatsVo.getTotalMachineFreeMem());
                machineStatsVo.setTotalInstanceMaxMem(instanceMaxMemory + machineStatsVo.getTotalInstanceMaxMem());
                machineStatsVo.setTotalInstanceUsedMem(instanceUsedmemory + machineStatsVo.getTotalInstanceUsedMem());
            } else {
                MachineStatsVo machineStatsVo = new MachineStatsVo();
                machineStatsVo.setMachineRoom(machineRoom);
                machineStatsVo.setTotalMachineMem(machineMemoryTotal);
                machineStatsVo.setTotalMachineFreeMem(machineFreeTotal);
                machineStatsVo.setTotalInstanceMaxMem(instanceMaxMemory);
                machineStatsVo.setTotalInstanceUsedMem(instanceUsedmemory);
                machineRoomMachineStatsVoMap.put(machineRoom, machineStatsVo);
            }
        }

        List<MachineStatsVo> machineStatsVoList = new ArrayList<MachineStatsVo>(machineRoomMachineStatsVoMap.values());

        MachineStatsVo totalMachineStatsVo = new MachineStatsVo();
        totalMachineStatsVo.setMachineRoom("total");
        for (MachineStatsVo machineStatsVo : machineStatsVoList) {
            totalMachineStatsVo.setTotalMachineMem(totalMachineStatsVo.getTotalMachineMem() + machineStatsVo.getTotalMachineMem());
            totalMachineStatsVo.setTotalMachineFreeMem(totalMachineStatsVo.getTotalMachineFreeMem() + machineStatsVo.getTotalMachineFreeMem());
            totalMachineStatsVo.setTotalInstanceMaxMem(totalMachineStatsVo.getTotalInstanceMaxMem() + machineStatsVo.getTotalInstanceMaxMem());
            totalMachineStatsVo.setTotalInstanceUsedMem(totalMachineStatsVo.getTotalInstanceUsedMem() + machineStatsVo.getTotalInstanceUsedMem());
        }
        machineStatsVoList.add(0, totalMachineStatsVo);

        return machineStatsVoList;
    }

    /**
     * 获取redis基准目录
     * 例如/media/disk1/fordata/redis_server/redis-cluster/cluster_mmuIllegalDupUnhandledCount_zw/sentinel-22090
     *
     * @param appId
     * @param port
     * @param instanceTypeEnum
     * @return
     */
    @Override
    public String getInstanceRemoteBasePath(long appId, int port, InstanceTypeEnum instanceTypeEnum) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            logger.warn(BaseTask.marker, "appId {} appDesc is null", appId);
            return "";
        }
        String instanceTypeName = instanceTypeEnum.getName();
        String basePath = InstanceTypeEnum.PIKA.getType() == instanceTypeEnum.getType() ? ConstUtils.PIKA_INSTALL_BASE_DIR : ConstUtils.REDIS_INSTALL_BASE_DIR;
        return String.format("%s/%s/%s-%d", basePath, appDesc.getName(), instanceTypeName, port);
    }

    public String getMachineRelativeDir(String host, int dirType) {
        MachineInfo machineInfo = machineDao.getMachineInfoByIp(host);
        if (machineInfo != null &&  machineInfo.isK8sMachine(machineInfo.getK8sType())) {
            return MachineProtocol.getK8sDir(host, dirType);
        }
        return MachineProtocol.getDir(dirType);
    }

    public Boolean isK8sMachine(String host) {
        MachineInfo machineInfo = machineDao.getMachineInfoByIp(host);
        if (machineInfo != null && machineInfo.isK8sMachine(machineInfo.getK8sType())) {
            return true;
        }
        return false;
    }

    public String getFirstMachineIp(){
        List<MachineInfo> machines = machineDao.getAllMachines();
        if(!CollectionUtils.isEmpty(machines)){
            return machines.get(0).getIp();
        }
        return null;
    }
}
