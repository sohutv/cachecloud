package com.sohu.cache.redis;

import com.sohu.cache.web.util.DateUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by yijunzhang on 14-9-26.
 */
public class ReshardProcess {

    /**
     * 存储每个slot迁移的数量
     */
    private ConcurrentMap<Integer, Long> slotProcessMap = new ConcurrentSkipListMap<Integer, Long>();

    /**
     * 已完成迁移的slot数量
     */
    private volatile int reshardSlot;

    /**
     * 需要迁移的总数
     */
    private volatile int totalSlot;

    /**
     * 0,上线节点
     * 1,下线节点
     */
    private volatile int type;

    /**
     * 0:运行中
     * 1:完成
     * 2:出错
     */
    private volatile int status;

    private volatile Date beginTime;

    private volatile Date endTime;

    public ReshardProcess() {
        beginTime = new Date();
    }

    public Map<Integer, Long> getSlotProcessMap() {
        return slotProcessMap;
    }

    public int getReshardSlot() {
        return reshardSlot;
    }

    public int getTotalSlot() {
        return totalSlot;
    }

    public void setTotalSlot(int totalSlot) {
        this.totalSlot = totalSlot;
    }

    public void addReshardSlot(int slot, long moveCount) {
        reshardSlot += 1;
        slotProcessMap.put(slot, moveCount);
    }

    public void setStatus(int status) {
        this.status = status;
        if (Arrays.asList(1,2).contains(status) && endTime == null) {
            endTime = new Date();
        }
    }

    public String getStatusDesc() {
        if (status == 0) {
            return "运行中";
        } else if (status == 1) {
            return "完成";
        } else {
            return "出错";
        }
    }

    public void setReshardSlot(int reshardSlot) {
        this.reshardSlot = reshardSlot;
    }

    public int getStatus() {
        return status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "ReshardProcess{" +
                "slotProcessMap=" + slotProcessMap +
                ", reshardSlot=" + reshardSlot +
                ", totalSlot=" + totalSlot +
                ", type=" + type +
                ", status=" + status +
                ", beginTime=" + DateUtil.formatYYYYMMddHHMMSS(beginTime) +
                ", endTime=" + DateUtil.formatYYYYMMddHHMMSS(endTime) +
                '}';
    }
}
