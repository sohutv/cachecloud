package com.sohu.cache.web.service;

import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.ConfigRestartRecord;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.web.enums.RestartStatusEnum;
import com.sohu.cache.web.vo.AppRedisConfigVo;
import com.sohu.cache.web.vo.ExecuteResult;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/13 16:09
 * @Description: 应用滚动重启修改配置
 */
public interface AppScrollRestartService {

    /**
     * 处理应用实例信息（封装主从信息）
     * @param instanceInfoList
     * @param appDesc
     */
    boolean handleAppInstanceInfo(List<InstanceInfo> instanceInfoList, AppDesc appDesc);

    /**
     * 根据主节点分组
     * @param instanceList
     * @return
     */
    Map<Integer, List<InstanceInfo>> instanceGroupByMaster(List<InstanceInfo> instanceList);

    /**
     * 根据用户信息，应用信息，重启请求信息，操作类型生成重启记录并保存
     * @param appUser
     * @param appDesc
     * @param paramObj
     * @param opertateType
     * @return
     */
    long generateAndSaveConfigRestartRecord(AppUser appUser, AppDesc appDesc, Object paramObj, Integer opertateType, List<InstanceInfo> pointedInstanceList);

    /**
     * 保存重启记录
     * @param configRestartRecord
     */
    void saveConfigRestartRecord(ConfigRestartRecord configRestartRecord);

    /**
     * 更新重启记录
     * @param configRestartRecord
     */
    void updateConfigRestartRecord(ConfigRestartRecord configRestartRecord);

    /**
     * 更新修改配置、重启记录
     * @param recordId
     * @param restartStatusEnum
     * @param lastLog
     */
    void updateConfigRestartRecord(long recordId, RestartStatusEnum restartStatusEnum, String... lastLog);

    /**
     * 查询重启记录
     * @param id
     * @return
     */
    ConfigRestartRecord getConfigRestartRecord(long id);

    /**
     * 查询重启记录及信息
     * @param configRestartRecord
     * @return
     */
    List<ConfigRestartRecord> getConfigRestartRecordByCondition(Model model, ConfigRestartRecord configRestartRecord, int pageNo, int pageSize);

    /**
     * 保存过程日志到redis
     * @param id
     * @param log
     */
    void saveConfigRestartLog(long id, String log);

    /**
     * 获取过程日志，并删除
     * @param id
     * @return
     */
    List<String> getAndDeleteConfigRestartLog(long id);

    /**
     * 删除redis过程日志
     * @param id
     */
    void deleteConfigRestartLog(long id);

    /**
     * 添加停止滚动重启标志
     * @param appId
     * @return
     */
    boolean addStopRestartFlag(Long appId);

    /**
     * 删除停止滚动重启标志
     * @param appId
     * @return
     */
    boolean deleteStopRestartFlag(Long appId);

    /**
     * 判断是否有停止滚动重启标志
     * @param appId
     * @return
     */
    boolean existsStopRestartFlag(Long appId);

    /**
     * 处理滚动重启
     * @param appDesc
     * @param instanceInfoList
     * @param appRedisConfigVo
     * @return
     */
    ExecuteResult handleRestart(AppUser appUser, AppDesc appDesc, List<InstanceInfo> instanceInfoList, AppRedisConfigVo appRedisConfigVo);

    /**
     * 按分组处理滚动重启
     * @param appUser
     * @param appDesc
     * @param appRedisConfigVo
     * @param instanceInfoList
     * @param pointedInstanceList
     * @param groupMap
     */
    void handleRestartByGroup(AppUser appUser, AppDesc appDesc, AppRedisConfigVo appRedisConfigVo, List<InstanceInfo> instanceInfoList, List<InstanceInfo> pointedInstanceList, Map<Integer, List<InstanceInfo>> groupMap);

    /**
     * 处理修改配置
     * @param appUser
     * @param appDesc
     * @param instanceInfoList
     * @param appRedisConfigVo
     * @return
     */
    Map<String,Object> handleConfig(AppUser appUser, AppDesc appDesc, List<InstanceInfo> instanceInfoList, AppRedisConfigVo appRedisConfigVo);

    /**
     * 判断应用是否正在滚动重启
     * @param appId
     * @return
     */
    boolean isAppOnScrollRestart(long appId);

    /**
     * 执行failover 并check重试
     * @param retryTime
     * @param slaveInstance
     * @param appDesc
     * @return
     */
    boolean failoverAndCheckIdempotent(int retryTime, InstanceInfo slaveInstance, AppDesc appDesc);

    /**
     * 执行failover并检查
     * @param slaveInstance 必须包括masterHost & masterPort
     * @param appDesc
     * @return
     */
    Optional<String> clusterFailoverAndCheck(InstanceInfo slaveInstance, AppDesc appDesc);

    /**
     * 判断实例是否完成加载dump文件
     * @param instanceInfo
     */
    boolean checkLoadFinish(InstanceInfo instanceInfo);

    /**
     * 判断实例是否准备完成
     * @param instanceInfo
     */
    boolean checkSlaveReadyFinish(InstanceInfo instanceInfo);

}
