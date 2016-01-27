package com.sohu.cache.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 应用和用户的对应关系
 * 
 * @author leifu
 * @Time 2014年6月5日
 */
public class AppToUser implements Serializable {

    private static final long serialVersionUID = 1326072190198022633L;

    /**
     * 自增id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 应用id
     */
    private Long appId;
    

    public AppToUser() {
        super();
    }

    public AppToUser(Long userId, Long appId) {
        super();
        this.userId = userId;
        this.appId = appId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }
}
