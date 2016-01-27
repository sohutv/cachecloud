package com.sohu.cache.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class InstanceCommandStats implements Comparable<InstanceCommandStats> {

    /**
     * 应用id
     */
    private long instanceId;

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

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public long getCommandCount() {
        return commandCount;
    }

    public void setCommandCount(long commandCount) {
        this.commandCount = commandCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public int compareTo(InstanceCommandStats o) {
        if (o.commandCount > this.commandCount) {
            return 1;
        } else if (o.commandCount < this.commandCount) {
            return -1;
        }
        return 0;
    }
    
    public Long getTimeStamp() throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = sdf.parse(String.valueOf(this.collectTime));
        return date.getTime();
    }

    @Override
    public String toString() {
        return "InstanceCommandStats{" +
                "instanceId=" + instanceId +
                ", collectTime=" + collectTime +
                ", commandName='" + commandName + '\'' +
                ", commandCount=" + commandCount +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
