package redis.clients.jedis.valueobject;

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
     *
     */
    private volatile int type;

    /**
     * 0:运行中
     * 1:完成
     * 2:出错
     */
    private volatile int status;

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

    public void addReshardSlot(int slot, long removeCount) {
        reshardSlot += 1;
        slotProcessMap.put(slot, removeCount);
    }

    public void setStatus(int status) {
        this.status = status;
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
}
