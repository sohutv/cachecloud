package com.sohu.cache.server.data;

import org.apache.commons.lang.math.NumberUtils;
/**
 * 内存使用情况
 */
public class Memory implements LineParser{
	public static final String FLAG = "MEM";
	//总内存，单位M
	private float total;
	//总空闲内存，单位M
	private float totalFree;
	//buffer，单位M
	private float buffer;
	//cache，单位M
	private float cache;
	//swap，单位M
	private float swap;
	//swap空闲内存，单位M
	private float swapFree;
	
	/**
	 * line format:
	 * MEM,Memory MB bx-50-13,memtotal,hightotal,lowtotal,swaptotal,memfree,highfree,lowfree,swapfree,memshared,cached,active,bigfree,buffers,swapcached,inactive
	 * MEM,T0001,48288.7,0.0,48288.7,8189.4,132.6,0.0,132.6,8189.1,-0.0,24210.6,30819.7,-1.0,153.9,0.0,16451.1
	 */
	public void parse(String line, String timeKey) throws Exception{
		if(line.startsWith(FLAG)) {
			String[] items = line.split(",");
			if(!items[1].equals(timeKey)) {
				return;
			}
			total = NumberUtils.toFloat(items[2]);
			swap = NumberUtils.toFloat(items[5]);
			totalFree = NumberUtils.toFloat(items[6]);
			swapFree = NumberUtils.toFloat(items[9]);
			cache = NumberUtils.toFloat(items[11]);
			buffer = NumberUtils.toFloat(items[14]);
		}
	}
	
	public float getTotal() {
		return total;
	}

	public float getTotalFree() {
		return totalFree;
	}

	public float getBuffer() {
		return buffer;
	}

	public float getCache() {
		return cache;
	}

	public float getSwap() {
		return swap;
	}

	public float getSwapFree() {
		return swapFree;
	}

	@Override
	public String toString() {
		return "Memory [total=" + total + ", totalFree=" + totalFree
				+ ", buffer=" + buffer + ", cache=" + cache + ", swap=" + swap
				+ ", swapFree=" + swapFree + "]";
	}
}
