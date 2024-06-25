package com.sohu.cache.web.vo;

import lombok.Data;

@Data
public class MachineStatsVo {

    private String machineRoom;

    private long totalMachineMem;

    private long totalMachineFreeMem;

    private long totalInstanceMaxMem;

    private long totalInstanceUsedMem;

    private long totalMachineDisk;

    private long totalMachineFreeDisk;

    private long totalInstanceApplyDisk;

    private long totalInstanceUsedDisk;

    private double machineDiskUsedRatio;

    private double instanceMemUsedRatio;

    private double instanceDiskUsedRatio;

    public double getMachineMemUsedRatio() {
        if (totalMachineMem == 0) {
            return 0;
        }
        return (totalMachineMem - totalMachineFreeMem) * 100.0 / totalMachineMem * 1.0;
    }

    public double getMachineDiskUsedRatio() {
        return (totalMachineDisk - totalMachineFreeDisk) * 100.0 / totalMachineDisk * 1.0;
    }

    public double getInstanceMemUsedRatio() {
        if (totalInstanceMaxMem == 0) {
            return 0;
        }
        return totalInstanceUsedMem * 100.0 / totalInstanceMaxMem * 1.0;
    }

    public double getInstanceDiskUsedRatio() {
        if (totalInstanceApplyDisk == 0) {
            return 0;
        }
        return (totalInstanceUsedDisk) * 100.0 / (totalInstanceApplyDisk * 1024 * 1024 * 1.0);
    }

}