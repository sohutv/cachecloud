package com.sohu.cache.constant;

/**
 * 机器的性能状态参数的峰值
 *
 * User: lingguo
 * Date: 14-6-27
 */
public enum  MachinePeakStatus {
    MEMORY_PEAK("memoryUsageRatio"),    /* 内存峰值 */
    CPU_PEAK("cpuUsage"),               /* cpu峰值 */
    LOAD_PEAK("load"),             /* 负载峰值 */
    INFLOW_PEAK("inflow"),         /* 流入流量峰值 */
    OUTFLOW_PEAK("outflow");       /* 流出流量峰值 */

    String value;

    MachinePeakStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
