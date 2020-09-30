package com.sohu.cache.entity;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Data
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
