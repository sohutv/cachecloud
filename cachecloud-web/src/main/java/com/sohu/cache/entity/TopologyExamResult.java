package com.sohu.cache.entity;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rucao on 2019/1/31
 */
@Data
public class TopologyExamResult {
    private long id;

    private long appId;

    private String type;

    private String status;

    private String description;

    private Date createTime;

    public TopologyExamResult(long appId, String type, String status, String description){
        this.appId=appId;
        this.type=type;
        this.status=status;
        this.description=description;
        this.createTime=new Date();
    }

    public String getCreateTimeFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (createTime != null) {
            return sdf.format(createTime);
        }
        return "";
    }

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }
}
