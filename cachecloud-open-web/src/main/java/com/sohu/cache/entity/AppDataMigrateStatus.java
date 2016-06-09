package com.sohu.cache.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.sohu.cache.constant.AppDataMigrateEnum;
import com.sohu.cache.constant.AppDataMigrateStatusEnum;

/**
 * 迁移状态
 * 
 * @author leifu
 * @Date 2016-6-9
 * @Time 下午5:13:13
 */
public class AppDataMigrateStatus {

    /**
     * 自增id
     */
    private long id;

    /**
     * 迁移工具所在机器ip
     */
    private String migrateMachineIp;
    
    /**
     * 迁移工具所占port
     */
    private int migrateMachinePort;

    /**
     * 目标实例列表
     */
    private String sourceServers;

    /**
     * 源迁移类型,0:single,1:redis cluster,2:rdb file,3:twemproxy
     */
    private int sourceMigrateType;

    /**
     * 目标实例列表
     */
    private String targetServers;

    /**
     * 目标迁移类型,0:single,1:redis cluster,2:rdb file,3:twemproxy
     */
    private int targetMigrateType;

    /**
     * 源应用id
     */
    private long sourceAppId;

    /**
     * 目标应用id
     */
    private long targetAppId;

    /**
     * 操作人
     */
    private long userId;

    /**
     * 迁移执行状态
     */
    private int status;

    /**
     * 迁移开始执行时间
     */
    private Date startTime;

    /**
     * 迁移结束执行时间
     */
    private Date endTime;
    
    /**
     * 日志路径
     */
    private String logPath;

    /**
     * 配置路径
     */
    private String configPath;
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMigrateMachineIp() {
        return migrateMachineIp;
    }

    public void setMigrateMachineIp(String migrateMachineIp) {
        this.migrateMachineIp = migrateMachineIp;
    }

    public int getMigrateMachinePort() {
        return migrateMachinePort;
    }

    public void setMigrateMachinePort(int migrateMachinePort) {
        this.migrateMachinePort = migrateMachinePort;
    }

    public String getSourceServers() {
        return sourceServers;
    }

    public void setSourceServers(String sourceServers) {
        this.sourceServers = sourceServers;
    }

    public int getSourceMigrateType() {
        return sourceMigrateType;
    }
    
    public String getSourceMigrateTypeDesc() {
        AppDataMigrateEnum appDataMigrateEnum = AppDataMigrateEnum.getByIndex(sourceMigrateType);
        return appDataMigrateEnum == null ? "异常" :appDataMigrateEnum.getType();
    }

    public void setSourceMigrateType(int sourceMigrateType) {
        this.sourceMigrateType = sourceMigrateType;
    }

    public String getTargetServers() {
        return targetServers;
    }

    public void setTargetServers(String targetServers) {
        this.targetServers = targetServers;
    }

    public int getTargetMigrateType() {
        return targetMigrateType;
    }
    
    public String getTargetMigrateTypeDesc() {
        AppDataMigrateEnum appDataMigrateEnum = AppDataMigrateEnum.getByIndex(targetMigrateType);
        return appDataMigrateEnum == null ? "异常" :appDataMigrateEnum.getType();
    }

    public void setTargetMigrateType(int targetMigrateType) {
        this.targetMigrateType = targetMigrateType;
    }

    public long getSourceAppId() {
        return sourceAppId;
    }

    public void setSourceAppId(long sourceAppId) {
        this.sourceAppId = sourceAppId;
    }

    public long getTargetAppId() {
        return targetAppId;
    }

    public void setTargetAppId(long targetAppId) {
        this.targetAppId = targetAppId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }
    
    public String getStatusDesc() {
        AppDataMigrateStatusEnum appDataMigrateStatusEnum = AppDataMigrateStatusEnum.getByStatus(status);
        return appDataMigrateStatusEnum == null ? "异常" : appDataMigrateStatusEnum.getInfo();
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }
    
    public String getStartTimeFormat() {
        if (startTime == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(startTime);
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
    
    public String getEndTimeFormat() {
        if (endTime == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(endTime);
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public String toString() {
        return "AppDataMigrateStatus [id=" + id + ", migrateMachineIp=" + migrateMachineIp + ", migrateMachinePort="
                + migrateMachinePort + ", sourceServers=" + sourceServers + ", sourceMigrateType=" + sourceMigrateType
                + ", targetServers=" + targetServers + ", targetMigrateType=" + targetMigrateType + ", sourceAppId="
                + sourceAppId + ", targetAppId=" + targetAppId + ", userId=" + userId + ", status=" + status
                + ", startTime=" + startTime + ", endTime=" + endTime + ", logPath=" + logPath + ", configPath="
                + configPath + "]";
    }



}
