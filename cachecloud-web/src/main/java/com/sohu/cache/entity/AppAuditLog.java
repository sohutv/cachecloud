package com.sohu.cache.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import net.sf.json.JSONObject;
import com.sohu.cache.constant.AppAuditLogTypeEnum;

/**
 * 资源的审批发布日志
 * @author leifu
 */
@Data
public class AppAuditLog implements Serializable {

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

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
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

}


