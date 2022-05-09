package com.sohu.cache.web.vo;

import com.sohu.cache.web.util.DateUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/14 11:11
 * @Description: 应用redis 命令校验结果
 */
@Data
public class RedisCommandCheckResult implements Serializable {

    private Date createTime;

    private String createTimeStr;

    /**
     * 宿主机ip
     */
    private String machineIps;

    /**
     * pod ip
     */
    private String podIp;

    /**
     * 命令
     */
    private String command;

    /**
     * 配置项
     */
    private String key;

    /**
     * 操作人
     */
    private String userName;

    /**
     * redis版本
     */
    private boolean success;

    public String getCreateTimeStr(){
        return DateUtil.formatYYYYMMddHHMMSS(createTime);
    }

}
