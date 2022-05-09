package com.sohu.cache.web.service.impl;

import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.constant.*;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.util.AppKeyUtil;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.DeployInfoEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.enums.UseTypeEnum;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.vo.AppDetailVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Module;
import redis.clients.jedis.exceptions.JedisDataException;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用操作实现类
 *
 * @author leifu
 * @Time 2014年10月21日
 */
@Service("appService")
public class AppServiceImpl implements AppService {

    private Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);
    /**
     * 应用相关dao
     */
    @Autowired
    private AppDao appDao;
    /**
     * 应用日志相关dao
     */
    @Autowired
    private AppAuditLogDao appAuditLogDao;
    /**
     * 实例相关dao
     */
    @Autowired
    private InstanceDao instanceDao;

    /**
     * 应用用户关系相关dao
     */
    @Autowired
    private AppToUserDao appToUserDao;

    /**
     * 应用申请相关dao
     */
    @Autowired
    private AppAuditDao appAuditDao;
    /**
     * 用户信息dao
     */
    @Autowired
    private AppUserDao appUserDao;
    /**
     * 应用统计dao
     */
    @Autowired
    private AppStatsDao appStatsDao;
    @Autowired
    private InstanceStatsDao instanceStatsDao;
    @Autowired
    @Lazy
    private RedisCenter redisCenter;
    @Autowired
    @Lazy
    private MachineCenter machineCenter;
    @Autowired
    private MachineDao machineDao;
    @Autowired
    private MachineStatsDao machineStatsDao;
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private AppService appService;
    @Autowired
    private AppClientStatisticGatherDao appClientStatisticGatherDao;
    @Autowired
    private AppStatsCenter appStatsCenter;

    @PostConstruct
    public void init() {
        asyncService.assemblePool(AsyncThreadPoolFactory.APP_POOL, AsyncThreadPoolFactory.APP_THREAD_POOL);
    }

    @Override
    public int getAppDescCount(AppUser appUser, AppSearch appSearch) {
        int count = 0;
        // 管理员获取全部应用
        if (AppUserTypeEnum.ADMIN_USER.value().equals(appUser.getType())) {
            count = appDao.getAllAppCount(appSearch);
        } else {
            count = appDao.getUserAppCount(appUser.getId());
        }
        return count;
    }

    @Override
    public List<InstanceInfo> getAppMasterInstanceInfoList(long appId) {
        List<InstanceInfo> resultList = new ArrayList<InstanceInfo>();
        List<InstanceInfo> appInstanceInfoList = getAppInstanceInfo(appId);
        for (InstanceInfo instanceInfo : appInstanceInfoList) {
            if ((instanceInfo.isRedisData() || instanceInfo.isPika()) && "master".equals(instanceInfo.getRoleDesc())
                    && instanceInfo.getMasterInstanceId() == 0) {
                resultList.add(instanceInfo);
            }
        }
        return resultList;
    }

    @Override
    public List<AppDesc> getAppDescList(AppUser appUser, AppSearch appSearch) {
        List<AppDesc> list;
        // 管理员获取全部应用
        if (AppUserTypeEnum.ADMIN_USER.value().equals(appUser.getType())) {
            list = appDao.getAllAppDescList(appSearch);
        } else {
            list = appDao.getAppDescList(appUser.getId());
        }
        return list;
    }

    @Override
    public AppDesc getByAppId(Long appId) {
        AppDesc appDesc = null;
        try {
            appDesc = appDao.getAppDescById(appId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return appDesc;
    }

    @Override
    public int save(AppDesc appDesc) {
        return appDao.save(appDesc);
    }

    @Override
    public int update(AppDesc appDesc) {
        return appDao.update(appDesc);
    }

    @Override
    public boolean saveAppToUser(Long appId, Long userId) {
        try {
            // 用户id下应用
            List<AppToUser> list = appToUserDao.getByUserId(userId);
            if (CollectionUtils.isNotEmpty(list)) {
                for (AppToUser appToUser : list) {
                    if (appToUser.getAppId().equals(appId)) {
                        return true;
                    }
                }
            }
            appToUserDao.save(new AppToUser(userId, appId));
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void updateAppAuditStatus(Long id, Long appId, Integer status, AppUser appUser) {
        appAuditDao.updateAppAuditUser(id, status, appUser.getId());
        AppDesc appDesc = appDao.getAppDescById(appId);
        AppAudit appAudit = appAuditDao.getAppAudit(id);

        // 只有应用创建才会设置状态
        if (AppAuditType.APP_AUDIT.getValue() == appAudit.getType()) {
            if (AppCheckEnum.APP_PASS.value().equals(status)) {
                appDesc.setStatus(AppStatusEnum.STATUS_PUBLISHED.getStatus());
                appDesc.setPassedTime(new Date());
                appDao.update(appDesc);
            } else if (AppCheckEnum.APP_REJECT.value().equals(status)) {
                appDesc.setStatus(AppStatusEnum.STATUS_DENY.getStatus());
                appDao.update(appDesc);
            }
        }

        // 保存审批日志
        AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(),
                AppAuditLogTypeEnum.APP_CHECK);
        if (appAuditLog != null) {
            appAuditLogDao.save(appAuditLog);
        }
    }

    @Override
    public void updateUserAuditStatus(Long id, Integer status, Long operateId) {
        appAuditDao.updateAppAuditUser(id, status, operateId);
    }

    @Override
    public List<AppToUser> getAppToUserList(Long appId) {
        return appToUserDao.getByAppId(appId);
    }

    @Override
    public AppDesc getAppByName(String appName) {
        return appDao.getByAppName(appName);
    }

    /**
     * 获取app 基本实例信息
     * @param appId
     * @return
     */
    @Override
    public List<InstanceInfo> getAppBasicInstanceInfo(Long appId) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if(appDesc == null){
            return new ArrayList<>();
        }
        return instanceDao.getInstListByAppId(appId);
    }

    @Override
    public List<InstanceInfo> getAppInstanceInfo(Long appId) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        String password = appDesc.getPasswordMd5();
        List<InstanceInfo> resultList = instanceDao.getInstListByAppId(appId);
        return getInstancelistInfo(appId, password, resultList);
    }

    @Override
    public List<InstanceInfo> getAppOnlineInstanceInfo(Long appId) {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if(appDesc == null){
            return Collections.EMPTY_LIST;
        }
        String password = appDesc.getPasswordMd5();
        List<InstanceInfo> resultList = instanceDao.getEffectiveInstListByAppId(appId);
        return getInstancelistInfo(appId, password, resultList);
    }

    public List<InstanceInfo> getInstancelistInfo(Long appId, String password, List<InstanceInfo> resultList) {
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
                    // 幂等操作
                    BooleanEnum isMaster = redisCenter.isMaster(appId, host, port);
                    instanceInfo.setRoleDesc(isMaster);
                    if (BooleanEnum.FALSE == isMaster) {
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
                    // 设置模块信息
                    Jedis jedis = null;
                    try {
                        if (type == ConstUtils.CACHE_REDIS_STANDALONE || type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                            jedis = redisCenter.getJedis(appId, host, port);
                            List<Module> modules = jedis.moduleList();
                            instanceInfo.setModules(modules);
                        }
                    }catch (JedisDataException e){
                        if("ERR unknown command 'MODULE'".equals(e.getMessage())){
                            logger.info("checkInstanceModule {}:{} error , message:{}", host, port, e.getMessage());
                        }else {
                            logger.error("checkInstanceModule {}:{} error , message:{}", host, port, e.getMessage(), e);
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
        return resultList;
    }

    @Override
    public List<InstanceStats> getAppInstanceStats(Long appId) {
        List<InstanceStats> instanceStats = instanceStatsDao.getInstanceStatsByAppId(appId);
        return instanceStats;
    }

    @Override
    public SuccessEnum deleteAppToUser(Long appId, Long userId) {
        try {
            appToUserDao.deleteAppToUser(appId, userId);
            return SuccessEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
    }

    @Override
    public List<AppAudit> getAppAudits(Integer status, Integer type, Long auditId, Long userId, Long operateId) {
        List<AppAudit> list = appAuditDao.selectWaitAppAudits(status, type, auditId, userId, operateId);
        for (Iterator<AppAudit> i = list.iterator(); i.hasNext(); ) {
            AppAudit appAudit = i.next();
            AppDesc appDesc = appDao.getAppDescById(appAudit.getAppId());
            appAudit.setAppDesc(appDesc);
        }
        return list;
    }

    @Override
    public Map<String, Object> getStatisticGroupByStatus(Long userId, Long operateId, Date startTime, Date endTime) {
        List<Map<String, Object>> statis = appAuditDao.getStatisticGroupByStatus(userId, operateId, startTime, endTime);
        return statis.stream().collect(Collectors.toMap(stat -> MapUtils.getString(stat, "status"), stat -> MapUtils.getInteger(stat, "count")));
    }

    @Override
    public Map<String, Object> getStatisticGroupByType(Long userId, Long operateId, Date startTime, Date endTime) {
        List<Map<String, Object>> statis = appAuditDao.getStatisticGroupByType(userId, operateId, startTime, endTime);
        return statis.stream().collect(Collectors.toMap(stat -> MapUtils.getString(stat, "type"), stat -> MapUtils.getInteger(stat, "count")));
    }

    public List<AppAudit> getAppAudits(Long appid, Integer type) {
        return appAuditDao.getAppAuditByCondition(appid, type);
    }

    @Override
    public AppAudit saveAppScaleApply(AppDesc appDesc, AppUser appUser, String applyMemSize, String appScaleReason,
                                      AppAuditType appScale) {
        AppAudit appAudit = new AppAudit();
        appAudit.setAppId(appDesc.getAppId());
        appAudit.setUserId(appUser.getId());
        appAudit.setUserName(appUser.getName());
        appAudit.setModifyTime(new Date());
        appAudit.setParam1(applyMemSize);
        appAudit.setParam2(appScaleReason);
        appAudit.setInfo("扩容申请---申请容量:" + applyMemSize + ", 申请原因: " + appScaleReason);
        appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
        appAudit.setType(appScale.getValue());
        appAuditDao.insertAppAudit(appAudit);

        //保存扩容申请
        AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(),
                AppAuditLogTypeEnum.APP_SCALE_APPLY);
        if (appAuditLog != null) {
            appAuditLogDao.save(appAuditLog);
        }

        return appAudit;
    }

    @Override
    public AppAudit saveAppChangeConfig(AppDesc appDesc, AppUser appUser, Long instanceId, String appConfigKey,
                                        String appConfigValue, String appConfigReason, AppAuditType modifyConfig) {
        AppAudit appAudit = new AppAudit();
        appAudit.setAppId(appDesc.getAppId());
        appAudit.setUserId(appUser.getId());
        appAudit.setUserName(appUser.getName());
        appAudit.setModifyTime(new Date());
        appAudit.setParam1(String.valueOf(instanceId));
        appAudit.setParam2(appConfigKey);
        appAudit.setParam3(appConfigValue);
        appAudit.setInfo("修改配置项:" + appConfigKey + ", 配置值: " + appConfigValue + ", 修改原因: " + appConfigReason);
        appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
        appAudit.setType(modifyConfig.getValue());
        appAuditDao.insertAppAudit(appAudit);

        //保存日志
        AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(),
                AppAuditLogTypeEnum.APP_CONFIG_APPLY);
        if (appAuditLog != null) {
            appAuditLogDao.save(appAuditLog);
        }

        return appAudit;

    }

    @Override
    public AppAudit saveInstanceChangeConfig(AppDesc appDesc, AppUser appUser, Long instanceId,
                                             String instanceConfigKey, String instanceConfigValue, String instanceConfigReason,
                                             AppAuditType instanceModifyConfig) {
        AppAudit appAudit = new AppAudit();
        long appId = appDesc.getAppId();
        appAudit.setAppId(appId);
        appAudit.setUserId(appUser.getId());
        appAudit.setUserName(appUser.getName());
        appAudit.setModifyTime(new Date());
        appAudit.setParam1(String.valueOf(instanceId));
        appAudit.setParam2(instanceConfigKey);
        appAudit.setParam3(instanceConfigValue);
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        String hostPort = instanceInfo == null ? "" : (instanceInfo.getIp() + ":" + instanceInfo.getPort());
        appAudit.setInfo(
                "appId=" + appId + "下的" + hostPort + "实例申请修改配置项:" + instanceConfigKey + ", 配置值: " + instanceConfigValue
                        + ", 修改原因: " + instanceConfigReason);
        appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
        appAudit.setType(instanceModifyConfig.getValue());
        appAuditDao.insertAppAudit(appAudit);

        //保存日志
        AppAuditLog appAuditLog = AppAuditLog
                .generate(appDesc, appUser, appAudit.getId(), AppAuditLogTypeEnum.INSTANCE_CONFIG_APPLY);
        if (appAuditLog != null) {
            appAuditLogDao.save(appAuditLog);
        }

        return appAudit;
    }

    @Override
    public SuccessEnum updateRefuseReason(AppAudit appAudit, AppUser userInfo) {
        try {
            appAuditDao.updateRefuseReason(appAudit.getId(), appAudit.getRefuseReason());
            return SuccessEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
    }

    @Override
    public int getUserAppCount(Long userId) {
        int count = 0;
        try {
            // 表比较小
            List<AppToUser> list = appToUserDao.getByUserId(userId);
            if (CollectionUtils.isNotEmpty(list)) {
                count = list.size();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return count;
    }

    @Override
    public List<MachineStats> getAppMachineDetail(Long appId) {
        //应用信息
        Assert.isTrue(appId != null && appId > 0L);
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            logger.error("appDesc:id={} is not exist");
            return Collections.emptyList();
        }

        //应用实例列表
        List<InstanceInfo> appInstanceList = getAppInstanceInfo(appId);
        if (CollectionUtils.isEmpty(appInstanceList)) {
            return Collections.emptyList();
        }

        //防止重复
        Set<String> instanceMachineHosts = new HashSet<String>();
        //结果列表
        List<MachineStats> machineDetailVOList = new ArrayList<MachineStats>();
        //应用的机器信息
        for (InstanceInfo instanceInfo : appInstanceList) {
            String ip = instanceInfo.getIp();
            if (instanceMachineHosts.contains(ip)) {
                continue;
            } else {
                instanceMachineHosts.add(ip);
            }
            MachineStats machineStats = machineStatsDao.getMachineStatsByIp(ip);
            if (machineStats == null) {
                continue;
            }
            //已经分配的内存
            int memoryHost = instanceDao.getMemoryByHost(ip);
            machineStats.setMemoryAllocated(memoryHost);
            //机器信息
            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(ip);
            if (machineInfo == null) {
                continue;
            }
            //下线机器不展示
            if (machineInfo.isOffline()) {
                continue;
            }
            machineStats.setInfo(machineInfo);
            machineDetailVOList.add(machineStats);
        }
        return machineDetailVOList;
    }

    @Override
    public List<MachineStats> getAppMachine(Long appId) {
        //应用信息
        Assert.isTrue(appId != null && appId > 0L);
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            logger.error("appDesc:id={} is not exist");
            return Collections.emptyList();
        }

        //应用实例列表
        List<InstanceInfo> appInstanceList = getAppInstanceInfo(appId);
        if (CollectionUtils.isEmpty(appInstanceList)) {
            return Collections.emptyList();
        }

        //防止重复
        Set<String> instanceMachineHosts = new HashSet<String>();
        //结果列表
        List<MachineStats> machineDetailVOList = new ArrayList<MachineStats>();
        //应用的机器信息
        for (InstanceInfo instanceInfo : appInstanceList) {
            if(!instanceInfo.isOffline()){
                String ip = instanceInfo.getIp();
                if (instanceMachineHosts.contains(ip)) {
                    continue;
                } else {
                    instanceMachineHosts.add(ip);
                }
                MachineStats machineStats = machineStatsDao.getMachineStatsByIp(ip);
                if (machineStats == null) {
                    continue;
                }
                //已经分配的内存
                int memoryHost = instanceDao.getMemoryByHost(ip);
                machineStats.setMemoryAllocated(memoryHost);
                //机器信息
                MachineInfo machineInfo = machineCenter.getMachineInfoByIp(ip);
                if (machineInfo == null) {
                    continue;
                }
                //下线机器不展示
                if (machineInfo.isOffline()) {
                    continue;
                }
                machineStats.setInfo(machineInfo);
                machineDetailVOList.add(machineStats);
            }
        }
        return machineDetailVOList;
    }


    @Override
    public AppAudit getAppAuditById(Long appAuditId) {
        return appAuditDao.getAppAudit(appAuditId);
    }

    @Override
    public List<AppAudit> getAppAuditListByAppId(Long appId) {
        Assert.isTrue(appId != null && appId > 0L);
        List<AppAudit> appAudits = appAuditDao.getAppAuditByAppId(appId);
        if (CollectionUtils.isNotEmpty(appAudits)) {
            for (AppAudit appAudit : appAudits) {
                Long appAuditId = appAudit.getId();
                AppAuditLog log = appAuditLogDao.getAuditByType(appAuditId, AppAuditLogTypeEnum.APP_CHECK.value());
                if (log != null) {
                    log.setAppUser(appUserDao.get(log.getUserId()));
                }
                appAudit.setAppAuditLog(log);
            }
        }
        return appAudits;
    }

    @Override
    public AppAudit saveRegisterUserApply(AppUser appUser, AppAuditType registerUserApply) {
        AppAudit appAudit = new AppAudit();
        appAudit.setAppId(0);
        appAudit.setUserId(appUser.getId());
        appAudit.setUserName(appUser.getName());
        appAudit.setModifyTime(new Date());
        StringBuilder info = new StringBuilder();
        info.append(appUser.getChName() + "申请成为Cachecloud用户, 手机:" + appUser.getMobile() + ",邮箱:" + appUser.getEmail()
                + ",微信:" + appUser.getWeChat());
        if(!StringUtils.isEmpty(appUser.getCompany())){
            info.append(",公司:" + appUser.getCompany());
        }
        if(!StringUtils.isEmpty(appUser.getPurpose())){
            info.append(",使用目的:" + appUser.getPurpose());
        }
        appAudit.setInfo(info.toString());
        appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
        appAudit.setType(registerUserApply.getValue());
        appAuditDao.insertAppAudit(appAudit);
        return appAudit;
    }

    @Override
    public List<AppDesc> getAllAppDesc() {
        return appDao.getAllAppDescList(null);
    }

    @Override
    public SuccessEnum changeAppAlertConfig(long appId, int memAlertValue, int clientConnAlertValue,
                                            int hitPrecentAlertConfig, int isAccessMonitor, AppUser appUser) {
        if (appId <= 0 || memAlertValue <= 0 || clientConnAlertValue <= 0) {
            return SuccessEnum.FAIL;
        }
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            return SuccessEnum.FAIL;
        }
        try {
            // 修改报警阀值
            appDesc.setMemAlertValue(memAlertValue);
            appDesc.setClientConnAlertValue(clientConnAlertValue);
            appDesc.setHitPrecentAlertValue(hitPrecentAlertConfig);
            appDesc.setIsAccessMonitor(isAccessMonitor);
            appDao.update(appDesc);
            // 添加日志
            AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, 0L, AppAuditLogTypeEnum.APP_CHANGE_ALERT);
            if (appAuditLog != null) {
                appAuditLogDao.save(appAuditLog);
            }
            return SuccessEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SuccessEnum.FAIL;
        }
    }

    @Override
    public void updateAppKey(long appId) {
        appDao.updateAppKey(appId, AppKeyUtil.genSecretKey(appId));
    }

    @Override
    public String generateDeployInfo(Integer type,
                                     Integer isSalve,
                                     String room,
                                     Double size,
                                     Integer machineNum,
                                     Integer instanceNum,
                                     Integer useType,
                                     String machines,
                                     String excludeMachines,
                                     String sentinelMachines,
                                     List<DeployInfo> deployInfoList,
                                     List<MachineMemStatInfo> resMachines) {
        String result = DeployInfoEnum.SUCCESS.getValue();
        // 获取有效可用机器列表
        List<String> excludeMachineList = Arrays.asList(excludeMachines.split(","));
        List<String> sentinelMachineLIst = Arrays.asList(sentinelMachines.split(","));
        //存储满足条件的机器ip
        List<MachineMemStatInfo> machineCandi = new ArrayList<MachineMemStatInfo>();
        //存储real machines ip
        //Set<String> realMachines = new HashSet<String>();
        //每个机器的需求size，向上取整
        Double reqSize = Math.ceil(size / instanceNum) * Math.ceil(instanceNum * 1.0D / machineNum);
        Integer reqCpu = instanceNum % machineNum == 0 ? instanceNum / machineNum : instanceNum / machineNum + 1;

        if (useType == UseTypeEnum.Machine_special.getValue()) {
            if (machines != null && !machines.isEmpty()) {
                List<String> ipList = Arrays.asList(machines.split(","));
                if (ipList.size() < machineNum) {
                    logger.info("指定机器数小于分配机器数");
                    result = "指定机器数小于分配机器数";
                    return result;
                }
                List<MachineMemStatInfo> tmp = machineCenter.getValidMachineMemByIpList(ipList);
                for (MachineMemStatInfo memStatInfo : tmp) {
                    resMachines.add(memStatInfo);
                }
            } else {
                logger.info("部署类型为专用，指定机器为空");
                result = "machine list is null";
                return result;
            }
        } else {
            List<MachineMemStatInfo> machineMemStatInfoList = machineCenter
                    .getAllValidMachineMem(excludeMachineList, room, useType);
            for (MachineMemStatInfo memStatInfo : machineMemStatInfoList) {
                getMachineCandiList(memStatInfo, reqSize, reqCpu, isSalve, machineCandi);
            }
            getResMachines(machineCandi, machineNum, resMachines);
            if ((resMachines == null || resMachines.size() == 0) && useType == UseTypeEnum.Machine_test.getValue()) {
                getResMachines(machineMemStatInfoList, machineNum, resMachines);
            }
        }

        if (resMachines == null || resMachines.size() == 0) {
            logger.info("无可用机器");
            result = "可用机器数小于指定机器数";
            return result;
        }

        getDeployInfo(type, isSalve, resMachines, size, instanceNum, sentinelMachineLIst, deployInfoList);
        logger.info("deployInfoList: {}", deployInfoList);

        return result;
    }

    /**
     * @Description: 通过机器列表生成部署详情
     * @Author: caoru
     * @CreateDate: 2018/9/25 22:03
     */
    private void getDeployInfo(Integer type, Integer isSalve, List<MachineMemStatInfo> machineList, Double size,
                               Integer instanceNum, List<String> sentinelMachineLIst, List<DeployInfo> deployInfoList) {
        if (machineList == null)
            return;

        try {
            /**
             * redis cluster
             */
            if (TypeUtil.isRedisCluster(type)) {
                Double instanceSize = Math.ceil(size / instanceNum);
                //masterIp-memSize
                for (int i = 0; i < instanceNum; i++) {
                    DeployInfo deployInfo = new DeployInfo(type, machineList.get(i % machineList.size()).getIp(),
                            instanceSize.intValue());
                    deployInfoList.add(deployInfo);
                }
                //+slaveIp
                if (isSalve == 1) {
                    int i = 0;
                    for (int j = 0; j < deployInfoList.size(); j++) {
                        DeployInfo deployInfo = deployInfoList.get(j);
                        while (isSameRealMachine(deployInfo.getMasterIp(),
                                machineList.get(i % machineList.size()).getIp())) {
                            i++;
                        }
                        deployInfo.setSlaveIp(machineList.get(i % machineList.size()).getIp());
                        i++;
                    }
                }
            }
            /**
             * redis Sentinel
             */
            else if (TypeUtil.isRedisSentinel(type)) {
                if (isSalve == 1) {//master:size:slave
                    DeployInfo deployInfo = new DeployInfo(type, machineList.get(0).getIp(), size.intValue());
                    int i = 0;
                    while (isSameRealMachine(deployInfo.getMasterIp(),
                            machineList.get(i % machineList.size()).getIp())) {
                        i++;
                    }
                    deployInfo.setSlaveIp(machineList.get(i % machineList.size()).getIp());
                    deployInfoList.add(deployInfo);
                } else {//master:size
                    DeployInfo deployInfo = new DeployInfo(type, machineList.get(0).getIp(), size.intValue());
                    deployInfoList.add(deployInfo);
                }
                //sentinel list
                for (int i = 0; i < sentinelMachineLIst.size(); i++) {
                    DeployInfo sentinel = new DeployInfo(type, sentinelMachineLIst.get(i));
                    deployInfoList.add(sentinel);
                }
                machineList.addAll(machineCenter.getValidMachineMemByIpList(sentinelMachineLIst));

            }
            /**
             * redis standalone
             */
            else if (TypeUtil.isRedisStandalone(type)) {
                //master:size
                DeployInfo deployInfo = new DeployInfo(type, machineList.get(0).getIp(), size.intValue());
                deployInfoList.add(deployInfo);
            }
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
            logger.warn("getDeployInfo error");
        }
    }

    /**
     * @Description: 获取候选机器列表
     * @Author: caoru
     * @CreateDate: 2018/10/8 20:43
     */
    public void getMachineCandiList(MachineMemStatInfo memStatInfo, Double reqSize, Integer reqCpu, Integer isSalve,
                                    List<MachineMemStatInfo> machineCandi) {
        Integer mem = memStatInfo.getMem();//单位G
        Long applyMen = memStatInfo.getApplyMem();//单位bit
        Long availMen = mem * 1024 - applyMen / 1024 / 1024;////单位M
        Integer availCpu = memStatInfo.getCpu() - memStatInfo.getInstanceNum();
        if (availMen >= Math.ceil(reqSize) * (isSalve + 1) && availCpu >= reqCpu * (isSalve + 1)) {
            machineCandi.add(memStatInfo);
        }
    }

    @Override
    public Map getMachineDeployStat(Set<String> ipList, List<DeployInfo> deployInfoList) {
        Map<String, DeployInfoStat> machineDeployStatMap = new HashMap<String, DeployInfoStat>();
        if (!CollectionUtils.isEmpty(deployInfoList)) {
            for (String ip : ipList) {
                Integer masterNum = 0;
                Integer slaveNum = 0;
                Integer sentinelNum = 0;
                Integer twemproxylNum = 0;
                for (DeployInfo deployInfo : deployInfoList) {
                    masterNum += (deployInfo.getMasterIp() != null && deployInfo.getMasterIp().equals(ip)) || (deployInfo.getMasterPikaIp() != null && deployInfo.getMasterPikaIp().equals(ip)) ? 1 : 0;
                    slaveNum += (deployInfo.getSlaveIp() != null && deployInfo.getSlaveIp().equals(ip)) || (deployInfo.getSlavePikaIp() != null && deployInfo.getSlavePikaIp().equals(ip)) ? 1 : 0;
                    sentinelNum += deployInfo.getSentinelIp() != null && deployInfo.getSentinelIp().equals(ip) ? 1 : 0;
                    twemproxylNum += deployInfo.getTwemproxyIp() != null && deployInfo.getTwemproxyIp().equals(ip) ? 1 : 0;
                }
                DeployInfoStat deployInfoStat = new DeployInfoStat(masterNum, slaveNum, sentinelNum, twemproxylNum);
                machineDeployStatMap.put(ip, deployInfoStat);
            }
        }
        return machineDeployStatMap;
    }

    @Override
    public void getResMachines(List<MachineMemStatInfo> machineCandi, Integer machineNum,
                               List<MachineMemStatInfo> resMachines) {
        if (machineCandi == null) {
            return;
        }
        Map map = new HashMap();

        if (machineCandi.size() < machineNum) {
            return;
        } else {
            while (map.size() < machineNum) {
                int random = (int) (Math.random() * machineCandi.size());
                if (!map.containsKey(random)) {
                    map.put(random, "");
                    resMachines.add(machineCandi.get(random));
                }
            }
        }
    }

    /**
     * @param machinelist 机器信息
     * @param type        应用类型
     * @param instanceNum 实例数量
     * @param maxMemory   内存大小
     * @param hasSalve    是否有从节点
     * @return 添加部署Redis/Pika节点信息
     */
    public List<DeployInfo> generateInstanceInfo(List<String> machinelist, int type, int appType, int instanceNum, int maxMemory, int hasSalve, List<DeployInfo> deployInfoList) {
        if (!CollectionUtils.isEmpty(machinelist)) {
            for (int index = 0; index < instanceNum; index++) {
                DeployInfo deployInfo = null;
                if (DeployInfo.isRedisNode(type)) {
                    deployInfo = hasSalve == 1
                            ? DeployInfo.getRedisInfo(appType, machinelist.get(index % machinelist.size()), maxMemory, machinelist.get((index + 1) % machinelist.size()))
                            : DeployInfo.getRedisInfo(appType, machinelist.get(index % machinelist.size()), maxMemory, null);
                } else {
                    deployInfo = hasSalve == 1
                            ? DeployInfo.getPikaInfo(appType, machinelist.get(index % machinelist.size()), maxMemory, machinelist.get((index + 1) % machinelist.size()))
                            : DeployInfo.getPikaInfo(appType, machinelist.get(index % machinelist.size()), maxMemory, null);
                }
                if (deployInfo != null) {
                    deployInfoList.add(deployInfo);
                }
            }
        }
        return deployInfoList;
    }

    public List<DeployInfo> generateProxyinfo(List<String> proxylist, int type, int appType, int proxyNum, List<DeployInfo> deployInfoList) {
        if (!CollectionUtils.isEmpty(proxylist)) {
            for (int index = 0; index < proxyNum; index++) {
                DeployInfo deployInfo = null;
                if (DeployInfo.isSentinelNode(type)) {
                    deployInfo = DeployInfo.getSentinelInfo(appType, proxylist.get(index % proxylist.size()));
                } else {
                    deployInfo = DeployInfo.getTwemproxyInfo(appType, proxylist.get(index % proxylist.size()));
                }
                if (deployInfo != null) {
                    deployInfoList.add(deployInfo);
                }
            }
        }
        return deployInfoList;
    }

    @Override
    public List<AppDesc> checkAppStatus(List<String> appIds) {
        return appDao.getAppDescByIds(appIds);
    }

    @Override
    public List<AppTopMemFragRatio> getTopMemFragRatioApps(TimeBetween timeBetween) {
        long startTime = timeBetween.getStartTime();
        long endTime = timeBetween.getEndTime();
        return appStatsDao.getTopMemFragRatioApps(startTime, endTime);
    }

    @Override
    public Map<Long, Map<String, Object>> getAppClientStatGather(long appId, String gatherTime) {
        List<Map<String, Object>> appClientGatherStatList = appClientStatisticGatherDao.getAppClientStatisticByGatherTime(appId, gatherTime);
        return appClientGatherStatList.stream().collect(Collectors.toMap(appClientGatherStat -> MapUtils.getLong(appClientGatherStat, "app_id"), appClientGatherStat -> appClientGatherStat));
    }

    @Override
    public Map<String, List<Map<String, Object>>> getFilterAppClientStatGather(long appId, String gatherTime) {
        List<Map<String, Object>> appClientGatherStatList = appClientStatisticGatherDao.getExpAppStatisticByGatherTime(gatherTime);
        return groupAppClientStatGather(appClientGatherStatList);
    }

    private boolean appClientStatGatherFilter(Map<String, Object> appClientGatherStat) {
        if (MapUtils.isEmpty(appClientGatherStat)) {
            return false;
        }
        try {
            long appId = MapUtils.getLongValue(appClientGatherStat, "app_id", 0l);
            if (appService.getByAppId(appId).isTestOk()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

    }

    private Map<String, List<Map<String, Object>>> groupAppClientStatGather(List<Map<String, Object>> appClientGatherStatList) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(appClientGatherStatList)) {
            return result;
        }

        List<Map<String, Object>> expAppStats = new ArrayList<>();
        List<Map<String, Object>> latencyAppStats = new ArrayList<>();
        List<Map<String, Object>> memAlterAppStats = new ArrayList<>();
        List<Map<String, Object>> fragRatioAppStats = new ArrayList<>();
        List<Map<String, Object>> topologyAppStats = new ArrayList<>();


        try {
            appClientGatherStatList.forEach(appClientGatherMap -> {
                long appId = MapUtils.getLongValue(appClientGatherMap, "app_id", 0l);
                AppDesc appDesc = appService.getByAppId(appId);
                if (appDesc != null && !appDesc.isTestOk()) {
                    //增加应用是否下线判断，如应用下线，此条客户端统计信息不做处理
                    if(AppStatusEnum.STATUS_PUBLISHED.getStatus() != appDesc.getStatus()){
                        return;
                    }
                    AppDetailVO appDetail = appStatsCenter.getAppDetail(appId);
                    long mem = appDetail.getMem();

                    long exp_count = MapUtils.getLongValue(appClientGatherMap, "exp_count", 0l);
                    long slow_log_count = MapUtils.getLongValue(appClientGatherMap, "slow_log_count", 0l);
                    //double mem_used_ratio = MapUtils.getDoubleValue(appClientGatherMap, "mem_used_ratio", 0.00);
                    double avg_mem_frag_ratio = MapUtils.getDoubleValue(appClientGatherMap, "avg_mem_frag_ratio", 0.00);

                    double format_mem = mem / 1024.0;
                    double format_used_memory = MapUtils.getLongValue(appClientGatherMap, "used_memory", 0l) / 1024 / 1024 / 1024.0;
                    double mem_used_ratio = format_mem == 0 ? 0.0 : (format_used_memory / format_mem) * 100;
                    double format_used_memory_rss = MapUtils.getLongValue(appClientGatherMap, "used_memory_rss", 0l) / 1024 / 1024 / 1024.0;

                    int topology_exam_result = MapUtils.getIntValue(appClientGatherMap, "topology_exam_result", -1);
                    if (exp_count > 0) {
                        expAppStats.add(appClientGatherMap);
                    }
                    if (slow_log_count > 100) {
                        latencyAppStats.add(appClientGatherMap);
                    }
                    if (mem > 1024 * 10 && mem_used_ratio <= 40.0) {
                        appClientGatherMap.put("format_mem", String.format("%.2f", format_mem));
                        appClientGatherMap.put("format_used_memory", String.format("%.2f", format_used_memory));
                        appClientGatherMap.put("mem_used_ratio", Double.valueOf(String.format("%.2f", mem_used_ratio)));
                        memAlterAppStats.add(appClientGatherMap);
                    }
                    if (format_used_memory * 1024 > 500 && avg_mem_frag_ratio > 1.2) {
                        appClientGatherMap.put("format_used_memory", String.format("%.2f", format_used_memory));
                        appClientGatherMap.put("format_used_memory_rss", String.format("%.2f", format_used_memory_rss));
                        fragRatioAppStats.add(appClientGatherMap);
                    }
                    if (topology_exam_result == 1) {
                        topologyAppStats.add(appClientGatherMap);
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            Collections.sort(latencyAppStats, (o1, o2) -> {
                long val1 = (long) o1.get("slow_log_count");
                long val2 = (long) o2.get("slow_log_count");
                return val1 > val2 ? -1 : 1; // 从大到小
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            Collections.sort(memAlterAppStats, (o1, o2) -> {
                double val1 = (double) o1.get("mem_used_ratio");
                double val2 = (double) o2.get("mem_used_ratio");
                return val1 > val2 ? 1 : -1; // 从小到大
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            Collections.sort(fragRatioAppStats, (o1, o2) -> {
                double val1 = (double) o1.get("avg_mem_frag_ratio");
                double val2 = (double) o2.get("avg_mem_frag_ratio");
                return val1 > val2 ? -1 : 1; // 从大到小
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        result.put("expAppStats", expAppStats);
        result.put("latencyAppStats", latencyAppStats.subList(0, latencyAppStats.size() < 10 ? latencyAppStats.size() : 10));
        result.put("memAlterAppStats", memAlterAppStats.subList(0, memAlterAppStats.size() < 10 ? memAlterAppStats.size() : 10));
        result.put("fragRatioAppStats", fragRatioAppStats.subList(0, fragRatioAppStats.size() < 10 ? fragRatioAppStats.size() : 10));
        result.put("topologyAppStats", topologyAppStats);

        return result;
    }

    @Override
    public AppAudit saveAppKeyAnalysis(AppDesc appDesc, AppUser appUser, String appAnalysisReason, String nodeInfo) {
        AppAudit appAudit = new AppAudit();
        appAudit.setAppId(appDesc.getAppId());
        appAudit.setUserId(appUser.getId());
        appAudit.setUserName(appUser.getName());
        // nodeInfo 待分析节点地址
        if (StringUtils.hasText(nodeInfo)) {
            appAudit.setParam1(nodeInfo);
        }
        appAudit.setModifyTime(new Date());
        appAudit.setInfo("申请原因: " + appAnalysisReason);
        appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
        appAudit.setType(AppAuditType.KEY_ANALYSIS.getValue());
        Date now = new Date();
        appAudit.setCreateTime(now);
        appAudit.setModifyTime(now);
        appAuditDao.insertAppAudit(appAudit);

        // 保存日志
        AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(),
                AppAuditLogTypeEnum.KEY_VALUE_ANALYSIS);
        if (appAuditLog != null) {
            appAuditLogDao.save(appAuditLog);
        }

        return appAudit;
    }

    @Override
    public AppAudit saveAppDiagnostic(AppDesc appDesc, AppUser appUser, String reason) {
        AppAudit appAudit = new AppAudit();
        appAudit.setAppId(appDesc.getAppId());
        appAudit.setUserId(appUser.getId());
        appAudit.setUserName(appUser.getName());
        appAudit.setModifyTime(new Date());
        appAudit.setInfo("申请原因: " + reason);
        appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
        appAudit.setType(AppAuditType.APP_DIAGNOSTIC.getValue());
        Date now = new Date();
        appAudit.setCreateTime(now);
        appAudit.setModifyTime(now);
        appAuditDao.insertAppAudit(appAudit);

        // 保存日志
        AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(),
                AppAuditLogTypeEnum.APP_DIAGNOSTIC_APPLY);
        if (appAuditLog != null) {
            appAuditLogDao.save(appAuditLog);
        }

        return appAudit;
    }

    private void addRealMeachine(MachineInfo machineInfo, Set<String> realMachines) {
        if (machineInfo.getVirtual() == 0) {
            realMachines.add(machineInfo.getIp());
        } else {
            realMachines.add(machineInfo.getRealIp());
        }
    }

    private boolean isSameRealMachine(String ip1, String ip2) {
        if (ip1.equals(ip2))
            return true;
        MachineInfo machine1 = machineDao.getMachineInfoByIp(ip1);
        MachineInfo machine2 = machineDao.getMachineInfoByIp(ip2);
        if (machine1.getVirtual() == 1 && machine2.getVirtual() == 1) {
            return machine1.getExtraDesc().equals(machine2.getRealIp());
        } else {
            return machine1.getIp().equals(machine2.getIp());
        }
    }

    @Override
    public List<InstanceInfo> getAppInstanceByType(long appId, InstanceTypeEnum instanceTypeEnum) {
        List<InstanceInfo> resultList = new ArrayList<InstanceInfo>();

        List<InstanceInfo> appInstanceInfoList = getAppInstanceInfo(appId);
        for (InstanceInfo instanceInfo : appInstanceInfoList) {
            if (instanceTypeEnum.getType() == instanceInfo.getType()) {
                resultList.add(instanceInfo);
            }
        }

        return resultList;
    }

}
