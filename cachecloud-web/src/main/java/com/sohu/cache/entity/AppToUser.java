package com.sohu.cache.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用和用户的对应关系
 *
 * @author leifu
 */
@Data
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
    }

    public AppToUser(Long userId, Long appId) {
        super();
        this.userId = userId;
        this.appId = appId;
    }

}
