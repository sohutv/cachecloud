package com.sohu.cache.server.data;

import org.apache.commons.lang.math.NumberUtils;

/**
 * tcp连接
 */
public class Connection implements LineParser{
	public static final String FLAG = "TCP";
	
	private int established;
	private int timeWait;
	private int orphan;
	
	/**
	 * line format:
	 * TCP: inuse 454 orphan 0 tw 159620 alloc 454 mem 79
	 */
	public void parse(String line, String timeKey) throws Exception{
		if(line.startsWith(FLAG)) {
			String[] items = line.split("\\s+");
			for(int i = 0; i < items.length; ++i) {
				if(items[i].equals("inuse")) {
					established = NumberUtils.toInt(items[i+1]);
				} else if(items[i].equals("orphan")) {
					orphan = NumberUtils.toInt(items[i+1]);
				} else if(items[i].equals("tw")) {
					timeWait = NumberUtils.toInt(items[i+1]);
				}
			}
		}
	}
	public int getEstablished() {
		return established;
	}

	public int getTimeWait() {
		return timeWait;
	}

	public int getOrphan() {
		return orphan;
	}
}
