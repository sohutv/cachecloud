package com.sohu.cache.web.service;

import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.entity.*;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.web.enums.SuccessEnum;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 应用相关操作
 *
 * @author leifu
 * @Time 2014年10月21日
 */
public interface AppService {

    /**
     * 查询指定用户下的应用个数
     *
     * @param appUser
     * @return
     */
    int getAppDescCount(AppUser appUser, AppSearch appSearch);

    /**
     * 获取应用master节点
     *
     * @param appId
     * @return
     */
    List<InstanceInfo> getAppMasterInstanceInfoList(long appId);

    /**
     * 查询指定用户下的所有的应用
     *
     * @param appUser
     * @return
     */
    List<AppDesc> getAppDescList(AppUser appUser, AppSearch appSearch);

    /**
     * 按照Appid取应用信息
     *
     * @param appId
     * @return
     */
    AppDesc getByAppId(Long appId);

    /**
     * 保存应用
     *
     * @param appDesc
     * @return
     */
    int save(AppDesc appDesc);

    /**
     * 更新应用
     *
     * @param appDesc
     * @return
     */
    int update(AppDesc appDesc);

    /**
     * 更新应用 pwd
     * @param appId
     * @param appPwd
     * @return
     */
    int updateAppPwd(long appId, String appPwd);

    /**
     * 获取应用下实例的基本信息
     * @param appId
     * @return
     */
    List<InstanceInfo> getAppBasicInstanceInfo(Long appId);

    /**
     * 获取应用下实例基本信息，并按照主从分组
     * @param appId
     * @return
     */
    Map<InstanceInfo, List<InstanceInfo>> getAppInstanceInfoGroup(Long appId);

        /**
         * 获取应用的实例
         *
         * @param appId
         * @return
         */
    List<InstanceInfo> getAppInstanceInfo(Long appId);

    List<InstanceInfo> getAppOnlineInstanceInfo(Long appId);

    List<InstanceStats> getAppInstanceStats(Long appId);

    int updateWithCustomPwd(AppDesc appDesc);

    /**
     * 保存用户与应用的关系
     *
     * @param appId
     * @param userId
     * @return
     */
    boolean saveAppToUser(Long appId, Long userId);

    /**
     * 更新审核状态
     *
     * @param id      审批id
     * @param appId
     * @param status  审批状态
     * @param appUser 更新人
     */
    void updateAppAuditStatus(Long id, Long appId, Integer status, AppUser appUser);

    /**
     * 更新用户审核状态
     *
     * @param id     审批id
     * @param status 审批状态
     */
    void updateUserAuditStatus(Long id, Integer status, Long operateId);

    /**
     * 通过应用名获取应用
     *
     * @param appName
     * @return
     */
    AppDesc getAppByName(String appName);

    /**
     * 获取应用下的所有用户应用关系列表
     *
     * @param appId
     * @return
     */
    List<AppToUser> getAppToUserList(Long appId);

    /**
     * 删除用户应用关系
     *
     * @param appId
     * @param userId
     */
    SuccessEnum deleteAppToUser(Long appId, Long userId);

    /**
     * 获取审批列表
     *
     * @param status(参考AppAppCheckEnum)
     * @param type                      (参考AppAuditType)
     * @return
     */
    List<AppAudit> getAppAudits(Integer status, Integer type, Long auditId, Long userId, Long operateId);


    Map<String, Object> getStatisticGroupByStatus(Long userId, Long operateId, Date startTime, Date endTime);

    Map<String, Object> getStatisticGroupByType(Long userId, Long operateId, Date startTime, Date endTime);

    /**
     * 获取应用申请信息
     *
     * @param appid 应用id
     * @param type  (参考AppAuditType)
     */
    List<AppAudit> getAppAudits(Long appid, Integer type);

    /**
     * 保存扩容申请
     *
     * @param appDesc
     * @param appUser
     * @param applyMemSize   扩容容量
     * @param appScaleReason 扩容原因
     * @param appScale       申请类型
     */
    AppAudit saveAppScaleApply(AppDesc appDesc, AppUser appUser, String applyMemSize, String appScaleReason,
                               AppAuditType appScale);

    /**
     * 保存应用配置申请
     *
     * @param appDesc
     * @param appUser
     * @param instanceId     实例id
     * @param appConfigKey   配置项
     * @param appConfigValue 配置值
     * @param modifyConfig   申请类型
     */
    AppAudit saveAppChangeConfig(AppDesc appDesc, AppUser appUser, Long instanceId, String appConfigKey,
                                 String appConfigValue, String appConfigReason, AppAuditType modifyConfig);

    /**
     * 保存实例配置申请
     *
     * @param appDesc
     * @param appUser
     * @param instanceId
     * @param instanceConfigKey
     * @param instanceConfigValue
     * @param instanceConfigReason
     * @param instanceModifyConfig
     * @return
     */
    AppAudit saveInstanceChangeConfig(AppDesc appDesc, AppUser appUser, Long instanceId, String instanceConfigKey,
                                      String instanceConfigValue, String instanceConfigReason, AppAuditType instanceModifyConfig);

    /**
     * 获取审批信息
     *
     * @param appAuditId
     * @return
     */
    AppAudit getAppAuditById(Long appAuditId);

    /**
     * 驳回理由
     *
     * @param appAudit
     * @param userInfo
     */
    SuccessEnum updateRefuseReason(AppAudit appAudit, AppUser userInfo);

    /**
     * 获取用户的应用数量
     *
     * @param userId
     * @return
     */
    int getUserAppCount(Long userId);

    /**
     * 获取应用的机器信息
     *
     * @param appId
     * @return
     */
    List<MachineStats> getAppMachineDetail(Long appId);

    /**
     * 获取应用拥有实例的机器
     * @param appId
     * @return
     */
    public List<MachineStats> getAppMachine(Long appId);

    /**
     * 根据应用id获取审批记录
     *
     * @param appId
     * @return
     */
    List<AppAudit> getAppAuditListByAppId(Long appId);

    /**
     * 注册用户申请
     *
     * @param appUser
     * @param registerUserApply
     * @return
     */
    AppAudit saveRegisterUserApply(AppUser appUser, AppAuditType registerUserApply);

    /**
     * 获取所有应用
     */
    List<AppDesc> getAllAppDesc();

    /**
     * 修改报警配置
     *
     * @param appId
     * @param memAlertValue
     * @param clientConnAlertValue
     * @param appUser
     * @return
     */
    SuccessEnum changeAppAlertConfig(long appId, int memAlertValue, int clientConnAlertValue, int hitPrecentAlertValue,
                                     int isAccessMonitor, AppUser appUser);

    /**
     * 更新appKey
     *
     * @param appId
     */
    void updateAppKey(long appId);

    /**
     * @param machinelist 机器信息
     * @param type        参考NodeEum
     * @param appType     应用类型
     * @param instanceNum 实例数量
     * @param maxMemory   内存大小
     * @param hasSalve    是否有从节点
     * @return 添加部署Redis/Pika节点信息
     */
    public List<DeployInfo> generateInstanceInfo(List<String> machinelist, int type, int appType, int instanceNum, int maxMemory, int hasSalve, List<DeployInfo> deployInfoList);

    /**
     * @param proxylist 机器信息
     * @param type      参考NodeEum
     * @param appType   应用类型
     * @param proxyNum  实例数量
     * @return 添加sentinel/proxy节点
     */
    public List<DeployInfo> generateProxyinfo(List<String> proxylist, int type, int appType, int proxyNum, List<DeployInfo> deployInfoList);

    String generateDeployInfo(Integer type,
                              Integer isSalve,
                              String room,
                              Double size,
                              Integer machineNum,
                              Integer instanceNum,
                              Integer useType,
                              String machines,
                              String excludeMachines,
                              String sentinelMachines,
                              List<DeployInfo> getDeployInfo,
                              List<MachineMemStatInfo> resMachines);

    /**
     * @Description: 获取部署信息统计
     * @Author: caoru
     * @CreateDate: 2018/10/10 16:34
     */
    Map getMachineDeployStat(Set<String> ipList, List<DeployInfo> deployInfoList);

    /**
     * @Description: 计算内存和核数
     * @Author: caoru
     * @CreateDate: 2018/10/14 21:06
     */
    void getMachineCandiList(MachineMemStatInfo memStatInfo, Double reqSize, Integer reqCpu, Integer isSalve,
                             List<MachineMemStatInfo> machineCandi);

    /**
     * @Description: 从备选集中获取机器列表
     * @Author: caoru
     * @CreateDate: 2018/10/9 10:34
     */
    void getResMachines(List<MachineMemStatInfo> machineCandi, Integer machineNum,
                        List<MachineMemStatInfo> resMachines);

    /**
     * <p>
     * Description:
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/11/14
     */
    List<AppDesc> checkAppStatus(List<String> appids);

    /**
     * 键值分析
     *
     * @param appDesc
     * @param appUser
     * @param appAnalysisReason
     * @return
     */
    AppAudit saveAppKeyAnalysis(AppDesc appDesc, AppUser appUser, String appAnalysisReason, String nodeInfo);

    /**
     * 应用诊断
     *
     * @param appDesc
     * @param appUser
     * @param reason
     * @return
     */
    AppAudit saveAppDiagnostic(AppDesc appDesc, AppUser appUser, String reason);

    /**
     * 获取碎片率高的应用
     */
    List<AppTopMemFragRatio> getTopMemFragRatioApps(TimeBetween timeBetween);

    public List<InstanceInfo> getAppInstanceByType(long appId, InstanceTypeEnum instanceTypeEnum);

    /**
     * @param gatherTime
     * @return
     */
    Map<Long, Map<String, Object>> getAppClientStatGather(long appId, String gatherTime);

    Map<String, List<Map<String, Object>>> getFilterAppClientStatGather(long appId, String gatherTime);

    /**
     * 检查应用持久化配置并修复
     * @param appId
     * @param masterConfigMap
     * @param slaveConfigMap
     * @return 执行失败信息
     */
    String checkAppPersistenceConfigAndFix(long appId, Map<String, String> masterConfigMap, Map<String, String> slaveConfigMap);


    /**
     * 更新持久化类型
     * @param appId
     * @param persistenceType
     */
    boolean updateAppPersistenceType(long appId, int persistenceType);

    /**
     * 更新内存策略
     * @param appId
     * @param maxmemoryPolicy
     * @return
     */
    boolean updateAppMaxmemoryPolicy(long appId, int maxmemoryPolicy);

    /**
     * 获取用户所属应用的内存统计信息（包含版本）
     * @param userId
     * @param isAdmin
     * @return
     */
    List<AppCapacityStatisticsResult> getAppCapacityStatistics(Long userId, Boolean isAdmin);

    Map<String, Object> getAppCapacityStats(Long userId, Boolean isAdmin);

    /**
     * 获取用户所属应用的监控统计信息
     * @param userId
     * @param isAdmin
     * @param startTime
     * @param endTime
     * @return
     */
    List<AppMonitorStatisticsResult> getAppMonitorStatistics(Long userId, Boolean isAdmin, String startTime, String endTime);

    /**
     * 更新应用拓扑检测结果
     * @param topologyExam
     * @return
     */
    int saveAppTopologyExam(AppClientStatisticGather topologyExam);

    /**
     * 获取拓扑检测失败的应用
     * @param gatherTime
     * @return
     */
    List<AppClientStatisticGather> getTopologyExamFailedByGatherTime(String gatherTime);

}