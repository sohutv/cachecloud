package com.sohu.cache.web.service;

import com.sohu.cache.exception.SSHException;


/**
 * @Author: zengyizhao
 * @CreateTime: 2023/2/28 15:33
 * @Description: 迁移服务
 * @Version: 1.0
 */
public interface MigrateService {

    /**
     * 根据机器pod，强制迁移
     * @param sourceIp
     * @param targetIp
     * @throws SSHException
     */
    public void forceMigrate(String sourceIp, String targetIp) throws SSHException;

}
