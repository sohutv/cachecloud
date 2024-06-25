package com.sohu.cache.entity;

import lombok.Data;

/**
 * Created by hym on 14-10-30.
 */
@Data
public class MachineMemInfo {
    private String ip;
    private long applyMem;
    private long usedMem;
    private long applyDisk;//磁盘分配

    private long usedDisk;//磁盘实例使用
    private double usedMemRss;

    public long getLockedMem() {
        return this.applyMem - this.usedMem;
    }
}
