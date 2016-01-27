package com.sohu.cache.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yijunzhang on 14-10-20.
 */
public class AppAudit {

    private long id;

    private long appId;

    private long userId;

    private String userName;

    /**
     * 申请类型:0:申请应用,1:应用扩容,2:修改配置
     */
    private int type;

    /**
     * 预留参数1
     */
    private String param1;
    /**
     * 预留参数2
     */
    private String param2;
    /**
     * 预留参数3
     */
    private String param3;

    /**
     * 申请描述
     */
    private String info;

    /**
     * 0:等待审批; 1:审批通过; -1:驳回
     */
    private int status;

    private Date createTime;

    private Date modifyTime;

    /**
     * 驳回原因
     */
    private String refuseReason;

    private AppDesc appDesc;
    
    private AppAuditLog appAuditLog;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getParam3() {
        return param3;
    }

    public void setParam3(String param3) {
        this.param3 = param3;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public AppDesc getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(AppDesc appDesc) {
        this.appDesc = appDesc;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRefuseReason() {
        return refuseReason;
    }

    public void setRefuseReason(String refuseReason) {
        this.refuseReason = refuseReason;
    }

    public AppAuditLog getAppAuditLog() {
        return appAuditLog;
    }

    public void setAppAuditLog(AppAuditLog appAuditLog) {
        this.appAuditLog = appAuditLog;
    }

    public String getTypeDesc() {
        if (type == 0) {
            return "申请应用";
        } else if (type == 1) {
            return "应用扩容";
        } else if (type == 2) {
            return "修改配置";
        } else {
            return type + "";
        }
    }

    public String getStatusDesc() {
//        0:等待审批; 1:审批通过; -1:驳回
        if (status == 0) {
            return "等待审批";
        } else if (status == 1) {
            return "审批通过";
        } else if (status == -1) {
            return "驳回";
        } else {
            return status + "";
        }
    }
    
    public String getModifyTimeFormat(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(modifyTime != null){
            return sdf.format(modifyTime);
        }
        return "";
    }
    
    public String getCreateTimeFormat(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(createTime != null){
            return sdf.format(createTime);
        }
        return "";
    }

    @Override
    public String toString() {
        return "AppAudit [id=" + id + ", appId=" + appId + ", userId=" + userId + ", userName=" + userName + ", type="
                + type + ", param1=" + param1 + ", param2=" + param2 + ", param3=" + param3 + ", info=" + info
                + ", status=" + status + ", createTime=" + createTime + ", modifyTime=" + modifyTime
                + ", refuseReason=" + refuseReason + ", appDesc=" + appDesc + "]";
    }


}
