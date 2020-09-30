package com.sohu.cache.entity;

import lombok.Data;

/**
 * 用于group by结果
 * @author fulei
 */
@Data
public class ParamCount {

	private String param;
	
	private double count;
	
	private String url;

	public ParamCount() {
	}

	/**
	 * @param param
	 * @param count
	 * @param url
	 */
	public ParamCount(String param, double count, String url) {
		super();
		this.param = param;
		this.count = count;
		this.url = url;
	}

}
