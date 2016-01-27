package com.sohu.cache.machine.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.dao.MachineStatsDao;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineStats;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.machine.MachineDeployCenter;
import com.sohu.cache.machine.MachineProperty;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.schedule.SchedulerCenter;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.util.ConstUtils;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: lingguo
 * @time: 2014/8/29 17:26
 */
public class MachineDeployCenterImpl implements MachineDeployCenter {
    private Logger logger = LoggerFactory.getLogger(MachineDeployCenterImpl.class);

    private MachineDao machineDao;

    private MachineCenter machineCenter;

    private SchedulerCenter schedulerCenter;

    private MachineStatsDao machineStatsDao;

    /**
     * 将初始化脚本推送到指定的机器上
     *
     * @param ip    机器的ip
     * @return  是否推送成功，URL会抛出RunTime异常；
     */
    public boolean pushInitScript(String ip) {
        try {
            URL url = ClassLoader.getSystemResource("script/cachecloud-init.sh");
            File file = new File(url.toURI());
            if (file.exists()) {
                SSHUtil.scpFileToRemote(ip, file.getAbsolutePath(), MachineProtocol.CACHECLOUD_DIR);
            }
        } catch (URISyntaxException e) {
            logger.error("get local cachecloud-init.sh error.", e);
            return false;
        } catch (SSHException e) {
            logger.error("push cachecloud-init.sh to server: {} error.", ip, e);
            return false;
        }

        logger.info("cachecloud-init.sh is pushed to server: {}", ip);
        return true;
    }

    /**
     * 将机器加入资源池并统计、监控
     *
     * @param machineInfo
     * @return
     */
    @Override
    public boolean addMachine(MachineInfo machineInfo) {
        boolean success = true;

        if (machineInfo == null || Strings.isNullOrEmpty(machineInfo.getIp())) {
            logger.error("machineInfo is null or ip is valid.");
            return false;
        }
        // 将机器信息保存到db中
        try {
            machineDao.saveMachineInfo(machineInfo);
        } catch (Exception e) {
            logger.error("save machineInfo: {} to db error.", machineInfo.toString(), e);
            return false;
        }

        // 为机器添加统计和监控的定时任务
        try {
            MachineInfo thisMachine = machineDao.getMachineInfoByIp(machineInfo.getIp());
            if (thisMachine != null) {
                long hostId = thisMachine.getId();
                String ip = thisMachine.getIp();
                if (!machineCenter.deployMachineCollection(hostId, ip)) {
                    logger.error("deploy machine collection error, machineInfo: {}", thisMachine.toString());
                    success = false;
                }
                if (!machineCenter.deployMachineMonitor(hostId, ip)) {
                    logger.error("deploy machine monitor error, machineInfo: {}", thisMachine.toString());
                    success = false;
                }
            }
        } catch (Exception e) {
            logger.error("query machineInfo from db error, ip: {}", machineInfo.getIp(), e);
        }

        if (success) {
            logger.info("save and deploy machine ok, machineInfo: {}", machineInfo.toString());
        }
        return success;
    }

    /**
     * 批量添加机器
     *
     * @param machineInfoList
     * @return
     */
    @Override
    public boolean addAllMachines(List<MachineInfo> machineInfoList) {
        if (machineInfoList == null || machineInfoList.isEmpty()) {
            logger.error("machineInfoList is null.");
            return false;
        }

        boolean success = true;
        StringBuilder ipBuilder = new StringBuilder();
        for (MachineInfo machineInfo: machineInfoList) {
            if (!addMachine(machineInfo)) {
                success = false;
            }
            ipBuilder.append(machineInfo.getIp()).append(" ");
        }

        if (success) {
            logger.info("save and deploy machines in batch ok: ipList: {}", ipBuilder.toString());
        }
        return success;
    }

    /**
     * 删除机器，并删除相关的定时任务
     *
     * @param machineInfo
     * @return
     */
    @Override
    public boolean removeMachine(MachineInfo machineInfo) {
        if (machineInfo == null || Strings.isNullOrEmpty(machineInfo.getIp())) {
            logger.warn("machineInfo is null or ip is empty.");
            return false;
        }
        
        // 从db中删除machine和相关统计信息
        try {
            machineDao.removeMachineInfoByIp(machineInfo.getIp());
            machineStatsDao.deleteMachineStatsByIp(machineInfo.getIp());
        } catch (Exception e) {
            logger.error("remove machineInfo from db error, machineInfo: {}", machineInfo.toString(), e);
            return false;
        }
        
        /**
         * 从quartz中删除相关的定时任务，因为需要从db中查机器的id等信息，所以从db中删除机器的信息应该放在第2步；
         */
        try {
            MachineInfo thisMachine = machineDao.getMachineInfoByIp(machineInfo.getIp());
            TriggerKey collectionTriggerKey = TriggerKey.triggerKey(thisMachine.getIp(), ConstUtils.MACHINE_TRIGGER_GROUP + thisMachine.getId());
            if (!schedulerCenter.unscheduleJob(collectionTriggerKey)) {
                logger.error("remove trigger for machine error: {}", thisMachine.toString());
                return false;
            }
            TriggerKey monitorTriggerKey = TriggerKey.triggerKey(thisMachine.getIp(), ConstUtils.MACHINE_MONITOR_TRIGGER_GROUP + thisMachine.getId());
            if (!schedulerCenter.unscheduleJob(monitorTriggerKey)) {
                logger.error("remove trigger for machine monitor error: {}", thisMachine.toString());
                return false;
            }
        } catch (Exception e) {
            logger.error("query machineInfo from db error: {}", machineInfo.toString());
        }

        logger.info("remove and undeploy machine ok: {}", machineInfo.toString());
        return true;
    }

    /**
     * 批量删除机器
     *
     * @param machineInfoList
     * @return
     */
    @Override
    public boolean removeAllMachines(List<MachineInfo> machineInfoList) {
        if (machineInfoList != null || machineInfoList.isEmpty()) {
            logger.warn("machineInfoList is null.");
        }

        boolean success = true;
        StringBuilder ipBuilder = new StringBuilder();

        for (MachineInfo machineInfo: machineInfoList) {
            if (!removeMachine(machineInfo)) {
                success = false;
            }
            ipBuilder.append(machineInfo.getIp()).append(" ");
        }

        if (success) {
            logger.info("remove and undeploy machines in batch ok: {}", ipBuilder.toString());
        }
        return success;
    }

    /**
     * 为一个主从实例选择合适的机器
     *
     * @param maxMemory     实例的内存
     * @param pairHostId    主从不能在同一台机器上：如果当前实例为主，则pairId为对应从实例的hostId；
     *                      如果当前实例为从，则pairId为对应主实例的hostId；如果是第一次，则pairHostId为null；
     * @param excludeHostIds  需要排除的机器列表，会尽可能地排除；
     * @return              实例的hostId，如果没有可用的机器，返回null；
     */
    @Override
    public Long chooseBestMachine(long maxMemory, Long pairHostId, List<Long> excludeHostIds, int groupId) {
        List<MachineInfo> allMachines = machineDao.getMachinesByGroupId(groupId);    // todo
        if (allMachines == null || allMachines.size() == 0) {
            logger.warn("Warn: no machines is available., now: {}", new Date());
            return null;
        }

        Long reservedMemory = MachineProtocol.SYSTEM_RESERVED_MEMORY;
        List<MachineProperty> machinePropertyList = new ArrayList<MachineProperty>();
        for (MachineInfo minfo: allMachines) {
            if (pairHostId != null && pairHostId == minfo.getId()) {
                continue;       // 将主从对应的实例所在的机器排除
            }

            MachineStats mstat = machineStatsDao.getMachineStatsByHostId(minfo.getId());
            if (mstat == null) {
                logger.warn("warn: cannot find machineStats by HostId: {}", minfo.getId());
                continue;
            }

            // 将机器上现有的实例的内存累加
            List<InstanceStats> instanceStatsList = machineStatsDao.getInstStatOfMachine(minfo.getId());
            if (instanceStatsList != null && instanceStatsList.size() > 0) {
                reservedMemory = instanceStatsList.get(0).getMaxMemory() * 2;   // 默认预留实例最大max_memory的2被内存
            }
            MachineProperty machineProperty = new MachineProperty(mstat.getHostId(), Long.valueOf(mstat.getMemoryFree()),
                    Double.valueOf(mstat.getTraffic()), Double.valueOf(mstat.getLoad()));
            long totalUsedMemory = reservedMemory;
            for (InstanceStats istat: instanceStatsList) {
                totalUsedMemory += istat.getMaxMemory();
            }

            // 将内存满足要求的机器放入候选机器列表
            if (machineProperty.getMemory() - totalUsedMemory > maxMemory) {
                machineProperty.setMemory(machineProperty.getMemory() - totalUsedMemory);
                machinePropertyList.add(machineProperty);
            }
        }

        // 当前机器的memory都满足要求，现在根据traffic和load排序，并选择最优的机器
        Ordering<MachineProperty> comparator = Ordering.from(new MachineProperty());
        List<MachineProperty> sortedMachines = comparator.sortedCopy(machinePropertyList);
        MachineProperty bestMachine = sortedMachines.get(0);

        // 如果将排除列表中的机器排除后，有机器可选，则选择最优的一台，否则使用排除前的最优机器；
        List<Long> hostIdList = new LinkedList<Long>();
        for (MachineProperty machineProperty: sortedMachines) {
            hostIdList.add(machineProperty.getHostId());
        }

        if (excludeHostIds != null && excludeHostIds.size() > 0) {
            for (int i = 0; i < hostIdList.size(); i++) {
                if (excludeHostIds.contains(hostIdList.get(i))) {
                    sortedMachines.remove(i);
                }
            }
            if (sortedMachines.size() > 0) {
                bestMachine = sortedMachines.get(0);
            }
        }

        return bestMachine.getHostId();
    }

    public void setMachineDao(MachineDao machineDao) {
        this.machineDao = machineDao;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public void setSchedulerCenter(SchedulerCenter schedulerCenter) {
        this.schedulerCenter = schedulerCenter;
    }

    public void setMachineStatsDao(MachineStatsDao machineStatsDao) {
        this.machineStatsDao = machineStatsDao;
    }

}
