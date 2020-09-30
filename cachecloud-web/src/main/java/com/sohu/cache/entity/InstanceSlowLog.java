package com.sohu.cache.entity;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;

/**
 * 实例慢查询日志
 *
 * @author leifu
 */
@Data
public class InstanceSlowLog {

    private long id;

    /**
     * 实例id
     */
    private long instanceId;

    /**
     * app id
     */
    private long appId;

    /**
     * ip地址
     */
    private String ip;

    /**
     * port
     */
    private int port;

    /**
     * 慢查询日志id
     */
    private long slowLogId;

    /**
     * 耗时
     */
    private int costTime;

    /**
     * 命令
     */
    private String command;

    /**
     * 记录创建时间
     */
    private Timestamp createTime;

    /**
     * 慢查询发生时间
     */
    private Timestamp executeTime;

    public String getCommand() {
        int maxLength = 30;
        if (StringUtils.isNotBlank(command) && command.length() > maxLength) {
            return command.substring(0, maxLength) + "...";
        }
        return command;
    }

    public Timestamp getCreateTime() {
        return (Timestamp) createTime.clone();
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = (Timestamp) createTime.clone();
    }

    public Timestamp getExecuteTime() {
        return (Timestamp) executeTime.clone();
    }

    public void setExecuteTime(Timestamp executeTime) {
        this.executeTime = (Timestamp) executeTime.clone();
    }
}
