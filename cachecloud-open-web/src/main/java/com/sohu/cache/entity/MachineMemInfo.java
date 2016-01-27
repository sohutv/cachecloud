package com.sohu.cache.entity;

/**
 * Created by hym on 14-10-30.
 */
public class MachineMemInfo {
    private String ip;
    private long applyMem;
    private long usedMem;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getApplyMem() {
        return applyMem;
    }

    public void setApplyMem(long applyMem) {
        this.applyMem = applyMem;
    }

    public long getUsedMem() {
        return usedMem;
    }

    public void setUsedMem(long usedMem) {
        this.usedMem = usedMem;
    }

    public long getLockedMem() {
        return this.applyMem - this.usedMem;
    }
}
