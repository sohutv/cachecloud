package com.sohu.cache.web.service;

import java.util.List;

import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.entity.*;
import com.sohu.cache.web.enums.SuccessEnum;

/**
 * 应用相关操作
 * @author leifu
 * @Time 2014年10月21日
 */
public interface AppService {
    
    /**
     * 查询指定用户下的应用个数
     * @param appUser
     * @return
     */
    int getAppDescCount(AppUser appUser, AppSearch appSearch);

    /**
     * 查询指定用户下的所有的应用
     * @param appUser
     * @return
     */
    List<AppDesc> getAppDescList(AppUser appUser, AppSearch appSearch);

    /**
     * 按照Appid取应用信息
     * @param appId
     * @return
     */
    AppDesc getByAppId(Long appId);

    /**
     * 保存应用
     * @param appDesc
     * @return
     */
    int save(AppDesc appDesc);

    /**
     * 更新应用
     * @param appDesc
     * @return
     */
    int update(AppDesc appDesc);

    /**
     * 获取应用的实例
     * @param appId
     * @return
     */
    List<InstanceInfo> getAppInstanceInfo(Long appId);

    List<InstanceStats> getAppInstanceStats(Long appId);

    /**
     * 保存用户与应用的关系
     * @param appId
     * @param userId
     * @return
     */
    boolean saveAppToUser(Long appId, Long userId);

    /**
     * 更新审核状态
     * @param id 审批id
     * @param appId
     * @param status 审批状态
     * @param appUser 更新人
     */
    void updateAppAuditStatus(Long id, Long appId, Integer status, AppUser appUser);

    /**
     * 更新用户审核状态
     * @param id 审批id
     * @param status 审批状态
     */
    void updateUserAuditStatus(Long id, Integer status);

    
    /**
     * 通过应用名获取应用
     * @param appName
     * @return
     */
    AppDesc getAppByName(String appName);

    /**
     * 获取应用下的所有用户应用关系列表
     * @param appId
     * @return
     */
    List<AppToUser> getAppToUserList(Long appId);
    
    /**
     * 删除用户应用关系
     * @param appId
     * @param userId
     */
    SuccessEnum deleteAppToUser(Long appId, Long userId);

    /**
     * 获取审批列表
     * @param status(参考AppAppCheckEnum)
     * @param type (参考AppAuditType)
     * @return
     */
    List<AppAudit> getAppAudits(Integer status, Integer type);

    /**
     * 保存扩容申请
     * @param appDesc
     * @param appUser 
     * @param applyMemSize 扩容容量
     * @param appScaleReason 扩容原因
     * @param appScale 申请类型
     */
    AppAudit saveAppScaleApply(AppDesc appDesc, AppUser appUser, String applyMemSize, String appScaleReason, AppAuditType appScale);

    /**
     * 保存应用配置申请
     * @param appDesc
     * @param appUser
     * @param instanceId 实例id
     * @param appConfigKey 配置项
     * @param appConfigValue 配置值
     * @param modifyConfig 申请类型
     */
    AppAudit saveAppChangeConfig(AppDesc appDesc, AppUser appUser, Long instanceId, String appConfigKey, String appConfigValue,String appConfigReason, AppAuditType modifyConfig);

    /**
     * 保存实例配置申请
     * @param appDesc
     * @param appUser
     * @param instanceId
     * @param instanceConfigKey
     * @param instanceConfigValue
     * @param instanceConfigReason
     * @param instanceModifyConfig
     * @return
     */
    AppAudit saveInstanceChangeConfig(AppDesc appDesc, AppUser appUser, Long instanceId, String instanceConfigKey, String instanceConfigValue, String instanceConfigReason, AppAuditType instanceModifyConfig);
    
    /**
     * 获取审批信息
     * @param appAuditId
     * @return
     */
    AppAudit getAppAuditById(Long appAuditId);

    /**
     * 驳回理由
     * @param appAudit
     * @param userInfo
     */
    SuccessEnum updateRefuseReason(AppAudit appAudit, AppUser userInfo);

    /**
     * 获取用户的应用数量
     * @param userId
     * @return
     */
    int getUserAppCount(Long userId);

    /**
     * 获取应用的机器信息
     * @param appId
     * @return
     */
    List<MachineStats> getAppMachineDetail(Long appId);

    /**
     * 根据应用id获取审批记录
     * @param appId
     * @return
     */
    List<AppAudit> getAppAuditListByAppId(Long appId);

    /**
     * 注册用户申请
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
     * @param appId
     * @param memAlertValue
     * @param clientConnAlertValue
     * @param appUser
     * @return
     */
    SuccessEnum changeAppAlertConfig(long appId, int memAlertValue, int clientConnAlertValue, AppUser appUser);

    /**
     * 更新appKey
     * @param appId
     */
    void updateAppKey(long appId);
    
}
