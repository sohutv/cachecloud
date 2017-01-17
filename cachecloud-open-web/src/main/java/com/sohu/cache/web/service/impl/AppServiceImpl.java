package com.sohu.cache.web.service.impl;

import com.sohu.cache.constant.*;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.util.AppKeyUtil;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppService;

import org.springframework.util.Assert;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.HostAndPort;

/**
 * 应用操作实现类
 *
 * @author leifu
 * @Time 2014年10月21日
 */
public class AppServiceImpl implements AppService {

    private Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    /**
     * 应用相关dao
     */
    private AppDao appDao;

    /**
     * 应用日志相关dao
     */
    private AppAuditLogDao appAuditLogDao;

    /**
     * 实例相关dao
     */
    private InstanceDao instanceDao;

    /**
     * 应用用户关系相关dao
     */
    private AppToUserDao appToUserDao;

    /**
     * 应用申请相关dao
     */
    private AppAuditDao appAuditDao;
    
    /**
     * 用户信息dao
     */
    private AppUserDao appUserDao;

    private InstanceStatsDao instanceStatsDao;

    private RedisCenter redisCenter;

    private MachineCenter machineCenter;
    
    private MachineStatsDao machineStatsDao;
    
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
    public List<AppDesc> getAppDescList(AppUser appUser, AppSearch appSearch) {
        List<AppDesc> list = new ArrayList<AppDesc>();
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
        Assert.isTrue(appId > 0);

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
        appAuditDao.updateAppAudit(id, status);
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
    public void updateUserAuditStatus(Long id, Integer status) {
        appAuditDao.updateAppAudit(id, status);
    }

    @Override
    public List<AppToUser> getAppToUserList(Long appId) {
        return appToUserDao.getByAppId(appId);
    }

    @Override
    public AppDesc getAppByName(String appName) {
        return appDao.getByAppName(appName);
    }

    @Override
    public List<InstanceInfo> getAppInstanceInfo(Long appId) {
        List<InstanceInfo> resultList = instanceDao.getInstListByAppId(appId);
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
    public List<AppAudit> getAppAudits(Integer status, Integer type) {
        List<AppAudit> list = appAuditDao.selectWaitAppAudits(status, type);
        for (Iterator<AppAudit> i = list.iterator(); i.hasNext(); ) {
            AppAudit appAudit = i.next();
            AppDesc appDesc = appDao.getAppDescById(appAudit.getAppId());
//            if (appDesc == null) {
//                i.remove();
//            }
            appAudit.setAppDesc(appDesc);
        }
        return list;
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
    public AppAudit saveAppChangeConfig(AppDesc appDesc, AppUser appUser, Long instanceId, String appConfigKey, String appConfigValue, String appConfigReason, AppAuditType modifyConfig) {
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
        appAudit.setInfo("appId=" + appId + "下的" + hostPort + "实例申请修改配置项:" + instanceConfigKey + ", 配置值: " + instanceConfigValue + ", 修改原因: " + instanceConfigReason);
        appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
        appAudit.setType(instanceModifyConfig.getValue());
        appAuditDao.insertAppAudit(appAudit);

        //保存日志
        AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(), AppAuditLogTypeEnum.INSTANCE_CONFIG_APPLY);
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
                if(log != null){
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
        appAudit.setInfo(appUser.getChName() + "申请成为Cachecloud用户, 手机:" + appUser.getMobile() + ",邮箱:" + appUser.getEmail());
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
    public SuccessEnum changeAppAlertConfig(long appId, int memAlertValue, int clientConnAlertValue, AppUser appUser) {
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

    public void setAppDao(AppDao appDao) {
        this.appDao = appDao;
    }

    public void setAppAuditLogDao(AppAuditLogDao appAuditLogDao) {
        this.appAuditLogDao = appAuditLogDao;
    }

    public void setAppToUserDao(AppToUserDao appToUserDao) {
        this.appToUserDao = appToUserDao;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setAppAuditDao(AppAuditDao appAuditDao) {
        this.appAuditDao = appAuditDao;
    }

    public void setInstanceStatsDao(InstanceStatsDao instanceStatsDao) {
        this.instanceStatsDao = instanceStatsDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public void setMachineStatsDao(MachineStatsDao machineStatsDao) {
        this.machineStatsDao = machineStatsDao;
    }

    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    

}
