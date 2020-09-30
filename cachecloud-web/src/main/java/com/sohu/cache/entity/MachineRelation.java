package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by chenshi on 2019/5/21.
 */
@Data
public class MachineRelation {

    /**
     * 主键id
     */
    private int id;

    /**
     * 虚拟ip
     */
    private String ip;

    /**
     * 宿主机ip
     */
    private String realIp;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * pod 描述信息
     */
    private String extraDesc;

    /**
     * pod状态 0:offline 下线  1：online 上线  2：pending
     * PodStatusEnum.OFFLINE   PodStatusEnum.ONLINE
     */
    private int status;

    private int isSync;

    private String syncTime;

    /**
     * 任务id
     */
    private long taskid;

    public MachineRelation() {
    }

    public MachineRelation(String ip, String realIp, Date updateTime, String extraDesc, int status) {
        this.ip = ip;
        this.realIp = realIp;
        this.updateTime = updateTime;
        this.extraDesc = extraDesc;
        this.status = status;
    }

    public Date getUpdateTime() {
        return (Date) updateTime.clone();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = (Date) updateTime.clone();
    }

}
