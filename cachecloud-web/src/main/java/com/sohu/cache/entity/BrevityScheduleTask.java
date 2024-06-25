package com.sohu.cache.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.sohu.cache.schedule.brevity.BrevityScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by yijunzhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrevityScheduleTask {

    @JSONField(serialize = false)
    private Integer id;

    private Integer type;

    private Long version;

    private String host;

    private Integer port;

    private Date createTime;

    private BrevityScheduleType brevityScheduleType;

    private Integer instanceType;

    @JSONField(serialize = false)
    public BrevityScheduleType getBrevityScheduleType() {
        return BrevityScheduleType.typeOf(type);
    }

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    @JSONField(serialize = false)
    public String getHostPort(){
        return this.getHost() + ":" + this.getPort();
    }

    @JSONField(serialize = false)
    public String getKeyField(){
        return this.getType() + "_" + this.getHost() + "_" + this.getPort();
    }
}
