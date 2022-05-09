package com.sohu.cache.web.service.impl;

import com.sohu.cache.entity.*;
import com.sohu.cache.web.enums.AlertTypeEnum;
import com.sohu.cache.web.service.AppAlertRecordService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.dao.AppAlertRecordDao;
import com.sohu.cache.web.vo.AppDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/3 13:34
 * @Description: 报警记录服务实现类
 */
@Slf4j
@Service
public class AppAlertRecordServiceImpl implements AppAlertRecordService {

    @Autowired
    private AppAlertRecordDao appAlertRecordDao;

    /**
     * 保存报警信息
     *
     * @param appAlertRecord
     * @return
     */
    @Override
    public int saveAlertInfo(AppAlertRecord appAlertRecord) {
        return appAlertRecordDao.save(appAlertRecord);
    }

    /**
     * 批量保存报警信息
     * @param appAlertRecordList
     * @return
     */
    @Override
    public int saveBatchAlertInfo(List<AppAlertRecord> appAlertRecordList) {
        return appAlertRecordDao.batchSave(appAlertRecordList);
    }

    /**
     * 异步保存报警信息
     * @param appAlertRecord
     * @return
     */
    @Override
    public void asyncSaveAlertInfo(AppAlertRecord appAlertRecord) {
        AsyncThreadPoolFactory.ALERT_RECORD_THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                appAlertRecordDao.save(appAlertRecord);
            }
        });
    }

    /**
     * 异步批量保存报警信息
     * @param appAlertRecordList
     * @return
     */
    @Override
    public void asyncSaveBatchAlertInfo(List<AppAlertRecord> appAlertRecordList) {
        AsyncThreadPoolFactory.ALERT_RECORD_THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                appAlertRecordDao.batchSave(appAlertRecordList);
            }
        });
    }

    /**
     * 根据报警类型，保存报警信息
     * @param type 类型
     * @param object
     * @return
     */
    @Override
    public int saveAlertInfoByType(AlertTypeEnum type, String title, String content, Object... object) {
        try{
            Date date = new Date();
            List<AppAlertRecord> appAlertRecordList = this.getAlertRecordByType(type, date, title, content, object);
            if(CollectionUtils.isNotEmpty(appAlertRecordList)){
                this.asyncSaveBatchAlertInfo(appAlertRecordList);
            }
        }catch (Exception e){
            log.error("saveAlertInfoByType error : ", e);
        }
        return 1;
    }

    /**
     * 根据邮件类型及信息生成对应的报警信息记录
     * @param type
     * @param title
     * @param content
     * @param objects
     * @return
     */
    private List<AppAlertRecord> getAlertRecordByType(AlertTypeEnum type, Date date, String title, String content, Object... objects){
        if(type == null){
            return null;
        }
        if(objects == null || objects.length < 1){
            return null;
        }
        List<AppAlertRecord> appAlertRecordList = new ArrayList<>();
        if(type == AlertTypeEnum.INSTANCE_RUNNING_STATE_CHANGE){
            InstanceInfo info = (InstanceInfo) (objects[0]);
            generateAlertRecordByInstanceInfo(type, date, appAlertRecordList, title, content, info);
        } else if(type == AlertTypeEnum.INATANCE_EXCEPTION_STATE_MONITOR
                || type == AlertTypeEnum.INSTANCE_MINUTE_MONITOR
                || type == AlertTypeEnum.POD_RESTART_INSTANCE_RECOVER){
            //此类型，传入的content为空，content需自己组装
            List<InstanceAlertValueResult> alertInstInfoList = (List<InstanceAlertValueResult>) (objects[0]);
            generateAlertRecordByInstanceAlertList(type, date, appAlertRecordList, title, alertInstInfoList);
        }else if(type == AlertTypeEnum.MACHINE_MEMORY_OVER_PRESET
                || type == AlertTypeEnum.POD_RESTART_SYNC_TASK){
            String ip = (String) (objects[0]);
            generateAlertRecordByIp(type, date, appAlertRecordList, title, content, ip);
        }else if(type == AlertTypeEnum.MACHINE_MANAGE){
            //此类型，传入的content为空，content需自己组装
            List<OperationAlertValueResult> recoverInstInfo = (List<OperationAlertValueResult>) (objects[0]);
            generateAlertRecordByOPerationList(type, date, appAlertRecordList, title, recoverInstInfo);
        }else if(type == AlertTypeEnum.APP_SHARD_CLENT_CONNECTION
                || type == AlertTypeEnum.APP_SHARD_MEM_USED_RATIO){
            AppDetailVO appDetailVO = (AppDetailVO) (objects[0]);
            InstanceStats instanceStats = (InstanceStats) (objects[1]);
            generateAlertRecordByAppAndInstance(type, date, appAlertRecordList, title, content, appDetailVO, instanceStats);
        }else if(type == AlertTypeEnum.APP_CLIENT_CONNECTION
                || type == AlertTypeEnum.APP_HIT_RATIO
                || type == AlertTypeEnum.APP_MEM_USED_RATIO){
            AppDetailVO appDetailVO = (AppDetailVO) (objects[0]);
            generateAlertRecordByAppDetailVO(type, date, appAlertRecordList, title, content, appDetailVO);
        }
        //此种类型type == AlertTypeEnum.APP_MINUTE_MONITOR不做处理，与 AlertTypeEnum.INSTANCE_MINUTE_MONITOR重复
        return appAlertRecordList;
    }

    /**
     * 根据实例信息生成报警信息
     * @param type
     * @param appAlertRecordList
     * @param title
     * @param content
     * @param instanceInfo
     */
    private void generateAlertRecordByInstanceInfo(AlertTypeEnum type, Date date, List<AppAlertRecord> appAlertRecordList, String title, String content, InstanceInfo instanceInfo){
        AppAlertRecord appAlertRecord = new AppAlertRecord();
        appAlertRecord.setVisibleType(type.getVisibleType());
        appAlertRecord.setImportantLevel(type.getImportantLevel());
        appAlertRecord.setTitle(title);
        appAlertRecord.setContent(content);
        appAlertRecord.setAppId(instanceInfo.getAppId());
        appAlertRecord.setInstanceId(Long.valueOf(instanceInfo.getId()));
        appAlertRecord.setIp(instanceInfo.getIp());
        appAlertRecord.setPort(instanceInfo.getPort());
        appAlertRecord.setCreateTime(date);
        appAlertRecordList.add(appAlertRecord);
    }

    /**
     * 根据机器ip生成报警信息
     * @param type
     * @param appAlertRecordList
     * @param title
     * @param content
     * @param ip
     */
    private void generateAlertRecordByIp(AlertTypeEnum type, Date date, List<AppAlertRecord> appAlertRecordList, String title, String content, String ip){
        AppAlertRecord appAlertRecord = new AppAlertRecord();
        appAlertRecord.setVisibleType(type.getVisibleType());
        appAlertRecord.setImportantLevel(type.getImportantLevel());
        appAlertRecord.setTitle(title);
        appAlertRecord.setContent(content);
        appAlertRecord.setIp(ip);
        appAlertRecord.setCreateTime(date);
        appAlertRecordList.add(appAlertRecord);
    }

    /**
     * 根据应用详细信息生成报警信息
     * @param type
     * @param appAlertRecordList
     * @param title
     * @param content
     * @param appDetailVO
     */
    private void generateAlertRecordByAppDetailVO(AlertTypeEnum type, Date date, List<AppAlertRecord> appAlertRecordList, String title, String content, AppDetailVO appDetailVO){
        AppAlertRecord appAlertRecord = new AppAlertRecord();
        appAlertRecord.setVisibleType(type.getVisibleType());
        appAlertRecord.setImportantLevel(type.getImportantLevel());
        appAlertRecord.setTitle(title);
        appAlertRecord.setContent(content);
        long appId = appDetailVO.getAppDesc().getAppId();
        appAlertRecord.setAppId(appId);
        appAlertRecord.setCreateTime(date);
        appAlertRecordList.add(appAlertRecord);
    }

    /**
     * 根据应用和实例信息生成报警信息
     * @param type
     * @param appAlertRecordList
     * @param title
     * @param content
     * @param appDetailVO
     * @param instanceStats
     */
    private void generateAlertRecordByAppAndInstance(AlertTypeEnum type, Date date, List<AppAlertRecord> appAlertRecordList, String title, String content, AppDetailVO appDetailVO, InstanceStats instanceStats){
        AppAlertRecord appAlertRecord = new AppAlertRecord();
        appAlertRecord.setVisibleType(type.getVisibleType());
        appAlertRecord.setImportantLevel(type.getImportantLevel());
        appAlertRecord.setTitle(title);
        appAlertRecord.setContent(content);
        long appId = appDetailVO.getAppDesc().getAppId();
        appAlertRecord.setAppId(appId);
        appAlertRecord.setInstanceId(instanceStats.getInstId());
        appAlertRecord.setIp(instanceStats.getIp());
        appAlertRecord.setPort(instanceStats.getPort());
        appAlertRecord.setCreateTime(date);
        appAlertRecordList.add(appAlertRecord);
    }

    /**
     * 根据实例报警结果列表生成报警信息
     * @param type
     * @param appAlertRecordList
     * @param title
     * @param alertInstInfoList
     */
    private void generateAlertRecordByInstanceAlertList(AlertTypeEnum type, Date date, List<AppAlertRecord> appAlertRecordList, String title, List<InstanceAlertValueResult> alertInstInfoList){
        for(InstanceAlertValueResult instanceAlert : alertInstInfoList){
            AppAlertRecord appAlertRecord = new AppAlertRecord();
            appAlertRecord.setVisibleType(type.getVisibleType());
            appAlertRecord.setImportantLevel(type.getImportantLevel());
            appAlertRecord.setTitle(title);
            appAlertRecord.setAppId(instanceAlert.getAppId());
            appAlertRecord.setIp(instanceAlert.getInstanceInfo().getIp());
            appAlertRecord.setPort(instanceAlert.getInstanceInfo().getPort());
            StringBuilder sb = new StringBuilder();
            if(type == AlertTypeEnum.INATANCE_EXCEPTION_STATE_MONITOR) {
                sb.append("状态:");
                sb.append(instanceAlert.getInstanceInfo().getStatusDesc());
                sb.append(", 说明:");
                sb.append(instanceAlert.getOtherInfo());
            }
            if(type == AlertTypeEnum.INSTANCE_MINUTE_MONITOR) {
                if(instanceAlert.getInstanceAlertConfig() != null && instanceAlert.getInstanceAlertConfig().getImportantLevel() != null){
                    appAlertRecord.setImportantLevel(instanceAlert.getInstanceAlertConfig().getImportantLevel());
                }
                sb.append("属性值:");
                sb.append(instanceAlert.getInstanceAlertConfig().getAlertConfig());
                sb.append(", 说明:");
                sb.append(instanceAlert.getAlertMessage());
                sb.append(", 其他信息:");
                sb.append(instanceAlert.getOtherInfo());
            }
            if(type == AlertTypeEnum.POD_RESTART_INSTANCE_RECOVER) {
                sb.append("实例恢复时间:");
                sb.append(instanceAlert.getOtherInfo());
            }
            appAlertRecord.setContent(sb.toString());
            appAlertRecord.setCreateTime(date);
            appAlertRecordList.add(appAlertRecord);
        }
    }

    /**
     * 根据机器操作结果列表生成报警信息
     * @param type
     * @param appAlertRecordList
     * @param title
     * @param recoverInstInfo
     */
    private void generateAlertRecordByOPerationList(AlertTypeEnum type, Date date, List<AppAlertRecord> appAlertRecordList, String title, List<OperationAlertValueResult> recoverInstInfo){
        for(OperationAlertValueResult operationAlertValueResult : recoverInstInfo){
            AppAlertRecord appAlertRecord = new AppAlertRecord();
            appAlertRecord.setVisibleType(type.getVisibleType());
            appAlertRecord.setImportantLevel(type.getImportantLevel());
            appAlertRecord.setTitle(title);
            MachineInfo machineInfo = operationAlertValueResult.getMachineInfo();
            StringBuilder sb = new StringBuilder();
            if(machineInfo != null){
                appAlertRecord.setIp(machineInfo.getIp());
                sb.append("ip:");
                sb.append(machineInfo.getIp());
                sb.append(", real_ip:");
                sb.append(machineInfo.getRealIp());
                sb.append(", machineInfo(内存:");
                sb.append(machineInfo.getMem());
                sb.append(", cpu:");
                sb.append(machineInfo.getCpu());
                sb.append(", 备注说明:");
                sb.append(machineInfo.getExtraDesc());
                sb.append("), ");
            }
            sb.append("操作:");
            sb.append(operationAlertValueResult.getType());
            sb.append(", staus:");
            sb.append(operationAlertValueResult.getStatus());
            sb.append(", message:");
            sb.append(operationAlertValueResult.getMessage());
            appAlertRecord.setCreateTime(date);
            appAlertRecord.setContent(sb.toString());
            appAlertRecordList.add(appAlertRecord);
        }
    }

}
