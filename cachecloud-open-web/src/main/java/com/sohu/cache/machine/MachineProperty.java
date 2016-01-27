package com.sohu.cache.machine;

import com.google.common.collect.ComparisonChain;

import java.util.Comparator;

/**
 * 选择机器时根据机器的memory、traffic和load进行排序；
 * 在memory满足的情况，排序规则是：traffic > load > memory
 *
 * @author: lingguo
 * @time: 2014/9/17 12:04
 */
public class MachineProperty implements Comparator<MachineProperty> {
    private long hostId;
    private long memory;
    private double traffic;
    private double load;

    public MachineProperty() {}

    public MachineProperty(long hostId, long memory, double traffic, double load) {
        this.hostId = hostId;
        this.memory = memory;
        this.traffic = traffic;
        this.load = load;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(long hostId) {
        this.hostId = hostId;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public double getTraffic() {
        return traffic;
    }

    public void setTraffic(double traffic) {
        this.traffic = traffic;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    @Override
    public int compare(MachineProperty o1, MachineProperty o2) {
        return ComparisonChain.start()
                .compare(o1.traffic, o2.traffic)
                .compare(o1.load, o2.load)
                .compare(o2.memory, o1.memory)
                .result();
    }
}
