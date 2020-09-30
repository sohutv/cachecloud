package com.sohu.cache.entity;

import lombok.Data;
import org.apache.commons.lang.math.NumberUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by yijunzhang on 14-6-9.
 */
@Data
public class AppStats {

    /**
     * 应用id
     */
    private long appId;

    /**
     * 收集时间:格式yyyyMMddHHmm/yyyyMMdd/yyyyMMddHH
     */
    private long collectTime;

    /**
     * 命中数量
     */
    private long hits;

    /**
     * 未命中数量
     */
    private long misses;

    /**
     * 命令执行次数
     */
    private long commandCount;

    /**
     * 内存占用
     */
    private long usedMemory;

    /**
     * 物理内存占用
     */
    private long usedMemoryRss;

    /**
     * 过期key数量
     */
    private long expiredKeys;

    /**
     * 驱逐key数量
     */
    private long evictedKeys;

    /**
     * 网络输入字节
     */
    private long netInputByte;

    /**
     * 网络输出字节
     */
    private long netOutputByte;

    /**
     * 客户端连接数
     */
    private int connectedClients;

    /**
     * 存储对象数
     */
    private long objectSize;

    /**
     * 进程系统态消耗(单位:秒)
     */
    private long cpuSys;
    /**
     * 进程用户态消耗(单位:秒)
     */
    private long cpuUser;

    /**
     * 子进程内核态消耗(单位:秒)
     */
    private long cpuSysChildren;
    /**
     * 子进程用户态消耗(单位:秒)
     */
    private long cpuUserChildren;

    /**
     * 累加的实例数
     */
    private int accumulation;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 命令统计集合
     */
    private List<AppCommandStats> commandStatsList;

    /**
     * 命中率
     *
     * @return
     */
    public long getHitPercent() {
        long total = hits + misses;
        if (total == 0) {
            return 0;
        } else {
            NumberFormat formatter = new DecimalFormat("0");
            return NumberUtils.toLong(formatter.format(hits * 100.0 / total));
        }
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
