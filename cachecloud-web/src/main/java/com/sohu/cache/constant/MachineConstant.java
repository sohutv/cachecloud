package com.sohu.cache.constant;

/**
 * User: lingguo
 * Date: 14-6-12
 * Time: 上午11:42
 */
public enum MachineConstant {
    Ip("ip"),
    Load("load"),
    Traffic("traffic"),
    CpuUsage("cpuUsage"),
    MemoryUsageRatio("memoryUsageRatio"),
    MemoryFree("memoryFree"),
    MemoryTotal("memoryTotal"),
    DiskUsage("diskUsageMap");

    private String value;

    MachineConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }


}
