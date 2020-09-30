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
    private double usedMemRss;

    public long getLockedMem() {
        return this.applyMem - this.usedMem;
    }
}
