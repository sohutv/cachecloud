package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: rucao
 * @Date: 2021/1/7 下午6:00
 */
@Data
public class AppImport {
    private long id;
    private long appId;
    private int memSize;
    private int sourceType;
    private String redisVersionName;
    private String instanceInfo;
    private String redisPassword;
    private int status;
    private int step;
    private long appBuildTaskId;
    private long migrateId;
    private Date createTime;
    private Date updateTime;
}
