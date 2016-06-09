package com.sohu.cache.entity;

import java.util.Date;

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
    private long migrateUserId;

    /**
     * 迁移执行状态
     */
    private int migrateStatus;

    /**
     * 迁移开始执行时间
     */
    private Date migrateStartTime;

    /**
     * 迁移结束执行时间
     */
    private Date migrateEndTime;

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

    public String getSourceServers() {
        return sourceServers;
    }

    public void setSourceServers(String sourceServers) {
        this.sourceServers = sourceServers;
    }

    public int getSourceMigrateType() {
        return sourceMigrateType;
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

    public long getMigrateUserId() {
        return migrateUserId;
    }

    public void setMigrateUserId(long migrateUserId) {
        this.migrateUserId = migrateUserId;
    }

    public int getMigrateStatus() {
        return migrateStatus;
    }

    public void setMigrateStatus(int migrateStatus) {
        this.migrateStatus = migrateStatus;
    }

    public Date getMigrateStartTime() {
        return migrateStartTime;
    }

    public void setMigrateStartTime(Date migrateStartTime) {
        this.migrateStartTime = migrateStartTime;
    }

    public Date getMigrateEndTime() {
        return migrateEndTime;
    }

    public void setMigrateEndTime(Date migrateEndTime) {
        this.migrateEndTime = migrateEndTime;
    }

    @Override
    public String toString() {
        return "AppDataMigrateStatus [id=" + id + ", migrateMachineIp=" + migrateMachineIp + ", sourceServers="
                + sourceServers + ", sourceMigrateType=" + sourceMigrateType + ", targetServers=" + targetServers
                + ", targetMigrateType=" + targetMigrateType + ", sourceAppId=" + sourceAppId + ", targetAppId="
                + targetAppId + ", migrateUserId=" + migrateUserId + ", migrateStatus=" + migrateStatus
                + ", migrateStartTime=" + migrateStartTime + ", migrateEndTime=" + migrateEndTime + "]";
    }


}
