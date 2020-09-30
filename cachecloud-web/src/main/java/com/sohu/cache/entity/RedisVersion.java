package com.sohu.cache.entity;

import lombok.Data;

/**
 * Created by chenshi on 2018/8/23.
 */
@Data
public class RedisVersion {
    /**
     * 主键id
     */
    private int id;
    /**
     * redis版本名称 格式:redis-主.子.小版本号
     */
    private String name;
    /**
     * 是否有效 1:有效 0:无效
     */
    private int status;
    /**
     * 安装目录
     */
    private String dir;
    /**
     * redis大版本号  格式:redis-主.子版本号
     */
    private String groups;
    /**
     * 大版本默认版本号(推荐)
     */
    private int isBind;

    /**
     * 资源id
     */
    private int resourceId;

    public RedisVersion() {
    }

    public RedisVersion(int id) {
        this.id = id;
    }

    public RedisVersion(String name, int status) {
        this.name = name;
        this.status = status;
    }

    public RedisVersion(String name, int status, String dir) {
        this.name = name;
        this.status = status;
        this.dir = dir;
    }

    public RedisVersion(String name, int status, String dir, String groups, int isBind, int resourceId) {
        this.name = name;
        this.status = status;
        this.dir = dir;
        this.groups = groups;
        this.isBind = isBind;
        this.resourceId = resourceId;
    }

    public RedisVersion(int id, String name, int status, String dir, String groups, int isBind, int resourceId) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.dir = dir;
        this.groups = groups;
        this.isBind = isBind;
        this.resourceId = resourceId;
    }
}
