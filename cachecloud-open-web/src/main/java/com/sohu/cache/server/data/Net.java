package com.sohu.cache.server.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;


/**
 * 网络流量
 */
public class Net implements LineParser{
	public static final String FLAG = "NET,";
	
	private float nin;
	private float nout;
	private StringBuilder ninDetail = new StringBuilder();
	private StringBuilder noutDetail = new StringBuilder();
	
	private List<NetworkInterfaceCard> ncList = new ArrayList<NetworkInterfaceCard>();
	
	/**
	 * line format:
	 * NET,Network I/O bx-50-13,lo-read-KB/s,eth0-read-KB/s,eth1-read-KB/s,eth2-read-KB/s,eth3-read-KB/s,lo-write-KB/s,eth0-write-KB/s,eth1-write-KB/s,eth2-write-KB/s,eth3-write-KB/s,
	 * NET,T0001,190.3,3317.8,0.0,0.0,0.0,190.3,3377.7,0.0,0.0,0.0,
	 */
	public void parse(String line, String timeKey) throws Exception{
		if(line.startsWith(FLAG)) {
			String[] items = line.split(",");
			if(items[1].startsWith("Network")) {
				for(int i = 0; i < items.length; ++i) {
					if(items[i].startsWith("eth")) {
						NetworkInterfaceCard nic = new NetworkInterfaceCard();
						nic.setName(items[i]);
						nic.setIdx(i);
						ncList.add(nic);
					}
				}
			} else {
				for(NetworkInterfaceCard nic : ncList) {
					nic.setValue(NumberUtils.toFloat(items[nic.getIdx()]));
				}
				caculate();
			}
		}
	}
	
	private void caculate() {
		float totalIn = 0;
		float totalOut = 0;
		for(NetworkInterfaceCard nic : ncList) {
			String[] array = nic.getName().split("-");
			if("read".equals(array[1])) {
				ninDetail.append(array[0]);
				ninDetail.append(",");
				ninDetail.append(nic.getValue());
				ninDetail.append(";");
				totalIn += nic.getValue();
			} else if("write".equals(array[1])) {
				noutDetail.append(array[0]);
				noutDetail.append(",");
				noutDetail.append(nic.getValue());
				noutDetail.append(";");
				totalOut += nic.getValue();
			}
		}
		nin = new BigDecimal(totalIn).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); 
		nout = new BigDecimal(totalOut).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); 
	}
	
	public float getNin() {
		return nin;
	}

	public float getNout() {
		return nout;
	}
	public String getNinDetail() {
		return ninDetail.toString();
	}

	public String getNoutDetail() {
		return noutDetail.toString();
	}

	class NetworkInterfaceCard{
		private String name;
		private float value;
		private int idx;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public float getValue() {
			return value;
		}
		public void setValue(float value) {
			this.value = value;
		}
		public int getIdx() {
			return idx;
		}
		public void setIdx(int idx) {
			this.idx = idx;
		}
	}
}
