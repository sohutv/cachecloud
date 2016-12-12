package com.sohu.cache.redis;

import com.sohu.cache.web.util.DateUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
    
    
    /**
     * Reshard状态
     */
    public static enum ReshardStatusEnum {
        RUNNING(0, "运行中"), 
        FINISH(1, "完成"), 
        ERROR(2, "出错");

        private int value;
        private String info;
        
        private final static Map<Integer, ReshardStatusEnum> MAP = new HashMap<Integer, ReshardStatusEnum>();
        static {
            for (ReshardStatusEnum reshardStatusEnum : ReshardStatusEnum.values()) {
                MAP.put(reshardStatusEnum.getValue(), reshardStatusEnum);
            }
        }
        
        public static ReshardStatusEnum getReshardStatusEnum(int value) {
            return MAP.get(value);
        }

        private ReshardStatusEnum(int value, String info) {
            this.value = value;
            this.info = info;
        }

        public int getValue() {
            return value;
        }

        public String getInfo() {
            return info;
        }
    }

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
        if (Arrays.asList(ReshardStatusEnum.FINISH.getValue(), ReshardStatusEnum.ERROR.getValue()).contains(status) && endTime == null) {
            endTime = new Date();
        }
    }

    public String getStatusDesc() {
        ReshardStatusEnum reshardStatusEnum = ReshardStatusEnum.getReshardStatusEnum(status);
        return reshardStatusEnum == null ? "" : reshardStatusEnum.getInfo();
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
