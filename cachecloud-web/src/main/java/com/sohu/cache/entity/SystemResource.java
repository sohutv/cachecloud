package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by chenshi on 2020/7/6.
 */
@Data
public class SystemResource {

    private int id;

    private String name;

    private String intro;

    private int type;

    private String dir;

    private Date lastmodify;

    private String url;

    private int status;

    private int ispush;

    private String username;

    private long taskId;

    private String compileInfo;

    private int orderNum;
}
