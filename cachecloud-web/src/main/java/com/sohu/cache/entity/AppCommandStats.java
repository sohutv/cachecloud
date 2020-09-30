package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * Created by yijunzhang
 */
@Data
public class AppCommandStats implements Comparable<AppCommandStats> {

    /**
     * 应用id
     */
    private long appId;

    /**
     * 收集时间:格式yyyyMMddHHmm/yyyyMMdd/yyyyMMddHH
     */
    private long collectTime;

    /**
     * 命令名称
     */
    private String commandName;

    /**
     * 命令执行次数
     */
    private long commandCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;
    @Override
    public int compareTo(AppCommandStats o) {
        if (o.commandCount > this.commandCount) {
            return 1;
        } else if (o.commandCount < this.commandCount) {
            return -1;
        }
        return 0;
    }

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public Date getModifyTime() {
        return (Date) modifyTime.clone();
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = (Date) modifyTime.clone();
    }
}
