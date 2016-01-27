package com.sohu.cache.entity;

import java.io.Serializable;
import java.util.Date;

import net.sf.json.JSONObject;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.sohu.cache.constant.AppAuditLogTypeEnum;

/**
 * 资源的审批发布日志
 * 
 * @author leifu
 * @Time 2014年6月5日
 */
public class AppAuditLog implements Serializable {

    private static final long serialVersionUID = 7218664733731725364L;

    /**
     * 日志id
     */
    private Long id;

    /**
     * 应用id
     */
    private Long appId;
    
    /**
     * 审批id
     */
    private Long appAuditId;
    
    /**
     * 用户id
     */
    private Long userId;
    
    /**
     * 用户
     */
    private AppUser appUser;
    
    /**
     * 日志详情 是个json
     */
    private String info;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 日志类型
     */
    private Integer type;
    
    public Long getId() {
        return id;
    }

    public void setId(Long appId) {
        this.id = appId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getAppAuditId() {
        return appAuditId;
    }

    public void setAppAuditId(Long appAuditId) {
        this.appAuditId = appAuditId;
    }
    
    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    /**
     * 生成日志
     * @param appDesc
     * @param appUser
     * @param appAuditId
     * @param type
     * @return
     */
    public static AppAuditLog generate(AppDesc appDesc, AppUser appUser, Long appAuditId, AppAuditLogTypeEnum type){
        if(appDesc == null || appUser == null || appAuditId == null){
            return null;
        }
        AppAuditLog log = new AppAuditLog();
        log.setAppId(appDesc.getAppId());
        log.setUserId(appUser.getId());
        log.setAppAuditId(appAuditId);
        log.setType(type.value());
        log.setCreateTime(new Date());
        log.setInfo(JSONObject.fromObject(appDesc).toString());
        return log;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }
    

}


