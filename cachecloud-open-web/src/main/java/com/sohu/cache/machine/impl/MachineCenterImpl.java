package com.sohu.cache.machine.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import redis.clients.jedis.HostAndPort;

import com.google.common.base.Strings;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.constant.MachineConstant;
import com.sohu.cache.constant.MachineInfoEnum.TypeEnum;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceStatsDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.dao.MachineStatsDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineMemInfo;
import com.sohu.cache.entity.MachineStats;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.machine.PortGenerator;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.schedule.SchedulerCenter;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.IdempotentConfirmer;
import com.sohu.cache.util.ObjectConvert;
import com.sohu.cache.util.ScheduleUtil;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.component.EmailComponent;
import com.sohu.cache.web.component.MobileAlertComponent;

/**
 * 机器接口的实现
 * User: lingguo
 * Date: 14-6-12
 * Time: 上午10:46
 */
public class MachineCenterImpl implements MachineCenter {
    private final Logger logger = LoggerFactory.getLogger(MachineCenterImpl.class);

    private SchedulerCenter schedulerCenter;

    private InstanceStatsCenter instanceStatsCenter;

    private MachineStatsDao machineStatsDao;

    private InstanceDao instanceDao;

    private InstanceStatsDao instanceStatsDao;

    private MachineDao machineDao;

    private RedisCenter redisCenter;

    
    /**
     * 邮箱报警
     */
    private EmailComponent emailComponent;

    /**
     * 手机短信报警
     */
    private MobileAlertComponent mobileAlertComponent;
    
	private AsyncService asyncService;
	
	public void init() {
		asyncService.assemblePool(AsyncThreadPoolFactory.MACHINE_POOL, 
				AsyncThreadPoolFactory.MACHINE_THREAD_POOL);
	}

    /**
     * 为当前机器收集信息创建trigger并部署
     *
     * @param hostId 机器id
     * @param ip     ip
     * @return 部署成功返回true，否则返回false
     */
    @Override
    public boolean deployMachineCollection(final long hostId, final String ip) {
        Assert.isTrue(hostId > 0);
        Assert.hasText(ip);

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put(ConstUtils.HOST_KEY, ip);
        dataMap.put(ConstUtils.HOST_ID_KEY, hostId);
        JobKey jobKey = JobKey.jobKey(ConstUtils.MACHINE_JOB_NAME, ConstUtils.MACHINE_JOB_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(ip, ConstUtils.MACHINE_TRIGGER_GROUP + hostId);
        boolean result = schedulerCenter.deployJobByCron(jobKey, triggerKey, dataMap, ScheduleUtil.getMachineStatsCron(hostId), false);

        return result;
    }
    
    @Override
    public boolean unDeployMachineCollection(long hostId, String ip) {
        Assert.isTrue(hostId > 0);
        Assert.hasText(ip);
        TriggerKey collectionTriggerKey = TriggerKey.triggerKey(ip, ConstUtils.MACHINE_TRIGGER_GROUP + hostId);
        Trigger trigger = schedulerCenter.getTrigger(collectionTriggerKey);
        if (trigger == null) {
            return true;
        }
        return schedulerCenter.unscheduleJob(collectionTriggerKey);
    }
    
    //异步执行任务
    public void asyncCollectMachineInfo(final long hostId, final long collectTime, final String ip) {
    	String key = "collect-machine-"+hostId+"-"+ip+"-"+collectTime;
		asyncService.submitFuture(AsyncThreadPoolFactory.MACHINE_POOL, new KeyCallable<Boolean>(key) {
            public Boolean execute() {
                try {
                	collectMachineInfo(hostId, collectTime, ip);
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
            machineStats = SSHUtil.getMachineInfo(ip, sshPort, ConstUtils.USERNAME, ConstUtils.PASSWORD);
            machineStats.setHostId(hostId);
            if (machineStats != null) {
                infoMap.put(MachineConstant.Ip.getValue(), machineStats.getIp());
                infoMap.put(MachineConstant.CpuUsage.getValue(), machineStats.getCpuUsage());
                infoMap.put(MachineConstant.MemoryUsageRatio.getValue(), machineStats.getMemoryUsageRatio());
                /**
                 * SSHUtil返回的内存单位为k，由于实例的内存基本存储单位都是byte，所以统一为byte
                 */
                if (machineStats.getMemoryFree() != null) {
                    infoMap.put(MachineConstant.MemoryFree.getValue(), Long.valueOf(machineStats.getMemoryFree()) * ConstUtils._1024);
                } else {
                    infoMap.put(MachineConstant.MemoryFree.getValue(), 0);
                }
                infoMap.put(MachineConstant.MemoryTotal.getValue(), Long.valueOf(machineStats.getMemoryTotal()) * ConstUtils._1024);
                infoMap.put(MachineConstant.Load.getValue(), machineStats.getLoad());
                infoMap.put(MachineConstant.Traffic.getValue(), machineStats.getTraffic());
                infoMap.put(MachineConstant.DiskUsage.getValue(), machineStats.getDiskUsageMap());
                infoMap.put(ConstUtils.COLLECT_TIME, collectTime);
                instanceStatsCenter.saveStandardStats(infoMap, ip, (int) hostId, ConstUtils.MACHINE);
                machineStats.setMemoryFree(Long.valueOf(machineStats.getMemoryFree()) * ConstUtils._1024 + "");
                machineStats.setMemoryTotal(Long.valueOf(machineStats.getMemoryTotal()) * ConstUtils._1024 + "");
                machineStats.setModifyTime(new Date());
                machineStatsDao.mergeMachineStats(machineStats);
                logger.info("collect machine info done, host: {}, time: {}", ip, collectTime);
            }
        } catch (Exception e) {
            logger.error("collectMachineErrorStats=>" + machineStats);
            logger.error(e.getMessage(), e);
        }
        return infoMap;
    }

    /**
     * 为监控每台机器的状态部署trigger
     *
     * @param hostId 机器id
     * @param ip     ip
     * @return 是否部署成功
     */
    @Override
    public boolean deployMachineMonitor(final long hostId, final String ip) {
        Assert.isTrue(hostId > 0);
        Assert.hasText(ip);

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put(ConstUtils.HOST_KEY, ip);
        dataMap.put(ConstUtils.HOST_ID_KEY, hostId);

        JobKey jobKey = JobKey.jobKey(ConstUtils.MACHINE_MONITOR_JOB_NAME, ConstUtils.MACHINE_MONITOR_JOB_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(ip, ConstUtils.MACHINE_MONITOR_TRIGGER_GROUP + hostId);
        boolean result = schedulerCenter.deployJobByCron(jobKey, triggerKey, dataMap, ScheduleUtil.getHourCronByHostId
                (hostId), false);

        return result;
    }
    
    @Override
    public boolean unDeployMachineMonitor(long hostId, String ip) {
        Assert.isTrue(hostId > 0);
        Assert.hasText(ip);
        TriggerKey monitorTriggerKey = TriggerKey.triggerKey(ip, ConstUtils.MACHINE_MONITOR_TRIGGER_GROUP + hostId);
        Trigger trigger = schedulerCenter.getTrigger(monitorTriggerKey);
        if (trigger == null) {
            return true;
        }
        return schedulerCenter.unscheduleJob(monitorTriggerKey);
    }
    
    //异步执行任务
    public void asyncMonitorMachineStats(final long hostId, final String ip) {
    	String key = "monitor-machine-"+hostId+"-"+ip;
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
        double memoryUsage = ObjectConvert.percentToDouble(machineStats.getMemoryUsageRatio(), 0);
        double load = 0;
        try {
            load = Double.valueOf(machineStats.getLoad());
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
        }
        

        double memoryThreshold = ConstUtils.MEMORY_USAGE_RATIO_THRESHOLD;

        /**
         * 当机器的状态超过预设的阀值时，向上汇报或者报警
         */
        StringBuilder alertContent = new StringBuilder();
        // cpu使用率 todo
        if (cpuUsage > ConstUtils.CPU_USAGE_RATIO_THRESHOLD) {
            logger.warn("cpuUsageRatio is above security line. ip: {}, cpuUsage: {}%", ip, cpuUsage);
            alertContent.append("ip:").append(ip).append(",cpuUse:").append(cpuUsage);
        }

        // 内存使用率 todo
        if (memoryUsage > memoryThreshold) {
            logger.warn("memoryUsageRatio is above security line, ip: {}, memoryUsage: {}%", ip, memoryUsage);
            alertContent.append("ip:").append(ip).append(",memUse:").append(memoryUsage);
        }

        // 负载 todo
        if (load > ConstUtils.LOAD_THRESHOLD) {
            logger.warn("load is above security line, ip: {}, load: {}%", ip, load);
            alertContent.append("ip:").append(ip).append(",load:").append(load);
        }
        
        // 报警
        if (StringUtils.isNotBlank(alertContent.toString())) {
            String title = "cachecloud机器异常:";
            emailComponent.sendMailToAdmin(title, alertContent.toString());
            mobileAlertComponent.sendPhoneToAdmin(title + alertContent.toString());
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
                logger.error("cannot create /tmp/cachecloud directory.");
                return null;
            }
        }

        Path path = Paths.get(MachineProtocol.TMP_DIR + fileName);
        String remotePath = MachineProtocol.CONF_DIR + fileName;
        /**
         * 将配置文件的内容写到本地
         */
        try {
            BufferedWriter bufferedWriter = Files.newBufferedWriter(path, Charset.forName(MachineProtocol.ENCODING_UTF8));
            try {
                for (String line : content) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            } finally {
                if(bufferedWriter != null)
                    bufferedWriter.close();
            }
        } catch (IOException e) {
            logger.error("write redis config file error, ip: {}, filename: {}, content: {}, e", host, fileName, content, e);
            return null;
        }finally {

        }

        /**
         * 将配置文件推送到目标机器上
         */
        try {
            SSHUtil.scpFileToRemote(host, localAbsolutePath, MachineProtocol.CONF_DIR);
        } catch (SSHException e) {
            logger.error("scp config file to remote server error: ip: {}, fileName: {}", host, fileName, e);
            return null;
        }

        /**
         * 删除临时文件
         */
        File file = new File(localAbsolutePath);
        if (file.exists()) {
            file.delete();
        }

        return remotePath;
    }

    @Override
    public List<MachineStats> getMachineStats(String ipLike) {
        List<MachineInfo> machineInfoList = machineDao.getMachineInfoByLikeIp(ipLike);
        List<MachineStats> machineStatsList = new ArrayList<MachineStats>();
        for (MachineInfo machineInfo : machineInfoList) {
            String ip = machineInfo.getIp();
            MachineStats machineStats = machineStatsDao.getMachineStatsByIp(ip);
            if (machineStats == null) {
                machineStats = new MachineStats();
            }
            machineStats.setMemoryAllocated(instanceDao.getMemoryByHost(ip));
            machineStats.setInfo(machineInfo);
            machineStatsList.add(machineStats);
        }
        return machineStatsList;
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
        machineStats.setInfo(machineDao.getMachineInfoByIp(ip));
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
        if (resultList == null || resultList.isEmpty()) {
            return resultList;
        }
        if (resultList != null && resultList.size() > 0) {
            for (InstanceInfo instanceInfo : resultList) {
                int type = instanceInfo.getType();
                if(instanceInfo.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()){
                    continue;
                }
                if (TypeUtil.isRedisType(type)) {
                    if (TypeUtil.isRedisSentinel(type)) {
                        continue;
                    }
                    String host = instanceInfo.getIp();
                    int port = instanceInfo.getPort();
                    Boolean isMaster = redisCenter.isMaster(host, port);
                    instanceInfo.setRoleDesc(isMaster);
                    if(isMaster != null && !isMaster){
                        HostAndPort hap = redisCenter.getMaster(host, port);
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

        String remoteFilePath = MachineProtocol.LOG_DIR + logType + port + "-*.log";
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

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setSchedulerCenter(SchedulerCenter schedulerCenter) {
        this.schedulerCenter = schedulerCenter;
    }

    public void setMachineStatsDao(MachineStatsDao machineStatsDao) {
        this.machineStatsDao = machineStatsDao;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setMachineDao(MachineDao machineDao) {
        this.machineDao = machineDao;
    }

    public void setInstanceStatsDao(InstanceStatsDao instanceStatsDao) {
        this.instanceStatsDao = instanceStatsDao;
    }
    
    public void setEmailComponent(EmailComponent emailComponent) {
        this.emailComponent = emailComponent;
    }

    public void setMobileAlertComponent(MobileAlertComponent mobileAlertComponent) {
        this.mobileAlertComponent = mobileAlertComponent;
    }

    public void setInstanceStatsCenter(InstanceStatsCenter instanceStatsCenter) {
        this.instanceStatsCenter = instanceStatsCenter;
    }

	@Override
	public boolean deployServerCollection(long hostId, String ip) {
        Assert.hasText(ip);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put(ConstUtils.HOST_KEY, ip);
        JobKey jobKey = JobKey.jobKey(ConstUtils.SERVER_JOB_NAME, ConstUtils.SERVER_JOB_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(ip, ConstUtils.SERVER_TRIGGER_GROUP + ip);
        boolean result = schedulerCenter.deployJobByCron(jobKey, triggerKey, dataMap, ScheduleUtil.getFiveMinuteCronByHostId(hostId), false);

        return result;
	}

	@Override
	public boolean unDeployServerCollection(long hostId, String ip) {
        Assert.hasText(ip);
        TriggerKey collectionTriggerKey = TriggerKey.triggerKey(ip, ConstUtils.SERVER_TRIGGER_GROUP + ip);
        Trigger trigger = schedulerCenter.getTrigger(collectionTriggerKey);
        if (trigger == null) {
            return true;
        }
        return schedulerCenter.unscheduleJob(collectionTriggerKey);
	}
	
	@Override
    public Map<String, Integer> getMachineInstanceCountMap() {
	    List<Map<String,Object>> mapList = instanceDao.getMachineInstanceCountMap();
	    if (CollectionUtils.isEmpty(mapList)) {
	        return Collections.emptyMap();
	    }
	    
	    Map<String, Integer> resultMap = new HashMap<String, Integer>();
	    for(Map<String,Object> map : mapList) {
	        String ip = MapUtils.getString(map, "ip", "");
	        if (StringUtils.isBlank(ip)) {
	            continue;
	        }
	        int count = MapUtils.getIntValue(map, "count");
	        resultMap.put(ip, count);
	    }
        return resultMap;
    }

	public void setAsyncService(AsyncService asyncService) {
		this.asyncService = asyncService;
	}

    
    
}
