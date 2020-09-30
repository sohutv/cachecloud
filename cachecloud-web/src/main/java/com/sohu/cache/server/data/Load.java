package com.sohu.cache.server.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;

/**
 * 系统负载
 */
public class Load implements LineParser{
	public static final Pattern PATTERN = Pattern.compile(
			"^BBBP,[0-9]+,uptime,.*(\\d+\\.\\d+), (\\d+\\.\\d+), (\\d+\\.\\d+)");
	//1分钟负载
	private float load1;
	//5分钟负载
	private float load5;
	//15分钟负载
	private float load15;
	
	/**
	 * line format:
	 * BBBP,585,uptime," 09:35:00 up 567 days, 15:07,  0 users,  load average: 0.60, 0.63, 0.67"
	 */
	public void parse(String line, String timeKey) throws Exception{
		Matcher matcher = PATTERN.matcher(line);
		if(matcher.find()) {
			load1 = NumberUtils.toFloat(matcher.group(1));
			load5 = NumberUtils.toFloat(matcher.group(2));
			load15 = NumberUtils.toFloat(matcher.group(3));
		}
	}
	
	public float getLoad1() {
		return load1;
	}
	public float getLoad5() {
		return load5;
	}
	public float getLoad15() {
		return load15;
	}

	@Override
	public String toString() {
		return "Load [load1=" + load1 + ", load5=" + load5 + ", load15="
				+ load15 + "]";
	}
}
