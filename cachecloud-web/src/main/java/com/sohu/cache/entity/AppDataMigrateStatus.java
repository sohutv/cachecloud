package com.sohu.cache.entity;

import com.sohu.cache.constant.AppDataMigrateEnum;
import com.sohu.cache.constant.AppDataMigrateStatusEnum;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 迁移状态
 */
@Data
public class AppDataMigrateStatus {

    /**
     * 自增id
     */
    private long id;

    /**
     * 迁移任务id
     */
    private String migrateId;

    /**
     * 迁移工具 0：redis-shake；1：redis-migrate-tool
     */
    private int migrateTool;
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
     * 源redis版本
     */
    private String redisSourceVersion;

    /**
     * 目标redis版本
     */
    private String redisTargetVersion;

    /**
     * 操作人id
     */
    private long userId;
    /**
     * 操作人name
     */
    private String userName;

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

    public String getStatusDesc() {
        AppDataMigrateStatusEnum appDataMigrateStatusEnum = AppDataMigrateStatusEnum.getByStatus(status);
        return appDataMigrateStatusEnum == null ? "异常" : appDataMigrateStatusEnum.getInfo();
    }

    public String getTargetMigrateTypeDesc() {
        AppDataMigrateEnum appDataMigrateEnum = AppDataMigrateEnum.getByIndex(targetMigrateType);
        return appDataMigrateEnum == null ? "异常" : appDataMigrateEnum.getType();
    }

    public String getSourceMigrateTypeDesc() {
        AppDataMigrateEnum appDataMigrateEnum = AppDataMigrateEnum.getByIndex(sourceMigrateType);
        return appDataMigrateEnum == null ? "异常" : appDataMigrateEnum.getType();
    }

    public String getStartTimeFormat() {
        if (startTime == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(startTime);
    }

    public String getEndTimeFormat() {
        if (endTime == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(endTime);
    }

    public Date getStartTime() {
        if (null == startTime) {
            return null;
        }
        return (Date) startTime.clone();
    }

    public void setStartTime(Date startTime) {
        if (null == startTime) {
            this.startTime = null;
        } else {
            this.startTime = (Date) startTime.clone();
        }
    }

    public Date getEndTime() {
        if (null == endTime) {
            return null;
        }
        return (Date) endTime.clone();
    }

    public void setEndTime(Date endTime) {
        if (null == endTime) {
            this.endTime = null;
        } else {
            this.endTime = (Date) endTime.clone();
        }
    }
}
