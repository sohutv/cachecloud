package com.sohu.cache.entity;

import lombok.Data;

@Data
public class MachineInstanceStat {

	private String ip;

    private long maxMemory;
	
	private long usedMemory;

	private long applyDisk;

	private long usedDisk;

}