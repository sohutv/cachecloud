package com.sohu.cache.web.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Author: zengyizhao
 * @CreateTime: 2022/9/2 10:57
 * @Description:
 * @Version: 1.0
 */
@Data
public class ModuleVersionDetailVo {

    private int id;

    private String name;

    private String tag;

    private int moduleId;

    private int versionId;

    private Date createTime;

    private int status;

    private String soPath;

    private String gitUrl;

    private String info;

}
