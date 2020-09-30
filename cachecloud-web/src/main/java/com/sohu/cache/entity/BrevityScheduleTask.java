package com.sohu.cache.entity;

import com.sohu.cache.schedule.brevity.BrevityScheduleType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Created by yijunzhang
 */
@Data
@Builder
public class BrevityScheduleTask {

    private int id;

    private int type;

    private long version;

    private String host;

    private int port;

    private Date createTime;

    private BrevityScheduleType brevityScheduleType;

    public BrevityScheduleType getBrevityScheduleType() {
        return BrevityScheduleType.typeOf(type);
    }

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }
}
