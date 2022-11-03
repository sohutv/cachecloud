package com.sohu.cache.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: zengyizhao
 * @CreateTime: 2022/9/1 16:09
 * @Description: 应用模块对应表
 * @Version: 1.0
 */
@Data
public class AppToModule implements Serializable {

    private static final long serialVersionUID = 1326072190123022633L;

    /**
     * 自增id
     */
    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 模块id
     */
    private Integer moduleId;


    /**
     * 模块版本id
     */
    private Integer moduleVersionId;

    public AppToModule() {
    }

    public AppToModule(Long appId, Integer moduleId, Integer moduleVersionId) {
        this.appId = appId;
        this.moduleId = moduleId;
        this.moduleVersionId = moduleVersionId;
    }

    public AppToModule(Long id, Long appId, Integer moduleId, Integer moduleVersionId) {
        this.id = id;
        this.appId = appId;
        this.moduleId = moduleId;
        this.moduleVersionId = moduleVersionId;
    }
}
