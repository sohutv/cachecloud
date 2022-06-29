package com.sohu.cache.machine.impl;

import com.google.common.base.Strings;
import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.constant.MachineConstant;
import com.sohu.cache.constant.MachineInfoEnum;
import com.sohu.cache.constant.MachineInfoEnum.TypeEnum;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.*;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.machine.PortGenerator;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.enums.DirEnum;
import com.sohu.cache.report.ReportDataComponent;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.*;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.CheckEnum;
import com.sohu.cache.web.enums.MachineMemoryDistriEnum;
import com.sohu.cache.web.enums.AlertTypeEnum;
import com.sohu.cache.web.service.AppAlertRecordService;
import com.sohu.cache.web.vo.MachineEnv;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
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
    @Autowired
    private SSHService sshService;
    @Autowired
    protected AsyncService asyncService;
    @Autowired
    private ForkJoinPool forkJoinPool;
    @Autowired
    private AppAlertRecordService appAlertRecordService;
    @Autowired
    private ReportDataComponent reportDataComponent;

    /**
     * 邮箱报警
     */
    @Autowired
    private EmailComponent emailComponent;

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

                //上报数据
                reportDataComponent.reportMachineData(machineStats);

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
            appAlertRecordService.saveAlertInfoByType(AlertTypeEnum.MACHINE_MEMORY_OVER_PRESET, title, alertContent.toString(), ip);
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

        String tmpDirectory = MachineProtocol.TMP_DIR + host + "/";

        String localAbsolutePath = tmpDirectory + fileName;
        File tmpDir = new File(tmpDirectory);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                logger.error("cannot create dir:{} directory. ", tmpDir.getAbsolutePath());
            }
        }

        Path path = Paths.get(tmpDirectory + fileName);
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
            long machineMemoryTotal = machineInfo.getMem() * 1024L;
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
        if (machineInfo != null && machineInfo.isK8sMachine(machineInfo.getK8sType())) {
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

    public Map<String, Object> getExceptionMachineEnv(Date searchDate) {

        Map<String, Object> exceptionMap = new HashMap<String, Object>();
        Map<String, Object> allMachineEnvMap = getAllMachineEnv(searchDate, MachineInfoEnum.MachineTypeEnum.ALL.getValue());
        // 过滤需要监控的数据
        List<Map<String, Object>> containerlist = (List<Map<String, Object>>) allMachineEnvMap.get(MachineInfoEnum.MachineEnum.CONTAINER.getValue());
        List<Map<String, Object>> hostlist = (List<Map<String, Object>>) allMachineEnvMap.get(MachineInfoEnum.MachineEnum.HOST.getValue());

        exceptionMap.put(MachineInfoEnum.MachineEnum.CONTAINER.getValue(), containerlist.stream().filter(map -> MapUtils.getInteger(map, "status") != CheckEnum.CONSISTENCE.getValue()).collect(Collectors.toList()));
        exceptionMap.put(MachineInfoEnum.MachineEnum.HOST.getValue(), hostlist.stream().filter(map -> MapUtils.getInteger(map, "status") != CheckEnum.CONSISTENCE.getValue()).collect(Collectors.toList()));
        return exceptionMap;
    }

    public Map<String, Object> getAllMachineEnv(Date searchDate, int type) {

        Map<String, Object> resultMap = new HashMap<String, Object>();

        List<MachineInfo> allMachines = machineDao.getAllMachines();
        Set<String> iplist = new HashSet<String>();
        Set<String> hostlist = new HashSet<String>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        if (!CollectionUtils.isEmpty(allMachines)) {
            for (MachineInfo machineInfo : allMachines) {
                iplist.add(machineInfo.getIp());
                hostlist.add(machineInfo.getRealIp());
            }
        }
        /**
         *  检测容器:
         *  1.内存分配策略
         *  2.thp大内存页配置
         *  3.内存swap配置
         *  4.容器nproc配置
         */
        String container_cmd =
                "cat /proc/sys/vm/overcommit_memory;" +
                        "cat /proc/sys/vm/swappiness;" +
                        "cat /sys/kernel/mm/transparent_hugepage/enabled;" +
                        "cat /sys/kernel/mm/transparent_hugepage/defrag;" +
                        "cat /etc/security/limits.d/*-nproc.conf | grep '*          soft    nproc'" +
                        "";
        /**
         * 检测宿主机:
         * 1.检测用户连接的进程数 大于>=1024
         * 2.检测宿主机所有实例aof写盘阻塞 >=3次
         * 3.检测 somaxconn 512
         * 4.检测 sshpass安装
         * 5.运行redis实例总数
         * 6.ulimit 打开文件句柄检测
         * 7.磁盘/内存使用情况
         */
        String machine_cmd =
                "cat /proc/sys/net/core/somaxconn;" +
                        "cat /data/redis/logs/*/* | grep '" + dateFormat.format(searchDate) + "' | grep 'slow down Redis'  | wc -l;" +
                        "ps -u cachecloud -L | wc -l;" +
                        "sshpass -V | head -1;" +
                        "ulimit -n;" +
                        "echo 0;" +
                        "df -h | grep '/dev' | grep '/data' | awk '{print $5\"(\"$3\"/\"$2\")\"}';" +
                        "ps -ef | grep redis | wc -l;" +
                        "";


        List<Map<String, Object>> containerInfo = new ArrayList<>();
        List<Map<String, Object>> machineInfo = new ArrayList<>();
        long phase1 = System.currentTimeMillis();
        if (type == MachineInfoEnum.MachineTypeEnum.CONTAINER.getValue() || type == MachineInfoEnum.MachineTypeEnum.ALL.getValue()) {
            if (!CollectionUtils.isEmpty(iplist)) {
                ForkJoinTask<Map<String, Map<String, Object>>> container_task = forkJoinPool.submit(() -> iplist.parallelStream().collect(Collectors.toMap(containerIp -> containerIp, containerIp -> new MachinetaskCallable(containerIp, container_cmd, sshService, MachineInfoEnum.MachineEnum.CONTAINER.getValue()).call())));
                try {
                    Map<String, Map<String, Object>> container_result = container_task.get(30, TimeUnit.SECONDS);
                    if (!MapUtils.isEmpty(container_result)) {
                        for (Map.Entry<String, Map<String, Object>> container : container_result.entrySet()) {
                            Map<String, Object> res = container.getValue();
                            if (!MapUtils.isEmpty(res)) {
                                containerInfo.add(res);
                            }
                        }
                    }
                    logger.info("container result size:{}", container_result.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }
        long phase2 = System.currentTimeMillis();
        logger.info("container check env cost time:{} ms", phase2 - phase1);

        if (type == MachineInfoEnum.MachineTypeEnum.HOST.getValue() || type == MachineInfoEnum.MachineTypeEnum.ALL.getValue()) {
            if (!CollectionUtils.isEmpty(hostlist)) {
                ForkJoinTask<Map<String, Map<String, Object>>> machine_task = forkJoinPool.submit(() -> hostlist.parallelStream().collect(Collectors.toMap(machineIp -> machineIp, machineIp -> new MachinetaskCallable(machineIp, machine_cmd, sshService, MachineInfoEnum.MachineEnum.HOST.getValue()).call())));
                try {
                    Map<String, Map<String, Object>> host_result = machine_task.get(30, TimeUnit.SECONDS);
                    if (!MapUtils.isEmpty(host_result)) {
                        for (Map.Entry<String, Map<String, Object>> host : host_result.entrySet()) {
                            Map<String, Object> res = host.getValue();
                            if (!MapUtils.isEmpty(res)) {
                                machineInfo.add(res);
                            }
                        }
                    }
                    logger.info("machine result size:{}", host_result.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.info("host check env cost time:{} ms", System.currentTimeMillis() - phase2);

        resultMap.put(MachineInfoEnum.MachineEnum.CONTAINER.getValue(), containerInfo);
        resultMap.put(MachineInfoEnum.MachineEnum.HOST.getValue(), machineInfo);
        return resultMap;
    }

    private class MachinetaskCallable implements Callable<Map<String, Object>> {

        private String ip;
        private String cmd;
        private SSHService sshService;
        private String type;

        public MachinetaskCallable(String ip, String cmd, SSHService sshService, String type) {
            this.ip = ip;
            this.cmd = cmd;
            this.sshService = sshService;
            this.type = type;
        }


        @Override
        public Map<String, Object> call() {

            Map<String, Object> machineResult = new HashMap<String, Object>();
            String info = null;
            try {
                info = sshService.execute(ip, cmd);
                machineResult.put("ip", ip);
                if (!StringUtil.isBlank(info)) {
                    if (type.equals(MachineInfoEnum.MachineEnum.CONTAINER.getValue())) {
                        MachineEnv containerEnv = convertContainer(info);
                        if (containerEnv != null) {
                            machineResult.put("envs", containerEnv);
                            machineResult.put("status", MachineEnv.checkContainer(containerEnv));
                        } else {
                            machineResult.put("status", CheckEnum.EXCEPTION.getValue());
                            machineResult.put("envs", MachineEnv.getDefaultEnv());
                        }
                    } else if (type.equals(MachineInfoEnum.MachineEnum.HOST.getValue())) {
                        MachineEnv hostEnv = convertHost(info);
                        if (hostEnv != null) {
                            machineResult.put("envs", hostEnv);
                            machineResult.put("status", MachineEnv.checkHost(hostEnv));
                        } else {
                            machineResult.put("status", CheckEnum.EXCEPTION.getValue());
                            machineResult.put("envs", MachineEnv.getDefaultEnv());
                        }
                    }
                } else {
                    machineResult.put("status", CheckEnum.EXCEPTION.getValue());
                    machineResult.put("envs", MachineEnv.getDefaultEnv());
                }
            } catch (SSHException e) {
                logger.error("MachinetaskCallable ip:{} error msg :{}",ip,e.getMessage());
                machineResult.put("status", CheckEnum.EXCEPTION.getValue());
                machineResult.put("envs", MachineEnv.getDefaultEnv());
            }

            return machineResult;
        }
    }


    public MachineEnv convertContainer(String cmdResult) {

        String[] envs = cmdResult.split("\n");
        String nproc = "";
        try {
            nproc = StringUtils.isBlank(envs[4]) ? "" : envs[4];
        } catch (Exception e) {
            logger.error("MachineEnv convertContainer cmdResult:{} error {}:", cmdResult, e.getMessage());
        }
        return new MachineEnv(envs[0], envs[1], envs[2], envs[3], nproc);

    }

    public MachineEnv convertHost(String cmdResult) {

        int fsync_delay_times = -1;
        int nproc_threads = -1;
        int unlimit = -1;
        int unlimit_used = -1;
        int instanceNum = -1;
        try {
            String[] envs = cmdResult.split("\n");
            fsync_delay_times = StringUtils.isBlank(envs[1]) ? -1 : Integer.parseInt(envs[1]);
            nproc_threads = StringUtils.isBlank(envs[2]) ? -1 : Integer.parseInt(envs[2]);
            unlimit = StringUtils.isBlank(envs[4]) ? -1 : Integer.parseInt(envs[4]);
            unlimit_used = StringUtils.isBlank(envs[5]) ? -1 : Integer.parseInt(envs[5]);
            instanceNum = StringUtils.isBlank(envs[7]) ? -1 : Integer.parseInt(envs[7]);
            return new MachineEnv(envs[0], fsync_delay_times, nproc_threads, envs[3], unlimit_used, unlimit, envs[6], instanceNum);
        } catch (Exception e) {
            logger.error("convertMachine error :{} {}", cmdResult, e.getMessage(), e);
            return new MachineEnv("-1", fsync_delay_times, nproc_threads, "", unlimit_used, unlimit, "", instanceNum);

        }
    }

    public String getFirstMachineIp() {
        List<MachineInfo> machines = machineDao.getAllMachines();
        if (!CollectionUtils.isEmpty(machines)) {
            return machines.get(0).getIp();
        }
        return null;
    }

    public List<MachineStats> checkMachineModule(List<MachineStats> machineStatsList) {

        if (!CollectionUtils.isEmpty(machineStatsList)) {
            for (MachineStats machineStats : machineStatsList) {

                String moduleBasePath = ConstUtils.MODULE_BASE_PATH;
                String cmd = String.format("cd %s && ls -l | grep .so", moduleBasePath);
                String cmd2 = String.format("cat /etc/redhat-release");

                try {
                    String ip = machineStats.getInfo().getIp();
                    String executeResult = sshService.execute(ip, cmd);
                    logger.info("ip :{} ,exe cmd :{},module info:{}", ip, cmd,   executeResult);

                    Map<String,Object> moduleInfo = new HashMap<String,Object>();
                    for(String moduleName : ConstUtils.MODULE_LIST){
                        if(!StringUtil.isBlank(executeResult)) {
                            moduleInfo.put(moduleName, executeResult.contains(moduleName));
                        }else{
                            moduleInfo.put(moduleName, false);
                        }
                    }

                    String version = sshService.execute(ip, cmd2);
                    machineStats.setVersionInfo(version);
                    machineStats.setModuleInfo(moduleInfo);
                } catch (SSHException e) {
                    e.printStackTrace();
                }
            }
        }
        return machineStatsList;
    }

    public boolean checkMachineMemory(String ip){

        MachineStats machineStats = machineStatsDao.getMachineStatsByIp(ip);
        float memThreshold= Float.parseFloat(machineStats.getMemoryFree())/Float.parseFloat(machineStats.getMemoryTotal());
        if (machineStats == null || memThreshold < 0.15) {
            return false;
        }
        return true;
    }
}
