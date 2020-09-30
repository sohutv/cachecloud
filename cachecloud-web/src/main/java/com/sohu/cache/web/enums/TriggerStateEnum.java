package com.sohu.cache.web.enums;

/**
 * @author fulei
 * @date 2018年8月8日
 * @time 下午3:29:48
 */
public enum TriggerStateEnum {

	WAITING("WAITING", "等待"),
	ACQUIRED("ACQUIRED", "正常运行"),
	PAUSED("PAUSED", "暂停"),
	BLOCKED("BLOCKED", "阻塞"),
	ERROR("ERROR", "错误");
	
	private String state;
	
	private String info;
	
	private TriggerStateEnum(String state, String info) {
		this.state = state;
		this.info = info;
	}

	public String getState() {
		return state;
	}

	public String getInfo() {
		return info;
	}
	
}
