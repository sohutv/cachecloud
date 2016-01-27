package com.sohu.cache.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 系统用户信息
 * 
 * @author leifu
 * @Time 2014年6月5日
 */
public class AppUser implements Serializable {

    private static final long serialVersionUID = 7425158151337667662L;

    /**
     * 自增id
     */
    private Long id;

    /**
     * 用户名(英文，域账户)
     */
    private String name;
    
    /**
     * 中文名
     */
    private String chName;
    
    /**
     * 用户域账户邮箱
     */
    private String email;

    /**
     * 用户手机
     */
    private String mobile;

    /**
     * 用户类型(类型参考AppUserTypeEnum)
     */
    private int type;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getChName() {
        return chName;
    }

    public void setChName(String chName) {
        this.chName = chName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }

    public static AppUser buildFrom(Long userId, String name, String chName, String email, String mobile,
            Integer type) {
        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setName(name);
        appUser.setChName(chName);
        appUser.setEmail(email);
        appUser.setMobile(mobile);
        appUser.setType(type);
        return appUser;
    }

}
