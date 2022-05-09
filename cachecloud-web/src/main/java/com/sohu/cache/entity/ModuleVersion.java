package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by chenshi on 2021/5/7.
 */
@Data
public class ModuleVersion {

    private int id;

    private String tag;

    private int moduleId;

    private int versionId;

    private Date createTime;

    private int status;

    private String soPath;
}
