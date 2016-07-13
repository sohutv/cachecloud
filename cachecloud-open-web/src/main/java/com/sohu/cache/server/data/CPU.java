package com.sohu.cache.server.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
/**
 *	cpu状况
 */
public class CPU implements LineParser{
	public static final String FLAG = "CPU";
	public static final String CPU_ALL = "CPU_ALL";
	private Usage allUsage;
	//包含各个虚拟cpu的情况
	private List<Usage> cpuList = new ArrayList<Usage>();
	
	/**
	 * line format:
	 * CPU001,CPU 1 bx-50-13,User%,Sys%,Wait%,Idle%
	 * CPU002,CPU 2 bx-50-13,User%,Sys%,Wait%,Idle%
	 * CPU_ALL,CPU Total bx-50-13,User%,Sys%,Wait%,Idle%,Busy,CPUs
	 * CPU001,T0001,1.8,0.9,4.5,92.9
	 * CPU002,T0001,3.6,1.8,0.0,94.6
	 * CPU_ALL,T0001,2.1,1.3,0.6,95.9,,16
	 */
	public void parse(String line, String timeKey) throws Exception{
		if(line.startsWith(FLAG)) {
			String[] items = line.split(",", 6);
			if(items.length != 6) {
				return;
			}
			if(!items[1].equals(timeKey)) {
				return;
			}
			Usage usage = new Usage();
			usage.setUser(NumberUtils.toFloat(items[2]));
			usage.setSys(NumberUtils.toFloat(items[3]));
			usage.setWait(NumberUtils.toFloat(items[4]));
			if(CPU_ALL.equals(items[0])) {
				allUsage = usage;
			} else {
				usage.setName(items[0]);
				cpuList.add(usage);
			}
		}
	}

	public List<Usage> getCpuList() {
		return cpuList;
	}
	
	public Usage getAllUsage() {
		return allUsage;
	}
	
	public float getUser() {
		return allUsage == null ? 0 : allUsage.getUser();
	}
	
	public float getSys() {
		return allUsage == null ? 0 : allUsage.getSys();
	}
	
	public float getWait() {
		return allUsage == null ? 0 : allUsage.getWait();
	}
	
	public String getExt(){
		StringBuilder sb = new StringBuilder();
		for(Usage usage : cpuList) {
			sb.append(usage.getName());
			sb.append(",");
			sb.append(usage.getUser());
			sb.append(",");
			sb.append(usage.getSys());
			sb.append(",");
			sb.append(usage.getWait());
			sb.append(";");
		}
		return sb.toString();
	}
	public String toString() {
		return "CPU [cpuList=" + cpuList + "]";
	}

	/**
	 * cpu使用率
	 */
	public class Usage{
		//代表那个cpu
		private String name;
		//用户空间使用率
		private float user;
		//内核空间使用率
		private float sys;
		//wio
		private float wait;
		public float getUser() {
			return user;
		}
		public void setUser(float user) {
			this.user = user;
		}
		public float getSys() {
			return sys;
		}
		public void setSys(float sys) {
			this.sys = sys;
		}
		public float getWait() {
			return wait;
		}
		public void setWait(float wait) {
			this.wait = wait;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return "Usage [name=" + name + ", user=" + user + ", sys=" + sys
					+ ", wait=" + wait + "]";
		}
	}
}
