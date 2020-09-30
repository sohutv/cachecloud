package com.sohu.cache.web.vo;

import lombok.Data;

@Data
public class MachineStatsVo {

	private String machineRoom;

	private long totalMachineMem;

    private long totalMachineFreeMem;
	
	private long totalInstanceMaxMem;
	
	private long totalInstanceUsedMem;

	public double getMachineMemUsedRatio() {
		return (totalMachineMem - totalMachineFreeMem) * 100.0 / totalMachineMem * 1.0;
	}

	public double getInstanceMemUsedRatio() {
		return totalInstanceUsedMem * 100.0 / totalInstanceMaxMem * 1.0;
	}

}